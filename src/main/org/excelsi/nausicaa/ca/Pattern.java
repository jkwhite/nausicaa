package org.excelsi.nausicaa.ca;


public interface Pattern {
    Archetype archetype();
    byte next(int pattern);
    void tick();
}
