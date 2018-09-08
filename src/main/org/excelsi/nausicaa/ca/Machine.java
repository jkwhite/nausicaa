package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Machine {
    private final Archetype _a;
    private final Datamap _d;
    private final Genome _g;
    private final Codon[] _prg;
    private final int[] _inst;
    private final IntTape _ti;
    private final FloatTape _tf;


    public Machine(Archetype a, Datamap d, Genome g) {
        _a = a;
        _d = d;
        _g = g;
        _prg = g.codons(new Implicate(a, d));
        _inst = new int[_prg.length];
        if(a.isDiscrete()) {
            _ti = new IntTape(32768);
            _tf = null;
        }
        else {
            _tf = new FloatTape(32768);
            _ti = null;
        }
    }

    public Machine copy(Implicate im) {
        return new Machine(_a, im.datamap(), _g);
    }

    //public int compute(final int[] p) {
    //}

    public void compute(final IO io) {
        if(io.v==Values.discrete) {
            _ti.reset();
            final int[] p = io.ii;
            for(int i=0;i<_prg.length;i++) {
                //long st = System.currentTimeMillis();
                _prg[i].op(p, _ti);
                //long en = System.currentTimeMillis();
                //if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
                //_inst[i] += en-st;
                if(_ti.stopped()) break;
            }
            int res = _ti.pop();
            //if(res<0) res=-res;
            res = res % _a.colors();
            if(res<0) {
                res = _a.colors()+res;
            }
            io.io = res;
        }
        else {
            _tf.reset();
            final float[] p = io.fi;
            for(int i=0;i<_prg.length;i++) {
                //long st = System.currentTimeMillis();
                _prg[i].op(p, _tf);
                //long en = System.currentTimeMillis();
                //if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
                //_inst[i] += en-st;
                if(_tf.stopped()) break;
            }
            float res = _tf.pop();
            //if(res<0) res=-res;
            res = res % (_a.colors()-1);
            if(res<0) {
                res = _a.colors()+res-1;
            }
            io.fo = res;
        }
    }

    //public Machine mutate(Archetype a, GenomeFactory gf, MutationFactor m) {
    public Machine mutate(Implicate im, GenomeFactory gf, MutationFactor m) {
        //return new Machine(_a, _d, _g.mutate(new Implicate(_a, _d), gf, m));
        return new Machine(_a, im.datamap(), _g.mutate(im, gf, m));
    }

    public void tick() {
        for(int i=0;i<_prg.length;i++) {
            _prg[i].tick();
            //_inst[i] = 0;
        }
    }

    @Override public String toString() {
        return _g.toString();
    }

    public void dump() {
        for(int i=0;i<_prg.length;i++) {
            System.err.print(_prg[i].code()+"="+_inst[i]+", ");
        }
        System.err.println();
    }

    private static void d(String s, Object... args) {
        //System.err.println(String.format(s, args));
    }
}
