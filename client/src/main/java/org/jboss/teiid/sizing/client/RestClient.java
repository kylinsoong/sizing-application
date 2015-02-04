package org.jboss.teiid.sizing.client;

import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;


public class RestClient {
	
	static final String TMP_URL = "https://access.devgssci.devlab.phx1.redhat.com/labs/jbossdvsat/application/recommendations?start_at=1422201600&end_at=1522288000&pass_code=2d619d7805814b1721ebb7c6e9b3ca61";
	
	static final String PING = "nscvosanefinvfiwejojfoweijwoe3woo:";
	
	static final String USER = "rhn-support-jshi";
	static final String PASS = "redhat";
	
	static final String HOSTNAME = "access.devgssci.devlab.phx1.redhat.com";
	static final String BASE_URL = "https://" + HOSTNAME + "/labs/jbossdvsat";
	
	static String pass_code;
	static String start;
	static String end;
	
	static final String GET_ALL = BASE_URL + "/application/recommendations?" + pass_code;
	static final String GET_SECTION = BASE_URL + "?start_at=" + start + "&end_at=" + end + "&pass_code=" + pass_code;

	public static void main(String[] args) throws NoSuchAlgorithmException {
		
		AuthCache authCache = new BasicAuthCache();
		
		AuthScheme basicAuth = new BasicScheme();
		authCache.put(new HttpHost(BASE_URL), basicAuth);
		
		BasicHttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient, localContext);
		
		ResteasyClient client = new ResteasyClientBuilder().httpEngine(engine).build();
		ResteasyWebTarget target = client.target(TMP_URL);
		Response response = target.request().get();
		Object obj = response.getEntity();
		System.out.println(obj);
	}
	

}
