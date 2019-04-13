package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface Pattern {
    Archetype archetype();
    byte next(int pattern, byte[] p2);
    default int next(int pattern, int[] p2, Ctx ctx) {
        throw new UnsupportedOperationException();
    }
    default double next(int pattern, double[] p2, Ctx ctx) {
        throw new UnsupportedOperationException();
    }
    default Pattern copy() {
        throw new UnsupportedOperationException();
    }
    void tick();
    default boolean usesSource() { return true; }
    default boolean usesContext() { return false; }

    static class Ctx {
        /** coordinates */
        public int[] c;
        /** random */
        public Random r;
    }
}
