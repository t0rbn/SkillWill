package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.session.Session;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
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

  private final SessionRepository sessionRepo;

  private final UserRepository userRepository;

  @Autowired
  public SessionService(SessionRepository sessionRepo, UserRepository UserRepository) {
    this.sessionRepo = sessionRepo;
    this.userRepository = UserRepository;
  }

  private boolean isTokenInProxy(String token) {
    try {
      var authUrl = new URL(oAuthUrl);
      var connection = (HttpURLConnection) authUrl.openConnection();
      connection.addRequestProperty("Cookie", "_oauth2_proxy=" + token);
      connection.connect();

      var responseCode = connection.getResponseCode();
      connection.disconnect();

      logger.debug("Successfully checked token with oauth proxy, result {}", responseCode);
      return responseCode == HttpStatus.ACCEPTED.value();
    } catch (IOException e) {
      logger.error("Failed to check session token at oauth Proxy");
      return false;
    }
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private boolean isTokenInDB(String token) {
    return sessionRepo.findByToken(token) != null;
  }

  public String extractMail(String token) {
    return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public User getUserByToken(String token) {
    var session = getSession(token);
    if (session == null) {
      return null;
    }
    return userRepository.findByEmailIgnoreCase(session.getEmail());
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
      if (userRepository.findByEmailIgnoreCase(extractMail(token)) == null) {
        // user not in db yet, will create
        var newUser = new User(extractMail(token));
        userRepository.insert(newUser);
        logger.info("Successfully created new user {}", newUser.getEmail());
      }

      // session not in DB, but in proxy -> create new session and revalidate old ones
      refreshUserSessions(extractMail(token));
      var newSession = new Session(token);
      sessionRepo.insert(newSession);
      return newSession;
    }

    logger.debug("Failed to get Session for token {}: no session found", token);
    return null;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void refreshUserSessions(String mail) {
    var cleanables = sessionRepo.findByEmail(mail).stream()
      .filter(session -> !isTokenInProxy(session.getToken()))
      .collect(Collectors.toList());

    logger.debug("will remove {} sessions for mail {}", cleanables.size(), mail);
    sessionRepo.deleteAll(cleanables);
  }


  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public boolean checkToken(String token, String email) {
    logger.debug("checking token {} for user {}", token, email);
    return getSession(token) != null && getUserByToken(token) != null && getUserByToken(token).getEmail().equals(email);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void cleanUp() {
    var cleanables = sessionRepo.findAll()
      .stream()
      .filter(session -> !isTokenInProxy(session.getToken()) || userRepository.findByEmailIgnoreCase(session.getEmail()) == null)
      .collect(Collectors.toList());

    logger.info("Starting session cleanup, will remove {} sessions", cleanables.size());
    sessionRepo.deleteAll(cleanables);
  }

}
