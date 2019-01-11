package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Machine {
    private static final int TAPE_LENGTH = 65535*2;
    private final Archetype _a;
    private final Datamap _d;
    private final Language _lang;
    private final Genome _g;
    private final Codon[] _prg;
    private final int[] _inst;
    private final IntTape _ti;
    private final FloatTape _tf;
    private long _e = 0;


    public Machine(Implicate im, Genome g) {
        _a = im.archetype();
        _d = im.datamap();
        _lang = im.language();
        _g = g;
        _prg = g.codons(im);
        _inst = new int[_prg.length];
        if(_a.isDiscrete()) {
            _ti = new IntTape(TAPE_LENGTH);
            _tf = null;
        }
        else {
            _tf = new FloatTape(TAPE_LENGTH);
            _ti = null;
        }
    }

    public boolean isDeterministic() {
        for(Codon c:_prg) {
            if(!c.deterministic()) {
                return false;
            }
        }
        return true;
    }

    public Machine copy(Datamap dm) {
        return new Machine(new Implicate(_a, dm, _lang), _g);
    }

    //public int compute(final int[] p) {
    //}

    // WEIGHT IS ALPHA
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
                if(_ti.jumped()!=0) {
                    int j = _ti.jumped() % _prg.length;
                    if(j<0) j=0; //throw new IllegalStateException("negative jump: "+j);
                    i += j;
                    //System.err.println("jumped by "+j);
                    //if(i<_prg.length-1) System.err.println("next inst: "+_prg[i+1]);
                    //else System.err.println("end of the line");
                    _ti.jump(0);
                }
            }
            int res = _ti.pop();
            //if(res<0) res=-res;
            res = res % _a.colors();
            if(res<0) {
                res = _a.colors()+res;
            }
            io.io = res;
            if(_ti.pos()>0) _e += _ti.pos();
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
                if(_tf.jumped()!=0) {
                    int j = _tf.jumped() % _prg.length;
                    if(j<0) j=0; //throw new IllegalStateException("negative jump: "+j);
                    i += j;
                    //System.err.println("jumped by "+j);
                    _tf.jump(0);
                }
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
        return new Machine(im, _g.mutate(im, gf, m));
    }

    public void tick() {
        for(int i=0;i<_prg.length;i++) {
            _prg[i].tick();
            //_inst[i] = 0;
        }
        //if(_e>0) System.err.println("energy: "+_e);
        _e = 0;
        if(_ti!=null) _ti.reset();
        if(_tf!=null) _tf.reset();
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
