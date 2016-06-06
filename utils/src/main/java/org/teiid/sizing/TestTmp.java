package org.teiid.sizing;

//TODO-- need remove 
public class TestTmp {

    public static void main(String[] args) throws Exception {
        
//        System.out.println((TeiidUtils.MB * 100) / 256);      
//        System.out.println(4 * TeiidUtils.KB * 100);
//        System.out.println(TeiidUtils.MB * 100);

        args = new String[]{"deserialization", "200", "1024"};
        TeiidUtils.main(args);
    }

}
