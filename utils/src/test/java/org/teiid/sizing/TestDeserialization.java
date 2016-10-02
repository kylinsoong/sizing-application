package org.teiid.sizing;

import static org.junit.Assert.*;
import static org.teiid.sizing.utils.JDBCUtils.*;
import static org.teiid.sizing.Deserialization.*;
import static org.teiid.sizing.Main.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class TestDeserialization {
    
    @Test
    public void testDeserializationGCTime() throws Exception {
        
        Connection conn = getConnection(driver, url, username, password);
        
        try {
            execute(conn, TABEL_TRUNCATE, false);
        } catch (Exception e) {
        }
        
        Deserialization deserialization = new Deserialization(5, 32, 0.75, false, false, false);
        deserialization.preparation();
        
        String sql = "SELECT COL_A, COL_B, COL_C, COL_D, COL_E, COL_F, COL_G, COL_H FROM DESERIALIZETEST";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        long gcCountStart = collectionCount();
        long gcTimesStart = collectionTimes();
        System.gc();
        while(rs.next()){
            rs.getString(1);
            rs.getString(2);
            rs.getString(3);
            rs.getString(4);
            rs.getString(5);
            rs.getString(6);
            rs.getString(7);
            rs.getString(8);
        }
        
        assertTrue((collectionCount() - gcCountStart) > 0);
        assertTrue((collectionTimes() - gcTimesStart) > 0);
        
        close(rs, stmt, conn);
    }
    
    @Test
    public void testGCTime() {
        System.gc();
        long count = collectionCount();
        for(int i = 0 ; i < 3 ; i ++) {
            System.gc();
        }
        assertEquals(6, (collectionCount() - count));
    }

}
