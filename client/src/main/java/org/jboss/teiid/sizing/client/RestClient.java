package org.jboss.teiid.sizing.client;


public class RestClient {
	
	static final String TMP_URL = "https://access.devgssci.devlab.phx1.redhat.com/labs/jbossdvsat/application/recommendations?start_at=1422201600&end_at=1522288000&pass_code=4546a0f918415962475a96aee2a3d5c0";
	
	static final String USER = "rhn-support-jshi";
	static final String PASS = "redhat";
	
	static final String HOSTNAME = "access.devgssci.devlab.phx1.redhat.com";
	static final String BASE_URL = "https://" + HOSTNAME + "/labs/jbossdvsat";
	
	static String pass_code;
	static String start;
	static String end;
	
	static final String GET_ALL = BASE_URL + "/application/recommendations?" + pass_code;
	static final String GET_SECTION = BASE_URL + "?start_at=" + start + "&end_at=" + end + "&pass_code=" + pass_code;

	public static void main(String[] args) {
//		Client client = ClientBuilder.newBuilder().build();
//		WebTarget target = client.target(TMP_URL);
//		Response response = target.request().get();
		
//		RestRequestHelper
	}

}
