package org.excelsi.nausicaa;


import java.awt.Frame;
import org.excelsi.nausicaa.ca.CA;


public interface UIActions {
    Frame getRoot();
    void doWait(Runnable r, long initialDelay);
    Config getConfig();
    CA getActiveCA();
    void setActiveCA(CA c);
    void branch(CA c);
    PlaneDisplayProvider getPlaneDisplayProvider();
    PlanescapeProvider getPlanescapeProvider();
}
