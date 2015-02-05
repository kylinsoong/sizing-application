package org.jboss.teiid.sizing.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

public class RestClientHelper {
		
	private static final String DEFAULT_USER = "rhn-support-jshi";
	private static final String DEFAULT_PASS = "redhat";
	private static final String DEFAULT_HOST = "access.devgssci.devlab.phx1.redhat.com";
	
	static final String PING = "nscvosanefinvfiwejojfoweijwoe3woo:";
	
	static MD5Encrypt md5 = new MD5Encrypt(PING);
	
	public static String baseURL(){
		return baseURL(DEFAULT_HOST);
	}
	
	public static String baseURL(String hostname){
		return "https://" + hostname + "/labs/jbossdvsat";
	}
	
	public static String getURL(){
		return getURL(DEFAULT_HOST);
	}
	
	public static String getURL(Date start, Date end){
		return getURL(DEFAULT_HOST, start, end);
	}
	
	public static String getURL(String hostname, Date start, Date end){
		String startStamp = md5.getTimeStamp(start);
		String endStamp = md5.getTimeStamp(end);
		return baseURL(hostname) + "/application/recommendations?start_at=" + startStamp + "&end_at=" + endStamp + "&pass_code=" + md5.generatePassCode();
	}
	
	public static String getURL(String hostname){
		return baseURL(hostname) + "/application/recommendations?pass_code=" + md5.generatePassCode();
	}
	
	public static ResteasyClient createResteasyClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		return createResteasyClient(DEFAULT_HOST, DEFAULT_USER, DEFAULT_PASS);
	}

	public static ResteasyClient createResteasyClient(String hostname, String username, String password) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		HttpClientContext context = null;
		
		// authentication
		if (username != null && password != null) {
			
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			
//			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
			HttpHost targetHost = new HttpHost(hostname, 443, "https");
			AuthScope scope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			
			credsProvider.setCredentials(scope, credentials);
			
			AuthCache authCache = new BasicAuthCache();
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(targetHost, basicAuth);
			
			context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
			context.setAuthCache(authCache);

			builder.setDefaultCredentialsProvider(credsProvider);
		}
		
		// disable SSL
		SSLContextBuilder sslbuilder = new SSLContextBuilder();
		sslbuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslbuilder.build());
		builder.setSSLSocketFactory(sslsf);
		
		HttpClient httpClient = builder.build();
		ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient, context);
		
		ResteasyClient client = new ResteasyClientBuilder().httpEngine(engine).build();
		
		return client;
	}
}
