package org.excelsi.ca;


public class BlackWhite {
    public static final int W = pack(255,255,255,255);
    public static final int B = pack(0,0,0,255);


    public int getWidth() {
        return 3;
    }

    private static int pack(int r, int g, int b, int a) {
        return a+(r<<8)+(g<<16)+(b<<24);
    }
}
