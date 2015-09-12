package org.excelsi.ca;


import java.util.Iterator;


public class Patterns {
    public static Iterable<Pattern> iterator(int dims, int size, int colors) {
        // 1 1 3 4
        // 1 2 5 6
        // 2 1 9 10
        // 2 2 9
        // 3 1 27 28
        int plen = 1 + (int) Math.pow(2*size+1, dims);
        int np = (int) Math.pow(colors, plen-1);
        int tlen = np * plen;
        System.err.println(dims+"x"+size+"x"+colors+" => "+tlen);
        byte[] patterns = new byte[tlen];
        for(int i=0;i<tlen;i++) {
            patterns[i] = 0;
        }
        return null;
    }
}
