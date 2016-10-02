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

import static org.teiid.sizing.Main.*;
import static org.teiid.sizing.utils.CSVUtils.*;
import static org.teiid.sizing.utils.JDBCUtils.*;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.teiid.logging.LogManager;

/**
 * In java deserialization is pretty expensive, we can see it’s 100% processor occupation, the size of content to deserialize is critical for performance, 
 * the bigger of size, the larger of time.
 * 
 * Each time have a reference size, (size 1, time 1),(size 2, time 2), …​, (size n, time n). In statistics, time and size are in a linear regression approach:
 * <pre>
 * time = weight * size
 * </pre>
 * time - a integer variable, represent how many milliseconds took in deserializing a specific size content.
 * size - a integer variable, represent how many bytes need to be deserialized.
 * weight - a integer constant, represent the weight of linear regression.
 * @author kylin
 *
 */
public class Deserialization implements Tools {
    
    private final long size;
    private final int count;
    private final boolean dumptuples;
    private final double ratio;
    private final boolean refresh;
    private final boolean exportCSV;
    
    static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    static String url = "jdbc:derby:./target/derbyDB;create=true";
    static String username = "user";
    static String password = "user";

    public Deserialization(long size, int count, double ratio, boolean refresh, boolean dumptuples, boolean exportCSV) {
        super();
        this.size = size;
        this.count = count;
        this.ratio = ratio;
        this.refresh = refresh;
        this.dumptuples = dumptuples;
        this.exportCSV = exportCSV;
    }
    
    @Override
    public void start() throws Exception {
        
        preparation();
        
        countDeserializingTime();
        
        countRegressionWeight();
        
        exportResultToCsv();
    }

    public void countRegressionWeight() throws Exception {
        
        System.out.println("Calculating linear regression weight");
        
        Connection conn = getConnection(driver, url, username, password);
        
        if(dumptuples) {
            execute(conn, SQL_DUMP_TUPLES, false);
        }
        
        List<Double> roughweights = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        NumberFormat formatter = new DecimalFormat("#0.0000000000");
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL_WEIGHT_TUPLES);
            while(rs.next()){
                roughweights.add(rs.getDouble(1));            
            }
        } finally {
            close(rs, stmt);
        } 
        
        double roughweight = average(roughweights);
        if(dumptuples){
            System.out.println("Rough Weight: " + formatter.format(roughweight));
        }
        
        Collections.sort(roughweights);
        int trim = (int) ((1 - ratio) * roughweights.size()) / 2 ;
        int start = trim > 0 ? trim -1 : 0 ;
        for(int i = start ; i < roughweights.size() - trim ; i ++) {
            weights.add(roughweights.get(i));
        }
        
        double weight = average(weights);
        System.out.println("      Weight: " + formatter.format(Double.parseDouble(formatter.format(weight))));
        
        close(conn);
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
        
        long rowCount = 0;
        
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
            
            // if true truncate tuples table
            if(refresh) {
                execute(conn, TABEL_DESERIALIZERESULT_TRUNCATE, false);
            }
            
            rowCount = executeGetCount(conn, "SELECT COUNT(COL_A) FROM DESERIALIZETEST");
        } finally {
            close(conn);
        }
        
        long expectRow = MB * size / ROW_SIZE;
        if(rowCount < expectRow){
            long sizebytes = (expectRow - rowCount) * ROW_SIZE ;
            int sizemb = (int) (sizebytes / MB);
            initTestData(sizemb);
        }
    }
    
    public void exportResultToCsv() throws Exception {

        if(!exportCSV){
            return;
        }
        
        Path result = Files.createFile(Paths.get(".", "deserialization-weight-" +System.currentTimeMillis() + ".csv"));
        
        System.out.println("Export Result to " + result);
        
        Connection conn = getConnection(driver, url, username, password);
        
        List<Long> sizes = new ArrayList<>();
        List<Integer> times = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        NumberFormat formatter = new DecimalFormat("#0.0000000000");
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL_DUMP_TUPLES);
            while(rs.next()){
                sizes.add(rs.getLong(1));
                times.add(rs.getInt(2));
                weights.add(rs.getDouble(3));            
            }
        } finally {
            close(rs, stmt, conn);
        } 
        
        List<Double> weightsClone = new ArrayList<>(weights.size());
        for(Double d : weights){
            weightsClone.add(new Double(d));
        }
        Collections.sort(weightsClone);
        int trim = (int) ((1 - ratio) * weights.size()) / 2 ;
        int start = trim > 0 ? trim -1 : 0 ;
        int end = weights.size() - trim;
        Double largerThan = weightsClone.get(start);
        Double lessThan = weightsClone.get(end);
        
        FileWriter writer = new FileWriter(result.toFile());
        writeLine(writer, Arrays.asList("SIZE", "TIME", "WEIGHT"));
        
        for(int i = 0 ; i < weights.size() ; i ++) {
           if(weights.get(i) >= largerThan && weights.get(i) <= lessThan){
               writeLine(writer, Arrays.asList(String.valueOf(sizes.get(i)), String.valueOf(times.get(i)), formatter.format(weights.get(i))));
           }
        }
        
        writer.flush();
        writer.close();
    }
    
    ThreadPoolExecutor executor;

    /**
     * @param size - size(MB) to insert
     * @throws Exception
     */
    private void initTestData(int size) throws Exception {

        AtomicInteger totalSize = new AtomicInteger(size);
        AtomicInteger insertSize = new AtomicInteger(0);
        
        int promptlength = 0;
                
        long start = System.currentTimeMillis();
        System.out.println("Prepare deserialization data");
                
        int threadscount = countThreadnumber(totalSize.get());
        executor = new ThreadPoolExecutor(threadscount, threadscount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        
        for(int i = 0 ; i < threadscount ; i ++) {
            executor.execute(new InsertTask(totalSize,  insertSize));
        }
        
        while(totalSize.get() >= 0){
            Thread.sleep(500);
            for(; promptlength > 0 ; promptlength--) {
                System.out.print('\b');
            }
            String prompt = countPercentage(insertSize.get(), size);
            promptlength = prompt.length();
            System.out.print(prompt);
            if(totalSize.get() == 0) {
                break;
            }
        }
        
        executor.shutdown();
              
        System.out.println("\nInsert " + (size * MB / ROW_SIZE) + " rows, size " + insertSize.get() + "MB, spend " + (System.currentTimeMillis() - start) + " milliseconds");
    }
    
    private int countThreadnumber(int size) {
        
        int count = size / 100 + 1;
        int maxProcessors = Runtime.getRuntime().availableProcessors() * 2 ;
        if(count > maxProcessors) {
            count = maxProcessors;
        }

        return count;
    }
    
    private class InsertTask implements Runnable {
        
        private AtomicInteger totalSize;
        private AtomicInteger insertSize;
                
        public InsertTask(AtomicInteger totalSize, AtomicInteger insertSize){
            this.totalSize = totalSize;
            this.insertSize = insertSize;
        }

        @Override
        public void run() {
            
//            System.out.println("InsertTask " + Thread.currentThread().getName() + " Start");
            
            Connection conn = null;      
            
              
            try {
                conn = getConnection(driver, url, username, password);
                conn.setAutoCommit(false);
                
                
                while(totalSize.get() > 0) {
                    // chunk is 1MB
                    insertchunk(conn);                    
                    insertSize.getAndIncrement();
                    int leftsize = totalSize.getAndDecrement();
                    if(executor.getActiveCount() > 1 && leftsize < 50) {
                        break;
                    }
                }
            } catch(Exception e) {
                LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
            }finally {
                try {
                    close(conn);
                } catch (SQLException e) {
                    LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
                }
//                System.out.println("InsertTask " + Thread.currentThread().getName() + " Stop");
            }
        }

        private void insertchunk(Connection conn) {

            PreparedStatement pstmt = null;
            
            try {
                pstmt = conn.prepareStatement(TABEL_INSERT);
                for(int i = 0 ; i < MB_ROW ; i ++){
                    pstmt.setString(1, char32string());
                    pstmt.setString(2, char32string());
                    pstmt.setString(3, char32string());
                    pstmt.setString(4, char32string());
                    pstmt.setString(5, char32string());
                    pstmt.setString(6, char32string());
                    pstmt.setString(7, char32string());
                    pstmt.setString(8, char32string());
                    pstmt.addBatch();
                }
                
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
            } finally {
                try {
                    close(pstmt);
                } catch (SQLException e) {
                    LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
                }
            }
        }
        
    }

    private DecimalFormat df = new DecimalFormat("#.00");

    private String countPercentage(int insertSize, int count) {
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
        boolean dumptuples = false;
        double ratio = 0.75;
        boolean refresh = false;
        boolean exportCSV = false;

        if(args.length < 1) {
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
        } else if(args.length == 3){
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
                ratio = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else if(args.length == 4){
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
                ratio = Double.parseDouble(args[2]);
                refresh = Boolean.parseBoolean(args[3]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else if(args.length == 5){
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
                ratio = Double.parseDouble(args[2]);
                refresh = Boolean.parseBoolean(args[3]);
                dumptuples = Boolean.parseBoolean(args[4]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else if(args.length == 6){
            try {
                size = Integer.parseInt(args[0]);
                count = Integer.parseInt(args[1]);
                ratio = Double.parseDouble(args[2]);
                refresh = Boolean.parseBoolean(args[3]);
                dumptuples = Boolean.parseBoolean(args[4]);
                exportCSV = Boolean.parseBoolean(args[5]);
            } catch (NumberFormatException e) {
                usage();
            }         
        } else {
            usage();
        }
        
        if(count % 2 != 0 && count >= 1) {
            usage();
        }
        
        new Deserialization(size, count, ratio, refresh, dumptuples, exportCSV).start();
          
    }

    

    private static void usage() {
        System.out.println("Usage: deserialization <size>");
        System.out.println("       deserialization <size> <counts>");
        System.out.println("       deserialization <size> <counts> <ratio>");
        System.out.println("       deserialization <size> <counts> <ratio> <refresh>");
        System.out.println("       deserialization <size> <counts> <ratio> <refresh> <dumptuples>");
        System.out.println("       deserialization <size> <counts> <ratio> <refresh> <dumptuples> <exportCSV>");
        System.out.println("Options:");
        System.out.println("       <size> - total size in MB to be deserialized, a integer, eg, 100, 200, etc");
        System.out.println("       <counts> - total number of tuples(size, time) to be collected, a integer which should be equals 1 << X, eg, 256, 512, 1024, 2048, etc");
        System.out.println("       <ratio> - a float, less than 1 and larger than 0, the ratio for regression weight");
        System.out.println("       <refresh> - whether refresh tuples, default is false");
        System.out.println("       <dumptuples> - whether dump tuples, default is false");
        System.out.println("       <exportCSV> - whether export result to a csv file, default is false");
        Runtime.getRuntime().exit(1);
    }

}
