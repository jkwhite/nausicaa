package org.excelsi.nausicaa.ca;


public class Implicate {
    private final Archetype _a;
    private final Datamap _d;


    public Implicate(Archetype a, Datamap d) {
        _a = a;
        _d = d;
    }

    public Archetype archetype() {
        return _a;
    }

    public Datamap datamap() {
        return _d;
    }
}
