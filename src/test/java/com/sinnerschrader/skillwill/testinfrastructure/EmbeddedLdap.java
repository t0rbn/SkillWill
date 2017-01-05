package com.sinnerschrader.skillwill.testinfrastructure;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;


/**
 * Embedded LDAP used for integration testing
 *
 * @author torree
 */
@Component
public class EmbeddedLdap {

	private InMemoryDirectoryServer dirServer = null;

	@PostConstruct
	public void startup() throws LDAPException, IOException {
		InMemoryDirectoryServerConfig serverconfig = new InMemoryDirectoryServerConfig("dc=sinnerschrader,dc=com");
		serverconfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", InetAddress.getLoopbackAddress(), 1338, null));
		serverconfig.setSchema(null);

		dirServer = new InMemoryDirectoryServer(serverconfig);
		dirServer.startListening();

		reset();
	}

	public void reset() throws LDAPException, IOException {
		File ldifFile = new File(getClass().getClassLoader().getResource("testuser.ldif").getFile());
		dirServer.importFromLDIF(true, new LDIFReader(ldifFile));
	}

}
