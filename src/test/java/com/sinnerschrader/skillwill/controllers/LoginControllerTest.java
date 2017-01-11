package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;
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

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

	@Before
	public void setUp() throws LDAPException, IOException {
		embeddedLdap.reset();
	}

	@Test
	public void testLoginValid() throws JSONException {
		ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONObject resJSON = new JSONObject(res.getBody());
		assertTrue(resJSON.getString("session").matches("^[a-zA-Z0-9]{32}$"));
	}

	@Test
	public void testLoginUserInvalid() throws JSONException {
		ResponseEntity<String> res = loginController.login("IAmUnknown", "fleischcreme");
		assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED);
		JSONObject resJSON = new JSONObject(res.getBody());
		assertFalse(resJSON.has("session"));
	}

	@Test
	public void testLoginPasswordInvalid() throws JSONException {
		ResponseEntity<String> res = loginController.login("foobar", "cremefleisch");
		assertTrue(res.getStatusCode() == HttpStatus.UNAUTHORIZED);
		JSONObject resJSON = new JSONObject(res.getBody());
		assertFalse(resJSON.has("session"));
	}

	@Test
	public void testLogoutValid() throws JSONException {
		ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		JSONObject resJSON = new JSONObject(res.getBody());
		assertTrue(loginController.logout(resJSON.getString("session")).getStatusCode() == HttpStatus.OK);
	}

	@Test
	public void testLogoutInvalidSession() {
		ResponseEntity<String> res = loginController.login("foobar", "fleischcreme");
		assertTrue(res.getStatusCode() == HttpStatus.OK);
		assertTrue(loginController.logout("NotQuiteASessionId").getStatusCode() == HttpStatus.BAD_REQUEST);
	}


}
