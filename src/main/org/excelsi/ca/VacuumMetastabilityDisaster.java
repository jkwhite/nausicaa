package org.excelsi.ca;


public class VacuumMetastabilityDisaster {
    private Rule _r;
    private int _x;
    private int _y;


    public VacuumMetastabilityDisaster(Rule r, int x, int y) {
        _r = r;
        _x = x;
        _y = y;
    }

    public Rule rule() { return _r; }

    public int x() { return _x; }

    public int y() { return _y; }

    public String toString() { return "rule dim: "+_r.dimensions()+", x: "+_x+", y: "+_y; }
}
