package com.sinnerschrader.skillwill.controllers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sinnerschrader.skillwill.testinfrastructure.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LoginControllerTest {

	private LoginController loginController;
	
	private EmbeddedLdap embeddedLdap;

	@Before
	public void setUp() throws LDAPException, IOException {
		embeddedLdap = new EmbeddedLdap();
		embeddedLdap.start();

		loginController = new LoginController();
	}
	
	@Test
	public void loginValid() {
		assertTrue(loginController.login("foobar", "fleischcreme").getStatusCode() == HttpStatus.OK);
	}

}
