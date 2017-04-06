package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.exceptions.CredentialsException;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Service handling Login and Logout
 * uses sessionService, but also creates new Persons on login
 *
 * @author torree
 */
@Service
@EnableRetry
public class LoginService {

  private final Logger logger = LoggerFactory.getLogger(LoginService.class);

  @Autowired
  private LdapService ldapService;

  @Autowired
  private PersonRepository personRepo;

  @Autowired
  private SessionService sessionService;

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public String login(String username, String password) {
    if (!ldapService.canAuthenticate(username, password)) {
      logger.info("Failed to login user {}", username);
      throw new CredentialsException("invalid credentials");
    }

    // Insert User if not already exisitng
    // So a user does not need to create an account fist
    if (personRepo.findById(username) == null) {
      try {
        Person newPerson = new Person(username);
        personRepo.insert(newPerson);
        ldapService.syncUser(newPerson);
        logger.info("Successfully created new user {}", username);
      } catch (DuplicateKeyException e) {
        // User has been created by another process while trying to create.
        // Ignore and continue
      }
    }

    logger.info("Successfully logged in {}", username);
    return sessionService.createSession(username);

  }

  public void logout(String session) throws IllegalArgumentException {
    sessionService.logout(session);
    logger.info("Logged out session {}", session);
  }

}
