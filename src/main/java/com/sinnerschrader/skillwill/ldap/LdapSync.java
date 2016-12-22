package com.sinnerschrader.skillwill.ldap;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.person.Person;
import com.sinnerschrader.skillwill.repositories.PersonRepository;

/**
 * Utility class adding details from LDAP
 * to Persons
 *
 * @author torree
 *
 */
@Component
public class LdapSync {

	private static Logger logger = LoggerFactory.getLogger(LdapSync.class);

	private String ldapBaseDN;

	@Autowired
	private PersonRepository personRepo;

	// @Value on static String does not work
	// -> non-static setter works
	@Value("${ldapBaseDN}")
	private void setLdapBaseDN(String ldapBaseDN) {
		this.ldapBaseDN = ldapBaseDN;
	}

	public void syncUser(Person person) {
		PersonalLDAPDetails ldapDetails = null;
		NamingEnumeration<SearchResult> answer;
		LdapContext ctx = LdapUtils.getLdapContext();

		try {
			answer = ctx.search(ldapBaseDN, "uid=" + person.getId(), LdapUtils.getSearchControls());
			if (answer.hasMore()) {
				Attributes attrs = answer.next().getAttributes();
				ldapDetails = new PersonalLDAPDetails();
				ldapDetails.setFirstName(attrs.get("givenName").get().toString());
				ldapDetails.setLastName(attrs.get("sn").get().toString());
				ldapDetails.setMail(attrs.get("mail").get().toString());
				ldapDetails.setLocation(attrs.get("l").get().toString());
				ldapDetails.setPhone(attrs.get("telephoneNumber").get().toString());
				ldapDetails.setTitle(attrs.get("title").get().toString());

				person.setLdapDetails(ldapDetails);
				personRepo.save(person);
			} else {
				logger.error("Failed to find user {} with LDAP details, but user is in DB", person.getId());
			}
		} catch (NamingException e) {
			logger.error("Faied to retrieve Information from LDAP: Naming Error");
		}
	}

	public void syncUsers(List<Person> persons) {
		for (Person p : persons) {
			syncUser(p);
		}
	}

	@Scheduled(cron = "${ldapSyncCron}")
	private void syncAll() {
		logger.info("Starting regular LDAP sync, this may take a while");
		syncUsers(personRepo.findAll());
		logger.info("Finished regular LDAP sync");
	}

}
