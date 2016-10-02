package org.teiid.sizing;

import static org.teiid.sizing.Main.*;
import static org.teiid.sizing.utils.JDBCUtils.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.teiid.logging.LogManager;

public class MapPerfBenchmark implements Tools {
    
    private static final int SIZE = 500000;
    
    public static class Timer {
        long startTime = 0, endTime = 0, total = 0;
        
        public Timer(long startTime){
            this.startTime = startTime;
        }
        
        public void setEnd(long endTime) {
            if(this.endTime > 0) {
                return;
            }
            this.endTime = endTime;
        }
        
        public long getTotal() {
            return (endTime - startTime) / 1000000L;
        }
    }
    
    static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    static String url = "jdbc:derby:./target/mapPerfDB;create=true";
    static String username = "user";
    static String password = "user";
    
    private final int count;
    
    public MapPerfBenchmark(int count) {
        this.count = count;
    }
    
    @Override
    public void start() throws Exception {

        initialization();
        
        benchmark();
        
        dumpResult();
    }
    
    protected void benchmark() throws Exception {

        int cur = 0;
        while(true) {
            
            cur++ ;
            for(int i = 0 ; i < 7 ; i ++){
                int threads = 1 << i ;
                benchmark(new ConcurrentHashMap<String, Integer>(), threads);
                benchmark(new ConcurrentSkipListMap<String, Integer>(), threads);
                benchmark(Collections.synchronizedMap(new HashMap<String, Integer>()), threads);
                benchmark(Collections.synchronizedMap(new TreeMap<String, Integer>()), threads);
            }
            
            if(cur >= this.count){
                break;
            }
        }
    }

    protected void dumpResult() throws Exception {
        
        execute(getConnection(driver, url, username, password), SQL_MAPPERFBENCHMARK_SELECT, true);
    }

    protected void initialization() throws Exception {
        
        Connection conn = null;

        try {
            conn = getConnection(driver, url, username, password);
            try {
                execute(conn, TABLE_MAPPERFBENCHMARK_CREATE, false);
            } catch (SQLException e) {
                LogManager.logInfo(LOGGING_CONTEXT, e.getMessage());
            }
        } finally {
            close(conn);
        }
    }

    public Timer benchmark(final Map<String, Integer> map, int threads) throws Exception{
        
        String className = map.getClass().getSimpleName();
        System.out.println("Performance Benchmark for " + className + ", with " + threads + " threads");
        
        final Timer timer = new Timer(System.nanoTime());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for(int i = 0 ; i < threads ; i ++){
            executor.execute(new Runnable(){

                @Override
                public void run() {
                    while(true){
                        int size = map.size();
                        if(size >= SIZE){
                            timer.setEnd(System.nanoTime());
                            break;
                        }
                        String key = UUID.randomUUID().toString();
                        map.put(key, 2048);
                        map.get(key);
                    }             
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1000 * 10, TimeUnit.SECONDS);
        
        String sql = SQL_MAPPERFBENCHMARK_INSERT_PREFIX + "('" + className + "', " + threads + ", " + timer.getTotal() + ", " + map.size()  + ", " + map.size()  + ", " + map.size() + ")";
        execute(getConnection(driver, url, username, password), sql, true);
        map.clear();
        System.gc();
        Thread.sleep(200);
        return timer;
    }

    public static void main(String[] args) throws Exception {
        
        int count = 10 ;
        
        if(args.length == 1) {
            try {
                count = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                usage();
            }   
        } else if(args.length > 1){
            usage();
        }
        
        new MapPerfBenchmark(count).start();

    }
    
    private static void usage() {
        System.out.println("Usage: mapPerf <count>");
        System.out.println("Options:");
        System.out.println("       <count> - how many time add/retrieve the map, default is 10");
        Runtime.getRuntime().exit(1);
    }

}
