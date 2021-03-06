package org.excelsi.nausicaa.ca;


import java.util.Random;
import javafx.scene.paint.Color;


public final class Colors {
    public static final int BLACK = pack(0,0,0,256);
    public static final int COLOR_MASK = 0x00ffffff;
    public static final int ALPHA_MASK = 0xff000000;


    public static int opacify(int c) {
        return c | ALPHA_MASK;
    }

    public static int bound(int c) {
        c = c % 256;
        if(c<0) c = 256 + c;
        return c;
    }

    public static float bound(float c) {
        return Math.min(1f,Math.max(0f,c));
    }

    public static double bound(double c) {
        return Math.min(1d,Math.max(0d,c));
    }

    public static int integer(float c) {
        return (int) (c*255f);
    }

    public static int integer(double c) {
        return (int) (c*255d);
    }

    public static int pack(int r, int g, int b) {
        return pack(r, g, b, 255);
    }

    public static int pack(int r, int g, int b, int a) {
        return b|(g<<8)|(r<<16)|(a<<24);
    }

    public static int packBounded(int r, int g, int b, int a) {
        return bound(b)|(bound(g)<<8)|(bound(r)<<16)|(bound(a)<<24);
    }

    public static int pack(float r, float g, float b) {
        return pack(r, g, b, 1f);
    }

    public static int pack(float r, float g, float b, float a) {
        return integer(b)|(integer(g)<<8)|(integer(r)<<16)|(integer(a)<<24);
    }

    public static int packBounded(float r, float g, float b) {
        return packBounded(r,g,b,1f);
    }

    public static int packBounded(float r, float g, float b, float a) {
        return integer(bound(b))|(integer(bound(g))<<8)|(integer(bound(r))<<16)|(integer(bound(a))<<24);
    }

    public static int pack(double r, double g, double b) {
        return pack(r, g, b, 1d);
    }

    public static int pack(double r, double g, double b, double a) {
        return integer(b)|(integer(g)<<8)|(integer(r)<<16)|(integer(a)<<24);
    }

    public static int packBounded(double r, double g, double b) {
        return packBounded(r,g,b,1d);
    }

    public static int packBounded(double r, double g, double b, double a) {
        return integer(bound(b))|(integer(bound(g))<<8)|(integer(bound(r))<<16)|(integer(bound(a))<<24);
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

    public static int avg(int c1, int c2, float w) {
        int c10 = c1 & 0xff;
        int c11 = (c1>>8)&0xff;
        int c12 = (c1>>16)&0xff;
        int c13 = (c1>>24)&0xff;
        int c20 = c2 & 0xff;
        int c21 = (c2>>8)&0xff;
        int c22 = (c2>>16)&0xff;
        int c23 = (c2>>24)&0xff;
        int w1 = wgt(c10,c20,w);
        int w2 = wgt(c11,c21,w);
        int w3 = wgt(c12,c22,w);
        int w4 = wgt(c13,c23,w);
        return pack(w3,w2,w1,w4);
    }

    private static int wgt(final int c1, final int c2, final float zw) {
        return (int)(c1*zw+c2*(1f-zw));
    }

    public static int avg(int c1, int c2, double w) {
        int c10 = c1 & 0xff;
        int c11 = (c1>>8)&0xff;
        int c12 = (c1>>16)&0xff;
        int c13 = (c1>>24)&0xff;
        int c20 = c2 & 0xff;
        int c21 = (c2>>8)&0xff;
        int c22 = (c2>>16)&0xff;
        int c23 = (c2>>24)&0xff;
        int w1 = wgt(c10,c20,w);
        int w2 = wgt(c11,c21,w);
        int w3 = wgt(c12,c22,w);
        int w4 = wgt(c13,c23,w);
        return pack(w3,w2,w1,w4);
    }

    private static int wgt(final int c1, final int c2, final double zw) {
        return (int)(c1*zw+c2*(1d-zw));
    }

    public static int[] unpackRgb(int c, int[] u) {
        u[0] = c & 0xff;
        u[1] = (c>>8)&0xff;
        u[2] = (c>>16)&0xff;
        return u;
    }

    public static boolean isBlack(int c) {
        int r = c & 0xff;
        int g = (c>>8)&0xff;
        int b = (c>>16)&0xff;
        return r==0&&g==0&&b==0;
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

    public static void extractChannels(int[] src, int[][] chans) {
        final int[] r = chans[0];
        final int[] g = chans[1];
        final int[] b = chans[2];
        final int[] a = chans[3];
        for(int i=0;i<src.length;i++) {
            final int v = src[i];
            b[i] = v & 0xff;
            g[i] = (v>>8) & 0xff;
            r[i] = (v>>16) & 0xff;
            a[i] = (v>>24) & 0xff;
        }
    }

    public static String toColorString(int c) {
        int[] u = unpack(c);
        return "[R: "+u[2]+" G: "+u[1]+" B: "+u[0]+" A: "+u[3]+"]";
    }

    public static void dump(int rgb, String msg) {
        System.err.println(msg+": "+toColorString(rgb));
    }

    public static int fromString(final String c) {
        final Color col = Color.valueOf(c);
        return pack((int)(255*col.getRed()), (int)(255*col.getGreen()), (int)(255*col.getBlue()), 255);
    }

    public static int randomColor(Random om) {
        return pack(om.nextInt(256), om.nextInt(256),
                om.nextInt(256),
                255);
    }

    public static int randomColor(Random om, int min) {
        return pack(randomShade(om, min), randomShade(om, min), randomShade(om, min), 255);
    }

    public static int randomShade(Random om, int min) {
        return om.nextBoolean() ? om.nextInt(min)+(256-min) : om.nextInt(min);
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
