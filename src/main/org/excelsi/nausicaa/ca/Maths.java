package org.excelsi.nausicaa.ca;


public final class Maths {
    public static int pow(final int k, int e) {
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
