package org.excelsi.nausicaa;


import java.util.Iterator;
import java.util.Random;


public class Patterns {
    public static Iterable<Pattern2> iterator(Archetype a) {
        //final int plen = 1 + (int) Math.pow(2*a.size()+1, a.dims());
        //final int np = (int) Math.pow(a.colors(), plen-1);
        final int plen = a.patternLength();
        final int np = a.totalPatterns();
        //final long max = (long) Math.pow(a.colors(), np);
        final long max = a.totalRules();
        return new Iterable<Pattern2>() {
            @Override public Iterator<Pattern2> iterator() {
                return new Iterator<Pattern2>() {
                    long _current = 0;


                    @Override public boolean hasNext() {
                        return _current < max;
                    }

                    @Override public void remove() {
                    }

                    @Override public Pattern2 next() {
                        //String s = Integer.toString(_current, colors);
                        //System.err.println(_current+": "+s);
                        return forIndex(a, _current++);
                    }
                };
            }
        };
    }

    public static Pattern2 forIndex(Archetype a, long index) {
        //final int plen = 1 + (int) Math.pow(2*a.size()+1, a.dims());
        //final int np = (int) Math.pow(a.colors(), plen-1);
        final int plen = a.patternLength();
        final int np = a.totalPatterns();
        String s = Long.toString(index, a.colors());
        System.err.println(index+" => "+s);
        byte[] p = new byte[np];
        int i = p.length-1;
        int j = s.length()-1;
        for(;i>=0&&j>=0;i--) {
            //p[p.length-1-i] = (byte) (s.charAt(i)-'0');
            //p[i] = (byte) (s.charAt(i)-'0');
            p[i] = (byte) (s.charAt(j--)-'0');
        }
        while(i-->0) {
            p[i] = 0;
        }
        return new Pattern2(a, index, plen-1, p);
    }

    public static Pattern2 random(Archetype a, Random r) {
        byte[] p = new byte[a.totalPatterns()];
        for(int i=0;i<p.length;i++) {
            p[i] = (byte) r.nextInt(a.colors());
        }
        return new Pattern2(a, -1, a.sourceLength(), p);
    }

    public static String formatPattern(byte... ps) {
        StringBuilder b = new StringBuilder();
        for(byte p:ps) {
            b.append((char) (p+'0'));
        }
        return b.toString();
    }
}
