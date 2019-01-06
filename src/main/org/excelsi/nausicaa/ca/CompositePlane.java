package org.excelsi.nausicaa.ca;


public interface CompositePlane extends Plane, Sliceable {
    Plane[] planes();
    CompositePlane emptyCopy();
}
