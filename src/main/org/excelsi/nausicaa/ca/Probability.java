package org.excelsi.nausicaa.ca;


import java.util.Random;


@FunctionalInterface
public interface Probability {
    int p(Random r, int max);


    public static Probability linear() {
        return (r, max)->{ return r.nextInt(max); };
    }

    public static Probability gaussian() {
        return (r, max)->{ return (int) Math.max(0, Math.min(max-1, (r.nextGaussian() + max/2))); };
    }

    public static Probability lowpass() {
        return (r, max)->{ return (int) Math.max(0, Math.min(max-1, (r.nextGaussian()))); };
    }

    public static Probability highpass() {
        return (r, max)->{ return (int) Math.max(0, Math.min(max-1, (r.nextGaussian() + max))); };
    }
}
