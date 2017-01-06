package com.sinnerschrader.skillwill.services;


import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.PersonalLDAPDetails;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling LDAP auth and data retrieval
 *
 * @author torree
 */
@Service
@EnableRetry
public class LdapService {

	private static Logger logger = LoggerFactory.getLogger(LdapService.class);

	private static String ldapUrl;

	private static String ldapBaseDN;

	@Autowired
	private PersonRepository personRepo;

	@SuppressWarnings("static-access")
	@Value("${ldapUrl}")
	private void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	@SuppressWarnings("static-access")
	@Value("${ldapBaseDN}")
	public void setLdapBaseDN(String ldapBaseDN) {
		this.ldapBaseDN = ldapBaseDN;
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void syncUser(Person person) {
		PersonalLDAPDetails newLdapDetails = null;
		NamingEnumeration<SearchResult> answer;
		LdapContext ctx = getLdapContext();

		try {
			answer = ctx.search(ldapBaseDN, "uid=" + person.getId(), getSearchControls());
			if (answer.hasMore()) {
				Attributes attrs = answer.next().getAttributes();
				newLdapDetails = new PersonalLDAPDetails(
						attrs.get("givenName").get().toString(),
						attrs.get("sn").get().toString(),
						attrs.get("mail").get().toString(),
						attrs.get("telephoneNumber").get().toString(),
						attrs.get("l").get().toString(),
						attrs.get("title").get().toString()
				);

				if (!newLdapDetails.equals(person.getLdapDetails())) {
					person.setLdapDetails(newLdapDetails);
					personRepo.save(person);
				}
			} else {
				logger.warn("Failed to sync user {}: in DB but not found in LDAP, will remove", person.getId());
				personRepo.delete(person);
			}
		} catch (NamingException e) {
			logger.error("Faied to retrieve Information from LDAP: Naming Error");
		}
	}

	public void syncUsers(List<Person> persons, boolean forceUpdate) {
		if (!forceUpdate) {
			persons = persons.stream().filter(p -> p.getLdapDetails() == null).collect(Collectors.toList());
		}
		persons.forEach(p -> syncUser(p));
	}

	public boolean canAuthenticate(String username, String password) {
		// ctx is never used.
		// It will be assigned a new InitialLdapContext
		// If this assigment does not fail, the credentails are correct
		LdapContext ctx;

		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.PROVIDER_URL, ldapUrl);
			env.put(Context.REFERRAL, "follow");
			env.put(Context.SECURITY_PRINCIPAL, "uid=" + username + "," + ldapBaseDN);
			env.put(Context.SECURITY_CREDENTIALS, password);
			ctx = new InitialLdapContext(env, null);
			ctx.close();
			logger.debug("Successfully authenticated user {}", username);
			return true;
		} catch (AuthenticationException auth) {
			logger.debug("Failed to authenticate user {}", username);
			return false;
		} catch (Exception e) {
			logger.error("Failed to authenticate: LDAP Error", e);
			return false;
		}
	}

	private LdapContext getLdapContext() {
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.PROVIDER_URL, ldapUrl);
			env.put(Context.REFERRAL, "follow");
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException e) {
			logger.error("Failed to create LDAP Context: Naming Error", e);
		}
		return ctx;
	}

	private SearchControls getSearchControls() {
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = {"mail", "l", "telephoneNumber", "sn", "givenName", "title"};
		cons.setReturningAttributes(attrIDs);
		return cons;
	}

}
