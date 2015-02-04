/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.jboss.teiid.sizing.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.teiid.sizing.util.model.Prompt;
import org.jboss.teiid.sizing.util.model.Question;

public class TeiidUtilConsole {

	private TeiidUtilProperties props;

	public TeiidUtilConsole(TeiidUtilProperties props) {
		this.props = props;
	}

	private static final String TAB = "    ";

	private static final long DEFAULT_SLEEP_TIME = 1000;

	public void print(Object obj) {
		System.out.print(obj);
	}

	public void println(Object obj) {
		System.out.println(obj);
	}
	
	public void printlnPre(Object obj) {
		System.out.println();
		System.out.print(obj);
	}

	public void prompt(Object obj) {
		println("\n" + TAB + obj + "\n");
	}

	public void pause(Object obj) {
		print(obj);
		sleep(DEFAULT_SLEEP_TIME);
	}
	
	public void pause() {
		sleep(DEFAULT_SLEEP_TIME);
	}

	public void pauseln(Object obj) {
		println(obj);
		sleep(DEFAULT_SLEEP_TIME);
	}

	@SuppressWarnings("static-access")
	public void sleep(long time) {
		try {
			Thread.currentThread().sleep(time);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	static Set<String> expSet3 = new HashSet<String>();
	
	static {
		expSet3.add("1");
		expSet3.add("2");
		expSet3.add("3");
	}
	
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	public String modeClsSelection() {
		StringBuffer sb = new StringBuffer();
		sb.append("Select a tool to start\n");
		sb.append(TAB + "1. " + TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.sizeRecommendation") + "\n");
		sb.append(TAB + "2. " + TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.deserialization") + "\n");
		sb.append(TAB + "3. " + TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.serialization") + "\n");
		
		int value = readInteger(sb.toString(), 1, expSet3);
		if(value == 1){
			props.setProperty("mode", "sizeRecommendation");
		} else if(value == 2) {
			props.setProperty("mode", "deserialization");
		} else if(value == 3) {
			props.setProperty("mode", "serialization");
		}
		
		return props.getToolClass();
	}
	
	public String readString(String prompt, String value) {
		String result = "";
		println(prompt + " Default [" + value + "]");
		String input = null;
		try {
			input = br.readLine();
		} catch (IOException e) {
			throw new IllegalArgumentException("readString Error", e);
		}
		if (input.equals("") || input.trim().equals("")) {
			result = value;
		} else {
			result = input;
		}
		return result;
	}
	
	public String readString(String prompt) {
		String result = "";
		print(prompt);
		try {
			result = br.readLine();
		} catch (IOException e) {
			throw new IllegalArgumentException("readString Error", e);
		}
		return result;
	}

	
	public int readInteger(String prompt, int value, Set<String> expSet) {
		int result = -1;
		while (true) {
			println(prompt + "Default [" + value + "]");
			String input = null;
			try {
				input = br.readLine();
			} catch (IOException e) {
				throw new IllegalArgumentException("readInteger Error", e);
			}
			
			if (input.equals("") || input.trim().equals("")) {
				result = value;
				break;
			}
			
			if(!expSet.contains(input)){
				continue;
			}
			
			try {
				result = Integer.parseInt(input);
				break;
			} catch (NumberFormatException e) {
				prompt("You should input a int type value");
			}
		}
		return result;
	}
	
	public boolean isQuit() {
		String msg = "Are you sure to quit ?\n" + " [1]. Yes\n" + " [2]. No\n"
				+ "Default is [1]";
		int a = '1';
		int b = '2';
		int res = readWithDef(msg, a, a, b);
		if (res == a) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isExit() {
		String msg = "Are you sure to exit ?\n" + " [1]. Yes\n" + " [2]. No\n"
				+ "Default is [1]";
		int a = '1';
		int b = '2';
		int res = readWithDef(msg, a, a, b);
		if (res == a) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean readFromCli(String prompt) {
		String msg =  prompt + "\n" + " [1]. Yes\n"
				+ " [2]. No\n" + "Default is [1]";
		int a = '1';
		int b = '2';
		int res = readWithDef(msg, a, a, b);
		if (res == a) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int readWithDef(String prompt, int def, int... params) {
		Set set = new HashSet();
		for (int i : params) {
			set.add(i);
		}
		int result = 0;
		while (true) {
			result = keyPress(prompt);
			if (set.contains(result)) {
				break;
			} else if (result == 10) {
				result = def;
				break;
			}
		}
		return result;
	}
	
	public int keyPress(String msg) {
		println(msg);
		try {
			int ret = System.in.read();
			System.in.skip(System.in.available());
			return ret;
		} catch (IOException e) {
			return 0;
		}
	}

	public void sizeRecommendationIntro() {
		Prompt prompt = new Prompt();
		prompt.setTitle(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.sizeRecommendation.title"));
		prompt.addItem(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.sizeRecommendation.item1"));
		prompt.addItem(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.sizeRecommendation.item2"));
		prompt.addItem(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.sizeRecommendation.item3"));
		
		print(prompt);
	}
	
	public void deserializationIntro() {
		println(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.deserialization.desc"));
		println("");
	}

	public void serializationIntro() {
		println(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.serialization.desc"));
		println("");
	}
	
	public void getQuestionAnswer(Question question) {
		while(true) {
			String answer = readString(question.getQuestion(), question.getDefAnswer());
			if(question.isValidAnswer(answer)){
				question.setAnswer(answer);
				break;
			}
		}
	}

}
