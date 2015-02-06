package org.jboss.teiid.sizing.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;


public class RestClient {

	
	static final String USER = "rhn-support-jshi";
	static final String PASS = "redhat";
	
	static final String HOSTNAME = "access.devgssci.devlab.phx1.redhat.com";
	
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, 1, 1);
		Date start = calendar.getTime();
		Date end = new Date();
		String url = RestClientHelper.getURL(HOSTNAME,start, end);
		
		System.out.println(url);
		
		ResteasyClient client = RestClientHelper.createResteasyClient(HOSTNAME, USER, PASS);

		WebTarget target = client.target(url);
		
		Response response = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
				
		response.bufferEntity();
		
//		System.out.println(response.hasEntity());
//		System.out.println(response.readEntity(String.class));
//		response.
	
		
		client.close();
	}
	

}
