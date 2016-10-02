package org.teiid.sizing;

import static org.junit.Assert.*;
import static org.teiid.sizing.Main.*;
import static org.teiid.sizing.utils.JDBCUtils.*;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class TestMapPerfBenchmark {

    @Test
    public void testMapPerfTable() throws Exception {
        
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String url = "jdbc:derby:./target/mapPerfDB;create=true";
        String username = "user";
        String password = "user";
        
        Connection conn = getConnection(driver, url, username, password);
        
        try {
            execute(conn, TABLE_MAPPERFBENCHMARK_CREATE, false);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        execute(conn, "INSERT INTO MAPPERFBENCHMARK (CLASS_NAME, THREADS, T_TIME, READ_COUNT, WRITE_COUNT, GETSIZE_COUNT) VALUES ('ConcurrentHashMap', 64, 4000, 500000, 500000, 500000)", false);
        execute(conn, "INSERT INTO MAPPERFBENCHMARK (CLASS_NAME, THREADS, T_TIME, READ_COUNT, WRITE_COUNT, GETSIZE_COUNT) VALUES ('ConcurrentHashMap', 64, 4000, 500000, 500000, 500000)", false);
        execute(conn, "INSERT INTO MAPPERFBENCHMARK (CLASS_NAME, THREADS, T_TIME, READ_COUNT, WRITE_COUNT, GETSIZE_COUNT) VALUES ('ConcurrentHashMap', 64, 4000, 500000, 500000, 500000)", false);
        
        execute(conn, SQL_MAPPERFBENCHMARK_SELECT, false);
        
        try {
            execute(conn, TABLE_MAPPERFBENCHMARK_TRUNCATE, false);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        close(conn);
    }
    
    @Test
    public void testMapPerfTable_1() throws Exception {
        MapPerfBenchmark benchmark = new MapPerfBenchmark(1);
        benchmark.initialization();
        benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 1);
        benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 2);
        benchmark.benchmark(new ConcurrentHashMap<String, Integer>(), 4);
        benchmark.dumpResult();
    }
    
    @Test
    public void testMapPerfTable_2() {
        assertEquals(1<<0, 1);
        assertEquals(1<<1, 2);
        assertEquals(1<<2, 4);
        assertEquals(1<<3, 8);
        assertEquals(1<<4, 16);
        assertEquals(1<<5, 32);
        assertEquals(1<<6, 64);
    }
}
