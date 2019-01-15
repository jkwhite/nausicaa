package org.excelsi.nausicaa.ca;


import java.util.Map;


public class Implicate {
    private final Archetype _a;
    private final Datamap _d;
    private final Language _l;
    private final Varmap _vars;


    public Implicate(Archetype a, Datamap d, Language lang) {
        this(a, d, lang, new Varmap());
        //Thread.dumpStack();
    }

    public Implicate(Archetype a, Datamap d, Language lang, Varmap vars) {
        _a = a;
        _d = d;
        _l = lang;
        _vars = vars;
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

    public Varmap vars() {
        return _vars;
    }
}
