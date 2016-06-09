package org.excelsi.nausicaa.ca;


import java.math.BigInteger;


public class Info {
    private final CA _ca;


    public Info(CA ca) {
        _ca = ca;
    }

    public String b10Id() {
        return b10Id(_ca.getRule(), 1000000);
    }

    public int colors() {
        return _ca.getPalette().getColorCount();
    }

    @Override public String toString() {
        return colors()+"/"+b10Id();
    }

    public static String b10Id(final Rule r, final int max) {
        String id = r.id();
        if(id.length()<max) {
            BigInteger rval = new BigInteger(r.id(), r.colorCount());
            final String frval = rval.toString(10);
            return frval;
        }
        else {
            return "~"+Integer.toString(Math.abs(id.hashCode()));
        }
    }
}
