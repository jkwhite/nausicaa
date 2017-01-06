package org.excelsi.nausicaa.ca;


public class Machine {
    private final Genome _g;
    private final Op[] _prg;
    private final Tape _t;


    public Machine(Genome g) {
        _g = g;
        _prg = g.ops();
        _t = new Tape(128);
    }

    public Machine copy() {
        return new Machine(_g);
    }

    public byte compute(final byte[] p) {
        _t.reset();
        //d("===============");
        //d("init: "+_t);
        for(int i=0;i<_prg.length;i++) {
            _prg[i].op(p, _t);
            //d(_prg[i]+" "+_t);
        }
        int res = _t.pop();
        //d("res: "+res);
        //d("===============");
        return (byte) res;
    }

    @Override public String toString() {
        return _g.toString();
    }

    private static void d(String s, Object... args) {
        //System.err.println(String.format(s, args));
    }
}
