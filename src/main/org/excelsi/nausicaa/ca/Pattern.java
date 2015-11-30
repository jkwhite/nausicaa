package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.Iterator;


public class Pattern {
    private int[] _p;
    private int _dims;


    public Pattern(int[] pattern, int dims) {
        _p = pattern;
        _dims = dims;
    }

    public int result() {
        return _p[_p.length-1];
    }

    public int[] pattern() {
        return _p;
    }

    public boolean equals(Object o) {
        if(o instanceof Pattern) {
            Pattern op = (Pattern) o;
            if(_p.length == op._p.length) {
                for(int i=0;i<_p.length-1;i++) {
                    if(_p[i]!=op._p[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int h = 0;
        for(int i=0;i<_p.length-1;i++) {
            h ^= _p[i];
            h = Integer.rotateLeft(h, 1);
        }
        return h;
    }

    public Pattern mirror() {
        int[] m = new int[_p.length];
        for(int i=0;i<_p.length-1;i++) {
            m[m.length-2-i] = _p[i];
        }
        m[m.length-1] = _p[_p.length-1];
        return new Pattern(m, _dims);
    }

    public Iterator<Pattern> mirrors() {
        switch(_dims) {
            case 1:
                return Arrays.asList(new Pattern[]{mirror()}).iterator();
            case 2:
                final Pattern[] ps = new Pattern[3];
                return new Iterator<Pattern>() {
                    int[] start = _p;
                    int i=0;

                    public Pattern next() {
                        int[] next = rotate(start);
                        ps[i] = new Pattern(next, _dims);
                        start = next;
                        Pattern ret = ps[i];
                        i++;
                        //System.err.println("i="+i);
                        return ret;
                    }

                    public boolean hasNext() {
                        //System.err.println("hasnext: "+i+", "+ps.length);
                        return i<ps.length;
                    }

                    public void remove() { }
                };
        }
        throw new Error("unsupported dimensionality "+dimensionality());
    }

    public static int get(int[] p, int m, int n) {
        return p[3*n+m];
    }

    public static void set(int[] p, int m, int n, int v) {
        p[3*n+m] = v;
    }

    /*
    0,0 > 2,0
    1,0 > 2,1
    2,0 > 2,2
    0,1 > 1,0
    1,1 > 1,1
    2,1 > 1,2
    0,2 > 0,0
    1,2 > 0,1
    2,2 > 0,2
    */

    public static int[] rotate(int[] o) {
        //System.out.println("source");
        //print(o);
        int[] p = new int[10];
        for(int i=0;i<=2;i++) {
            for(int j=0;j<=2;j++) {
                set(p, 2-i, j, get(o, j, i));
            }
        }
        //System.out.println("dest");
        //print(p);
        return p;
    }

    public static void print(int[] p) {
        for(int i=0;i<=2;i++) {
            for(int j=0;j<=2;j++) {
                System.out.print(get(p, j, i)+" ");
            }
            System.out.println();
        }
    }


    public int dimensionality() {
        return _p.length==4?1:2;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("[");
        for(int i=0;i<_p.length;i++) {
            b.append(_p[i]+",");
        }
        b.setLength(b.length()-1);
        b.append("]");
        return b.toString();
    }
}
