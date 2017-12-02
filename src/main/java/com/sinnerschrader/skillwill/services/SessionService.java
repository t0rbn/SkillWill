package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.domain.user.Role;
import com.sinnerschrader.skillwill.repositories.userRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.session.Session;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Manage all sessions
 * NOTE: there can be multiple sessions
 * for one username
 *
 * @author torree
 */
@Service
@EnableRetry
public class SessionService {

  private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

  // Minutes of inactivity before the session is destroyed
  @Value("${sessionExpireDuration}")
  private int expireDuration;

  @Value("${oAuthUrl}")
  private String oAuthUrl;

  @Autowired
  private SessionRepository sessionRepo;

  @Autowired
  private userRepository userRepository;

  @Autowired
  private LdapService ldapService;

  private boolean isTokenInProxy(String token) {
    try {
      URL authUrl = new URL(oAuthUrl);
      HttpURLConnection connection = (HttpURLConnection) authUrl.openConnection();
      connection.addRequestProperty("Cookie", "_oauth2_proxy=" + token);
      connection.connect();

      int resonseCode = connection.getResponseCode();
      connection.disconnect();

      logger.debug("Successfully checked token with oauth proxy, result {}", resonseCode);
      return resonseCode == HttpStatus.ACCEPTED.value();
    } catch (IOException e) {
      logger.error("Failed to check session token at oauth Proxy");
      return false;
    }
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private boolean isTokenInDB(String token) {
    return sessionRepo.findByToken(token) != null;
  }

  private String getMailFromToken(String token) {
    return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
  }

  private boolean isTokenMail(String token, String mail) {
    String tokenMail;
    try {
      tokenMail = getMailFromToken(token);
    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }

    return tokenMail.equals(mail);
  }

  private boolean isTokenMail(String token, User user) {
    try {
      return isTokenMail(token, user.getLdapDetails().getMail());
    } catch (NullPointerException e) {
      logger.warn("Cannot verify mail, user {}, has empty LDAP details", user.getId());
      return false;
    }
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public boolean checkToken(String token, String userId) {
    User userFromId = userRepository.findByIdIgnoreCase(userId);

    if (userFromId == null) {
      if (isTokenInProxy(token)) {
        // create user, create session, return true
        User newUser = ldapService.createUserByMail(getMailFromToken(token));
        userRepository.insert(newUser);
        sessionRepo.insert(new Session(token));
        logger.debug("Successfully new user {}", userId);
        return true;
      }
      return false;
    }

    if (isTokenInDB(token)) {
      return true;
    }

    if (isTokenInProxy(token)) {
      sessionRepo.insert(new Session(token));
      return true;
    }

    logger.debug("Failed to authenticate user {}", userId);
    return false;
  }

  public boolean checkTokenRole(String token, Role role) {
    if (!isTokenInDB(token)) {
      if (isTokenInProxy(token)) {
        sessionRepo.insert(new Session(token));
      } else {
        return false;
      }
    }

    User user = userRepository.findByMail(getMailFromToken(token));
    return user != null && user.getRole() == role;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void clearUserSessions(User user) {
    String mail;

    try {
      mail = user.getLdapDetails().getMail();
    } catch (NullPointerException e) {
      logger.warn("Cannot check session for user {}: no mail in LDAP details", user.getId());
      return;
    }

    List<Session> clearables = sessionRepo.findByMail(mail)
      .stream()
      .filter(session -> !isTokenInProxy(session.getToken()))
      .collect(Collectors.toList());

    logger.debug("will remove {} session for user {}", clearables.size(), user.getId());
    sessionRepo.deleteAll(clearables);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void cleanUp() {
    List<Session> clearables = sessionRepo.findAll()
      .stream()
      .filter(session -> !isTokenInProxy(session.getToken()))
      .collect(Collectors.toList());

    logger.info("Starting session cleanup, will remove {} sessions", clearables.size());
    sessionRepo.deleteAll(clearables);
  }

}
