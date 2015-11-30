package org.excelsi.nausicaa.ca;


import gnu.trove.*;


public class TroveSelector implements Selector {
    private final TLongByteHashMap _p = new TLongByteHashMap();
    private final int[] _colors;
    //private final int[][] _pat;
    private final int _len;


    public TroveSelector(int[][] patterns, int[] colors) {
        _len = patterns[0].length-1;
        _colors = colors;
        //_pat = (int[][]) Rule1D.deepCopy(patterns);
        for(int i=0;i<patterns.length;i++) {
            int[] p = patterns[i];
            long key = key(p);
            //System.err.println("key: "+java.util.Arrays.toString(p)+" => "+key);
            //_p.put(new Key(p[i]), p[p.length-1]);
            if(_p.containsKey(key)) {
                throw new IllegalStateException("duplicate key "+key);
            }
            _p.put(key, colorIndexOf(p[p.length-1]));
            //_p.put(key(p), p[p.length-1]);
        }
    }

    public int next(int[] input, int offset) {
        //_lookup.set(input);
        long k = inputKey(input, offset);
        if(_p.containsKey(k)) {
            int c = _p.get(k);
            return c;
        }
        return -1;
        //return c==0?-1:c;
    }

    public int next(long k) {
        if(_p.containsKey(k)) {
            int c = _p.get(k);
            return c;
        }
        return -1;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        //for(int[] pat:_pat) {
            //b.append(java.util.Arrays.toString(pat));
            //b.append("; ");
        //}
        return b.toString();
    }

    public void longFind(int[] pat) {
        /*
        for(int[] p:_pat) {
            boolean found = true;
            for(int i=0;i<pat.length;i++) {
                if(p[i]!=pat[i]) {
                    found = false;
                    break;
                }
            }
            if(found) {
                System.err.println("found pattern");
                System.err.println("key: "+key(p));
                System.err.println("inputkey: "+inputKey(pat, 0));
                System.err.println("call: "+_p.get(inputKey(pat, 0)));
                System.err.println("keys: "+java.util.Arrays.toString(_p.keys()));
                System.err.println("colors: "+java.util.Arrays.toString(_colors));
                return;
            }
        }
        System.err.println("no match");
        */
    }

    private long key(int[] p) {
        return key(p, 0);
    }

    private static long key(int[] p, int offset) {
        long key = 0;
        for(int i=0;i<p.length-1;i++) {
            key = compute(key, i, p[i+offset]);
            //key += (1000000000L+i)*p[i+offset];
            //key ^= (key<<<i)+p[i*offset];
        }
        return key;
    }

    private static final long MAX = Integer.MAX_VALUE;
    private static long compute(long key, int idx, int val) {
        //return key+(1000000000L+idx)*val;
        //System.err.println("key="+key+", idx="+idx+", val="+val);
        long v = val;
        long i = idx;
        //return key+MAX*i+v+MAX/2L;
        return key ^ (Long.rotateLeft((7*val)&Colors.COLOR_MASK, 3*idx));
    }

    public long inputKey(int[][] ps, int offset) {
        long key = 0;
        int off = 0;
        for(int j=0;j<ps.length;j++) {
            int[] p = ps[j];
            for(int i=0;i<3;i++) {
                key = compute(key, off++, p[i+offset]);
            }
        }
        return key;
    }

    public long inputKey(int[] p, int offset) {
        try {
            long key = 0;
            for(int i=0;i<_len;i++) {
                key = compute(key, i, p[i+offset]);
            }
            return key;
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("short p: p.length="+p.length+", offset="+offset+", _len="+_len);
        }
    }

    private byte colorIndexOf(int color) {
        for(int i=0;i<_colors.length;i++) {
            if(_colors[i]==color) {
                return (byte) i;
            }
        }
        return (byte) -1;
    }

    private static final class Key {
        int[] _p;
        int _h;

        public Key(int[] pat) {
            _p = pat;
            for(int i=0;i<_p.length-1;i++) {
                _h ^= _p[i];
            }
        }

        public boolean equals(Object o) {
            Key k = (Key) o;
            for(int i=0;i<_p.length;i++) {
                if(_p[i]!=k._p[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
