package org.teiid.sizing.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CSVUtils {

    private static final String DEFAULT_SEPARATOR = ", ";

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(Writer w, List<String> values, String separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }
    
    public static void writeLine(Writer w, List<String> values, String separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }
    
    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }
}
