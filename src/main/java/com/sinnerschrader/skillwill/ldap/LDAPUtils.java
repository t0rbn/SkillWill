package com.sinnerschrader.skillwill.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility Class for LDAP
 * 
 * @author torree
 *
 */
@Component
public class LDAPUtils {

	private static Logger logger = LoggerFactory.getLogger(LDAPUtils.class);

	private static String ldapUrl;

	@SuppressWarnings("static-access")
	@Value("${ldapUrl}")
	private void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	public static LdapContext getLdapContext() {
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.PROVIDER_URL, ldapUrl);
			env.put(Context.REFERRAL, "follow");
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException nex) {
			logger.error("LDAP Naming Error");
			nex.printStackTrace();
		}
		return ctx;
	}

	public static SearchControls getSearchControls() {
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { "mail", "l", "telephoneNumber", "sn", "givenName" };
		cons.setReturningAttributes(attrIDs);
		return cons;
	}

}
