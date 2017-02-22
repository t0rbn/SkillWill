package com.sinnerschrader.skillwill.services;


import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.PersonalLdapDetails;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling LDAP auth and data retrieval
 *
 * @author torree
 */
@Service
@Scope("singleton")
@EnableRetry
public class LdapService {

	private static Logger logger = LoggerFactory.getLogger(LdapService.class);

	private static String ldapUrl;

	private static String ldapBaseDN;

	private static int ldapPort;

	private static boolean ldapEmbeded;

	private static LDAPConnection ldapConnection;

	private static boolean ldapSsl;

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private EmbeddedLdap ldap;

	@SuppressWarnings("static-access")
	@Value("${ldapUrl}")
	private void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	@SuppressWarnings("static-access")
	@Value("${ldapPort}")
	private void setLdapPort(String portString) {
		this.ldapPort = Integer.parseInt(portString);
	}

	@SuppressWarnings("static-access")
	@Value("${ldapBaseDN}")
	public void setLdapBaseDN(String ldapBaseDN) {
		this.ldapBaseDN = ldapBaseDN;
	}

	@SuppressWarnings("static-access")
	@Value("${ldapEmbeded}")
	public void setLdapEmbed(String propString) {
		ldapEmbeded = propString.equals("true");
	}

	@SuppressWarnings("static-access")
	@Value("${ldapSsl}")
	public void setLdapSsl(String propString) {
		ldapSsl = Boolean.parseBoolean(propString);
	}

	@PostConstruct
	private void setup() {
		if (ldapEmbeded) {
			try {
				ldap.startup();
			} catch (LDAPException | IOException e) {
				logger.error("Failed to start embedded LDAP");
			}
		}

		try {

			if (ldapSsl) {
				SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
				SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
				ldapConnection = new LDAPConnection(sslSocketFactory);
			} else {
				ldapConnection = new LDAPConnection();
			}
			ldapConnection.connect(ldapUrl, ldapPort);
		} catch (LDAPException | GeneralSecurityException e) {
			logger.error("Failed to connect to LDAP", e.getStackTrace());
		}

	}

	@PreDestroy
	private void closeConnection() {
		if (ldapConnection != null) {
			ldapConnection.close();
		}
	}

	@Retryable(include=OptimisticLockingFailureException.class, maxAttempts=10)
	public void syncUser(Person person) {
		List<Person> lst = new ArrayList<>();
		lst.add(person);
		syncUsers(lst, false);
	}

	public List<Person> syncUsers(List<Person> persons, boolean forceUpdate) {
		List<Person> updatablePersons;

		if (forceUpdate) {
			updatablePersons = persons;
		} else {
			updatablePersons = persons.stream().filter(p -> p.getLdapDetails() == null).collect(Collectors.toList());
		}

		if (updatablePersons.isEmpty()) {
			return persons;
		}

		logger.info("Starting LDAP sync for users: {}", persons.stream().map(p -> p.getId()).collect(Collectors.joining(", ")));

		try {
			// searchString is an LDAP expression, e.g. "(|(uid=foobar)(uid=bazfoo))"
			String searchString = "(|" + persons.stream().map(p -> "(uid=" + p.getId() + ")").collect(Collectors.joining("")) + ")";
			SearchResult res = ldapConnection.search(new SearchRequest(ldapBaseDN, SearchScope.SUB, searchString));
			for (Person person : updatablePersons) {
				SearchResultEntry entry = res.getSearchEntry("uid=" + person.getId() + "," + ldapBaseDN);
				try {
					PersonalLdapDetails newDetails = new PersonalLdapDetails(
							entry.getAttributeValue("givenName"),
							entry.getAttributeValue("sn"),
							entry.getAttributeValue("mail"),
							entry.getAttributeValue("telephoneNumber"),
							entry.getAttributeValue("l"),
							entry.getAttributeValue("title")
					);
					if (!newDetails.equals(person.getLdapDetails())) {
						person.setLdapDetails(newDetails);
						personRepo.save(person);
					}
				} catch (NullPointerException e) {
					logger.info("Failed to sync user {}: will remove from DB", person.getId());
					personRepo.delete(person);
				}
			}
		} catch (LDAPException e) {
			logger.error("Failed to sync with LDAP: LDAP error", e);
		}

		return persons;
	}

	public boolean canAuthenticate(String username, String password) {
		try {
			BindRequest bindRequest = new SimpleBindRequest("uid=" + username + "," + ldapBaseDN, password);
			BindResult bindResult = ldapConnection.bind(bindRequest);
			if (bindResult.getResultCode().equals(ResultCode.SUCCESS)) {
				return true;
			}
			return false;
		} catch (LDAPBindException e) {
			return false;
		} catch (LDAPException e) {
			logger.error("Failed to authenticate: LDAP error", e);
		}
		return false;
	}
}
