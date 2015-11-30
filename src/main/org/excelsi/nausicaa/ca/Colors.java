package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Colors {
    public static final int BLACK = pack(0,0,0,256);
    public static final int COLOR_MASK = 0x00ffffff;
    public static final int ALPHA_MASK = 0xff000000;


    public static int pack(int r, int g, int b) {
        return pack(r, g, b, 255);
    }

    public static int pack(int r, int g, int b, int a) {
        //return a+(r<<8)+(g<<16)+(b<<24);
        return b|(g<<8)|(r<<16)|(a<<24);
    }

    public static int[] unpack(int c) {
        return unpack(c, new int[4]);
    }

    public static int[] unpack(int c, int[] u) {
        u[0] = c & 0xff;
        u[1] = (c>>8)&0xff;
        u[2] = (c>>16)&0xff;
        u[3] = (c>>24)&0xff;
        return u;
    }

    public static int alpha(int c) {
        return c>>24&0xff;
    }

    public static int setAlpha(int c, int a) {
        return setAlpha(c, a, 0);
    }

    public static int setAlpha(int c, int a, int offset) {
        a += offset;
        c = (c&COLOR_MASK) | ((a&0xff)<<24);
        return c;
    }

    public static String toColorString(int c) {
        int[] u = unpack(c);
        return "[R: "+u[2]+" G: "+u[1]+" B: "+u[0]+" A: "+u[3]+"]";
    }

    public static int randomColor(Random om) {
        return pack(om.nextInt(256), om.nextInt(256),
                om.nextInt(256),
                255);
                //253 + Rand.om.nextInt(3));
                //200 + Rand.om.nextInt(53));
                //randAlpha(om));
    }

    public static int randAlpha() {
        return randAlpha(Rand.om);
    }

    public static int randAlpha(Random om) {
        switch(om.nextInt(5)) {
            case 0:
                return 253;
            case 1:
                return 254;
            default:
                return 255;
        }
    }

}
