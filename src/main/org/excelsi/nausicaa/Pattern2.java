package org.excelsi.nausicaa;


public class Pattern2 {
    private final Archetype _a;
    private final long _id;
    private final int _length;
    private final byte[] _target;


    /**
     * Constructs a new Pattern.
     *
     * @param length number of bytes in pattern, e.g. 3 or 9
     * @param colors number of colors in pattern
     * @param next end results
     */
    public Pattern2(Archetype a, long id, int length, byte[] target) {
        _a = a;
        _id = id;
        _length = length;
        _target = target;
    }

    public long id() {
        return _id;
    }

    public byte next(int pattern) {
        return _target[pattern];
    }

    public int length() {
        return _length;
    }

    public Archetype archetype() {
        return _a;
    }

    @Override public String toString() {
        return Patterns.formatPattern(_target);
    }

    public String toDetail() {
        StringBuilder b = new StringBuilder();
        int i=0;
        for(byte[] p:_a.sources()) {
            b.append(Patterns.formatPattern(p));
            b.append(" => ");
            b.append(Patterns.formatPattern(_target[i++]));
            b.append("\n");
        }
        return b.toString();
    }
}
