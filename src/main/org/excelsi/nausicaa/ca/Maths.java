package org.excelsi.nausicaa.ca;


public final class Maths {
    public static final float PI = (float) Math.PI;
    public static final float PI2 = 2f * (float) Math.PI;

    public static int syshash(Object o) {
        return System.identityHashCode(o);
    }

    public static int pow(final int k, int e) {
        if(e<0) {
            throw new IllegalArgumentException("negative e: "+e);
        }
        int result = 1;
        int k2p    = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;
    }

    public static int abs(final int v) {
        return v<0?-v:v;
    }

    public static int excl(int v) {
        //System.err.print(v+" => ");
        int res = v;
        while(v-->1) {
            res *= v;
        }
        //System.err.println(res);
        return res;
    }

    private Maths() {}
}
