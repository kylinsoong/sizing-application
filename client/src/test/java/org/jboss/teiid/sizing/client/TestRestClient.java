package org.jboss.teiid.sizing.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.junit.Test;

public class TestRestClient {
	
	@Test
	public void testInitialization() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		ResteasyClient client = RestClientHelper.createResteasyClient();
		assertNotNull(client);
		client.close();
	}
	
	@Test
	public void testGetURL() {
		assertEquals(RestClientHelper.getURL(), RestClientHelper.getURL());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, 1, 1);
		Date start = calendar.getTime();
		Date end = new Date();
		assertEquals(RestClientHelper.getURL(start, end), RestClientHelper.getURL(start, end));
	}
	
	

}
