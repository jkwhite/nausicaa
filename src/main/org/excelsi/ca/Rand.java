package org.excelsi.ca;


import java.util.Random;


public final class Rand {
    public static final Random om = new Random();
    private static int _r = 8;


    private Rand() {
    }

    public static int seed() {
        return _r;
    }

    public static int newSeed() {
        _r = om.nextInt();
        return seed();
    }
}
