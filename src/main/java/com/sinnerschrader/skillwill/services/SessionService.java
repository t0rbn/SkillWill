package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.Role;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
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

  @Autowired
  private PersonRepository personRepository;

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public String create(String username) {
    String key = null;
    do {
      key = UUID.randomUUID().toString().replaceAll("-", "");
    } while (key == null || sessionRepo.findByKey(key) != null);

    // Session is initialized with expire date = now
    // renew to set initial expiration date
    Session session = new Session(key, username, new Date());
    sessionRepo.insert(session);
    renew(session);

    logger.debug("Created new session for {}", username);

    return session.getKey();
  }

  public void remove(String sessionKey) {
    Session session = sessionRepo.findByKey(sessionKey);

    if (session == null) {
      logger.debug("Failed to log out session with key {}: no session found", sessionKey);
      throw new IllegalArgumentException("session key not found or username not matching");
    }

    sessionRepo.delete(session);
  }

  public boolean check(Session session) {
    if (session == null) {
      return false;
    }

    if (session.isExpired() || getPerson(session) == null) {
      logger.debug("Failed checking session {}: expired; will remove from DB", session.getKey());
      sessionRepo.delete(session);
      return false;
    }

    renew(session);
    logger.debug("Successfully checked session {}", session.getKey());
    return true;
  }

  public boolean check(String sessionKey) {
    return check(sessionRepo.findByKey(sessionKey));
  }

  public boolean check(String sessionKey, String username) {
    Session session = sessionRepo.findByKey(sessionKey);
    return check(session) && session.getUsername().equals(username);
  }

  public boolean check(String sessionKey, Role role) {
    Session session = sessionRepo.findByKey(sessionKey);
    return check(session) && getPerson(session).getRole() == role;
  }

  private Person getPerson(Session session) {
    return personRepository.findByIdIgnoreCase(session.getUsername());
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  private void renew(Session session) {
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
