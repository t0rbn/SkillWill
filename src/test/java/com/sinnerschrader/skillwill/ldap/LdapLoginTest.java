package com.sinnerschrader.skillwill.ldap;

import com.sinnerschrader.skillwill.testinfrastructure.EmbeddedLdap;
import com.unboundid.ldap.sdk.LDAPException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for LdapLogin
 *
 * @author torree
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LdapLoginTest {

	@Autowired
	private EmbeddedLdap ldap;

	@Before
	public void setUp() throws IOException, LDAPException {
		ldap.reset();
	}

	@Test
	public void testValidCredentials() {
		assertTrue(LdapLogin.canAuthenticate("foobar", "fleischcreme"));
	}

	@Test
	public void testInvalidUser() {
		assertFalse(LdapLogin.canAuthenticate("IAmUnknown", "fleischcreme"));
	}

	@Test
	public void testInvalidPassword() {
		assertFalse(LdapLogin.canAuthenticate("foobar", "cremefleisch"));
	}

}
