package org.jboss.teiid.sizing.client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

public class MD5Encrypt {
	
	private String ping;
	
	private final MessageDigest md5;
	
	public MD5Encrypt(String ping) {
		this.ping = ping;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No MD5 Algorithm Exist", e);
		}
	} 
	
	public String generatePassCode() {
		String token = getToken();
		String encrypt = ping + token;
		md5.reset();
		md5.update(encrypt.getBytes(), 0, encrypt.length());
		return new BigInteger(1,md5.digest()).toString(16);
	}
	
	public static String generatePassCode(String ping) throws NoSuchAlgorithmException {
		String token = getToken();
		String encrypt = ping + token;
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(encrypt.getBytes(), 0, encrypt.length());
		return new BigInteger(1,md5.digest()).toString(16);
	}

	public static String getToken() {
		String base = new Date().getTime() + "";
		Long token = new Long(base.substring(0, base.length() - 3));
		token = token / (60*60*24*3);
		return token.toString();
	}

	public static void main(String[] args) {
		
		long ping = 1422947470 /(60*60*24*3);
		System.out.println(ping);
		
		System.out.println(new java.util.Date().getTime());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2015, 2, 4);
		
		System.out.println(calendar.getTime().getTime());
		

	}

}
