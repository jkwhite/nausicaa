package org.excelsi.nausicaa.ca;


public final class IntTape {
    //private int dump = 0;
    private final int[] _t;
    private int _i = -1;
    private int _s = -1;
    private boolean _a;
    private boolean _stop;


    public IntTape(int len) {
        _t = new int[len];
    }

    public void reset() {
        //d("reset");
        _i = -1;
        _s = -1;
        _a = false;
        _stop = false;
    }

    public void selectAgg(int n) {
        select(n, true, false);
    }

    public void selectAggAll() {
        select(-1, true, true);
    }

    public void selectIdx(int n) {
        select(n, false, false);
    }

    public void selectIdxAll() {
        select(-1, false, true);
    }

    public void select(int n, boolean a, boolean all) {
        //++dump;
        if(all) {
            n = -1;
        }
        else if(n>=_t.length) {
            n = n%_t.length;
        }
        else if(n<0) {
            n = (-n)%_t.length;
        }
        //System.err.println("agg 
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

    public int pos() {
        return _i;
    }

    public int pushAll(int[] v, int c) {
        int m = Math.min(v.length, c);
        if(_i+1+m>=_t.length) {
            m = _t.length-(_i+1);
        }
        System.arraycopy(v, 0, _t, _i+1, m);
        _i+=m;
        return m;
    }

    public int pushAll(int[] v, int c, int offset) {
        int m = Math.min(v.length-offset, c);
        System.arraycopy(v, offset, _t, _i+1, m);
        _i+=m;
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
        if(c>0) {
            _i -= c;
            if(_i<-1) {
                _i = -1;
            }
        }
        //for(int i=0;i<c;i++) {
            //pop();
        //}
    }

    public void stop() {
        _stop = true;
    }

    public boolean stopped() {
        return _stop;
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
