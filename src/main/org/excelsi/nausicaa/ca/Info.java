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

    public String summarize() {
        return summarize(-1);
    }

    public String summarize(final int trim) {
        String text = _ca.getRule().humanize();
        if(trim>0 && text.length()>trim) {
            text = "..."+text.substring(text.length()-trim,text.length());
        }
        return text;
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
