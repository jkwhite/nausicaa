package org.excelsi.nausicaa.ca;


public class ROptions {
    private boolean _unlockWrite;
    private int _offset;
    private int _series;
    private Plane _override;


    public ROptions(boolean unlockWrite, int offset, int series) {
        _unlockWrite = unlockWrite;
        _offset = offset;
        _series = series;
    }

    public boolean unlockWrite() { return _unlockWrite; }
    public int offset() { return _offset; }
    public int series() { return _series; }
    public Plane override() { return _override; }

    public void unlockWrite(boolean uw) {
        _unlockWrite = uw;
    }

    public void offset(int offset) {
        _offset = offset;
    }

    public void series(int series) {
        _series = series;
    }

    public void override(Plane p) {
        _override = p;
    }
}
