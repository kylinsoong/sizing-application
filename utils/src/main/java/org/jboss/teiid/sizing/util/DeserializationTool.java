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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jboss.teiid.sizing.util.validation.SerializationValidation;

public class DeserializationTool extends AbstractTeiidUtil {

	public DeserializationTool(TeiidUtilProperties props, TeiidUtilConsole console) {
		super(props, console, "Deserialization Tool");
	}

	@Override
	public void execute() throws Exception {
		
		start();
		
		console.deserializationIntro();
		
		SerializationValidation validation = new SerializationValidation("exit", "-q");
		while(true) {			
			String input = console.readString(TeiidUtilPlugin.Util.getString("TeiidUtil.Mode.deserialization.input"));
			if(validation.isLong(input)){
				if(input.equals("exit") || input.equals("-q")){
					break;
				} else {
					long size = Long.parseLong(input);
					caculation(size);
				}
			}
		}
		
		stop();
	}

	private void caculation(long size) {
		
	}
	
	protected Connection createDatabaseConnection() throws ClassNotFoundException, SQLException {
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver);
		String url = "jdbc:derby:sampleDB";
		Connection c = DriverManager.getConnection(url);
		return c;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		DeserializationTool tool = new DeserializationTool(null,  null);

		System.out.println(tool.createDatabaseConnection());
	}
	

}
