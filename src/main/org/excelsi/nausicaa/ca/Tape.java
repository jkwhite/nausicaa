package org.excelsi.nausicaa.ca;


public final class Tape {
    private final int[] _t;
    private int _i = -1;


    public Tape(int len) {
        _t = new int[len];
    }

    public void reset() {
        //d("reset");
        _i = -1;
    }

    public void push(int v) {
        if(_i<_t.length-1) {
            //d("push %d", v);
            _t[++_i] = v;
        }
        else {
            //d("push end");
        }
    }

    public int pop() {
        if(_i>=0) {
            int r = _t[_i];
            _i--;
            //d("pop %d", r);
            return r;
        }
        //d("pop end");
        return 0;
    }

    public int peek() {
        if(_i>=0) {
            return _t[_i];
        }
        return 0;
    }

    public int pushAll(int[] v, int c) {
        int m = Math.min(v.length, c);
        for(int i=0;i<m;i++) {
            push(v[i]);
        }
        return m;
    }

    public int popAll(int[] r, int c) {
        if(c==-1) {
            c = _i+1;
        }
        int ret = Math.min(_i+1,c);
        System.arraycopy(_t, _i+1-ret, r, 0, ret);
        //_i = -1;
        _i = Math.max(-1,_i-c);
        //d("popAll %d elems", ret);
        return ret;
    }

    public void skip(int c) {
        for(int i=0;i<c;i++) {
            pop();
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder("tape {pos:"+_i+", tape:[");
        for(int i=0;i<=_i;i++) {
            b.append(_t[i]).append(", ");
        }
        b.setLength(b.length()-1);
        b.append("]}");
        return b.toString();
    }

    private static void d(String s, Object... args) {
        //System.err.println(String.format(s, args));
    }
}
