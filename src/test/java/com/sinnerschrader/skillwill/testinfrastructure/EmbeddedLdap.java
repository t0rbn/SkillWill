package com.sinnerschrader.skillwill.testinfrastructure;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

public class EmbeddedLdap {

	public static Logger logger = LoggerFactory.getLogger(EmbeddedLdap.class);

	private InMemoryDirectoryServer dirServer = null;

	public void start() throws LDAPException, IOException {
		InMemoryDirectoryServerConfig serverconfig = new InMemoryDirectoryServerConfig("dc=sinnerschrader,dc=com");
		serverconfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", InetAddress.getLocalHost(), 1338, null));
		serverconfig.setSchema(null);

		File ldifFile = new File(getClass().getClassLoader().getResource("testuser.ldif").getFile());
		dirServer = new InMemoryDirectoryServer(serverconfig);
		dirServer.importFromLDIF(false, new LDIFReader(ldifFile));
		dirServer.startListening();
	}

}
