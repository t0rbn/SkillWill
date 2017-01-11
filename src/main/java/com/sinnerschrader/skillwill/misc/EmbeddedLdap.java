package com.sinnerschrader.skillwill.misc;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
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

	private InMemoryDirectoryServer dirServer = null;

	public void startup() throws LDAPException, IOException {
		InMemoryDirectoryServerConfig serverconfig = new InMemoryDirectoryServerConfig("dc=sinnerschrader,dc=com");
		serverconfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", InetAddress.getLoopbackAddress(), 1338, null));
		serverconfig.setSchema(null);

		dirServer = new InMemoryDirectoryServer(serverconfig);
		dirServer.startListening();

		reset();
	}

	public void reset() throws LDAPException, IOException {
		InputStream ldifStream = getClass().getResourceAsStream("/testuser.ldif");
		dirServer.importFromLDIF(true, new LDIFReader(ldifStream));
	}

}
