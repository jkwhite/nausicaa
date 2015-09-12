package org.excelsi.ca;


public class PhaseTransition {
    private final Rule _src;
    private final Rule _dst;
    private int _color;
    private boolean _mask;


    public PhaseTransition(Rule src, Rule dst, int color, boolean mask) {
        _src = src;
        _dst = dst;
        _color = color;
        _mask = mask;
    }

    public Rule source() { return _src; }
    public Rule dest() { return _dst; }
    public int color() { return _color; }
    public boolean mask() { return _mask; }

    public String toString() { return "src: "+_src+", dst: "+_dst+", color: "+_color; }
}
