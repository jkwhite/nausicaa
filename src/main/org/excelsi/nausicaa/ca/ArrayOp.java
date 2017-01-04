package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface ArrayOp {
    int[] op(byte[] a);


    public static ArrayOp histo(int colors) {
        final int[] zero = new int[colors];
        final int[] histo = new int[colors];
        return (a)->{
            //for(int i=0;i<histo.length;i++) {
                //histo[i] = 0;
            //}
            System.arraycopy(zero, 0, histo, 0, zero.length);
            for(int i=0;i<a.length;i++) {
                histo[(int)a[i]]++;
            }
            return histo;
        };
    }
}
