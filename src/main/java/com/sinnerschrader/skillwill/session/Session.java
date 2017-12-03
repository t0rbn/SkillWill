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
  private final String mail;

  @Version
  private Long version;

  public Session(String token) {
    this.token = token;
    this.mail = getMailFromToken(token);
  }

  private String getMailFromToken(String token) {
    // Oauth2 token: fooo|bar|baz -> foo = base64 encoded user mail
    try {
      return new String(Base64.getDecoder().decode(token.split("\\|")[0]));
    } catch (NullPointerException e) {
      return null;
    }
  }

  public String getMail() {
    return this.mail;
  }

  public String getToken() {
    return this.token;
  }

}
