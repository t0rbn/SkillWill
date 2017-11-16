package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.IOException;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for LoginController
 *
 * @author torree
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LoginControllerTest {

  @Autowired
  private LoginController loginController;

  @Autowired
  private EmbeddedLdap embeddedLdap;

  @Autowired
  private PersonRepository personRepository;

  @Before
  public void setUp() throws LDAPException, IOException {
    embeddedLdap.reset();

    personRepository.deleteAll();
    personRepository.insert(new Person("foobar"));
  }

  @Test
  public void testLoginValid() throws JSONException {
    ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONObject resJSON = new JSONObject(res.getBody());
    assertTrue(resJSON.getString("sessionKey").matches("^[a-zA-Z0-9]{32}$"));
  }

  @Test
  public void testLoginUserInvalid() throws JSONException {
    ResponseEntity<String> res = loginController.login("IAmUnknown", "fleischcreme");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    JSONObject resJSON = new JSONObject(res.getBody());
    assertFalse(resJSON.has("sessionKey"));
  }

  @Test
  public void testLoginPasswordInvalid() throws JSONException {
    ResponseEntity<String> res = loginController.login("foobar", "cremefleisch");
    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    JSONObject resJSON = new JSONObject(res.getBody());
    assertFalse(resJSON.has("sessionKey"));
  }

  @Test
  public void testLogoutValid() throws JSONException {
    ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    JSONObject resJSON = new JSONObject(res.getBody());
    assertEquals(HttpStatus.OK, loginController.logout(resJSON.getString("sessionKey")).getStatusCode());
  }

  @Test
  public void testLogoutInvalidSession() {
    ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(HttpStatus.BAD_REQUEST, loginController.logout("NotQuiteASessionId").getStatusCode());
  }

  @Test
  public void testCheckSessionValid() throws JSONException {
    String session = new JSONObject(loginController.login("foobar", "fleischcreme").getBody()).getString("sessionKey");
    ResponseEntity<String> res = loginController.checkSession(session, "foobar");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(session, new JSONObject(res.getBody()).getString("sessionKey"));
    assertTrue(new JSONObject(res.getBody()).getBoolean("valid"));
  }

  @Test
  public void testCheckSessionInvalid() throws JSONException {
    String session = "bananenmousse";
    ResponseEntity<String> res = loginController.checkSession(session, "foobar");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(session, new JSONObject(res.getBody()).getString("sessionKey"));
    assertFalse(new JSONObject(res.getBody()).getBoolean("valid"));
  }

  @Test
  public void testCreateNewUser() {
    personRepository.deleteAll();
    loginController.login("foobar", "fleischkrem");
    assertNotNull(personRepository.findById("foobar"));
  }

  public void testCreateNewUserIgnoreCapitalization() {
    personRepository.deleteAll();
    assertEquals(HttpStatus.OK, loginController.login("fOObaR", "fleischkrem").getStatusCode());

    Optional<Person> person = personRepository.findById("foobar");
    assertTrue(person.isPresent());
    assertEquals("foobar", person.get().getId());
  }

}
