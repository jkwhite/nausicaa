package org.excelsi.nausicaa.ca;


import gnu.trove.map.hash.TLongIntHashMap;


public class SparsePattern implements Pattern {
    private final Archetype _a;
    private final TLongIntHashMap _p;


    public SparsePattern(Archetype a) {
        _a = a;
        _p = new TLongIntHashMap();
    }

    @Override public Archetype archetype() {
        return _a;
    }

    @Override public byte next(int pattern, byte[] p2) {
        return (byte) 0;
    }

    @Override public int next(int pattern, int[] p2, Ctx ctx) {
        return 0;
    }

    @Override public Pattern copy() {
        return this;
    }

    @Override public void tick() {
    }
}
