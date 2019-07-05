package com.sinnerschrader.skillwill.services;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exceptions.CredentialsException;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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

  @Value("${oAuthUrl}")
  private String oAuthUrl;

  private final UserRepository userRepository;

  @Autowired
  public SessionService(UserRepository UserRepository) {
    this.userRepository = UserRepository;
  }

  @Cacheable(key = "session")
  public User getCurrentUser(String token) {
    if (!isTokenInProxy(token)) {
      throw new CredentialsException("session validation failed");
    }

    String emailFromSession = extractMail(token);
    User existingUser = userRepository.findByEmailIgnoreCase(emailFromSession);
    if (existingUser == null) {
      existingUser = userRepository.save(new User(emailFromSession));
    }
    return existingUser;
  }

  public void validateForUserId(String token, String userId) {
    if (!getCurrentUser(token).getId().equals(userId)) {
      throw new CredentialsException("validation against session failed");
    }
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

  public String extractMail(String token) {
    return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
  }

}
