package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.Arrays;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TDoubleIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.list.linked.TDoubleLinkedList;
import gnu.trove.procedure.TDoubleIntProcedure;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Codons {
    private static final Logger LOG = LoggerFactory.getLogger(Codons.class);
    private static final int BUF_SIZE = 32768;

    public static final String CONS = "a";
    public static final String POW = "i";
    public static final String INTERSECT = "u";
    public static final String AVG_N = "e";
    public static final String PUSH = "o";
    public static final String OR = "ka";
    public static final String PUSH_SURROUND = "ki";
    public static final String ROT_RIGHT = "ku";
    public static final String GREATER = "ke";
    public static final String ROT_LEFT = "ko";
    public static final String EQUAL = "ma";
    public static final String SUM = "mi";
    public static final String MULTIPLY = "mu";
    public static final String SUM_N = "me";
    public static final String MOD = "mo";
    public static final String MAX = "ta";
    public static final String MIN = "chi";
    public static final String XOR = "tsu";
    public static final String TIME = "te";
    public static final String AND = "to";
    public static final String LESSER = "na";
    public static final String SQRT = "ni";
    public static final String CBRT = "nu";
    public static final String NOT_EQUAL = "ne";
    public static final String PUSH_N = "no";
    public static final String PUSH_S = "ya";
    public static final String FORK = "yu";
    public static final String INTERSECT_S = "yo";
    public static final String STOP = "sa";
    public static final String POS = "shi";
    public static final String SUBTRACT = "su";
    public static final String NOT = "se";
    public static final String NOT_INTERSECT = "so";
    public static final String ABORT = "ga";
    public static final String AVG = "gi";
    public static final String COUNT = "gu";
    public static final String COUNT_FIXED = "pu";
    public static final String PUSH_ALL_ROT = "ge";
    public static final String PUSH_ALL = "go";
    public static final String JUMP = "ja";
    public static final String SKIP_N = "ji";
    public static final String MIN_N = "jo";
    public static final String IF = "ra";
    public static final String MAX_N = "ri";
    public static final String SIGMOID = "ru";
    public static final String SIN = "re";
    public static final String SKIP = "ro";
    public static final String HALT = "za";
    public static final String NON_ZERO = "zu";
    public static final String RAND = "ze";
    public static final String DATA = "da";
    public static final String TANH = "de";
    public static final String DUPLICATE = "do";
    public static final String EXCLAMATORY = "ha";
    public static final String HISTO = "hi";
    public static final String SUPERSYMMETRY = "hu";
    public static final String CONVOLVE = "he";
    public static final String DIVIDE = "ho";
    public static final String ROT_VEC_N = "ba";
    public static final String COS = "bi";
    public static final String LT = "bu";
    public static final String GT = "be";
    public static final String NEGATE = "bo";
    public static final String PUSH_CARDINAL = "pa";
    public static final String FILTER = "pi";
    public static final String ABS = "pe";
    public static final String EQUAL_A = "po";
    public static final String MOST = "wa";
    public static final String LEAST = "wo";
    public static final String COORD = "kya";
    public static final String COORD_REL = "jya";
    public static final String COORD_CIRC = "ryu";
    public static final String COORD_CIRC_REL = "ryo";
    public static final String MANDELBROT = "nya";
    public static final String MANDELBULB = "mya";
    public static final String BANDPASS = "cho";
    public static final String LIFE = "life";
    public static final String HODGE = "hodge";
    public static final String SLIME = "slime";


    public static Codon codon(final String s, final Implicate im) {
        //LOG.info("parsing codon '"+s+"'");
        if(s.indexOf('+')>0) {
            String[] phon = s.split("\\+");
            Codon[] cs = new Codon[phon.length];
            for(int i=0;i<phon.length;i++) {
                cs[i] = codon(phon[i], im);
            }
            return new Chain(cs);
        }
        else {
            if(Varmap.containsVar(s)) {
                return new Placeholder(s);
            }
            Language lang = im.language();
            if(lang==null) {
                lang = Languages.universal();
            }
            //String[] phon = lang.phonemes(s);
            String[] phon = new String[]{s};
            if(phon.length==1) {
                return universalCodon(phon[0], im);
            }
            else {
                Codon[] cs = new Codon[phon.length];
                for(int i=0;i<phon.length;i++) {
                    cs[i] = universalCodon(phon[i], im);
                }
                return new Chain(cs);
            }
        }
    }

    public static Codon universalCodon(final String s, final Implicate im) {
        //System.err.println("op '"+s+"'");
        int i=0;
        for(;i<s.length()&&Character.isAlphabetic(s.charAt(i));i++);
        final String code = s.substring(0,i);
        //System.err.println("code '"+code+"'");
        Integer p=-1;
        Double pf=-1d;
        if(i<s.length()) {
            if(s.substring(i).indexOf('.')>-1) {
                pf = Double.parseDouble(s.substring(i));
                p = pf.intValue();
            }
            else {
                p = Integer.parseInt(s.substring(i));
                pf = p.doubleValue();
            }
        }
        //System.err.println("arg '"+p+"'");
        if(code.startsWith(DATA)) {
            final String name = code.substring(DATA.length());
            return new Data(name, im.datamap().find(name));
        }
        else {
            switch(code) {
                case SUM:
                    return p==-1?new Sum():new Sumn(p);
                case SUM_N:
                    return new SumnN();
                case PUSH:
                    return new Push(p, im.archetype().sourceLength());
                case PUSH_N:
                    return new PushN();
                case PUSH_SURROUND:
                    return new PushO();
                case PUSH_S:
                    return new PushS();
                case MOD:
                    return new Mod();
                case PUSH_CARDINAL:
                    return new PushC();
                case PUSH_ALL:
                    return new PushA();
                case PUSH_ALL_ROT:
                    return new PushARot();
                case INTERSECT:
                    return new Intersects();
                case NOT_INTERSECT:
                    return new NotIntersects();
                case INTERSECT_S:
                    return new IntersectsSelf();
                case CONS:
                    return new Constant(p,pf);
                case EQUAL:
                    return new Equals();
                case EQUAL_A:
                    return new EqualsA(p);
                case NOT_EQUAL:
                    return new NotEquals();
                case IF:
                    return new If();
                case SUBTRACT:
                    return new Subtract();
                case MULTIPLY:
                    return p==-1?new Multiply():new Muln(p);
                case DIVIDE:
                    return new Divide();
                case POW:
                    return new Pow();
                case MIN:
                    return new Min(p);
                case MAX:
                    return new Max(p);
                case AVG:
                    return new Avg(p);
                case MIN_N:
                    return new MinN();
                case MAX_N:
                    return new MaxN();
                case AVG_N:
                    return new AvgN();
                case XOR:
                    return new Xor();
                case AND:
                    return new And();
                case OR:
                    return new Or();
                case ROT_RIGHT:
                    return new Rotright();
                case ROT_LEFT:
                    return new Rotleft();
                case SKIP:
                    return new Skip(p);
                case SKIP_N:
                    return new SkipN();
                case NON_ZERO:
                    return new Nonzero(p);
                case TIME:
                    return new Time();
                case HISTO:
                    //return new Histo(im.archetype().colors());
                    return im.archetype().isContinuous()
                        ? new HistoTroveFloat()
                        : new HistoTroveInt();
                case MOST:
                    return new Most(p);
                case LEAST:
                    return new Least(p);
                case DUPLICATE:
                    return new Duplicate();
                case EXCLAMATORY:
                    return new Exclamatory();
                case SUPERSYMMETRY:
                    return new Supersymmetry(im.archetype().colors()-1);
                case ROT_VEC_N:
                    return new RotVecN(im.archetype().sourceLength());
                case GT:
                    return new GreaterThan();
                case LT:
                    return new LessThan();
                case NEGATE:
                    return new Negate();
                case LESSER:
                    return new Lesser();
                case GREATER:
                    return new Greater();
                case SQRT:
                    return new Sqrt();
                case CBRT:
                    return new Cbrt();
                case FORK:
                    return new Fork(p, 10, im.archetype().colors()-1);
                case STOP:
                    return new Stop();
                case ABORT:
                    return new Abort();
                case POS:
                    return new Pos();
                case NOT:
                    return new Not();
                case FILTER:
                    return new Filter(p);
                case SIGMOID:
                    return new Sigmoid();
                case COS:
                    return new Cos();
                case SIN:
                    return new Sin();
                case TANH:
                    return new Tanh();
                case COUNT:
                    return new Count();
                case COUNT_FIXED:
                    return new CountFixed();
                case JUMP:
                    return new Jump();
                case HALT:
                    return new Halt();
                case CONVOLVE:
                    return new Convolve(im.archetype().sourceLength());
                case RAND:
                    return new Rand();
                case COORD:
                    return new Coord(p);
                case COORD_REL:
                    return new CoordRel(p);
                //case COORD_CIRC:
                    //return new CoordCirc(p);
                //case COORD_CIRC_REL:
                    //return new CoordCircRel(p);
                case MANDELBROT:
                    return new Mandelbrot();
                case MANDELBULB:
                    return new Mandelbulb();
                case BANDPASS:
                    return new Bandpass();
                case LIFE:
                    return new Life();
                case HODGE:
                    return new Hodge(im.archetype().colors());
                case SLIME:
                    return new Slime();
                case ABS:
                    return new Abs();
                default:
                    throw new IllegalStateException("unknown opcode '"+code+"'");
            }
        }
    }

    public static final class Nonzero implements Codon, IntTape.TapeOp, FloatTape.TapeOp, Unstable {
        private final int[] _t = new int[BUF_SIZE];
        private final int _c;

        public Nonzero(int c) {
            _c = c;
        }

        @Override public Codon copy() {
            return new Nonzero(_c);
        }

        @Override public String code() {
            return _c==-1?NON_ZERO:(NON_ZERO+_c);
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            if(_c==-1) {
                t.selectIdxAll();
            }
            else {
                t.selectIdx(_c);
            }
            t.apply(this, p);
        }

        @Override public int op(int[] t, int st, int en, int[] p) {
            int j=st-1;
            for(int i=st;i<=en;i++) {
                if(t[i]>0) {
                    t[++j] = t[i];
                    //System.err.println("i="+i+", ti="+t[i]+", j="+j);
                }
            }
            return j;
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            if(_c==-1) {
                t.selectIdxAll();
            }
            else {
                t.selectIdx(_c);
            }
            t.apply(this, p);
        }

        @Override public double op(double[] t, int st, int en, double[] p) {
            int j=st-1;
            for(int i=st;i<=en;i++) {
                if(t[i]>0) {
                    t[++j] = t[i];
                    //System.err.println("i="+i+", ti="+t[i]+", j="+j);
                }
            }
            return j;
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public Codon destabilize(Random r) {
            if(_c==-1) {
                return new Nonzero(1+r.nextInt(9));
            }
            else {
                int nc = _c+(r.nextInt(10)-4);
                if(nc<=0) {
                    nc = -1;
                }
                return new Nonzero(nc);
            }
        }
    }

    public static final class Most extends NAggregate {
        private final TDoubleIntHashMap _mf;
        private final TIntIntHashMap _mi;
        private final TDoubleIntProcedure _procf = new TDoubleIntProcedure() {
            @Override public boolean execute(double k, int v) {
                if(v>_mostValueF) {
                    _mostKeyF = k;
                    _mostValueF = v;
                }
                return true;
            }
        };
        private final TIntIntProcedure _proci = new TIntIntProcedure() {
            @Override public boolean execute(int k, int v) {
                if(v>_mostValueI) {
                    _mostKeyI = k;
                    _mostValueI = v;
                }
                return true;
            }
        };
        private double _mostKeyF;
        private int _mostValueF;
        private int _mostKeyI;
        private int _mostValueI;

        public Most(int c) {
            super(MOST, c);
            _mf = new TDoubleIntHashMap();
            _mi = new TIntIntHashMap();
        }

        @Override public Codon copy() {
            return new Most(_c);
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public double op(double[] t, int s, int e, double[] p) {
            _mf.clear();
            _mostKeyF = -1;
            _mostValueF = -1;
            for(int i=s;i<=e;i++) {
                _mf.adjustOrPutValue(t[i], 1, 1);
            }
            _mf.forEachEntry(_procf);
            return _mostKeyF;
        }

        @Override public int op(int[] t, int s, int e, int[] p) {
            _mi.clear();
            _mostKeyI = -1;
            _mostValueI = -1;
            for(int i=s;i<=e;i++) {
                _mi.adjustOrPutValue(t[i], 1, 1);
            }
            _mi.forEachEntry(_proci);
            return _mostKeyI;
        }
    }

    public static final class Least extends NAggregate {
        private final TDoubleIntHashMap _mf;
        private final TIntIntHashMap _mi;
        private final TDoubleIntProcedure _procf = new TDoubleIntProcedure() {
            @Override public boolean execute(double k, int v) {
                if(v<_leastValueF) {
                    _leastKeyF = k;
                    _leastValueF = v;
                }
                return true;
            }
        };
        private final TIntIntProcedure _proci = new TIntIntProcedure() {
            @Override public boolean execute(int k, int v) {
                if(v<_leastValueI) {
                    _leastKeyI = k;
                    _leastValueI = v;
                }
                return true;
            }
        };
        private double _leastKeyF;
        private int _leastValueF;
        private int _leastKeyI;
        private int _leastValueI;

        public Least(int c) {
            super(LEAST, c);
            _mf = new TDoubleIntHashMap();
            _mi = new TIntIntHashMap();
        }

        @Override public Codon copy() {
            return new Least(_c);
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] t, int s, int e, int[] p) {
            _mi.clear();
            _leastKeyI = -1;
            _leastValueI = Integer.MAX_VALUE;
            for(int i=s;i<=e;i++) {
                _mi.adjustOrPutValue(t[i], 1, 1);
            }
            _mi.forEachEntry(_proci);
            return _leastKeyI;
        }

        @Override public double op(double[] t, int s, int e, double[] p) {
            _mf.clear();
            _leastKeyF = -1;
            _leastValueF = Integer.MAX_VALUE;
            for(int i=s;i<=e;i++) {
                _mf.adjustOrPutValue(t[i], 1, 1);
            }
            _mf.forEachEntry(_procf);
            return _leastKeyF;
        }
    }

    public static final class HistoTroveInt implements Codon {
        private final TIntIntHashMap _m;
        private final TIntLinkedList _l;

        public HistoTroveInt() {
            _m = new TIntIntHashMap();
            _l = new TIntLinkedList();
        }

        @Override public Codon copy() {
            return new HistoTroveInt();
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            _m.clear();
            _l.clear();
            for(int i=0;i<p.length;i++) {
                final int v = _m.adjustOrPutValue(p[i], 1, 1);
                if(v==1) {
                    _l.add(p[i]);
                }
            }
            _l.sort();
            //for(int i=0;i<p.length;i++) {
                //t.push(_m.get(p[i]));
            //}
            //LOG.info("LIST: "+_l);
            for(int i=0;i<_l.size();i++) {
                final int v = _m.get(_l.get(i));
                //LOG.info("PUSHING: "+v);
                t.push(v);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            throw new IllegalStateException("int histo on double tape");
        }
    }

    public static final class HistoTroveFloat implements Codon {
        private final TDoubleIntHashMap _m;
        private final TDoubleLinkedList _l;

        public HistoTroveFloat() {
            _m = new TDoubleIntHashMap();
            _l = new TDoubleLinkedList();
        }

        @Override public Codon copy() {
            return new HistoTroveFloat();
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean supports(Values v) { return v==Values.continuous; }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            throw new IllegalStateException("double histo on int tape");
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            _m.clear();
            _l.clear();
            for(int i=0;i<p.length;i++) {
                final int v = _m.adjustOrPutValue(p[i], 1, 1);
                if(v==1) {
                    _l.add(p[i]);
                }
            }
            _l.sort();
            //for(int i=0;i<p.length;i++) {
            for(int i=0;i<_l.size();i++) {
                final int v = _m.get(_l.get(i));
                t.push(v);
                //t.push(_m.get(p[i]));
            }
        }
    }

    public static final class Histo implements Codon {
        private final short[] _h;
        private final int[] _z;

        public Histo(int colors) {
            _h = new short[colors];
            _z = new int[8192];
        }

        @Override public Codon copy() {
            return new Histo(_h.length);
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean supports(Values v) { return true; }
        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            //clear();
            int mx = 0;
            for(int i=0;i<p.length;i++) {
                final int v = p[i];
                _z[mx++] = v;
                _h[v]++;
            }
            for(int i=0;i<mx;i++) {
                final int zi = _z[i];
                final short c = _h[zi];
                if(c>0) {
                    t.push(c);
                    _h[zi] = (short)0;
                }
                _z[i] = 0;
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
        }

        private void clear() {
            for(int i=0;i<_h.length;i++) {
                _h[i] = (short)0;
            }
            for(int i=0;i<_z.length;i++) {
                _z[i] = 0;
            }
        }
    }

    public static class Histold implements Codon {
        private final int[] _h;

        public Histold(int colors) {
            _h = new int[colors];
        }

        @Override public Codon copy() {
            return new Histold(_h.length);
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean supports(Values v) { return true; }
        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            for(int i=0;i<_h.length;i++) {
                _h[i] = 0;
            }
            for(int i=0;i<p.length;i++) {
                _h[p[i]]++;
            }
            for(int i=0;i<_h.length;i++) {
                if(_h[i]>0) {
                    t.push(_h[i]);
                }
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
        }
    }

    public static final class Sum implements Codon, IntTape.TapeOp, FloatTape.TapeOp {
        private final int[] _t = new int[BUF_SIZE];

        @Override public Codon copy() {
            return new Sum();
        }

        @Override public String code() {
            return SUM;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean reversible() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.selectAggAll();
            t.apply(this, p);
        }

        @Override public int op(int[] t, int st, int en, int[] p) {
            int s = 0;
            for(int i=st;i<=en;i++) {
                s += t[i];
            }
            return s;
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.selectAggAll();
            t.apply(this, p);
        }

        @Override public double op(double[] t, int st, int en, double[] p) {
            double s = 0;
            for(int i=st;i<=en;i++) {
                s += t[i];
            }
            return s;
        }

        @Override public void compile(Compiler c) {
            Compiler.Node[] ns = c.register().popAll();
            c.register().push(new Compiler.Op("+", ns));
        }
    }

    public abstract static class NAggregate implements Codon, IntTape.TapeOp, FloatTape.TapeOp {
        private final String _n;
        protected final int _c;
        //private final int[] _t = new int[BUF_SIZE];

        public NAggregate(String n, int c) {
            _n = n;
            _c = c;
        }

        @Override public String code() {
            return _c==-1?_n:(_n+_c);
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            if(_c==-1) {
                //System.err.println("***** NAggregate SELECT ALL");
                t.selectAggAll();
            }
            else {
                //System.err.println("***** NAggregate SELECT AGG");
                t.selectAgg(_c);
            }
            //System.err.println("***** NAggregate APPLY START");
            t.apply(this, p);
            //System.err.println("***** NAggregate APPLY DONE");
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            if(_c==-1) {
                t.selectAggAll();
            }
            else {
                t.selectAgg(_c);
            }
            t.apply(this, p);
        }

        @Override public String generate(Random r) {
            return _c==-1?_n:(_n+(1+r.nextInt(9)));
        }
    }

    public abstract static class NAggregateN implements Codon, IntTape.TapeOp, FloatTape.TapeOp {
        private final String _n;
        //private final int[] _t = new int[BUF_SIZE];

        public NAggregateN(String n) {
            _n = n;
        }

        @Override public String code() {
            return _n;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }
        @Override public boolean supports(Values v) { return true; }
        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int n = t.pop();
            t.selectAgg(n);
            t.apply(this, p);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            int n = (int) t.pop();
            t.selectAgg(n);
            t.apply(this, p);
        }

        @Override public String generate(Random r) {
            return _n;
        }
    }

    public static class Skip implements Codon, Unstable {
        private final int _c;

        public Skip(int c) {
            _c = c;
        }

        @Override public Codon copy() {
            return new Skip(_c);
        }

        @Override public String code() {
            return _c==-1?SKIP:(SKIP+_c);
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean reversible() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.skip(_c);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.skip(_c);
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public String generate(Random r) {
            return SKIP+(1+r.nextInt(9));
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(10)-4);
            if(nc<=0) {
                nc = 1;
            }
            return new Skip(nc);
        }
    }

    public static class SkipN implements Codon {
        @Override public Codon copy() {
            return new SkipN();
        }

        @Override public String code() {
            return SKIP_N;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int s = t.pop();
            t.skip(s);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double s = t.pop();
            t.skip((int)Math.ceil(s));
        }

        @Override public String generate(Random r) {
            return SKIP_N;
        }
    }

    public static class Fork implements Codon {
        private final int _b;
        private final double _bi;
        private final int _m;
        private int _c;

        public Fork(int c, int bins, int max) {
            _c = c;
            _b = bins;
            _m = max;
            _bi = 1d/_b;
        }

        @Override public Codon copy() {
            return new Fork(_c, _b, _m);
        }

        @Override public String code() {
            return FORK;
        }

        @Override public boolean usesPattern() { return true; }
        @Override public boolean usesTape() { return true; }
        @Override public boolean positioning() { return true; }
        @Override public boolean supports(Values v) { return true; }
        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int pos = t.pos();
            if(pos>0) {
                int idx = _c==-1?p.length/2:_c%p.length;
                int s = p[p.length/2];
                double n = (double)s/_m;
                int i = 0;
                while(i*_bi<n) i++;
                int ms = i;
                t.skip(ms);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            int pos = t.pos();
            if(pos>0) {
                int idx = _c==-1?p.length/2:_c%p.length;
                double s = p[p.length/2];
                double n = s/_m;
                int i = 0;
                while(i*_bi<n) i++;
                int ms = i;
                t.skip(ms);
            }
        }

        @Override public String generate(Random r) {
            return FORK;
        }
    }

    public static class Stop implements Codon {
        @Override public Codon copy() {
            return new Stop();
        }

        @Override public String code() {
            return STOP;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            if(v==0) {
                t.stop();
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = t.pop();
            if(v==0) {
                t.stop();
            }
        }

        @Override public String generate(Random r) {
            return STOP;
        }
    }

    public static class Halt implements Codon {
        @Override public Codon copy() {
            return new Halt();
        }

        @Override public String code() {
            return HALT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.stop();
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.stop();
        }

        @Override public String generate(Random r) {
            return HALT;
        }
    }

    public static class Abort implements Codon {
        @Override public Codon copy() {
            return new Abort();
        }

        @Override public String code() {
            return ABORT;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.push(p[p.length/2]);
            t.stop();
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.push(p[p.length/2]);
            t.stop();
        }

        @Override public String generate(Random r) {
            return ABORT;
        }
    }

    public static class Pos implements Codon {
        @Override public Codon copy() {
            return new Pos();
        }

        @Override public String code() {
            return POS;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() {
            return true;
        }

        @Override public boolean supports(Values v) { return true; }
        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.push(t.pos());
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.push(t.pos());
        }

        @Override public String generate(Random r) {
            return POS;
        }
    }

    public static final class Push implements Codon, Unstable {
        private final int _p;
        private final int _m;

        public Push(int p, int m) {
            // if(p<0) throw new IllegalArgumentException("p must be >= 0: "+p);
            _p = p;
            _m = m;
        }

        @Override public Codon copy() {
            return new Push(_p, _m);
        }

        @Override public String code() {
            return PUSH+_p;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return true; }

        @Override public boolean symmetric() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int c = _p;
            if(c>=p.length) {
                c = c%p.length;
            }
            t.push(p[c]);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            int c = _p;
            if(c>=p.length) {
                c = c%p.length;
            }
            t.push(p[c]);
        }

        @Override public String generate(Random r) {
            return PUSH+(_p==-1?r.nextInt(_m):_p);
        }

        @Override public Codon destabilize(Random r) {
            int v = Math.max(1,_p/3);
            int np = _p+((1+r.nextInt(v))*(r.nextBoolean()?1:-1));
            if(np>_m) {
                np = _m;
            }
            else if(np<0) {
                np = 0;
            }
            return new Push(np, _m);
        }

        @Override public void compile(Compiler c) {
            Compiler.Variable v = Compiler.Variable.next(c.nativeType());
            v.value("p["+_p+"]");
            c.register().push(v);
        }
    }

    public static class PushN implements Codon {
        public PushN() {
        }

        @Override public Codon copy() {
            return new PushN();
        }

        @Override public String code() {
            return PUSH_N;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean usesTape() { return true; }
        @Override public boolean reversible() { return true; }
        @Override public boolean symmetric() { return false; }
        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            long s = t.pop();
            long s2 = s%p.length;
            if(s2<0) {
                s2 = -s2;
            }
            if(s2<0) {
                throw new IllegalArgumentException("how did s get to "+s2+" from "+s+" and "+p.length);
            }
            t.push(p[(int)s2]);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            long s = (long) t.pop();
            long s2 = s%p.length;
            if(s2<0) {
                s2 = -s2;
            }
            if(s2<0) {
                throw new IllegalArgumentException("how did s get to "+s2+" from "+s+" and "+p.length);
            }
            t.push(p[(int)s2]);
        }

        @Override public String generate(Random r) {
            return PUSH_N;
        }
    }

    public static class PushS implements Codon {
        public PushS() {
        }

        @Override public Codon copy() {
            return new PushS();
        }

        @Override public String code() {
            return PUSH_S;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.push(p[p.length/2]);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.push(p[p.length/2]);
        }

        @Override public String generate(Random r) {
            return PUSH_S;
        }
    }

    public static final class Constant implements Codon, Unstable {
        private final int _p;
        private final double _pf;

        public Constant(int p, double pf) {
            _p = p;
            _pf = pf;
        }

        @Override public Codon copy() {
            return new Constant(_p, _pf);
        }

        @Override public String code() {
            return _pf==_p?(CONS+_p):(CONS+_pf);
        }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean reversible() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.push(_p);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.push(_pf);
        }

        @Override public String generate(Random r) {
            //return CONS+(_p==-1?r.nextInt(27):_p);
            return CONS+(_p==-1?r.nextInt(27):_p);
        }

        @Override public Codon destabilize(Random r) {
            int v = Math.max(1,_p/3);
            int np = _p+((1+r.nextInt(v))*(r.nextBoolean()?1:-1));
            double npf = _p+((1+r.nextFloat()*v)*(r.nextBoolean()?1d:-1d));
            return new Constant((int)npf, npf);
        }

        @Override public void compile(Compiler c) {
            Compiler.Variable v = Compiler.Variable.next(c.nativeType());
            switch(c.implicate().archetype().values()) {
                case discrete:
                    v.value(_p+"");
                    break;
                case continuous:
                    v.value(_pf+"");
                    break;
                default:
                    throw new IllegalArgumentException("unknown value mode "+c.implicate().archetype().values());
            }
            c.register().push(v);
        }

        @Override public String toString() {
            return "Constant"+_pf;
        }
    }

    public static class PushO implements Codon {
        @Override public String code() {
            return PUSH_SURROUND;
        }

        @Override public Codon copy() {
            return new PushO();
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return true; }

        @Override public boolean symmetric() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int sum = 0;
            for(int i=0;i<p.length;i++) {
                sum += p[i];
            }
            int mid = p.length/2;
            int start = Math.abs(p[Math.abs(sum)%p.length])%p.length;
            int s = 0;
            for(int i=start;s<p.length;s++) {
                if(i!=mid) t.push(p[i]);
                if(++i==p.length) i=0;
            }
        }

        public void op_old(int[] p, IntTape t, Pattern.Ctx c) {
            int mid = p.length/2;
            int start = Math.abs(p[mid]%p.length);
            int s = 0;
            for(int i=start;s<p.length;s++) {
                if(i!=mid) t.push(p[i]);
                if(++i==p.length) i=0;
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double sum = 0;
            for(int i=0;i<p.length;i++) {
                sum += p[i];
            }
            int mid = p.length/2;
            int start = (int) Math.abs(p[Math.abs((int)sum)%p.length])%p.length;
            int s = 0;
            for(int i=start;s<p.length;s++) {
                if(i!=mid) t.push(p[i]);
                if(++i==p.length) i=0;
            }
        }

        public void op_old(double[] p, FloatTape t, Pattern.Ctx c) {
            int mid = p.length/2;
            int start = Math.abs((int)p[mid]%p.length);
            int s = 0;
            for(int i=start;s<p.length;s++) {
                if(i!=mid) t.push(p[i]);
                if(++i==p.length) i=0;
            }
        }
    }

    public static class PushA implements Codon {
        @Override public String code() {
            return PUSH_ALL;
        }

        @Override public Codon copy() {
            return new PushA();
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean symmetric() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int n = p.length/2;
            int m = 0;
            m += t.pushAll(p, p.length-n-1, n+1);
            m += t.pushAll(p, n);
            t.push(p[n]);
            m += 1;
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            int n = p.length/2;
            int m = 0;
            m += t.pushAll(p, p.length-n-1, n+1);
            m += t.pushAll(p, n);
            t.push(p[n]);
            m += 1;
        }
    }

    public static class PushC implements Codon {
        @Override public String code() {
            return PUSH_CARDINAL;
        }

        @Override public Codon copy() {
            return new PushC();
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean symmetric() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            if(p.length==9) {
                t.push(p[5]);
                t.push(p[7]);
                t.push(p[3]);
                t.push(p[1]);
            }
            else if(p.length==5) {
                t.push(p[0]);
                t.push(p[3]);
                t.push(p[4]);
                t.push(p[1]);
            }
            else {
                for(int i=1;i<p.length;i+=2) {
                    t.push(p[i]);
                }
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            if(p.length==9) {
                t.push(p[5]);
                t.push(p[7]);
                t.push(p[3]);
                t.push(p[1]);
            }
            else if(p.length==5) {
                t.push(p[0]);
                t.push(p[3]);
                t.push(p[4]);
                t.push(p[1]);
            }
            else {
                for(int i=1;i<p.length;i+=2) {
                    t.push(p[i]);
                }
            }
        }
    }

    public static class PushARot implements Codon {
        @Override public Codon copy() {
            return new PushARot();
        }

        @Override public String code() {
            return PUSH_ALL_ROT;
        }

        @Override public boolean usesPattern() { return true; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean symmetric() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int n = t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            int n = (int)t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
        }
    }

    public static class Bandpass implements Codon {
        @Override public Codon copy() {
            return new Bandpass();
        }

        @Override public String code() {
            return BANDPASS;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?mid:0;
            t.push(in);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double up = t.pop();
            double low = t.pop();
            double mid = t.pop();
            double in = (mid >= low && mid <= up)?mid:0;
            t.push(in);
        }
    }

    public static class Intersects implements Codon {
        @Override public Codon copy() {
            return new Intersects();
        }

        @Override public String code() {
            return INTERSECT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?1:0;
            t.push(in);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double up = t.pop();
            double low = t.pop();
            double mid = t.pop();
            int in = (mid >= low && mid <= up)?1:0;
            t.push(in);
        }
    }

    public static class NotIntersects implements Codon {
        @Override public Codon copy() {
            return new NotIntersects();
        }

        @Override public String code() {
            return NOT_INTERSECT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid < low || mid > up)?1:0;
            t.push(in);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double up = t.pop();
            double low = t.pop();
            double mid = t.pop();
            int in = (mid < low || mid > up)?1:0;
            t.push(in);
        }
    }

    public static final class IntersectsSelf implements Codon {
        @Override public Codon copy() {
            return new IntersectsSelf();
        }

        @Override public String code() {
            return INTERSECT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?mid:0;
            t.push(in);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double up = t.pop();
            double low = t.pop();
            double mid = t.pop();
            double in = (mid >= low && mid <= up)?mid:0;
            t.push(in);
        }
    }

    public static class Equals implements Codon {
        @Override public Codon copy() {
            return new Equals();
        }

        @Override public String code() {
            return EQUAL;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1==v2)?1:0;
            t.push(eq);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v1 = t.pop();
            double v2 = t.pop();
            int eq = (v1==v2)?1:0;
            t.push(eq);
        }
    }

    // POTENTIAL STATE LEAK - ARRAYS.EQUALS
    public static class EqualsA implements Codon {
        private final int _c;
        private final int[] _s1;
        private final int[] _s2;
        private final double[] _ds1;
        private final double[] _ds2;


        public EqualsA(int c) {
            _c = c;
            _s1 = new int[_c<0?0:_c];
            _s2 = new int[_c<0?0:_c];
            _ds1 = new double[_c<0?0:_c];
            _ds2 = new double[_c<0?0:_c];
        }

        @Override public Codon copy() {
            return new EqualsA(_c);
        }

        @Override public String generate(Random r) {
            return EQUAL_A+(_c==-1?r.nextInt(9):_c);
        }

        @Override public String code() {
            return EQUAL_A;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.popAll(_s1, _c);
            t.popAll(_s2, _c);
            boolean e = Arrays.equals(_s1, _s2);
            t.push(e?1:0);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.popAll(_ds1, _c);
            t.popAll(_ds2, _c);
            boolean e = Arrays.equals(_ds1, _ds2);
            t.push(e?1:0);
        }
    }

    public static class Time implements Codon {
        private int _t = 0;

        @Override public Codon copy() {
            return new Time();
        }

        @Override public String code() {
            return TIME;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesContext() {
            // while Time does not technically use context,
            // it effectively does as that is what _t represents.
            // eventually Time should migrate to using Pattern.Context's
            // time tracker instead of keeping its own.
            return true;
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.push(_t);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.push(_t);
        }

        @Override public void tick() {
            _t++;
        }
    }

    public static class NotEquals implements Codon {
        @Override public Codon copy() {
            return new NotEquals();
        }

        @Override public String code() {
            return NOT_EQUAL;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1!=v2)?1:0;
            t.push(eq);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v1 = t.pop();
            double v2 = t.pop();
            int eq = (v1!=v2)?1:0;
            t.push(eq);
        }
    }

    public abstract static class Binary implements Codon, IntTape.TapeOp, FloatTape.TapeOp {
        private final String _code;

        public Binary(String code) {
            _code = code;
        }

        @Override public String code() {
            return _code;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean supports(Values v) { return true; }
        @Override public boolean usesTape() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            t.selectAgg(2);
            t.apply(this, p);
        }

        @Override public int op(int[] t, int s, int e, int[] p) {
            int ts = s==e?t[s]:t[s];
            int m = expr(t[e], ts);
            return m;
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            t.selectAgg(2);
            t.apply(this, p);
        }

        @Override public double op(double[] t, int s, int e, double[] p) {
            double ts = s==e?t[s]:t[s];
            double m = expr(t[e], ts);
            return m;
        }

        abstract int expr(int v1, int v2);

        abstract double expr(double v1, double v2);
    }

    public static class Subtract extends Binary {
        public Subtract() { super(SUBTRACT); }
        @Override public Codon copy() { return new Subtract(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1-v2;
        }
        @Override double expr(double v1, double v2) {
            return v1-v2;
        }
    }

    public static class Multiply extends Binary implements Transmutable {
        public Multiply() { super(MULTIPLY); }
        @Override public Codon copy() { return new Multiply(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1*v2;
        }
        @Override double expr(double v1, double v2) {
            return v1*v2;
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new Divide();
        }
    }

    public static class Divide extends Binary implements Transmutable {
        public Divide() { super(DIVIDE); }
        @Override public Codon copy() { return new Divide(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1/v2;
        }
        @Override double expr(double v1, double v2) {
            return v2==0?v1:v1/v2;
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new Divide();
        }
    }

    public static class Mod extends Binary {
        public Mod() { super(MOD); }
        @Override public Codon copy() { return new Mod(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1%v2;
        }
        @Override double expr(double v1, double v2) {
            return v2==0?v1:v1%v2;
        }
    }

    public static class Pow extends Binary {
        public Pow() { super(POW); }
        @Override public Codon copy() { return new Pow(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            int v = Maths.pow(v1,Math.max(0,Math.abs(v2)));
            return v;
        }
        @Override double expr(double v1, double v2) {
            return (double)Math.pow(v1,v2);
        }
    }

    public static class Sqrt implements Codon, Transmutable {
        @Override public Codon copy() { return new Sqrt(); }

        @Override public String code() {
            return SQRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = (int) Math.sqrt(Math.abs(t.pop()));
            t.push(v);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = (double) Math.sqrt(Math.abs(t.pop()));
            t.push(v);
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new Cbrt();
        }
    }

    public static class Cbrt implements Codon, Transmutable {
        @Override public Codon copy() { return new Cbrt(); }

        @Override public String code() {
            return CBRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = (int) Math.cbrt(t.pop());
            t.push(v);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = (double) Math.cbrt(t.pop());
            t.push(v);
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new Sqrt();
        }
    }

    public static class Sumn extends NAggregate implements Unstable {
        public Sumn() { super(SUM, -1); }
        public Sumn(int c) { super(SUM, c); }
        @Override public Codon copy() { return new Sumn(_c); }

        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            double s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
            if(nc<=0) {
                nc = -1;
            }
            return new Sumn(nc);
        }
    }

    public static class Muln extends NAggregate implements Unstable {
        public Muln() { super(MULTIPLY, -1); }
        public Muln(int c) { super(MULTIPLY, c); }
        @Override public Codon copy() { return new Sumn(_c); }

        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s *= vs[i];
            }
            return s;
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            double s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s *= vs[i];
            }
            return s;
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
            if(nc<=0) {
                nc = -1;
            }
            return new Muln(nc);
        }
    }

    public static class SumnN extends NAggregateN {
        public SumnN() { super(SUM_N); }
        @Override public Codon copy() { return new SumnN(); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }
        @Override public double op(double[] vs, int m, int e, double[] p) {
            double s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }
    }

    public static class Min extends NAggregate implements Unstable {
        public Min() { super(MIN, -1); }
        public Min(int c) { super(MIN, c); }
        @Override public Codon copy() { return new Min(_c); }
        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            double min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
            if(nc<=0) {
                nc = -1;
            }
            return new Min(nc);
        }
    }

    public static final class MinN extends NAggregateN {
        public MinN() { super(MIN_N); }

        @Override public Codon copy() { return new MinN(); }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
        @Override public double op(double[] vs, int m, int e, double[] p) {
            double min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
    }

    public static class Max extends NAggregate implements Unstable {
        public Max() { super(MAX, -1); }
        public Max(int c) { super(MAX, c); }
        @Override public Codon copy() { return new Max(_c); }
        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                final int t = vs[i];
                if(t>max) max=t;
            }
            return max;
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            double max = vs[m];
            for(int i=m+1;i<=e;i++) {
                final double t = vs[i];
                if(t>max) max=t;
            }
            return max;
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(10)-4);
            if(nc<=0) {
                nc = -1;
            }
            return new Max(nc);
        }
    }

    private static void debug(String m) {
        LOG.debug(m);
    }

    public static final class MaxN extends NAggregateN {
        public MaxN() { super(MAX_N); }
        @Override public Codon copy() { return new MaxN(); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
        @Override public double op(double[] vs, int m, int e, double[] p) {
            double max = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
    }

    public static final class Avg extends NAggregate implements Unstable {
        public Avg() { super(AVG, -1); }
        public Avg(int c) { super(AVG, c); }
        @Override public Codon copy() { return new Avg(_c); }
        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            if(1+e-m==0) return 0;
            int sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            return sum/(1+e-m);
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            if(1+e-m==0) return 0f;
            double sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            return sum/(1+e-m);
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
            if(nc<=0) {
                nc = -1;
            }
            return new Avg(nc);
        }
    }

    public static class Convolve extends NAggregate {
        public Convolve() { super(CONVOLVE, -1); }
        public Convolve(int c) { super(CONVOLVE, -1); }
        @Override public Codon copy() { return new Convolve(); }
        @Override public boolean supports(Values v) { return true; }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int ac = 0;
            switch(p.length) {
                case 9:
                    ac = vs[m+8]*p[0] + vs[m+7]*p[1] + vs[m+6]*p[2] + vs[m+5]*p[3] + vs[m+4]*p[4] + vs[m+3]*p[5]
                        + vs[m+2]*p[6] + vs[m+1]*p[7] + vs[m]*p[8];
                    break;
                default:
                    int j=p.length-1;
                    for(int i=m;i<=e;i++) {
                        ac = ac + vs[i]*p[j];
                        if(--j<0) j=p.length-1;
                    }
                    break;
            }
            return ac;
        }

        @Override public double op(double[] vs, int m, int e, double[] p) {
            double ac = 0;
            switch(p.length) {
                case 9:
                    ac = vs[m+8]*p[0] + vs[m+7]*p[1] + vs[m+6]*p[2] + vs[m+5]*p[3] + vs[m+4]*p[4] + vs[m+3]*p[5]
                        + vs[m+2]*p[6] + vs[m+1]*p[7] + vs[m]*p[8];
                    break;
                default:
                    int j=p.length-1;
                    for(int i=m;i<=e;i++) {
                        ac = ac + vs[i]*p[j];
                        if(--j<0) j=p.length-1;
                    }
                    break;
            }
            return ac;
        }
    }

    private static void dump(int[] vs, int m, int e, int[] p, int sum, int res) {
        LOG.debug("vs.length="+vs.length+", m="+m+", e="+e+", sum="+sum+", res="+res);
    }

    public static final class AvgN extends NAggregateN {
        public AvgN() { super(AVG_N); }

        @Override public Codon copy() { return new AvgN(); }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            int res = sum/(1+e-m);
            return res;
        }
        @Override public double op(double[] vs, int m, int e, double[] p) {
            double sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            double res = sum/(1+e-m);
            return res;
        }
    }

    public static class Xor extends Binary {
        public Xor() { super(XOR); }
        @Override public Codon copy() { return new Xor(); }
        @Override int expr(int v1, int v2) {
            return v1^v2;
        }
        @Override double expr(double v1, double v2) {
            //return v1;
            return Double.longBitsToDouble(
                Double.doubleToLongBits(v1) ^ Double.doubleToLongBits(v2));
        }
    }

    public static class GreaterThan extends Binary implements Transmutable {
        public GreaterThan() { super(GT); }
        @Override public Codon copy() { return new GreaterThan(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1>v2?1:0;
        }
        @Override double expr(double v1, double v2) {
            return v1>v2?1:0;
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new LessThan();
        }
    }

    public static class LessThan extends Binary implements Transmutable {
        public LessThan() { super(LT); }
        @Override public Codon copy() { return new LessThan(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1<v2?1:0;
        }
        @Override double expr(double v1, double v2) {
            return v1<v2?1:0;
        }
        @Override public Codon transmute(Implicate im, Random r) {
            return new GreaterThan();
        }
    }

    public static final class Greater extends Binary {
        public Greater() { super(GREATER); }
        @Override public Codon copy() { return new Greater(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1>=v2?v1:0;
        }
        @Override double expr(double v1, double v2) {
            return v1>=v2?v1:0;
        }
    }

    public static final class Lesser extends Binary {
        public Lesser() { super(LESSER); }
        @Override public Codon copy() { return new Lesser(); }
        @Override public boolean supports(Values v) { return true; }
        @Override int expr(int v1, int v2) {
            return v1<=v2?v1:0;
        }
        @Override double expr(double v1, double v2) {
            return v1<=v2?v1:0;
        }
    }

    public static class And extends Binary {
        public And() { super(AND); }
        @Override public Codon copy() { return new And(); }
        @Override int expr(int v1, int v2) {
            return v1&v2;
        }
        @Override double expr(double v1, double v2) {
            return v1;
        }
    }

    public static class Or extends Binary {
        public Or() { super(OR); }
        @Override public Codon copy() { return new Or(); }
        @Override int expr(int v1, int v2) {
            return v1|v2;
        }
        @Override double expr(double v1, double v2) {
            return v1;
        }
    }

    public static class Rotright extends Binary {
        public Rotright() { super(ROT_RIGHT); }
        @Override public Codon copy() { return new Rotright(); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateRight(v1,v2);
        }
        @Override double expr(double v1, double v2) {
            return v1;
        }
    }

    public static class Rotleft extends Binary {
        public Rotleft() { super(ROT_LEFT); }
        @Override public Codon copy() { return new Rotleft(); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateLeft(v1,v2);
        }
        @Override double expr(double v1, double v2) {
            return v1;
        }
    }

    public static class If implements Codon {
        @Override public Codon copy() { return new If(); }

        @Override public String code() {
            return IF;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int cond = t.pop();
            int fl = t.pop();
            int tr = t.pop();
            int res = cond!=0?tr:fl;
            t.push(res);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double cond = t.pop();
            double fl = t.pop();
            double tr = t.pop();
            double res = cond!=0?tr:fl;
            t.push(res);
        }
    }

    public static class Duplicate implements Codon {
        @Override public Codon copy() { return new Duplicate(); }

        @Override public String code() {
            return DUPLICATE;
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.peek();
            t.push(v);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = t.peek();
            t.push(v);
        }
    }

    public static class Negate implements Codon {
        @Override public Codon copy() { return new Negate(); }

        @Override public String code() {
            return NEGATE;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            t.push(-v);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = t.pop();
            t.push(-v);
        }
    }

    public static class Not implements Codon {
        @Override public Codon copy() { return new Not(); }

        @Override public String code() {
            return NOT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            t.push(v==0?1:0);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = t.pop();
            t.push(v==0?1:0);
        }
    }

    public static class Supersymmetry implements Codon {
        private final int _max;

        public Supersymmetry(int max) {
            _max = max;
        }

        @Override public Codon copy() { return new Supersymmetry(_max); }

        @Override public String code() {
            return SUPERSYMMETRY;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            int r = _max-v;
            t.push(r);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = t.pop();
            double r = _max-v;
            t.push(r);
        }
    }

    public static class Exclamatory implements Codon {
        @Override public Codon copy() { return new Exclamatory(); }

        @Override public String code() {
            return EXCLAMATORY;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }
        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            int x = Maths.excl(v);
            t.push(x);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
        }
    }

    public static class Filter implements Codon {
        private final int _c;


        public Filter(int c) {
            _c = c;
        }

        @Override public Codon copy() { return new Filter(_c); }

        @Override public String code() {
            return FILTER+_c;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean supports(Values v) { return true; }
        @Override public boolean usesTape() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            final int v = t.pop();
            int m = 0;
            for(int i=0;i<_c;i++) {
                int n = t.pop();
                if(n==v) {
                    m++;
                }
            }
            t.push(m);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            final double v = t.pop();
            int m = 0;
            for(int i=0;i<_c;i++) {
                double n = t.pop();
                if(n==v) {
                    m++;
                }
            }
            t.push(m);
        }
    }

    public static class Sigmoid implements Codon, Transmutable {
        @Override public Codon copy() { return new Sigmoid(); }

        @Override public String code() {
            return SIGMOID;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.continuous; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = 1d/(1d+(double)Math.exp(-t.pop()));
            t.push(v);
        }

        @Override public Codon transmute(Implicate im, Random r) {
            switch(r.nextInt(3)) {
                case 0:
                    return new Sin();
                case 1:
                    return new Cos();
                case 2:
                default:
                    return new Tanh();
            }
        }
    }

    public static class Tanh implements Codon, Transmutable {
        @Override public Codon copy() { return new Tanh(); }

        @Override public String code() {
            return TANH;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.continuous; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = (double) Math.tanh(t.pop());
            t.push(v);
        }

        @Override public Codon transmute(Implicate im, Random r) {
            switch(r.nextInt(3)) {
                case 0:
                    return new Sin();
                case 1:
                    return new Cos();
                case 2:
                default:
                    return new Sigmoid();
            }
        }
    }

    public static class Count implements Codon {
        @Override public Codon copy() { return new Count(); }

        @Override public String code() {
            return COUNT;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int v = t.pop();
            int c = 0;
            for(int i=0;i<p.length;i++) {
                if(p[i]==c) c++;
            }
            t.push(c);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double v = t.pop();
            int c = 0;
            for(int i=0;i<p.length;i++) {
                if(p[i]==c) c++;
            }
            t.push(c);
        }
    }

    public static class CountFixed implements Codon {
        @Override public Codon copy() { return new CountFixed(); }

        @Override public String code() {
            return COUNT_FIXED;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int v = t.pop();
            int c = 0;
            for(int i=0;i<p.length;i++) {
                if(p[i]==v) c++;
            }
            t.push(c);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double v = t.pop();
            int c = 0;
            for(int i=0;i<p.length;i++) {
                if(p[i]==v) c++;
            }
            t.push(c);
        }
    }

    public static class Jump implements Codon {
        @Override public Codon copy() { return new Jump(); }

        @Override public String code() {
            return JUMP;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }
        @Override public boolean positioning() { return true; }
        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx c) {
            int v = t.pop();
            //System.err.println("set jump by "+v);
            //t.jump(Math.abs(v));
            t.jump(v);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx c) {
            double v = (double) Math.ceil(t.pop());
            //System.err.println("set jump by "+v);
            //t.jump(Math.abs((int)v));
            t.jump((int)v);
        }
    }

    public static class Rand implements Codon {
        //private static final Random R = new Random();

        @Override public Codon copy() { return new Rand(); }

        @Override public String code() {
            return RAND;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean usesContext() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return false; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int m = 1;
            int v = t.pop();
            if(v==0) {
                t.push(0);
            }
            else {
                if(v<0) {
                    v=-v;
                    m=-1;
                }
                t.push(m*ctx.r.nextInt(v));
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double v = t.pop();
            t.push(v*ctx.r.nextDouble());
        }
    }

    public static class Coord implements Codon, Unstable, Transmutable {
        private final int _c;


        public Coord(int c) {
            _c = c;
        }

        @Override public Codon copy() { return new Coord(_c); }

        @Override public String code() { return _c==-1?COORD:(COORD+_c); }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        @Override public boolean usesContext() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            if(_c==-1) {
                for(int i=0;i<ctx.c.length;i++) {
                    t.push(ctx.c[i]);
                }
            }
            else {
                t.push(ctx.c[_c%ctx.c.length]);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            if(_c==-1) {
                for(int i=0;i<ctx.c.length;i++) {
                    t.push(ctx.c[i]);
                }
            }
            else {
                t.push(ctx.c[_c%ctx.c.length]);
            }
        }

        @Override public Codon destabilize(Random r) {
            return new Coord(r.nextInt(4)-1);
        }

        @Override public Codon transmute(Implicate im, Random r) {
            return new CoordRel(_c);
        }
    }

    public static class CoordRel implements Codon, Unstable, Transmutable {
        private final int _c;


        public CoordRel(int c) {
            _c = c;
        }

        @Override public Codon copy() { return new CoordRel(_c); }

        @Override public String code() { return _c==-1?COORD_REL:(COORD_REL+_c); }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return false; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        @Override public boolean usesContext() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            if(_c==-1) {
                for(int i=0;i<ctx.cr.length;i++) {
                    //t.push(ctx.cr[i]);
                    t.push(ctx.c[i]);
                }
            }
            else {
                //t.push(ctx.cr[_c%ctx.cr.length]);
                t.push(ctx.c[_c%ctx.c.length]);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            if(_c==-1) {
                for(int i=0;i<ctx.cr.length;i++) {
                    t.push(ctx.cr[i]);
                }
            }
            else {
                t.push(ctx.cr[_c%ctx.cr.length]);
            }
        }

        @Override public Codon destabilize(Random r) {
            return new CoordRel(r.nextInt(4)-1);
        }

        @Override public Codon transmute(Implicate im, Random r) {
            return new Coord(_c);
        }
    }

    public static class Mandelbrot implements Codon {
        private static final int SAFETY = 100;


        @Override public Codon copy() { return new Mandelbrot(); }

        @Override public String code() { return MANDELBROT; }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            double x1 = 0;
            double y1 = 0;
            double scl = t.pop();
            int z = Math.min(t.pop(), SAFETY);
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            while(z>0 && x1*x1+y1*y1<4) {
                z--;
                double xx = x1*x1 - y1*y1 + cx;
                y1 = 2 * x1 *y1 + cy;
                x1 = xx;
            }
            t.push(z);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double x1 = 0;
            double y1 = 0;
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            //if(z>100) System.err.println("Z"+z);
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            //System.err.println("cx: "+cx+", cy: "+cy+", z: "+z+", scl: "+scl);
            while(z>0 && x1*x1+y1*y1<4) {
                z--;
                double xx = x1*x1 - y1*y1 + cx;
                y1 = 2 * x1 * y1 + cy;
                x1 = xx;
            }
            //System.err.println("fz: "+z);
            t.push(z);
        }
    }

    public static class Mandelbulb implements Codon {
        private static final int SAFETY = 100;


        @Override public Codon copy() { return new Mandelbulb(); }

        @Override public String code() { return MANDELBULB; }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        public int isInFractal(double x, double y, double z, double d, int max) {
            double posX = x;
            double posY = y;
            double posZ = z;

            double dr = 1.0;
            double r = 0.0;

            int i = 0;
            for (i = 0; i < 10; i++) {
                r = Math.sqrt(x * x + y * y + z * z);
                if (r > d)
                    break;

                // convert to polar coordinates
                double theta = Math.acos(z / r);
                double phi = Math.atan2(y, x);
                dr = Math.pow(r, 8 - 1.0) * 8 * dr + 1.0;

                // scale and rotate the point
                double zr = Math.pow(r, 8);
                theta = theta * 8;
                phi = phi * 8;

                // convert back to cartesian coordinates
                x = zr * Math.sin(theta) * Math.cos(phi);
                y = zr * Math.sin(phi) * Math.sin(theta);
                z = zr * Math.cos(theta);

                x += posX;
                y += posY;
                z += posZ;
            }
            double d2 = (0.5 * Math.log(r) * r / dr);
            return (int) (100d*d2);
        }

        public double isInFractalReal(double x, double y, double z, double d, int max) {
            double posX = x;
            double posY = y;
            double posZ = z;

            double dr = 1.0;
            double r = 0.0;

            int i = 0;
            for (i = 0; i < 10; i++) {
                r = Math.sqrt(x * x + y * y + z * z);
                if (r > d)
                    break;

                // convert to polar coordinates
                double theta = Math.acos(z / r);
                double phi = Math.atan2(y, x);
                dr = Math.pow(r, 8 - 1.0) * 8 * dr + 1.0;

                // scale and rotate the point
                double zr = Math.pow(r, 8);
                theta = theta * 8;
                phi = phi * 8;

                // convert back to cartesian coordinates
                x = zr * Math.sin(theta) * Math.cos(phi);
                y = zr * Math.sin(phi) * Math.sin(theta);
                z = zr * Math.cos(theta);

                x += posX;
                y += posY;
                z += posZ;
            }
            double d2 = (0.5 * Math.log(r) * r / dr);
            return d2;
        }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            double cz = t.pop()/scl;
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;

            z = isInFractal(cx, cy, cz, 2d, z);

            t.push(z);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            double cz = t.pop()/scl;
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            double d2 = isInFractalReal(cx, cy, cz, 2d, z);

            t.push(d2);
        }
    }

    public static class MandelbulbX implements Codon {
        private static final int SAFETY = 100;


        @Override public Codon copy() { return new MandelbulbX(); }

        @Override public String code() { return MANDELBULB; }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            double cz = t.pop()/scl;
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;

            double x1 = 0;
            double y1 = 0;
            double z1 = 0;
            // double x1 = cx;
            // double y1 = cy;
            // double z1 = cz;
            //if(z>100) System.err.println("Z"+z);
            //System.err.println("cx: "+cx+", cy: "+cy+", z: "+z+", scl: "+scl);
            while(z>0 && x1*x1+y1*y1+z1*z1<2) {
                z--;
                // double xx = x1*x1 - y1*y1 + cx;
                // y1 = 2 * x1 * y1 + cy;
                // x1 = xx;
                double r = Math.sqrt(z1*z1+y1*y1+x1*x1);
                double yAng = Math.atan2(Math.sqrt(x1*x1+y1*y1), z1);
                double zAng = Math.atan2(y1, x1);

                double newx = (r*r) * Math.sin(2*yAng + 0.5*Math.PI) * Math.cos(2*zAng + Math.PI);
                double newy = (r*r) * Math.sin(2*yAng + 0.5*Math.PI) * Math.sin(2*zAng + Math.PI);
                double newz = (r*r) * Math.cos(2*yAng + 0.5*Math.PI);

                double xx = newx + cx;
                double yy = newy + cx;
                double zz = newz + cx;

                x1 = xx;
                y1 = yy;
                z1 = zz;
            }
            //System.err.println("fz: "+z);
            t.push(z);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            // System.err.println("========================================================");
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            double cz = t.pop()/scl;
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            // System.err.println("cx="+cx+", cy="+cy+", cz="+cz);

            double x1 = 0;
            double y1 = 0;
            double z1 = 0;
            // double x1 = cx;
            // double y1 = cy;
            // double z1 = cz;
            //if(z>100) System.err.println("Z"+z);
            //System.err.println("cx: "+cx+", cy: "+cy+", z: "+z+", scl: "+scl);
            while(z>0 && x1*x1+y1*y1+z1*z1<2) {
                System.err.println("z="+z+", x1="+x1+", y1="+y1+", z1="+z1);
                z--;
                // double xx = x1*x1 - y1*y1 + cx;
                // y1 = 2 * x1 * y1 + cy;
                // x1 = xx;
                double r = Math.sqrt(z1*z1+y1*y1+x1*x1);
                double yAng = Math.atan2(Math.sqrt(x1*x1+y1*y1), z1);
                double zAng = Math.atan2(y1, x1);
                System.err.println("r="+r+", yAng="+yAng+", zAng="+zAng);

                double newx = (r*r) * Math.sin(2*yAng + 0.5*Math.PI) * Math.cos(2*zAng + Math.PI);
                double newy = (r*r) * Math.sin(2*yAng + 0.5*Math.PI) * Math.sin(2*zAng + Math.PI);
                double newz = (r*r) * Math.cos(2*yAng + 0.5*Math.PI);
                System.err.println("newx="+newx+", newy="+newy+", newz="+newz);

                double xx = newx + cx;
                double yy = newy + cx;
                double zz = newz + cx;

                x1 = xx;
                y1 = yy;
                z1 = zz;
                System.err.println("nx1="+x1+", ny1="+y1+", nz1="+z1);
            }
            //System.err.println("fz: "+z);
            t.push(z);
        }
    }

    public static class Hodge implements Codon {
        private static final PushS PUSHS = new PushS();
        private final int _cols;

        public Hodge(int cols) {
            _cols = cols;
        }

        @Override public Codon copy() { return new Hodge(_cols); }

        @Override public String code() { return HODGE; }

        @Override public boolean usesPattern() { return true; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public boolean deterministic() { return true; }

        public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int k1 = Math.max(1,t.pop());
            int k2 = Math.max(1,t.pop());
            int g = t.pop();

            int self = p[p.length/2];
            int next;
            if(self==0) {
                int infected = 0;
                int ill = 0;
                for(int i=0;i<p.length;i++) {
                    //if(i!=p.length/2) {
                        if(p[i]>0&&p[i]<_cols-1) {
                            infected++;
                        }
                        else if(p[i]==_cols-1) {
                            ill++;
                        }
                    //}
                }
                next = infected/k1+ill/k2;
            }
            else if(self<_cols-1) {
                int val=0;
                int infected=0;
                for(int i=0;i<p.length;i++) {
                    //if(i!=p.length/2) {
                        val += p[i];
                        if(p[i]>0&&p[i]<_cols-1) {
                            infected++;
                        }
                    //}
                }
                next = Math.min(g+val/infected, _cols-1);
            }
            else { // self==_cols-1
                next = 0;
            }
            t.push(next);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
        }
    }

    public static class Slime implements Codon {
        private static final PushS PUSHS = new PushS();

        public Slime() {
        }

        @Override public Codon copy() { return new Slime(); }

        @Override public String code() { return SLIME; }

        @Override public boolean usesPattern() { return true; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public boolean deterministic() { return true; }

        public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int self = p[p.length/2];
            int next = 0;
            switch(self) {
                case 0:
                    for(int i=0;i<p.length;i++) {
                        if(p[i]==2||p[i]==1) {
                            next = 2;
                            break;
                        }
                    }
                    break;
                case 1:
                case 4:
                    next = self;
                    break;
                default:
                    next = 1;
                    break;
            }
            t.push(next);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
        }
    }


    // ki mi a2 a3 u ki mi8 a3 ma ya ra
    public static class Life implements Codon {
        @Override public Codon copy() { return new Life(); }

        @Override public String code() { return LIFE; }

        @Override public boolean usesPattern() { return true; }

        @Override public boolean usesTape() { return false; }

        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public boolean deterministic() { return true; }

        public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int s0 = 0;
            for(int i=0;i<p.length;i++) {
                if(i!=p.length/2) s0 += p[i];
            }
            int a0 = 2;
            int a1 = 3;
            //boolean b0 = s0 >= a0 && s0 <= a1;
            int b0 = s0 >= a0 && s0 <= a1 ? 1 : 0;

            int s1 = 0;
            for(int i=0;i<p.length;i++) {
                if(i!=p.length/2) s1 += p[i];
            }

            int a2 = 3;
            //boolean b1 = s1 == a2;
            int b1 = s1 == a2 ? 1 : 0;
            
            int p0 = p[p.length/2];

            int res = p0 == 0 ? b1 : b0;
            t.push(res);
        }

        public void op0(int[] p, IntTape t, Pattern.Ctx ctx) {
            int s = 0;
            for(int i=0;i<p.length;i++) {
                s += p[i];
            }
            int self = p[p.length/2];
            s -= self;
            int r;
            if(self==1) {
                r = s==2||s==3 ? 1 : 0;
            }
            else {
                r = s==3 ? 1 : 0;
            }
            t.push(r);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
        }
    }

    public static class Biomorph implements Codon {
        private static final int SAFETY = 1000;


        @Override public Codon copy() { return new Mandelbrot(); }

        @Override public String code() { return MANDELBROT; }

        @Override public boolean usesPattern() { return false; }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public boolean deterministic() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            double x1 = 0;
            double y1 = 0;
            double scl = t.pop();
            int z = Math.min(t.pop(), SAFETY);
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            while(z>0 && x1*x1+y1*y1<4) {
                z--;
                double xx = x1*x1 - y1*y1 + cx;
                y1 = 2 * x1 * y1 + cy;
                x1 = xx;
            }
            t.push(z);
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            double x1 = 0;
            double y1 = 0;
            double scl = t.pop();
            int z = Math.min((int)t.pop(), SAFETY);
            double cy = t.pop()/scl;
            double cx = t.pop()/scl;
            while(z>0 && x1*x1+y1*y1<4) {
                z--;
                
            }
            t.push(z);
        }
    }

    public static class Cos implements Codon, Transmutable {
        @Override public Codon copy() { return new Cos(); }

        @Override public String code() {
            return COS;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.continuous; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            t.push((double)Math.cos(t.pop()));
        }

        @Override public Codon transmute(Implicate im, Random r) {
            switch(r.nextInt(3)) {
                case 0:
                    return new Sin();
                case 1:
                    return new Sigmoid();
                case 2:
                default:
                    return new Tanh();
            }
        }
    }

    public static class Abs implements Codon {
        @Override public Codon copy() { return new Abs(); }

        @Override public String code() {
            return ABS;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            t.push(Math.abs(t.pop()));
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            t.push(Math.abs(t.pop()));
        }
    }

    public static class Sin implements Codon, Transmutable {
        @Override public Codon copy() { return new Sin(); }

        @Override public String code() {
            return SIN;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }

        @Override public boolean supports(Values v) { return v==Values.continuous; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            t.push((double)Math.sin(t.pop()));
        }

        @Override public Codon transmute(Implicate im, Random r) {
            switch(r.nextInt(3)) {
                case 0:
                    return new Cos();
                case 1:
                    return new Sigmoid();
                case 2:
                default:
                    return new Tanh();
            }
        }
    }

    public static class RotVecN implements Codon {
        private final int[] _v;
        private final int[] _vt;
        private final double[] _dv;
        private final double[] _dvt;

        public RotVecN(int max) {
            _v = new int[max];
            _vt = new int[max];
            _dv = new double[max];
            _dvt = new double[max];
        }

        @Override public Codon copy() { return new RotVecN(_v.length); }

        @Override public String code() {
            return ROT_VEC_N;
        }

        @Override public boolean usesPattern() { return false; }
        @Override public boolean supports(Values v) { return true; }
        @Override public boolean usesTape() { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            int dist = t.pop();
            int len = Math.min(Math.max(0,t.pop()), _v.length);
            //int ipeek = t.peek();
            int m = t.popAll(_v, len);
            for(int i=0;i<len;i++) {
                int dest = (i+dist)%len;
                if(dest<0) {
                    dest = dest + len;
                }
                _vt[dest] = _v[i];
            }
            t.pushAll(_vt, len);
            //int opeek = t.peek();
            //if(ipeek!=opeek) {
                //System.err.println("i: p:"+ipeek+", d:"+dist+", l:"+len+", a:"+Arrays.toString(_v));
                //System.err.println("o: p:"+opeek+", d:"+dist+", l:"+len+", a:"+Arrays.toString(_vt));
            //}
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            int dist = (int) t.pop();
            int len = (int) Math.min(Math.max(0d,t.pop()), _dv.length);
            //int ipeek = t.peek();
            int m = t.popAll(_dv, len);
            for(int i=0;i<len;i++) {
                int dest = (i+dist)%len;
                if(dest<0) {
                    dest = dest + len;
                }
                _dvt[dest] = _dv[i];
            }
            t.pushAll(_dvt, len);
            //int opeek = t.peek();
            //if(ipeek!=opeek) {
                //System.err.println("i: p:"+ipeek+", d:"+dist+", l:"+len+", a:"+Arrays.toString(_v));
                //System.err.println("o: p:"+opeek+", d:"+dist+", l:"+len+", a:"+Arrays.toString(_vt));
            //}
        }
    }

    public static class Placeholder implements Codon {
        private final String _name;
        private long _cnt = 0;

        public Placeholder(String name) {
            _name = name;
        }

        @Override public Codon copy() { return new Placeholder(_name); }

        @Override public String code() { return _name; }

        @Override public boolean usesPattern() { return false; }
        @Override public boolean usesTape() { return false; }
        @Override public boolean supports(Values v) { return true; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            if(++_cnt%1000==0) LOG.debug("executing placeholder '"+_name+"'");
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            if(++_cnt%1000==0) LOG.debug("executing placeholder '"+_name+"'");
        }
    }

    public static class Data implements Codon {
        private final String _name;
        private final Index _i;
        private final int[] _s;

        public Data(String name, Index idx) {
            _name = name;
            _i = idx;
            _s = new int[idx.size()];
        }

        @Override public Codon copy() { return new Data(_name, _i); }

        @Override public String code() {
            return DATA+_name;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean usesTape() { return true; }
        @Override public boolean supports(Values v) { return v==Values.discrete; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            for(int i=0;i<_s.length;i++) {
                _s[i] = t.pop();
            }
            int r = _i.find(_s);
            if(r!=-1) {
                t.push(r);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
        }
    }

    public static final class Chain implements Codon, Unstable {
        private final Codon[] _cs;
        private final boolean _usesPattern;
        private final boolean _usesTape;
        private final boolean _deterministic;
        private final boolean _usesContext;
        private final boolean _unstable;
        private final boolean _positioning;

        public Chain(Codon... cs) {
            _cs = cs;
            boolean up = false;
            boolean ut = false;
            boolean det = true;
            boolean uctx = false;
            boolean unst = false;
            boolean pos = false;
            for(Codon c:cs) {
                if(c.usesPattern()) {
                    up = true;
                }
                if(c.usesTape()) {
                    ut = true;
                }
                if(!c.deterministic()) {
                    det = false;
                }
                if(c.usesContext()) {
                    uctx = true;
                }
                if(c.positioning()) {
                    pos = true;
                }
                if(c instanceof Unstable) {
                    unst = true;
                }
            }
            _usesPattern = up;
            _usesTape = ut;
            _deterministic = det;
            _usesContext = uctx;
            _unstable = unst;
            _positioning = pos;
        }

        @Override public Codon copy() {
            Codon[] cs = new Codon[_cs.length];
            for(int i=0;i<_cs.length;i++) {
                cs[i] = _cs[i].copy();
            }
            return new Chain(cs);
        }

        @Override public String code() {
            StringBuilder b = new StringBuilder(12);
            for(Codon c:_cs) {
                b.append(c.code()).append('+');
            }
            b.setLength(b.length()-1);
            return b.toString();
        }

        @Override public boolean usesContext() { return _usesContext; }
        @Override public boolean usesPattern() { return _usesPattern; }
        @Override public boolean usesTape() { return _usesTape; }
        @Override public boolean deterministic() { return _deterministic; }
        @Override public boolean supports(Values v) { return true; }
        @Override public boolean positioning() { return _positioning; }

        @Override public void op(int[] p, IntTape t, Pattern.Ctx ctx) {
            for(int i=0;i<_cs.length;i++) {
                _cs[i].op(p, t, ctx);
            }
        }

        @Override public void op(double[] p, FloatTape t, Pattern.Ctx ctx) {
            for(int i=0;i<_cs.length;i++) {
                _cs[i].op(p, t, ctx);
            }
        }

        @Override public Codon chain(Codon c) {
            Codon[] cs = new Codon[1+_cs.length];
            System.arraycopy(_cs, 0, cs, 0, _cs.length);
            cs[cs.length-1] = c;
            return new Codons.Chain(cs);
        }

        @Override public void tick() {
            for(int i=0;i<_cs.length;i++) {
                _cs[i].tick();
            }
        }

        @Override public Codon destabilize(Random r) {
            Codon[] cs = new Codon[_cs.length];
            System.arraycopy(_cs, 0, cs, 0, cs.length);
            if(_unstable) {
                int st = r.nextInt(cs.length);
                int en = st==0?cs.length:st-1;
                for(int i=st;i!=en;) {
                    final Codon c = cs[i];
                    if(c instanceof Unstable) {
                        //any = true;
                        Codon after = ((Unstable)c).destabilize(r);
                        LOG.debug("unstable before: "+c.code()+", after: "+after.code());
                        cs[i] = after;
                        if(!c.code().equals(after.code())) {
                            //de = true;
                            LOG.debug("found destabilize");
                            break;
                        }
                    }
                    if(++i==cs.length&&i!=en) {
                        i=0;
                    }
                }
            }
            return new Chain(cs);
        }

        @Override public String toString() {
            StringBuilder b = new StringBuilder();
            for(Codon c:_cs) {
                b.append(c.toString()).append('+');
            }
            b.setLength(b.length()-1);
            return b.toString();
        }

        public Codon[] childs() {
            return _cs;
        }
    }
}
