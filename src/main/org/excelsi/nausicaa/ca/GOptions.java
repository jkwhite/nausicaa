package org.excelsi.nausicaa.ca;


public class GOptions {
    private final boolean _doubleBuffer;
    private final int _parallel;
    private final int _stride;


    public GOptions(boolean db, int par, int s) {
        _doubleBuffer = db;
        _parallel = par;
        _stride = s;
    }

    public boolean doubleBuffer() { return _doubleBuffer; }
    public int parallel() { return _parallel; }
    public int stride() { return _stride; }

    public GOptions stride(int s) {
        return new GOptions(_doubleBuffer, _parallel, s);
    }
}
