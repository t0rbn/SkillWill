package com.sinnerschrader.skillwill.ldap;

import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.person.Person;

/**
 * Utility class adding details from LDAP
 * to Persons
 *
 * @author torree
 *
 */
@Component
public class LDAPEnricher {

	private static Logger logger = LoggerFactory.getLogger(LDAPEnricher.class);

	private static String ldapBaseDN;

	// @Value on static String does not work
	// -> non-static setter works
	@SuppressWarnings("static-access")
	@Value("${ldapBaseDN}")
	public void setLdapBaseDN(String ldapBaseDN) {
		this.ldapBaseDN = ldapBaseDN;
	}

	public static void enrichAll(List<Person> persons) {
		LdapContext ctx = LDAPUtils.getLdapContext();
		for (Person p : persons) {
			enrichPerson(p, ctx);
		}
	}

	public static void enrichPerson(Person person, LdapContext ctx) {
		PersonalLDAPDetails ldapDetails = null;
		NamingEnumeration<SearchResult> answer;

		logger.debug("Addind LDAP details to " + person.getId());

		try {
			answer = ctx.search(ldapBaseDN, "uid=" + person.getId(), LDAPUtils.getSearchControls());
			if (answer.hasMore()) {
				Attributes attrs = answer.next().getAttributes();
				ldapDetails = new PersonalLDAPDetails();
				ldapDetails.setFirstName(attrs.get("givenName").get().toString());
				ldapDetails.setLastName(attrs.get("sn").get().toString());
				ldapDetails.setMail(attrs.get("mail").get().toString());
				ldapDetails.setLocation(attrs.get("l").get().toString());
				ldapDetails.setPhone(attrs.get("telephoneNumber").get().toString());

				person.setLDAPDetails(ldapDetails);
			} else {
				logger.info("User " + person.getId() + " not found in LDAP");
				System.out.println("user not found.");
			}
		} catch (NamingException e) {
			logger.error("General LDAP Error");
			e.printStackTrace();
		}
	}

}
