/*
* JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
*/
package org.teiid.sizing;

import static org.teiid.sizing.util.JDBCUtils.*;
import static org.teiid.sizing.TeiidUtils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.teiid.logging.LogManager;

public class Deserialization {
    
    private final long size;
    private final int count;

    public Deserialization(long size, int count) {
        super();
        this.size = size;
        this.count = count;
    }
    
    public void start() throws Exception {
        
        preparation();
    }
    
    /**
     * Check the db preparation data by parameter <size>
     *
     */
    private void preparation() throws Exception {
        
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String url = "jdbc:derby:./target/derbyDB;create=true";
        
        Connection conn = getConnection(driver, url, "user", "user");
        
        try {
            execute(conn, TABEL_CREATE, false);
        } catch (SQLException e) {
            LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
        }
        
        long rowCount = executeGetCount(conn, "SELECT COUNT(col_a) FROM DESERIALIZETEST");
        long expectRow = MB * size / ROW_SIZE;
        if(rowCount < expectRow){
            initTestData(rowCount, expectRow, conn);
        }
        
        System.out.println(rowCount);
        
        close(conn);
    }

    private void initTestData(long rowCount, long expectRow, Connection conn) throws SQLException {

        long count = expectRow - rowCount;
        long index = 0;
        int insertSize = 0;
        
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = null;
        long start = System.currentTimeMillis();
        System.out.println("Insert Data to DESERIALIZETEST");
        
        try {
            pstmt = conn.prepareStatement(TABEL_INSERT);
            
            for(long i = 0 ; i < count ; i ++) {
                
                pstmt.setString(1, char32string());
                pstmt.setString(2, char32string());
                pstmt.setString(3, char32string());
                pstmt.setString(4, char32string());
                pstmt.setString(5, char32string());
                pstmt.setString(6, char32string());
                pstmt.setString(7, char32string());
                pstmt.setString(8, char32string());
                pstmt.addBatch();
                
                index ++;
                if(index == MB / ROW_SIZE){
                    pstmt.executeBatch();
                    conn.commit();
                    index = 0;
                    insertSize ++;
                    System.out.print('.');
                }
            }
        } finally {
            close(pstmt);
            conn.setAutoCommit(autoCommit);
            System.out.println("\nInsert " + count + " rows, szie " + insertSize + "MB, spend " + (System.currentTimeMillis() - start) + " milliseconds");
        }
    }

    public static void main(String[] args) throws Exception {
        
        int size =100, count =1000;

        if(args.length <= 0) {
            usage();
        } else {
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                usage();
            }
            
            new Deserialization(size, count).start();
        }
          
    }

    

    private static void usage() {
        System.out.println("Usage: deserialization <size(MB)> <counts>");
    }

}
