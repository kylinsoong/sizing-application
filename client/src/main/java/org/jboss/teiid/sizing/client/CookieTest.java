package org.jboss.teiid.sizing.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

public class CookieTest {
	
	static String URL = "https://idp.dev2.redhat.com/idp/authUser";
	
	static final String USER = "rhn-support-jshi";
	static final String PASS = "redhat";

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException {
		
		HttpClient client = RestClientHelper.createHttpClient(new HttpHost("idp.dev2.redhat.com", 443, "https"), "rhn-support-jshi", "redhat");
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", USER));
        nvps.add(new BasicNameValuePair("j_password", PASS));
		
		HttpPost httPost = new HttpPost(URL);
		httPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		
		HttpResponse response = client.execute(httPost);
		
		System.out.println(response.getStatusLine());
	}

}
