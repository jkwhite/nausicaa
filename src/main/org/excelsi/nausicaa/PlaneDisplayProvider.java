package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Plane;


public interface PlaneDisplayProvider {
    PlaneDisplay[] getDisplays();
    Plane getActivePlane();
    PlaneDisplay getActivePlaneDisplay();
}
