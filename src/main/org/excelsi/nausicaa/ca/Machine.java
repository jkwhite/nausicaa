package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Machine implements Humanizable {
    private static final Logger LOG = LoggerFactory.getLogger(Machine.class);
    private static final int TAPE_LENGTH = 65535*2;
    private static final int MAX_INSTRUCTIONS = 1000;
    private final Archetype _a;
    private final Datamap _d;
    private final Language _lang;
    private final Varmap _vars;
    private final Genome _g;
    private final Codon[] _prg;
    private final int[] _inst;
    private final IntTape _ti;
    private final FloatTape _tf;
    private final boolean _trace;
    private long _computeCount = 0;
    private long _e = 0;


    public Machine(Implicate im, Genome g) {
        this(im, g, false);
    }

    public Machine(Implicate im, Genome g, boolean trace) {
        _a = im.archetype();
        _d = im.datamap();
        _lang = im.language();
        _vars = im.vars();
        _g = g;
        _trace = trace;
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
        if(_trace) System.err.println("Machine: "+Arrays.toString(_prg));
    }

    public boolean isDeterministic() {
        for(Codon c:_prg) {
            if(!c.deterministic()) {
                return false;
            }
        }
        return true;
    }

    public boolean usesSource() {
        for(Codon c:_prg) {
            if(c.usesPattern()) {
                return true;
            }
        }
        return false;
    }

    public boolean usesContext() {
        for(Codon c:_prg) {
            if(c.usesContext()) {
                return true;
            }
        }
        return false;
    }

    public Machine copy(Datamap dm) {
        return new Machine(new Implicate(_a, dm, _lang, _vars), _g, _trace);
    }

    //public int compute(final int[] p) {
    //}

    // WEIGHT IS ALPHA
    public void compute(final IO io) {
        int execCount = 0;
        final Pattern.Ctx ctx = io.ctx;
        if(io.v==Values.discrete) {
            _ti.reset();
            final int[] p = io.ii;
            for(int i=0;i<_prg.length;i++) {
                //long st = System.currentTimeMillis();
                if(++execCount>MAX_INSTRUCTIONS) {
                    if(++_computeCount%10000==0) LOG.warn("******* EXEC LIMIT BREAK *******");
                    break;
                }
                if(_trace) {
                    System.err.println("Tape: "+_ti);
                    System.err.println("Codon: "+_prg[i]);
                }
                _prg[i].op(p, _ti, ctx);
                //long en = System.currentTimeMillis();
                //if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
                //_inst[i] += en-st;
                if(_ti.stopped()) break;
                if(_ti.jumped()!=0) {
                    int j = _ti.jumped() % _prg.length;
                    //if(j<0) j=0; //throw new IllegalStateException("negative jump: "+j);
                    i += j;
                    if(i<0) i=0;
                    //System.err.println("jumped by "+j);
                    //if(i<_prg.length-1) System.err.println("next inst: "+_prg[i+1]);
                    //else System.err.println("end of the line");
                    _ti.jump(0);
                }
            }

            /*
300/0.21948378900708806:kya go+chi9 su kya0 o4 go ra ya ka no do go+do+do+ja+tsu+gi+za+ni+ta ra ki+mu9 ma ge
200/0.2706857701305492:go ho jya2 ja a4 jya mo ka ku hi mo ka pa o4 yu
200/0.8103341563073866:go kya1 a14 ho gi go pe su su ne ru e wa
500/0.18948378900708806:kya go+chi9 su kya0 o4 go ra ya ka no do go+do+do+ja+tsu+gi+za+ni+ta ra ki+mu9 ma ge
400/0.4706857701305492:go ho jya2 ja a4 jya mo ka ku hi mo ka pa o4 yu
400/0.6103341563073866:go kya1 a14 ho gi go pe su su ne ru e wa
200/0.2694837890070881:kya go+chi9 su kya0 o4 go ra ya ka no do go+do+do+ja+tsu+gi+za+ni+ta ra ki+mu9 ma ge
80/0.1307085465522224:to pa+ta5 kya2 kya0 pa+ta5 su ki+ta9 ge a6.598080158233643 ro to sa o0 to ya ji gi
200/0.1126351711549516:kya1 to gi go+do+do+ja+tsu+gi+za+ni+ta pe bo ne za za re jo mi6 sa ge chi ge chi mi ge a3.7314462661743164 ki+gi9 ku ro1 u jo za
800/0.0694837890070881:kya go+chi9 su kya0 o4 go ra ya ka no do go+do+do+ja+tsu+gi+za+ni+ta ra ki+mu9 ma ge
300/1.0:pa pa ji re pa+chi5 go+mu9 hi to to to u kya0 ni ni ji ka
300/0.04589037605947676:go go+do+do+ja+tsu+gi+za+ni+ta mo na ri ki bi ja tsu jya go ga jya ra ki chi sa
100/0.05:go e jya0 ni wa ne shi pa+chi5 jya ji re e gi kya1 ki+mu9
200/0.4:ra ki+mi9 hu ro2 pe ro7 mi2 e go chi go+mi9 ya go+gi9
200/0.7:go jo he ge sa
*/
            //89:wa ta na na go ge no kya1 kya0 wo hu hu ga ke gu kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni e kya0 ke tsu he ka ki re ko ko kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni do jya2 jya1 chi gi4 gi4 gu hu gi2 hu kya1 a24 a14 o4 no sa do re o6 mi mi jya2 kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni ru be ku kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni hu ta sa wa tsu kya1 ku wa u wo wa wo ya u o4 na kya1 ho kya1 pa hu ji u ku ta kya kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni no u kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni ta ge kya0+kya0+mu2+kya1+kya1+mu2+mi2+ni pa ru ku hu ma wo ku ku ku ta mo hi yu o4 ke ni u u tsu wa wa su re po ho ho go ki ge wa za i kya2 ni ku jya1 u ra ke to gu ma hi ko za go o3 o4 u ro8 kya2 bi e u ke kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni mi go ki su ge a14 pe o4 e nu su e bi kya0+kya0+mi2+kya1+kya1+mi2+mi2+ni ku ke mi hu wo ri po re o4 be do bu ku sa kya a88.73789620399475 kya1 o0 mi do o5 e me hu o3 kya1 chi wo ho ta o2 u i gu su ru go wo po a26.38485622406006 ya tsu tsu bu gi o2 ke jya me ya ri tsu a23 de o0 bu
            if(_trace) System.err.println("Final Tape: "+_ti);
            int res = _ti.pop();
            //if(res<0) res=-res;
            res = res % _a.colors();
            if(res<0) {
                res = _a.colors()+res;
            }
            if(_trace) System.err.println("Final: "+res);
            io.io = res;
            if(_ti.pos()>0) _e += _ti.pos();
        }
        else {
            _tf.reset();
            final double[] p = io.fi;
            for(int i=0;i<_prg.length;i++) {
                //long st = System.currentTimeMillis();
                if(++execCount>MAX_INSTRUCTIONS) {
                    if(++_computeCount%1000==0) LOG.warn("******* EXEC LIMIT BREAK *******");
                    break;
                }
                if(_trace) {
                    System.err.println("Tape: "+_tf);
                    System.err.println("Codon: "+_prg[i]);
                }
                _prg[i].op(p, _tf, ctx);
                //long en = System.currentTimeMillis();
                //if(en-st>10) System.err.println("too long: "+(en-st)+" "+_prg[i]);
                //_inst[i] += en-st;
                if(_tf.stopped()) break;
                if(_tf.jumped()!=0) {
                    int j = _tf.jumped() % _prg.length;
                    //if(j<0) j=0; //throw new IllegalStateException("negative jump: "+j);
                    i += j;
                    if(i<0) i=0;
                    //System.err.println("jumped by "+j);
                    _tf.jump(0);
                }
            }
            if(_trace) System.err.println("Final Tape: "+_tf);
            double res = _tf.pop();
            if(res<0) res = -res;
            if(res>_a.colors()-1d) {
                res = res % (_a.colors()-1);
            }
            /*
            res = res % (_a.colors()-1);
            if(res<0) {
                res = _a.colors()+res-1;
            }
            */
            if(_trace) System.err.println("Final: "+res);
            io.fo = res;
        }
    }

    public Machine mutate(Implicate im, GenomeFactory gf, MutationFactor m) {
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

    @Override public String humanize() {
        return toString()+" ["+_prg.length+"]";
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
