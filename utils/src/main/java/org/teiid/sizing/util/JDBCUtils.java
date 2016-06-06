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
package org.teiid.sizing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtils {
	
	public static Connection getConnection(String driver, String url, String user, String pass) throws Exception {
		Class.forName(driver);
		return DriverManager.getConnection(url, user, pass); 
	}

	public static void close(Connection conn) throws SQLException {
	    close(null, null, conn);
	}
	
	public static void close(Statement stmt) throws SQLException {
	    close(null, stmt, null);
	}
	
	public static void close(ResultSet rs) throws SQLException {
	    close(rs, null, null);
    }
	
	public static void close(Statement stmt, Connection conn) throws SQLException {
	    close(null, stmt, conn);
    }
	
	public static void close(ResultSet rs, Statement stmt) throws SQLException {
	    close(rs, stmt, null);
	}
	
	public static void close(ResultSet rs, Statement stmt, Connection conn) throws SQLException {
	  
	    if (null != rs) {
            rs.close();
            rs = null;
        }
        
        if(null != stmt) {
            stmt.close();
            stmt = null;
        }
        
        if(null != conn) {
            conn.close();
            conn = null;
        }
	}
	
	public static void execute(Connection connection, String sql, boolean closeConn) throws Exception {
	    
	    System.out.println("SQL: " + sql); //$NON-NLS-1$ 
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.createStatement();
            boolean hasResults = stmt.execute(sql);
            if (hasResults) {
                rs = stmt.getResultSet();
                ResultSetMetaData metadata = rs.getMetaData();
                int columns = metadata.getColumnCount();
                for (int row = 1; rs.next(); row++) {
                    System.out.print(row + ": ");
                    for (int i = 0; i < columns; i++) {
                        if (i > 0) {
                            System.out.print(", ");
                        }
                        System.out.print(rs.getObject(i+1));
                    }
                    System.out.println();
                }
            }
        } finally {
            close(rs, stmt);
            if(closeConn)
                close(connection);
        }    
        System.out.println();
    }
	
	public static long executeGetCount(Connection conn, String sql) throws Exception {
	    
	    Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                return rs.getLong(1);
            } else {
                return 0;
            }
        } finally {
            close(rs, stmt);
        }
	}

	public static void executeQuery(Connection conn, String sql) throws SQLException {
		
		System.out.println("Query SQL: " + sql); //$NON-NLS-1$ 
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData metadata = rs.getMetaData();
            int columns = metadata.getColumnCount();
            for (int row = 1; rs.next(); row++) {
                System.out.print(row + ": ");
                for (int i = 0; i < columns; i++) {
                    if (i > 0) {
                        System.out.print(",");
                    }
                    System.out.print(rs.getString(i+1));
                }
                System.out.println();
            }
		} finally {
			close(rs, stmt);
		}
		
		System.out.println();
		
	}

	public static boolean executeUpdate(Connection conn, String sql) throws SQLException {
			
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} finally {
			close(stmt);
		}
		return true ;
	}
	
	public static Entity executeQueryCount(Connection conn, String sql) throws SQLException {
                
        Entity entity = new Entity();
        entity.setSql(sql);
        
        long start = System.currentTimeMillis();
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            entity.setQueryTime(System.currentTimeMillis() - start);
            int columns = rs.getMetaData().getColumnCount();
            while(rs.next()) {
                for (int i = 0 ; i < columns ; ++i) {
                    rs.getObject(i+1);
                }
            }
        } finally {
            JDBCUtils.close(rs, stmt);
        }
        
        entity.setDeserializeTime(System.currentTimeMillis() - start - entity.getQueryTime());
        
        return entity;
    }
	
	public static class Entity {
	    
	    private String sql;
	    
	    private long queryTime;
	    
	    private long deserializeTime;

	    public String getSql() {
	        return sql;
	    }

	    public void setSql(String sql) {
	        this.sql = sql;
	    }

	    public long getQueryTime() {
	        return queryTime;
	    }

	    public void setQueryTime(long queryTime) {
	        this.queryTime = queryTime;
	    }

	    public long getDeserializeTime() {
	        return deserializeTime;
	    }

	    public void setDeserializeTime(long deserializeTime) {
	        this.deserializeTime = deserializeTime;
	    }

	    @Override
	    public String toString() {
	        return "PerfEntity [sql=" + sql + ", queryTime=" + queryTime + ", deserializeTime=" + deserializeTime + "]";
	    }

	}
}
