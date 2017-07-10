package com.sinnerschrader.skillwill.services;


import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.PersonalLdapDetails;
import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
import com.sinnerschrader.skillwill.repositories.PersonRepository;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Service handling LDAP auth and data retrieval
 *
 * @author torree
 */
@Service
@EnableRetry
public class LdapService {

  private static final Logger logger = LoggerFactory.getLogger(LdapService.class);
  private static LDAPConnection ldapConnection;

  @Value("${ldapUrl}")
  private String ldapUrl;

  @Value("${ldapPort}")
  private int ldapPort;

  @Value("${ldapAuthBaseDN}")
  private String ldapAuthBaseDN;

  @Value("${ldapLookupBaseDN}")
  private String ldapLookupBaseDN;

  @Value("${ldapEmbeded}")
  private boolean ldapEmbeded;

  @Value("${ldapSsl}")
  private boolean ldapSsl;

  @Value("${ldapLookupUser}")
  private String ldapLookupUser;

  @Value("${ldapLookupPassword}")
  private String ldapLookupPassword;

  @Autowired
  private PersonRepository personRepo;

  @Autowired
  private EmbeddedLdap embeddedLdap;

  private void tryStartEmbeddedLdap() {
    if (!ldapEmbeded) {
      return;
    }

    try {
      embeddedLdap.startup();
    } catch (LDAPException | IOException e) {
      logger.error("Failed to start embedded LDAP");
    }
  }

  @PostConstruct
  private void openConnection() {
    tryStartEmbeddedLdap();

    try {
      if (ldapSsl) {
        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
        ldapConnection = new LDAPConnection(sslSocketFactory);
      } else {
        ldapConnection = new LDAPConnection();
      }
      ldapConnection.connect(ldapUrl, ldapPort);
      logger.info("Successfully connected to LDAP");
    } catch (LDAPException | GeneralSecurityException e) {
      logger.error("Failed to connect to LDAP", e);
    }
  }

  @PreDestroy
  private void closeConnection() {
    if (ldapConnection != null) {
      ldapConnection.close();
    }
  }

  private void ensureConnection() {
    if (!ldapConnection.isConnected()) {
      openConnection();
    }
  }

  private void bindAsTechnicalUser() throws LDAPException {
    ldapConnection.bind(new SimpleBindRequest("cn=" + ldapLookupUser + "," + ldapLookupBaseDN, ldapLookupPassword));
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void syncUser(Person person) {
    List<Person> lst = new ArrayList<>();
    lst.add(person);
    syncUsers(lst, false);
  }

  public List<Person> syncUsers(List<Person> persons, boolean forceUpdate) {
    logger.info("Starting LDAP sync for users: {}",
      persons.stream().map(Person::getId).collect(Collectors.joining(", ")));

    ensureConnection();

    try {
      bindAsTechnicalUser();
    } catch (LDAPException e) {
      logger.error("Failed to sync users: cannot bind as technical user", e);
    }

    List<Person> updatablePersons = forceUpdate
      ? persons
      : persons.stream().filter(p -> p.getLdapDetails() == null).collect(Collectors.toList());
    List<Person> returnPersons = new ArrayList<>(persons);

    if (CollectionUtils.isEmpty(updatablePersons)) {
      return persons;
    }

    // searchString is an LDAP expression, e.g. "(|(uid=foobar)(uid=bazfoo))"
    String searchString = "(|" + persons.stream()
      .map(p -> "(uid=" + p.getId() + ")")
      .collect(Collectors.joining("")) + ")";

    SearchResult searchResult;
    try {
      searchResult = ldapConnection.search(new SearchRequest(ldapAuthBaseDN, SearchScope.SUB, searchString));
    } catch (LDAPException e) {
      logger.error("Failed to sync users: LDAP error");
      return persons;
    }

    for (Person person : updatablePersons) {
      SearchResultEntry resultEntry;
      try {
        resultEntry = searchResult.getSearchEntry("uid=" + person.getId() + "," + ldapAuthBaseDN);
      } catch (LDAPException e) {
        logger.error("Failed to sync user {}: LDAP error", person.getId());
        continue;
      }

      if (resultEntry == null && checkAndRemoveFromDB(person.getId())) {
        returnPersons.remove(person);
        continue;
      }

      PersonalLdapDetails newDetails = new PersonalLdapDetails(resultEntry);

      if (!newDetails.equals(person.getLdapDetails())) {
        person.setLdapDetails(newDetails);
        personRepo.save(person);
      }
    }

    return returnPersons;
  }

  public boolean canAuthenticate(String username, String password) {
    ensureConnection();

    try {
      BindRequest bindRequest = new SimpleBindRequest("uid=" + username + "," + ldapAuthBaseDN, password);
      BindResult bindResult = ldapConnection.bind(bindRequest);
      return bindResult.getResultCode().equals(ResultCode.SUCCESS);
    } catch (LDAPBindException e) {
      return false;
    } catch (LDAPException e) {
      logger.error("Failed to authenticate: LDAP error", e);
    }
    return false;
  }

  // Check if a user is not in the LDAP, then remove from the DB
  private boolean checkAndRemoveFromDB(String uid) {
    if (StringUtils.isEmpty(uid)) {
      return false;
    }

    ensureConnection();

    try {
      bindAsTechnicalUser();
      SearchResult searchResult = ldapConnection.search(new SearchRequest(ldapAuthBaseDN, SearchScope.SUB, "(uid=" + uid + ")"));
      if (searchResult.getEntryCount() > 0) {
        logger.info("Found user {} in LDAP, will not remove", uid);
        return false;
      }
    } catch (LDAPException e) {
      logger.error("Failed to search for user {} in LDAP: LDAP error", uid);
      return false;
    }

    Person person = personRepo.findByIdIgnoreCase(uid);
    if (person == null) {
      logger.debug("Failed to remove person {}: not in DB", uid);
      return false;
    }

    personRepo.delete(person);
    logger.warn("Successfully deleted person {} (not found in LDAP)", uid);
    return true;
  }

}
