package org.excelsi.nausicaa.ca;


public class GOptions {
    private final boolean _doubleBuffer;
    private final int _parallel;
    private final int _stride;
    private final float _weight;
    private final int _hd;


    public GOptions() {
        this(true, 1, 0, 1f, 0);
    }
 
    public GOptions(boolean db, int par, int s, float w) {
        this(db, par, s, w, 0);
    }
 
    public GOptions(boolean db, int par, int s, float w, int hd) {
        _doubleBuffer = db;
        _parallel = par;
        _stride = s;
        _weight = w;
        _hd = hd;
    }

    public boolean doubleBuffer() { return _doubleBuffer; }
    public int parallel() { return _parallel; }
    public int stride() { return _stride; }
    public float weight() { return _weight; }
    public int higherDim() { return _hd; }

    public GOptions stride(int s) {
        return new GOptions(_doubleBuffer, _parallel, s, _weight, _hd);
    }

    public GOptions weight(float w) {
        return new GOptions(_doubleBuffer, _parallel, _stride, w, _hd);
    }

    public GOptions higherDim(int hd) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, hd);
    }
}
