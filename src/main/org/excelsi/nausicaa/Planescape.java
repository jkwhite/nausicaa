package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;


public interface Planescape {
    Plane getPlane();
    void setPlane(Plane p);
    Rule getRule();
    Rule compileRule();
    boolean delegateUnlock();
}
