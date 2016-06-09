package org.excelsi.nausicaa.ca;


public final class ThickenAll extends Thicken {
    public String name() { return "Thicken all"; }
    public String description() { return "Subtracts some background from all colors"; }


    @Override protected final byte mutate(Archetype a, MutationFactor f, byte t) {
        if(test(f, t)) {
            t = (byte) (_om.nextInt(a.colors()-1)+1);
        }
        return t;
    }
}
