package org.excelsi.nausicaa.ca;


import java.util.List;


public class Painter {
    private final Plane _p;


    public Painter(Plane p) {
        _p = p;
    }

    public Plane p() {
        return _p;
    }

    public Painter draw(int x, int y, String s) {
        return draw(x, y, 0, s);
    }

    public Painter draw(final int x, final int y, final int z, String s) {
        int i = x, j = y, k = z;
        for(int d=0;d<s.length();d++) {
            char c = s.charAt(d);
            if(c=='\n') {
                i = x;
                j++;
            }
            else if(Character.isDigit(c)) {
                int p = Integer.parseInt(""+c);
                _p.setCell(i, j, k, p);
            }
            i++;
        }
        return this;
    }

    public Painter dot(int c, int x, int y) {
        return dot(c, x, y, 0);
    }

    public Painter dot(int c, int x, int y, int z) {
        _p.setCell(x, y, z, c);
        return this;
    }

    public Painter line(int c, List<Integer> s, List<Integer> t) {
        return line(c, prim(s), prim(t));
    }

    public Painter line(int c, int[] s, int[] t) {
        if(s.length==1) {
            return line1d(c, s, t);
        }
        else if(s.length==2) {
            return line2d(c, s, t);
        }
        else {
            return line3d(c, s, t);
        }
    }

    public Painter line1d(int c, int[] s, int[] t) {
        throw new UnsupportedOperationException();
    }

    public Painter line2d(int c, int[] s, int[] t) {
        if(s[0]==t[0]) {
            int d = s[1]<t[1]?1:-1;
            for(int j=s[1];j!=t[1]+d;j+=d) {
                _p.setCell(s[0], j, c);
            }
        }
        else if(s[1]==t[1]) {
            int d = s[0]<t[0]?1:-1;
            for(int i=s[0];i!=t[0]+d;i+=d) {
                _p.setCell(i, s[1], c);
            }
        }
        else {
            throw new UnsupportedOperationException("diagonal line");
        }
        return this;
    }

    public Painter line3d(int c, int[] s, int[] t) {
        throw new UnsupportedOperationException();
    }

    private static int[] prim(List<Integer> l) {
        int[] p = new int[l.size()];
        for(int i=0;i<l.size();i++) {
            p[i] = l.get(i);
        }
        return p;
    }
}
