package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.repositories.SessionRepository;
import com.sinnerschrader.skillwill.session.Session;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
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

  @Autowired
  private SessionRepository sessionRepo;

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public String createSession(String username) {
    String key = null;
    do {
      key = UUID.randomUUID().toString().replaceAll("-", "");
    } while (key == null || sessionRepo.findByKey(key) != null);

    // Session is initialized with expire date = now
    // renew to set initial expiration date
    Session session = new Session(key, username, new Date());
    sessionRepo.insert(session);
    renewSession(session);

    logger.debug("Created new session for {}", username);

    return session.getKey();
  }

  public boolean isValidSession(String username, String sessionKey) {
    Session session = sessionRepo.findByKey(sessionKey);

    if (session == null) {
      logger.debug("Failed checking session {}: not in DB (already removed)", sessionKey);
      return false;
    }

    if (!session.getUsername().equals(username)) {
      logger.debug("Failed checking session {}: username does not match key", sessionKey);
      return false;
    }

    if (session.isExpired()) {
      logger.debug("Failed checking session {}: expired; will remove from DB", sessionKey);
      sessionRepo.delete(session);
      return false;
    }

    renewSession(session);
    logger.debug("Successfully checked session {}", sessionKey);

    return true;
  }

  public void logout(String sessionKey) {
    Session session = sessionRepo.findByKey(sessionKey);

    if (session == null) {
      logger.debug("Failed to log out session with key {}: no session found", sessionKey);
      throw new IllegalArgumentException("session key not found or username not matching");
    }

    sessionRepo.delete(session);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void renewSession(Session session) {
    logger.debug("Renewed session {}", session.getKey());
    session.renewSession(expireDuration);
    sessionRepo.save(session);
  }

  public void cleanUp() {
    List<Session> expiredSessions = sessionRepo.findAll().stream()
        .filter(Session::isExpired)
        .collect(Collectors.toList());

    sessionRepo.delete(expiredSessions);
  }

}
