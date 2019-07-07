package com.sinnerschrader.skillwill.misc;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exceptions.CredentialsException;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;



@Component
public class OAuthHelper {

  private static final Logger logger = LoggerFactory.getLogger(OAuthHelper.class);

  private final UserRepository userRepository;

  public OAuthHelper(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private String extractEmailFromToken(OAuth2AuthenticationToken token) {
    var email = token.getPrincipal().getAttributes().get("email").toString();
    logger.debug("extracted email {} from token", email);
    return email;
  }

  private String extractNameFromToken(OAuth2AuthenticationToken token) {
    var name = token.getPrincipal().getAttributes().get("name").toString();
    logger.debug("extracted name {} from token", name);
    return name;
  }

  public User getUserFromToken(OAuth2AuthenticationToken token) {
    User existingUser = userRepository.findByEmailIgnoreCase(extractEmailFromToken(token));
    if (existingUser == null) {
      existingUser = userRepository.save(new User(extractEmailFromToken(token), extractNameFromToken(token)));
    }
    return existingUser;
  }

  public void validateForUserId(OAuth2AuthenticationToken token, String userId) throws CredentialsException {
    logger.debug("checking for user {}", userId);
    if (!getUserFromToken(token).getId().equals(userId)) {
      throw new CredentialsException("user not permitted");
    }
  }

}
