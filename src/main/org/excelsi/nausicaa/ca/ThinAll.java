package org.excelsi.nausicaa.ca;


public final class ThinAll extends Thin {
    public String name() { return "Thin all"; }
    public String description() { return "Adds some background to all colors"; }


    @Override protected final byte mutate(Archetype a, byte t) {
        if(test(t)) {
            t = (byte) 0;
        }
        return t;
    }
}
