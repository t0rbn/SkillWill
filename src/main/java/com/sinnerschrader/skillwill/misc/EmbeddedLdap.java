package com.sinnerschrader.skillwill.misc;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;


/**
 * Embedded LDAP used for integration testing
 *
 * @author torree
 */
@Component
@Scope("singleton")
public class EmbeddedLdap {

	private static Logger logger = LoggerFactory.getLogger(EmbeddedLdap.class);

	private InMemoryDirectoryServer dirServer = null;

	public void startup() throws LDAPException, IOException {
		logger.warn("Starting embedded LDAP");

		InMemoryDirectoryServerConfig serverconfig = new InMemoryDirectoryServerConfig("dc=sinnerschrader,dc=com");
		serverconfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", InetAddress.getLoopbackAddress(), 1338, null));
		serverconfig.setSchema(null);

		dirServer = new InMemoryDirectoryServer(serverconfig);
		dirServer.startListening();

		reset();
	}

	public void reset() throws LDAPException, IOException {
		logger.warn("Resetting embedded LDAP");

		InputStream ldifStream = getClass().getResourceAsStream("/testuser.ldif");
		dirServer.importFromLDIF(true, new LDIFReader(ldifStream));
	}

}
