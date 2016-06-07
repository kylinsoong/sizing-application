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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import org.teiid.logging.LogManager;

public class Deserialization {
    
    private final long size;
    private final int count;
    
    static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    static String url = "jdbc:derby:./target/derbyDB;create=true";
    static String username = "user";
    static String password = "user";

    public Deserialization(long size, int count) {
        super();
        this.size = size;
        this.count = count;
    }
    
    public void start() throws Exception {
        
        preparation();
        
        countDeserializingTime();
        
        countRegressionRate();
    }
    
    //TODO -- Weka algorithm 
    public void countRegressionRate() throws Exception {
        
        Connection conn = getConnection(driver, url, username, password);
        execute(conn, "SELECT COUNT(*) FROM DESERIALIZERESULT", false);
        execute(conn, "SELECT * FROM DESERIALIZERESULT", true);
    }

    public void countDeserializingTime() throws Exception {
        
        System.out.println("Collect deserialization (size, time) tuples");
        long oristart = System.currentTimeMillis();
        long origcStart = collectionTimes();
        long origcCount = collectionCount();
        
        long expectRow = MB * size / ROW_SIZE;
        int gap = (int) (expectRow / count);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int promptlength = 0;
        
        try {
            conn = getConnection(driver, url, username, password);
            for(int i = 1 ; i <= count ; i ++){
                String sql = "SELECT COL_A, COL_B, COL_C, COL_D, COL_E, COL_F, COL_G, COL_H FROM DESERIALIZETEST FETCH FIRST " + gap * i + " ROWS ONLY";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                int columns = rs.getMetaData().getColumnCount();
                System.gc();// force a Full Garbage Collection
                long start = System.currentTimeMillis();
                long gcStart = collectionTimes();
                long gcCount = collectionCount();               
                while(rs.next()){
                    for (int j = 0 ; j < columns ; ++j) {
                        rs.getString(j+1);
                    }
                }
                
                long totalbytes = gap * i * ROW_SIZE;
                long totaldeserializingtime = System.currentTimeMillis() - start;
                long totalgctime = collectionTimes() - gcStart;
                long totalgcCount = collectionCount() - gcCount;
                String insertSQL = "INSERT INTO DESERIALIZERESULT (D_SIZE, D_TIME, GC_TIME, GC_COUNT) VALUES (" + totalbytes + ", " + totaldeserializingtime + ", " + totalgctime +", " + totalgcCount + ")";
                execute(conn, insertSQL, false);
                
                for(; promptlength > 0 ; promptlength--) {
                    System.out.print('\b');
                }
                String prompt = countPercentage(i, count);
                promptlength = prompt.length();
                System.out.print(prompt);             
            }
        } finally {
            close(rs, stmt, conn);
        }
        
        System.out.println("\nCollect " + count + " tuples, spend " + (System.currentTimeMillis() - oristart) + " milliseconds, garbage collection spend " + (collectionTimes() - origcStart) + ", garbage collection count " + (collectionCount() - origcCount));
             
    }

    /**
     * Check the db preparation data by parameter <size>
     *
     */
    public void preparation() throws Exception {
        
        Connection conn = null;
        
        try {
            conn = getConnection(driver, url, username, password);
            try {
                execute(conn, TABEL_CREATE, false);
            } catch (SQLException e) {
                LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
            }
            
            try {
                execute(conn, TABEL_DESERIALIZERESULT_CREATE, false);
            } catch (SQLException e) {
                LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
            }
            
            // each time truncate table
            execute(conn, TABEL_DESERIALIZERESULT_TRUNCATE, false);
            
            long rowCount = executeGetCount(conn, "SELECT COUNT(COL_A) FROM DESERIALIZETEST");
            long expectRow = MB * size / ROW_SIZE;
            if(rowCount < expectRow){
                initTestData(rowCount, expectRow, conn);
            }
        } finally {
            close(conn);
        }    
    }

    private void initTestData(long rowCount, long expectRow, Connection conn) throws SQLException {

        long count = expectRow - rowCount;
        long index = 0;
        int insertSize = 0;
        int promptlength = 0;
        
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
                    for(; promptlength > 0 ; promptlength--) {
                        System.out.print('\b');
                    }
                    String prompt = countPercentage(insertSize, count * ROW_SIZE / MB);
                    promptlength = prompt.length();
                    System.out.print(prompt);
                }
            }
        } finally {
            close(pstmt);
            conn.setAutoCommit(autoCommit);
            System.out.println("\nInsert " + count + " rows, size " + insertSize + "MB, spend " + (System.currentTimeMillis() - start) + " milliseconds");
        }
    }
    
    private DecimalFormat df = new DecimalFormat("#.00");

    private String countPercentage(int insertSize, long count) {
        double p = (double)insertSize / (double)count;
        String percentage = df.format(p);
        if(percentage.startsWith(".0")){
            percentage = percentage.substring(2);
        } else if(percentage.startsWith("1.")){
            percentage = "100";
        } else {
            percentage = percentage.substring(1);
        }
        return percentage +"% finished";
    }

    public static void main(String[] args) throws Exception {
        
        int size =100, count = 512;

        if(args.length <= 0) {
            usage();
        } else if(args.length == 1){
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else if(args.length == 2){
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else {
            usage();
        }
        
        if(count % 2 != 0) {
            usage();
        }
        
        new Deserialization(size, count).start();
          
    }

    

    private static void usage() {
        System.out.println("Usage: deserialization <size>");
        System.out.println("       deserialization <size> <counts>");
        System.out.println("    Parameters:");
        System.out.println("       <size> - total size in MB to be deserialized, a integer, eg, 100, 200, etc");
        System.out.println("       <counts> - total number of tuples(size, time) to be collected, a integer which should be equals 1 << X, eg, 256, 512, 1024, 2048, etc");
        Runtime.getRuntime().exit(1);
    }

}
