package org.jboss.teiid.sizing.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestMD5Encrypt {
	
	static final String PING = "nscvosanefinvfiwejojfoweijwoe3woo:";
	
	@Test
	public void testMD5Encrypt() {
		MD5Encrypt md5 = new MD5Encrypt(PING);
		assertEquals(md5.generatePassCode(), md5.generatePassCode());
	}

}
