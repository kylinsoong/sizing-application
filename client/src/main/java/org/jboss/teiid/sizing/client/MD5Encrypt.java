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

	public String getToken() {
		long time = getTime(new Date());
		long token = time / (60 * 60 * 24 * 3);
		return String.valueOf(token);
	}
	
	public String getTimeStamp(Date date) {
		long stamp = getTime(date);
		return String.valueOf(stamp);
	}
	
	public String getTimeStamp(int year, int month, int date) {
		long stamp = getTime(year, month, date);
		return String.valueOf(stamp);
	}

	public long getTime(Date date) {
		return date.getTime() / 1000;
	}
	
	public long getTime(int year, int month, int date) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date);
		return getTime(calendar.getTime());
	}
}
