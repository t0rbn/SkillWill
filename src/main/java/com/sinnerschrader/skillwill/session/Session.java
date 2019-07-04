package com.sinnerschrader.skillwill.session;

import java.util.Base64;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

/**
 * Sesion information
 *
 * @author torree
 */
public class Session {

  @Id
  private final String token;
  private final String email;

  @Version
  private Long version;

  public Session(String token) {
    this.token = token;
    this.email = getMailFromToken(token);
  }

  private String getMailFromToken(String token) {
    // Oauth2 token: fooo|bar|baz -> foo = base64 encoded user email
    try {
      return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
    } catch (NullPointerException e) {
      return null;
    }
  }

  public String getEmail() {
    return this.email;
  }

  public String getToken() {
    return this.token;
  }

}
