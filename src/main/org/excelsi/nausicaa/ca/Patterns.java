package org.excelsi.nausicaa.ca;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;


public class Patterns {
    public static Iterable<IndexedPattern> iterator(Archetype a) {
        //final int plen = 1 + (int) Math.pow(2*a.size()+1, a.dims());
        //final int np = (int) Math.pow(a.colors(), plen-1);
        final int plen = a.patternLength();
        final int np = a.totalPatterns();
        //final long max = (long) Math.pow(a.colors(), np);
        final long max = a.totalRules();
        return new Iterable<IndexedPattern>() {
            @Override public Iterator<IndexedPattern> iterator() {
                return new Iterator<IndexedPattern>() {
                    long _current = 0;


                    @Override public boolean hasNext() {
                        return _current < max;
                    }

                    @Override public void remove() {
                    }

                    @Override public IndexedPattern next() {
                        //String s = Integer.toString(_current, colors);
                        //System.err.println(_current+": "+s);
                        return forIndex(a, _current++);
                    }
                };
            }
        };
    }

    public static IndexedPattern custom(Archetype a, IndexedPattern.Transform transform) {
        final int plen = a.patternLength();
        final int np = a.totalPatterns();
        byte[] p = new byte[np];
        transform.modulate(a, p);
        return new IndexedPattern(a, 0, plen-1, p);
    }

    public static IndexedPattern custom(Archetype sa, byte[] source, Archetype ta, IndexedPattern.BinaryTransform transform) {
        final int plen = ta.patternLength();
        final int np = ta.totalPatterns();
        byte[] p = new byte[np];
        transform.modulate(sa, source, ta, p);
        return new IndexedPattern(ta, 0, plen-1, p);
    }

    public static IndexedPattern forIndex(Archetype a, long index) {
        //final int plen = 1 + (int) Math.pow(2*a.size()+1, a.dims());
        //final int np = (int) Math.pow(a.colors(), plen-1);
        String s = Long.toString(index, a.colors());
        return forIndex(a, s, a.colors());
    }

    public static IndexedPattern forIndex(Archetype a, String s, int base) {
        if(a.colors()!=base) {
            BigInteger i = new BigInteger(s, base);
            s = i.toString(a.colors());
        }
        final int plen = a.patternLength();
        final int np = a.totalPatterns();
        //System.err.println(index+" => "+s);
        byte[] p = new byte[np];
        //int i = p.length-1;
        //int j = s.length()-1;
        while(s.length()<p.length) {
            s = "0"+s;
        }
        for(int i=0;i<p.length;i++) {
            //if(i<s.length()) {
                p[p.length-i-1] = (byte)(s.charAt(i)-'0');
                //p[i] = (byte)(s.charAt(i)-'0');
            //}
        }
        System.err.println("PAT: "+Arrays.toString(p));

        /*
        for(;i>=0&&j>=0;i--) {
            //p[p.length-1-i] = (byte) (s.charAt(i)-'0');
            //p[i] = (byte) (s.charAt(i)-'0');
            p[i] = (byte) (s.charAt(j--)-'0');
        }
        while(i-->0) {
            p[i] = 0;
        }
        */
        return new IndexedPattern(a, /*index*/ -1, plen-1, p);
    }

    // index(10) -> index(b)

    public static byte[] expandSourceIndexOld(final Archetype a, long index, byte[] base) {
        String s = Long.toString(index, a.colors());
        int i = base.length-1;
        int j = s.length()-1;
        for(;i>=0&&j>=0;i--) {
            base[i] = (byte) (s.charAt(j--)-'0');
        }
        while(i-->0) {
            base[i] = 0;
        }
        final byte[] test = base.clone();
        expandSourceIndex(a, index, test);
        if(!Arrays.equals(base, test)) {
            throw new IllegalStateException("failed on "+a+", idx="+index);
        }
        return base;
    }

    public static void expandSourceIndex(final Archetype a, long index, final byte[] base) {
        int i = base.length-1;
        final int b = a.colors();
        while(index>0 && i>=0) {
            final byte d = (byte) (index % b);
            base[i--] = d;
            index = index / b;
        }
        while(i-->0) {
            base[i] = 0;
        }
    }

    public static int indexForSource(final int[] coefficients, final byte[] source) {
        int v = 0;

        //final int[] pow = new int[source.length];
        //for(int i=0;i<pow.length;i++) {
            //pow[pow.length-1-i] = (int) Math.pow(a.colors(), i);
        //}
        for(int i=0;i<source.length;i++) {
            v += source[i] * coefficients[i];
        }
        return v;
    }

    public static byte[] mirror(final byte[] source, final byte[] target) {
        int j = target.length-1;
        for(int i=0;i<source.length;i++) {
            target[j] = source[i];
            j--;
        }
        return target;
    }

    public static byte[] rotate(byte[] o, byte[] p) {
        //System.out.println("source");
        //print(o);
        //byte[] p = new byte[10];
        if(p.length>5) {
            for(int i=0;i<=2;i++) {
                for(int j=0;j<=2;j++) {
                    set(p, 2-i, j, get(o, j, i));
                }
            }
        }
        else {
            p[0] = o[1];
            p[1] = o[4];
            p[2] = o[2];
            p[3] = o[0];
            p[4] = o[3];
        }
        //System.out.println("dest");
        //print(p);
        return p;
    }

    public static byte get(byte[] p, int m, int n) {
        return p[3*n+m];
    }

    public static void set(byte[] p, int m, int n, byte v) {
        p[3*n+m] = v;
    }

    public static IndexedPattern random(Archetype a, Random r) {
        byte[] p = new byte[a.totalPatterns()];
        for(int i=0;i<p.length;i++) {
            if(r.nextInt(100)<30) {
                p[i] = (byte) r.nextInt(a.colors());
            }
            else {
                p[i] = 0;
            }
        }
        return new IndexedPattern(a, -1, a.sourceLength(), p);
    }

    public static String formatPattern(byte... ps) {
        StringBuilder b = new StringBuilder();
        for(byte p:ps) {
            b.append((char) (p+'0'));
        }
        return b.toString();
    }
}
