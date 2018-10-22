package org.excelsi.nausicaa.ca;


public class Implicate {
    private final Archetype _a;
    private final Datamap _d;
    private final Language _l;


    public Implicate(Archetype a, Datamap d, Language lang) {
        _a = a;
        _d = d;
        _l = lang;
    }

    public Archetype archetype() {
        return _a;
    }

    public Datamap datamap() {
        return _d;
    }

    public Language language() {
        return _l;
    }
}
