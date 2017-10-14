package org.excelsi.nausicaa.ca;


public final class Tape {
    private final int[] _t;
    private int _i = -1;
    private int _s = -1;
    private boolean _a;


    public Tape(int len) {
        _t = new int[len];
    }

    public void reset() {
        //d("reset");
        _i = -1;
        _s = -1;
        _a = false;
    }

    public void selectAgg(int n) {
        select(n, true);
    }

    public void selectIdx(int n) {
        select(n, false);
    }

    public void select(int n, boolean a) {
        if(_i==-1||n==0) {
            _s = -1;
        }
        else if(n==-1) {
            _s = 0;
        }
        else {
            _s = 1 + _i - n;
            if(_s<0) {
                _s = 0;
            }
        }
        _a = a;
    }

    public void apply(TapeOp op, int[] p) {
        //System.err.print("s="+_s+", i="+_i);
        if(_s>=0&&_i>=0) {
            int v = op.op(_t, _s, _i, p);
            //System.err.println(", v="+v);
            if(_a) {
                _i = _s;
                _t[_i] = v;
            }
            else {
                if(v>=_s-1&&v<=_i) {
                    _i = v;
                }
                else {
                    _s = -1;
                    throw new IllegalArgumentException("v "+v+" not in ("+_s+","+_i+")");
                }
            }
            _s = -1;
        }
        //else System.err.println();
    }

    public void push(int v) {
        if(_i<_t.length-1) {
            //d("push %d", v);
            _t[++_i] = v;
        }
        //else {
            //d("push end");
        //}
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
        if(ret>r.length) {
            ret = r.length;
        }
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

    public void op(final TapeOp op, final int[] p) {
        int ni = op.op(_t, 0, _i, p);
        _i = ni;
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

    public interface TapeOp {
        int op(int[] t, int s, int e, int[] p);
    }

    private static void d(String s, Object... args) {
        System.err.println(String.format(s, args));
    }
}
