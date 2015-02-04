package org.jboss.teiid.sizing.client;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

public class RestRequestHelper {
	
	private static int DEFAULT_TIMEOUT = 5;

	public static ClientRequestFactory createRestRequest(String restBaseUrl, String username, String password) {

		return createAuthenticatingRequestFactory(restBaseUrl, username, password, DEFAULT_TIMEOUT);
	}
	
	public static ClientRequestFactory createRestRequest(String restBaseUrl, String username, String password, int timeout) {

		return createAuthenticatingRequestFactory(restBaseUrl, username, password, timeout);
	}

	public static ClientRequestFactory createAuthenticatingRequestFactory(String urlString, String username, String password, int timeout) {
		
		try {
			URL realUrl = new URL(urlString);
			BasicHttpContext localContext = new BasicHttpContext();
			HttpClient preemptiveAuthClient = createPreemptiveAuthHttpClient(username, password, timeout, localContext);
			ClientExecutor clientExecutor = new ApacheHttpClient4Executor(preemptiveAuthClient, localContext);
			return new ClientRequestFactory(clientExecutor, realUrl.toURI());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("url argument (" + urlString + ") was not a proper url: " + e.getMessage());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("url argument (" + urlString + ") was not a proper url: " + e.getMessage());
		}

	}

	private static HttpClient createPreemptiveAuthHttpClient(String username, String password, int timeout, BasicHttpContext localContext) {
		
		BasicHttpParams params = new BasicHttpParams();
		int timeoutMilliSeconds = timeout * 1000;
		HttpConnectionParams.setConnectionTimeout(params, timeoutMilliSeconds);
		HttpConnectionParams.setSoTimeout(params, timeoutMilliSeconds);
		DefaultHttpClient client = new DefaultHttpClient(params);
		return null;
	}

}
