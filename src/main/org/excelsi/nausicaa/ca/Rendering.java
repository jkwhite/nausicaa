package org.excelsi.nausicaa.ca;


public class Rendering {
    public enum Composition {
        front, avg, channel
    };

    private final Composition _comp;

    public Rendering() {
        this(Composition.front);
    }

    public Rendering(Composition comp) {
        _comp = comp;
    }

    public Composition composition() {
        return _comp;
    }

    public Rendering composition(Composition comp) {
        return new Rendering(comp);
    }
}
