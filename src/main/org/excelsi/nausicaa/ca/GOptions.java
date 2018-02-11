package org.excelsi.nausicaa.ca;


public class GOptions {
    private final boolean _doubleBuffer;
    private final int _parallel;
    private final int _stride;
    private final float _weight;


    public GOptions() {
        this(true, 1, 0, 1f);
    }
 
    public GOptions(boolean db, int par, int s, float w) {
        _doubleBuffer = db;
        _parallel = par;
        _stride = s;
        _weight = w;
    }

    public boolean doubleBuffer() { return _doubleBuffer; }
    public int parallel() { return _parallel; }
    public int stride() { return _stride; }
    public float weight() { return _weight; }

    public GOptions stride(int s) {
        return new GOptions(_doubleBuffer, _parallel, s, _weight);
    }

    public GOptions weight(float w) {
        return new GOptions(_doubleBuffer, _parallel, _stride, w);
    }
}
