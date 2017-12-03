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

  private String extractMail(String token) {
    return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public User getUserByToken(String token) {
    Session session = getSession(token);
    if (session == null) {
      return null;
    }
    return userRepository.findByMail(session.getMail());
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private Session getSession(String token) {
    logger.debug("Getting session for token {}", token);

    if (isTokenInDB(token)) {
      logger.debug("Successfully found token {} in DB", token);
      return sessionRepo.findByToken(token);
    }

    if (isTokenInProxy(token)) {
      logger.debug("Successfully validated token {} with proxy", token);
      if (userRepository.findByMail(extractMail(token)) == null) {
        // user not in db yet, will create
        User newUser = ldapService.createUserByMail(extractMail(token));
        userRepository.insert(newUser);
        logger.info("Successfully created new user {}", newUser.getId());
      }

      // session not in DB, but in proxy -> create new session and revalidate old ones
      refreshUserSessions(extractMail(token));
      Session newSession = new Session(token);
      sessionRepo.insert(newSession);
      return newSession;
    }

    logger.debug("Failed to get Session for token {}: no session found", token);
    return null;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void refreshUserSessions(String mail) {
    List<Session> cleanables = sessionRepo.findByMail(mail).stream()
      .filter(session -> !isTokenInProxy(session.getToken()))
      .collect(Collectors.toList());

    logger.debug("will remove {} sessions for mail {}", cleanables.size(), mail);
    sessionRepo.deleteAll(cleanables);
  }


  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public boolean checkToken(String token, String userId) {
    logger.debug("checking token {} for user {}", token, userId);
    return getSession(token) != null && getUserByToken(token) != null && getUserByToken(token).getId().equals(userId);
  }

  public boolean checkTokenRole(String token, Role role) {
    logger.debug("checking token {} for role {}", token, role.toString());
    return getSession(token) != null && getUserByToken(token).getRole() == role;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void cleanUp() {
    List<Session> cleanables = sessionRepo.findAll()
      .stream()
      .filter(session -> !isTokenInProxy(session.getToken()) || userRepository.findByMail(session.getMail()) == null)
      .collect(Collectors.toList());

    logger.info("Starting session cleanup, will remove {} sessions", cleanables.size());
    sessionRepo.deleteAll(cleanables);
  }

}
