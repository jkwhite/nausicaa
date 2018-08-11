package org.excelsi.nausicaa.ca;


public interface Pattern {
    Archetype archetype();
    byte next(int pattern, byte[] p2);
    default int next(int pattern, int[] p2) {
        throw new UnsupportedOperationException();
    }
    default float next(int pattern, float[] p2) {
        throw new UnsupportedOperationException();
    }
    default Pattern copy() {
        throw new UnsupportedOperationException();
    }
    void tick();
}
