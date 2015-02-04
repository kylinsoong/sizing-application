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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public abstract class TeiidUtil {
	
	private static File configuration = null;
	
	static{
		System.setProperty("jboss.dv.tool", "/home/kylin/project/teiid-utils");
		configuration = new File(System.getProperty("jboss.dv.tool") + File.separator + "tools.xml");
		if(!(configuration.exists())) {
			throw new RuntimeException(configuration + " doesn't exists");
		}
	}

	public static void main(String[] args) throws Exception {
		
		TeiidUtilProperties props = loadProperties(configuration);
		
		TeiidUtilConsole console = new TeiidUtilConsole(props);
		
		start(props, console);	
	}
		
	protected static void start(TeiidUtilProperties props, TeiidUtilConsole console) throws Exception {
		String cls = console.modeClsSelection();
		@SuppressWarnings("rawtypes")
		Class[] types = {TeiidUtilProperties.class, TeiidUtilConsole.class};
		Object[] objs = {props, console};
		TeiidUtil util = (TeiidUtil) Thread.currentThread().getContextClassLoader().loadClass(cls).getConstructor(types).newInstance(objs);
		util.execute();
	}

	private static TeiidUtilProperties loadProperties(File configuration) throws Exception {
		TeiidUtilProperties props = new TeiidUtilProperties();
		
		InputStream in = null;
		try {
			in = new FileInputStream(configuration);
			props.loadFromXML(in);
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		return props;
	}

	public abstract void execute() throws Exception;

}
