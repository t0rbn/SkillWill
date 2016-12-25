package com.sinnerschrader.skillwill.ldap;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Use the LDAP to authenticate users
 *
 * @author torree
 */
@Component
public class LdapLogin {

    private static Logger logger = LoggerFactory.getLogger(LdapLogin.class);

    private static String ldapUrl;

    private static String ldapBaseDN;

    // @Value on static String does not work
    // -> non-static setter works
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

    public static boolean canAuthenticate(String username, String password) {
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

}

