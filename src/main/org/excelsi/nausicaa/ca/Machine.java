package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Machine {
    private final Archetype _a;
    private final Genome _g;
    private final Codon[] _prg;
    private final int[] _inst;
    private final Tape _t;


    public Machine(Archetype a, Genome g) {
        _a = a;
        _g = g;
        _prg = g.codons(a);
        _inst = new int[_prg.length];
        _t = new Tape(32768);
    }

    public Machine copy() {
        return new Machine(_a, _g);
    }

    public int compute(final int[] p) {
        _t.reset();
        //d("===============");
        //d("init: "+_t);
        for(int i=0;i<_prg.length;i++) {
            long st = System.currentTimeMillis();
            _prg[i].op(p, _t);
            long en = System.currentTimeMillis();
            if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
            _inst[i] += en-st;
            //d(_prg[i]+" "+_t);
        }
        int res = _t.pop();
        res = res % _a.colors();
        if(res<0) {
            //res = 0;
            //System.err.print(res+" => ");
            res = _a.colors()+res;
            //System.err.println(res);
        }
        //if(res>=_a.colors()) {
            //res = _a.colors()-1;
        //}
        //d("res: "+res);
        //d("===============");
        return (int) res;
    }

    public Machine mutate(Archetype a, GenomeFactory gf, Random r) {
        return new Machine(_a, _g.mutate(_a, gf, r));
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
