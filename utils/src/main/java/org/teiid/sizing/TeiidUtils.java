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

public class TeiidUtils {
    
    private static final String TAB = "    ";
    private static final String COLON = ": ";
    
    private static final String TOOLS_SERIALIZATION = "serialization";
    private static final String TOOLS_DESERIALIZATION = "deserialization";

    public static void main(String[] args) {

        if(args.length <= 0){
            usage();
        } else {
            String tool = args[0];
            String[] dest = new String[args.length -1];
            System.arraycopy(args, 1, dest, 0, dest.length);
            if(tool.equals(TOOLS_SERIALIZATION)) {
                Serialization.main(dest);
            } else if(tool.equals(TOOLS_DESERIALIZATION)) {
                Deserialization.main(dest);
            } else {
                usage();
            }
        }
    }

    private static void usage() {
        System.out.println("An sizing/profiling program must be given as the first argument.");
        System.out.println("Valid program names are:");
        System.out.println(TAB + TOOLS_SERIALIZATION + COLON + TeiidUtilsPlugin.Util.getString(TOOLS_SERIALIZATION));
        System.out.println(TAB + TOOLS_DESERIALIZATION + COLON + TeiidUtilsPlugin.Util.getString(TOOLS_DESERIALIZATION));
    }

}
