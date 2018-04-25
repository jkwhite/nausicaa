package org.excelsi.nausicaa.ca;


public class Rendering {
    public enum Composition {
        front, back, wavg, revwavg, channel, avg;

        public static Composition from(String s) {
            switch(s) {
                case "front":
                    return front;
                case "back":
                    return back;
                case "wavg":
                    return wavg;
                case "revwavg":
                    return revwavg;
                case "avg":
                    return avg;
                case "channel":
                    return channel;
            }
            throw new IllegalArgumentException("no such comp '"+s+"'");
        }
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
