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

import static org.teiid.sizing.TeiidUtils.LOGGING_SQL_CONTEXT;

import java.io.PrintStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.teiid.logging.LogManager;

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
	
	public static void close(ResultSet rs, Statement stmt) throws SQLException {
		close(rs, stmt, null);
	}
	
	public static void close(Statement stmt, Connection conn) throws SQLException {
		close(null, stmt, conn);
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
	    
	    LogManager.logDetail(LOGGING_SQL_CONTEXT, sql);
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.createStatement();
            boolean hasResults = stmt.execute(sql);
            if (hasResults) {
                rs = stmt.getResultSet();
                new ResultSetRenderer(rs).renderer();
            }
        } finally {
            close(rs, stmt);
            if(closeConn)
                close(connection);
        }   
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
		
	    LogManager.logDetail(LOGGING_SQL_CONTEXT, sql);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			new ResultSetRenderer(rs).renderer();
		} finally {
			close(rs, stmt);
		}
		
		System.out.println();
		
	}
	
	public static Set<String> query(Connection conn, String sql) throws Exception {
	    
	    Set<String> list = new HashSet<String>();
	    
	    Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                list.add(rs.getString(1));
            }
        } finally {
            close(rs, stmt);
        }
	    
	    return list;
	    
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
	
	public static class TableRenderer{
	    
	    private static final int MAX_CACHE_ELEMENTS = 500;

	    private final List<Column[]> cacheRows;
	    private boolean alreadyFlushed;
	    private int writtenRows;
	    private int separatorWidth;

	    private boolean enableHeader;
	    private boolean enableFooter;

	    protected ColumnMetaData[] meta;
	    protected final OutputDevice out;
	    protected final String colSeparator;
	    protected final String colPreSeparator;
	    
	    public TableRenderer(ColumnMetaData[] meta,
	                         OutputDevice out,
	                         String separator, 
	                         boolean enableHeader,
	                         boolean enableFooter) {
	        this.meta = meta;
	        this.out = out;
	        this.enableHeader = enableHeader;
	        this.enableFooter = enableFooter;

	        /*
	         * we cache the rows in order to dynamically determine the
	         * output width of each column.
	         */
	        this.cacheRows = new ArrayList<Column[]>(MAX_CACHE_ELEMENTS);
	        this.alreadyFlushed = false;
	        this.writtenRows = 0;
	        this.colSeparator = " " + separator;
	        this.colPreSeparator = separator;
	        this.separatorWidth = separator.length();
	    }

	    public TableRenderer(ColumnMetaData[] meta, OutputDevice out) {
	        this(meta, out, "|", true, true);
	    }
	    
	    public TableRenderer(OutputDevice out) {
	        this(null, out);
	    }
	    
	    public TableRenderer(ColumnMetaData[] meta) {
	        this(meta, new PrintStreamOutputDevice(System.out));
	    }


	    public void addRow(Column[] row) {
	        updateColumnWidths(row);
	        addRowToCache(row);
	    }

	    protected void addRowToCache(Column[] row) {
	        cacheRows.add(row);
	        if (cacheRows.size() >= MAX_CACHE_ELEMENTS) {
	            flush();
	        }
	    }
	    
	    /**
	     * return the meta data that is used to display this table.
	     */
	    public ColumnMetaData[] getMetaData() {
	        return meta;
	    }

	    public void setMeta(ColumnMetaData[] meta) {
	        this.meta = meta;
	    }

	    /**
	     * Overwrite this method if you need to handle customized columns.
	     * @param row
	     */
	    protected void updateColumnWidths(Column[] row) {
	        for (int i = 0; i < meta.length; ++i) {
	            if(row[i] == null) {
	                continue;
	            }
	            row[i].setAutoWrap(meta[i].getAutoWrap());
	            meta[i].updateWidth(row[i].getWidth());
	        }
	    }

	    public void renderer() {
	        flush();
	        if (writtenRows > 0 && enableFooter) {
	            printHorizontalLine();
	        }
	    }

	    /**
	     * flush the cached rows.
	     */
	    public void flush() {
	        if (!alreadyFlushed) {
	            if (enableHeader) {
	                printTableHeader();
	            }
	            alreadyFlushed = true;
	        }
	        Iterator<Column[]> rowIterator = cacheRows.iterator();
	        while (rowIterator.hasNext()) {
	            Column[] currentRow = rowIterator.next();
	            boolean hasMoreLines;
	            do {
	                hasMoreLines = false;
	                hasMoreLines = printColumns(currentRow, hasMoreLines);
	                out.println();
	            }
	            while (hasMoreLines);
	            ++writtenRows;
	        }
	        cacheRows.clear();
	    }

	    protected boolean printColumns(Column[] currentRow, boolean hasMoreLines) {
	        boolean first = true;
	        for (int i = 0; i < meta.length; ++i) {
	            if (!meta[i].doDisplay())
	                continue;
	            if(first){
	                out.print(this.colPreSeparator);
	                first = false;
	            }
	            hasMoreLines = printColumn(currentRow[i], hasMoreLines, i);
	        }
	        return hasMoreLines;
	    }

	    protected boolean printColumn(Column col,
	                                  boolean hasMoreLines,
	                                  int i) {
	        String txt;
	        out.print(" ");
	        if(col == null) {
	            txt = formatString( "null", ' ', meta[i].getWidth(),meta[i].getAlignment());
	            hasMoreLines = false;
	            out.print(txt);
	        } else {
	            txt = formatString( col.getNextLine(), ' ', meta[i].getWidth(),meta[i].getAlignment());
	            hasMoreLines |= col.hasNextLine();
	            if (col.isNull())
	                out.attributeGrey();
	            out.print(txt);
	            if (col.isNull())
	                out.attributeReset();
	        }
	        
	        out.print(colSeparator);
	        return hasMoreLines;
	    }

	    private void printHorizontalLine() {
	        boolean first = true;
	        for (int i = 0; i < meta.length; ++i) {
	            if (!meta[i].doDisplay())
	                continue;
	            String txt = null;;
	            if(first){
	                out.print("+");
	                first= false;
	            }
	            
	            txt = formatString("", '-', meta[i].getWidth() + separatorWidth + 1, ColumnMetaData.ALIGN_LEFT);
	            
	            out.print(txt);
	            out.print("+");
	        }
	        out.println();
	    }

	    private void printTableHeader() {
	        printHorizontalLine();
	        boolean first = true;
	        for (int i = 0; i < meta.length; ++i) {
	            if (!meta[i].doDisplay())
	                continue;
	            
	            if(first) {
	                out.print(colPreSeparator);
	                first = false;
	            }
	            
	            String txt;
	            txt = formatString(meta[i].getLabel(), ' ', meta[i].getWidth() + 1, ColumnMetaData.ALIGN_CENTER);
	            out.attributeBold();
	            out.print(txt);
	            out.attributeReset();
	            out.print(colSeparator);
	        }
	        out.println();
	        printHorizontalLine();
	    }

	    protected String formatString(String text,
	                                  char fillchar,
	                                  int len,
	                                  int alignment) {
	        // System.out.println("[formatString] len: " + len + ", text.length: " + text.length());
	        // text = "hi";
	        StringBuffer fillstr = new StringBuffer();

	        if (len > 4000) {
	            len = 4000;
	        }

	        if (text == null) {
	            text = "[NULL]";
	        }
	        int slen = text.length();

	        if (alignment == ColumnMetaData.ALIGN_LEFT) {
	            fillstr.append(text);
	        }
	        int fillNumber = len - slen;
	        int boundary = 0;
	        if (alignment == ColumnMetaData.ALIGN_CENTER) {
	            boundary = fillNumber / 2;
	        }
	        while (fillNumber > boundary) {
	            fillstr.append(fillchar);
	            --fillNumber;
	        }
	        if (alignment != ColumnMetaData.ALIGN_LEFT) {
	            fillstr.append(text);
	        }
	        while (fillNumber > 0) {
	            fillstr.append(fillchar);
	            --fillNumber;
	        }
	        return fillstr.toString();
	    }
	    
	    public static class Column {
	        
	        private final static String NULL_TEXT = "[NULL]";
	        private final static int NULL_LENGTH = NULL_TEXT.length();

	        private String columnText[]; // multi-rows
	        private int width;

	        /** This holds a state for the renderer */
	        private int pos;

	        public Column(long value) {
	            this(String.valueOf(value));
	        }

	        public Column(String text) {
	            if (text == null) {
	                width = NULL_LENGTH;
	                columnText = null;
	            }
	            else {
	                width = 0;
	                StringTokenizer tok = new StringTokenizer(text, "\n\r");
	                columnText = new String[tok.countTokens()];
	                for (int i = 0; i < columnText.length; ++i) {
	                    String line = (String)tok.nextElement();
	                    int lWidth = line.length();
	                    columnText[i] = line;
	                    if (lWidth > width) {
	                        width = lWidth;
	                    }
	                }
	            }
	            pos = 0;
	        }

	        /**
	         * Split a line at the nearest whitespace.
	         */
	        private String[] splitLine(String str, int autoCol) {
	            ArrayList<String> tmpRows = new ArrayList<String>(5);
	            int lastPos = 0;
	            int pos = lastPos + autoCol;
	            final int strLen = str.length();
	            while (pos < strLen) {
	                while (pos > lastPos && !Character.isWhitespace(str.charAt(pos))) {
	                    pos--;
	                }
	                if (pos == lastPos) { // no whitespace found: hard cut
	                    tmpRows.add(str.substring(lastPos, lastPos + autoCol));
	                    lastPos = lastPos + autoCol;
	                }
	                else {
	                    tmpRows.add(str.substring(lastPos, pos));
	                    lastPos = pos + /* skip space: */ 1;
	                }
	                pos = lastPos + autoCol;
	            }
	            if (lastPos < strLen-1) {
	                tmpRows.add(str.substring(lastPos));
	            }
	            return (String[]) tmpRows.toArray(new String[ tmpRows.size() ]);
	        }

	        /**
	         * replaces a row with multiple other rows.
	         */
	        private String[] replaceRow(String[] orig, int pos, String[] other) {
	            String result[] = new String[ orig.length + other.length - 1];
	            System.arraycopy(orig, 0, result, 0, pos);
	            System.arraycopy(other, 0, result, pos, other.length);
	            System.arraycopy(orig, pos+1, result, pos + other.length, orig.length-pos-1);
	            return result;
	        }

	        /**
	         * Set autowrapping at a given column.
	         */
	        void setAutoWrap(int autoWrapCol) {
	            if (autoWrapCol < 0 || columnText==null) 
	                return;
	            width = 0;
	            for (int i=0; i < columnText.length; ++i) {
	                int colWidth = columnText[i].length();
	                if (colWidth > autoWrapCol) {
	                    String[] multiRows = splitLine(columnText[i], autoWrapCol);
	                    for (int j = 0; j < multiRows.length; ++j) {
	                        int l = multiRows[j].length();
	                        if (l > width) {
	                            width = l;
	                        }
	                    }
	                    columnText = replaceRow(columnText, i, multiRows);
	                    i += multiRows.length; // next loop pos here.
	                }
	                else {
	                    if (colWidth > width) {
	                        width = colWidth;
	                    }
	                }
	            }
	        }

	        // package private methods for the table renderer.
	        int getWidth() {
	            return width;
	        }

	        boolean hasNextLine() {
	            return (columnText != null && pos < columnText.length);
	        }

	        boolean isNull() {
	            return (columnText == null);
	        }

	        String getNextLine() {
	            String result = "";
	            if (columnText == null) {
	                if (pos == 0)
	                    result = NULL_TEXT;
	            }
	            else if (pos < columnText.length) {
	                result = columnText[pos];
	            }
	            ++pos;
	            return result;
	        }
	        
	        public static class Factory {
	            
	            public static Column[] create(String... items) {
	                Column[] row = new Column[items.length];
	                for(int i = 0 ; i < items.length ; i ++){
	                    row[i] = new Column(items[i]);
	                }
	                return row;
	            }
	            
	            public static Column[] create(long... items) {
	                Column[] row = new Column[items.length];
	                for(int i = 0 ; i < items.length ; i ++){
	                    row[i] = new Column(items[i]);
	                }
	                return row;
	            }
	            
	            public static Column[] form(List<?> list) {
	                Column[] row = new Column[list.size()];
	                for(int i = 0 ; i < list.size() ; i ++){
	                    row[i] = new Column(String.valueOf(list.get(i)));
	                }
	                return row;
	            }
	        }
	    }
	    
	    public static class ColumnMetaData {
	        public static final int ALIGN_LEFT   = 1;
	        public static final int ALIGN_CENTER = 2;
	        public static final int ALIGN_RIGHT  = 3;

	        /** alignment; one of left, center, right */
	        private final int alignment;

	        /** the header of this column */
	        private final String label;

	        /** minimum width of this column; ususally set by the header width */
	        private final int initialWidth;

	        /** wrap columns automatically at this column; -1 = disabled */
	        private int autoWrapCol;

	        private int width;
	        private boolean display;

	        public ColumnMetaData(String header, int align) {
	            this(header, align, -1);
	        }

	        /**
	         * publically available constructor for the user.
	         */
	        public ColumnMetaData(String header, int align, int autoWrap) {
	            label = header;
	            initialWidth = header.length();
	            width = initialWidth;
	            alignment = align;
	            display = true;
	            autoWrapCol = autoWrap;
	        }
	        
            public ColumnMetaData(String header) {
	            this(header, ALIGN_LEFT);
	        }
	        
            public void resetWidth() { 
	            width = initialWidth; 
	        }

	        /**
	         * set, whether a specific column should be displayed.
	         */
	        public void setDisplay(boolean val) {
	            display = val; 
	        }
	        
	        public boolean doDisplay() {
	            return display; 
	        }

	        public void setAutoWrap(int col) {
	            autoWrapCol = col;
	        }
	        public int getAutoWrap() { 
	            return autoWrapCol; 
	        }

	        int getWidth() { 
	            return width; 
	        }
	        
	        String getLabel() { 
	            return label; 
	        }
	        
	        public int getAlignment() { 
	            return alignment; 
	        }
	        
	        void updateWidth(int w) {
	            if (w > width) {
	                width = w;
	            }
	        }
	        
	        public static class Factory {
	            
	            public static ColumnMetaData[] create(int align, String... items) {
	                ColumnMetaData[] metadata = new ColumnMetaData [items.length];
	                for(int i = 0 ; i < items.length ; i ++) {
	                    metadata[i] = new ColumnMetaData(items[i], align);
	                }
	                return metadata;
	            }
	            
	            public static ColumnMetaData[] create(String... items) {
	                return create(ALIGN_CENTER, items);
	            }
	            
	            public static ColumnMetaData[] form(String[] items) {
	                ColumnMetaData[] metadata = new ColumnMetaData [items.length];
	                for(int i = 0 ; i < items.length ; i ++) {
	                    metadata[i] = new ColumnMetaData(items[i], ColumnMetaData.ALIGN_CENTER);
	                }
	                return metadata;
	            }
	        }
	    }
	    
	    public static interface OutputDevice {
	        
	        void flush();
	        void write(byte[] buffer, int off, int len);
	        void print(String s);
	        void println(String s);
	        void println();

	        void attributeBold();
	        void attributeReset();
	        void attributeGrey();
	        
	        void close();

	        boolean isTerminal();

	    }
	    
	    public static class PrintStreamOutputDevice implements OutputDevice {
	        
	        private final PrintStream out;

	        public PrintStreamOutputDevice(PrintStream out) {
	            super();
	            this.out = out;
	        }

	        @Override
	        public void flush() {
	            out.flush();
	        }

	        @Override
	        public void write(byte[] buf, int off, int len) {
	            out.write(buf, off, len);
	        }

	        @Override
	        public void print(String s) {
	            out.print(s);
	        }

	        @Override
	        public void println(String s) {
	            out.println(s);
	        }

	        @Override
	        public void println() {
	            out.println();
	        }

	        @Override
	        public void attributeBold() {
	            
	        }

	        @Override
	        public void attributeReset() {
	            
	        }

	        @Override
	        public void attributeGrey() {
	            
	        }

	        @Override
	        public void close() {
	            out.close();
	        }

	        @Override
	        public boolean isTerminal() {
	            return false;
	        }

	    }
	}
	
	private static class ResultSetRenderer extends TableRenderer implements AutoCloseable{
		
		private final ResultSet rs;
		private final ResultSetMetaData meta;
		private final int columns;
		
		private final long clobLimit = 8192;
		
		public ResultSetRenderer(ResultSet rs) throws SQLException {
			super(new PrintStreamOutputDevice(System.out));
			this.rs = rs;
			this.meta = rs.getMetaData();
			this.columns = meta.getColumnCount();
			setMeta(getDisplayMeta(meta));
		}
		
		@Override
		public void renderer() {
			
			try {
				while(rs.next()) {
					Column[] currentRow = new Column[ columns ];
					for (int i = 0 ; i < columns ; ++i) {
					    int col = i+1;
			                    String colString;
			                    if (meta.getColumnType( col ) == Types.CLOB) {
			                        colString = readClob(rs.getClob( col ));
			                    }
			                    else {
			                        colString = rs.getString( col );
			                    }
					    Column thisCol = new Column(colString);
					    currentRow[i] = thisCol;
					}
					addRow(currentRow);
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
				
			super.renderer();
		}

		private ColumnMetaData[] getDisplayMeta(ResultSetMetaData m) throws SQLException {
	    	
			ColumnMetaData result[] = new ColumnMetaData [ columns ];
		
			for (int i = 0; i < result.length; ++i) {
			    int col = i+1;
			    int alignment  = ColumnMetaData.ALIGN_LEFT;
			    String columnLabel = m.getColumnLabel( col );
			    /*
			    int width = Math.max(m.getColumnDisplaySize(i),
						 columnLabel.length());
			    */
			    switch (m.getColumnType( col )) {
			    case Types.NUMERIC:  
			    case Types.INTEGER: 
			    case Types.REAL:
			    case Types.SMALLINT:
			    case Types.TINYINT:
					alignment = ColumnMetaData.ALIGN_RIGHT;
					break;
			    }
			    result[i] = new ColumnMetaData(columnLabel,alignment);
			}
			return result;
	    }
		
		private String readClob(Clob c) throws SQLException {
	        if (c == null) return null;
	        StringBuffer result = new StringBuffer();
	        long restLimit = clobLimit;
	        try {
	            Reader in = c.getCharacterStream();
	            char buf[] = new char [ 4096 ];
	            int r;

	            while (restLimit > 0 
	                   && (r = in.read(buf, 0, (int)Math.min(buf.length,restLimit))) > 0) 
	                {
	                    result.append(buf, 0, r);
	                    restLimit -= r;
	                }
	        }
	        catch (Exception e) {
	           out.println(e.toString());
	        }
	        if (restLimit == 0) {
	            result.append("...");
	        }
	        return result.toString();
	    }

		@Override
		public void close() throws Exception {
			out.close();
		}
		
	}

}
