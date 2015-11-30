package org.excelsi.nausicaa.ca;


import java.util.Random;


public final class Rand implements RandomFactory {
    public static final Random om = new Random();
    private static int _r = 8;


    public static int seed() {
        return _r;
    }

    public static int newSeed() {
        _r = om.nextInt();
        return seed();
    }

    public Random create() {
        return new Random(_r);
    }
}
