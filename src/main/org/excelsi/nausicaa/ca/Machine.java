package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Machine {
    private final Archetype _a;
    private final Genome _g;
    private final Codon[] _prg;
    private final Tape _t;


    public Machine(Archetype a, Genome g) {
        _a = a;
        _g = g;
        _prg = g.codons(a);
        _t = new Tape(4096);
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
            //d(_prg[i]+" "+_t);
        }
        int res = _t.pop();
        res = res % _a.colors();
        if(res<0) {
            res = 0;
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
        }
    }

    @Override public String toString() {
        return _g.toString();
    }

    private static void d(String s, Object... args) {
        //System.err.println(String.format(s, args));
    }
}
