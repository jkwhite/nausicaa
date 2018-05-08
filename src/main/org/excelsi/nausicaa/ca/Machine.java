package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Machine {
    private final Archetype _a;
    private final Datamap _d;
    private final Genome _g;
    private final Codon[] _prg;
    private final int[] _inst;
    private final Tape _t;


    public Machine(Archetype a, Datamap d, Genome g) {
        _a = a;
        _d = d;
        _g = g;
        _prg = g.codons(new Implicate(a, d));
        _inst = new int[_prg.length];
        _t = new Tape(32768);
    }

    public Machine copy() {
        return new Machine(_a, _d, _g);
    }

    public int compute(final int[] p) {
        _t.reset();
        for(int i=0;i<_prg.length;i++) {
            //long st = System.currentTimeMillis();
            _prg[i].op(p, _t);
            //long en = System.currentTimeMillis();
            //if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
            //_inst[i] += en-st;
            if(_t.stopped()) break;
        }
        int res = _t.pop();
        //if(res<0) res=-res;
        res = res % _a.colors();
        if(res<0) {
            res = _a.colors()+res;
        }
        return (int) res;
    }

    public Machine mutate(Archetype a, GenomeFactory gf, MutationFactor m) {
        return new Machine(_a, _d, _g.mutate(new Implicate(_a, _d), gf, m));
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
