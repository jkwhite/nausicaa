package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.Arrays;


public class Codons {
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
    public static final String AVG = "gi";
    public static final String PUSH_ALL_ROT = "ge";
    public static final String PUSH_ALL = "go";
    public static final String SKIP_N = "ji";
    public static final String MIN_N = "jo";
    public static final String IF = "ra";
    public static final String MAX_N = "ri";
    public static final String SKIP = "ro";
    public static final String NON_ZERO = "zu";
    public static final String DUPLICATE = "do";
    public static final String EXCLAMATORY = "ha";
    public static final String HISTO = "hi";
    public static final String SUPERSYMMETRY = "hu";
    public static final String DIVIDE = "ho";
    public static final String ROT_VEC_N = "ba";
    public static final String LT = "bu";
    public static final String GT = "be";
    public static final String NEGATE = "bo";


    public static Codon codon(final String s, final Archetype a) {
        //System.err.println("op '"+s+"'");
        int i=0;
        for(;i<s.length()&&Character.isAlphabetic(s.charAt(i));i++);
        final String code = s.substring(0,i);
        //System.err.println("code '"+code+"'");
        int p=-1;
        if(i<s.length()) {
            p = Integer.parseInt(s.substring(i));
        }
        //System.err.println("arg '"+p+"'");
        switch(code) {
            case SUM:
                return p==-1?new Sum():new Sumn(p);
            case SUM_N:
                return new SumnN();
            case PUSH:
                return new Push(p, a.sourceLength());
            case PUSH_N:
                return new PushN();
            case PUSH_S:
                return new PushS();
            case MOD:
                return new Mod();
            case PUSH_SURROUND:
                return new PushO();
            case PUSH_ALL:
                return new PushA();
            case PUSH_ALL_ROT:
                return new PushARot();
            case INTERSECT:
                return new Intersects();
            case INTERSECT_S:
                return new IntersectsSelf();
            case CONS:
                return new Constant(p);
            case EQUAL:
                return new Equals();
            case NOT_EQUAL:
                return new NotEquals();
            case IF:
                return new If();
            case SUBTRACT:
                return new Subtract();
            case MULTIPLY:
                return new Multiply();
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
                return new Histo(a.colors());
            case DUPLICATE:
                return new Duplicate();
            case EXCLAMATORY:
                return new Exclamatory();
            case SUPERSYMMETRY:
                return new Supersymmetry(a.colors()-1);
            case ROT_VEC_N:
                return new RotVecN(a.sourceLength());
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
                return new Fork(p, 10, a.colors()-1);
            case STOP:
                return new Stop();
            case POS:
                return new Pos();
            default:
                throw new IllegalStateException("unknown opcode '"+code+"'");
        }
    }

    public static final class Nonzero implements Codon, Tape.TapeOp, Unstable {
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

        @Override public void op(int[] p, Tape t) {
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

        @Override public Codon destabilize(Random r) {
            if(_c==-1) {
                return new Nonzero(1+r.nextInt(9));
            }
            else {
                int nc = _c+(r.nextInt(5)-2);
                if(nc<=0) {
                    nc = -1;
                }
                return new Nonzero(nc);
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

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
            clear();
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

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
            for(int i=0;i<_h.length;i++) {
                _h[i] = 0;
            }
            for(int i=0;i<p.length;i++) {
                _h[p[i]]++;
            }
            for(int i=0;i<_h.length;i++) {
                t.push(_h[i]);
            }
        }
    }

    public static final class Sum implements Codon, Tape.TapeOp {
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

        @Override public void op(int[] p, Tape t) {
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
    }

    public abstract static class NAggregate implements Codon, Tape.TapeOp {
        private final String _n;
        protected final int _c;
        private final int[] _t = new int[BUF_SIZE];

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

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
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

    public abstract static class NAggregateN implements Codon, Tape.TapeOp {
        private final String _n;
        private final int[] _t = new int[BUF_SIZE];

        public NAggregateN(String n) {
            _n = n;
        }

        @Override public String code() {
            return _n;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
            int n = t.pop();
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

        @Override public void op(int[] p, Tape t) {
            t.skip(_c);
        }

        @Override public String generate(Random r) {
            return SKIP+(1+r.nextInt(9));
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
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

        @Override public boolean reversible() { return true; }

        @Override public void op(int[] p, Tape t) {
            int s = t.pop();
            t.skip(s);
        }

        @Override public String generate(Random r) {
            return SKIP_N;
        }
    }

    public static class Fork implements Codon {
        private final int _b;
        private final float _bi;
        private final int _m;
        private int _c;

        public Fork(int c, int bins, int max) {
            _c = c;
            _b = bins;
            _m = max;
            _bi = 1f/_b;
        }

        @Override public Codon copy() {
            return new Fork(_c, _b, _m);
        }

        @Override public String code() {
            return FORK;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
            int pos = t.pos();
            if(pos>0) {
                int idx = _c==-1?p.length/2:_c%p.length;
                int s = p[p.length/2];
                float n = (float)s/_m;
                int i = 0;
                while(i*_bi<n) i++;
                //int ms = s % pos;
                int ms = i;
                t.skip(ms);
                //int npos = t.pos();
                //System.err.println("self: "+s+", bin: "+ms+", pos: "+pos+", npos: "+npos);
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

        @Override public boolean reversible() { return true; }

        @Override public void op(int[] p, Tape t) {
            int v = t.pop();
            if(v==0) {
                t.stop();
            }
        }

        @Override public String generate(Random r) {
            return STOP;
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

        @Override public boolean reversible() { return false; }

        @Override public void op(int[] p, Tape t) {
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

        @Override public void op(int[] p, Tape t) {
            t.push(p[_p]);
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
            return new Push(np, _m);
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

        @Override public boolean reversible() { return true; }

        @Override public boolean symmetric() { return false; }

        @Override public void op(int[] p, Tape t) {
            long s = t.pop();
            long s2 = s;
            if(s2>=p.length) {
                s2 = s2%p.length;
            }
            else if(s2<0) {
                s2 = (-s2)%p.length;
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

        @Override public void op(int[] p, Tape t) {
            t.push(p[p.length/2]);
        }

        @Override public String generate(Random r) {
            return PUSH_S;
        }
    }

    public static final class Constant implements Codon, Unstable {
        private final int _p;

        public Constant(int p) {
            _p = p;
        }

        @Override public Codon copy() {
            return new Constant(_p);
        }

        @Override public String code() {
            return CONS+_p;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean reversible() { return true; }

        @Override public void op(int[] p, Tape t) {
            t.push(_p);
        }

        @Override public String generate(Random r) {
            return CONS+(_p==-1?r.nextInt(27):_p);
        }

        @Override public Codon destabilize(Random r) {
            int v = Math.max(1,_p/3);
            int np = _p+((1+r.nextInt(v))*(r.nextBoolean()?1:-1));
            if(np<0) {
                np = 0;
            }
            return new Constant(np);
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

        @Override public void op(int[] p, Tape t) {
            int mid = p.length/2;
            //t.pushAll(p, mid);
            //t.pushAll(p, mid, mid+1);
            int start = p[mid]%p.length;
            //int c1 = start<mid?start-mid:p.length-start;
            //t.pushAll(p, c1, start);
            //int i2 = c1+start<mid?i1+1:mid;
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

        @Override public void op(int[] p, Tape t) {
            //t.pushAll(p, p.length);
            int n = p[p.length/2]%p.length; //t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
        }
    }

    public static class PushARot implements Codon {
        @Override public Codon copy() {
            return new PushARot();
        }

        @Override public String code() {
            return PUSH_ALL_ROT;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public boolean symmetric() { return false; }

        @Override public void op(int[] p, Tape t) {
            int n = t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
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

        @Override public void op(int[] p, Tape t) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?1:0;
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

        @Override public void op(int[] p, Tape t) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?mid:0;
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

        @Override public void op(int[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1==v2)?1:0;
            t.push(eq);
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
            return true;
        }

        @Override public void op(int[] p, Tape t) {
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

        @Override public void op(int[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1!=v2)?1:0;
            t.push(eq);
        }
    }

    public abstract static class Binary implements Codon, Tape.TapeOp {
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

        @Override public void op(int[] p, Tape t) {
            t.selectAgg(2);
            t.apply(this, p);
        }

        @Override public int op(int[] t, int s, int e, int[] p) {
            int ts = s==e?t[s]:t[s];
            int m = expr(t[e], ts);
            return m;
        }

        abstract int expr(int v1, int v2);
    }

    public static class Subtract extends Binary {
        public Subtract() { super(SUBTRACT); }
        @Override public Codon copy() { return new Subtract(); }
        @Override int expr(int v1, int v2) {
            return v1-v2;
        }
    }

    public static class Multiply extends Binary {
        public Multiply() { super(MULTIPLY); }
        @Override public Codon copy() { return new Multiply(); }
        @Override int expr(int v1, int v2) {
            return v1*v2;
        }
    }

    public static class Divide extends Binary {
        public Divide() { super(DIVIDE); }
        @Override public Codon copy() { return new Divide(); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1/v2;
        }
    }

    public static class Mod extends Binary {
        public Mod() { super(MOD); }
        @Override public Codon copy() { return new Mod(); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1%v2;
        }
    }

    public static class Pow extends Binary {
        public Pow() { super(POW); }
        @Override public Codon copy() { return new Pow(); }
        @Override int expr(int v1, int v2) {
            int v = Maths.pow(v1,Math.max(0,Math.abs(v2)));
            return v;
        }
    }

    public static class Sqrt implements Codon {
        @Override public Codon copy() { return new Sqrt(); }

        @Override public String code() {
            return SQRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            int v = (int) Math.sqrt(Math.abs(t.pop()));
            t.push(v);
        }
    }

    public static class Cbrt implements Codon {
        @Override public Codon copy() { return new Cbrt(); }

        @Override public String code() {
            return CBRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            int v = (int) Math.cbrt(t.pop());
            t.push(v);
        }
    }

    public static class Sumn extends NAggregate implements Unstable {
        public Sumn() { super(SUM, -1); }
        public Sumn(int c) { super(SUM, c); }
        @Override public Codon copy() { return new Sumn(_c); }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
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
    }

    public static class Min extends NAggregate implements Unstable {
        public Min() { super(MIN, -1); }
        public Min(int c) { super(MIN, c); }
        @Override public Codon copy() { return new Min(_c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int min = vs[m];
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
    }

    public static class Max extends NAggregate implements Unstable {
        public Max() { super(MAX, -1); }
        public Max(int c) { super(MAX, c); }
        @Override public Codon copy() { return new Max(_c); }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                final int t = vs[i];
                if(t>max) max=t;
            }
            return max;
        }

        @Override public Codon destabilize(Random r) {
            int nc = _c+(r.nextInt(5)-2);
            if(nc<=0) {
                nc = -1;
            }
            return new Max(nc);
        }
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
    }

    public static final class Avg extends NAggregate implements Unstable {
        public Avg() { super(AVG, -1); }
        public Avg(int c) { super(AVG, c); }
        @Override public Codon copy() { return new Avg(_c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int sum = vs[m];
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

    private static void dump(int[] vs, int m, int e, int[] p, int sum, int res) {
        System.err.println("vs.length="+vs.length+", m="+m+", e="+e+", sum="+sum+", res="+res);
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
    }

    public static class Xor extends Binary {
        public Xor() { super(XOR); }
        @Override public Codon copy() { return new Xor(); }
        @Override int expr(int v1, int v2) {
            return v1^v2;
        }
    }

    public static class GreaterThan extends Binary {
        public GreaterThan() { super(GT); }
        @Override public Codon copy() { return new GreaterThan(); }
        @Override int expr(int v1, int v2) {
            return v1>v2?1:0;
        }
    }

    public static class LessThan extends Binary {
        public LessThan() { super(LT); }
        @Override public Codon copy() { return new LessThan(); }
        @Override int expr(int v1, int v2) {
            return v1<v2?1:0;
        }
    }

    public static final class Greater extends Binary {
        public Greater() { super(GREATER); }
        @Override public Codon copy() { return new Greater(); }
        @Override int expr(int v1, int v2) {
            return v1>=v2?v1:0;
        }
    }

    public static final class Lesser extends Binary {
        public Lesser() { super(LESSER); }
        @Override public Codon copy() { return new Lesser(); }
        @Override int expr(int v1, int v2) {
            return v1<=v2?v1:0;
        }
    }

    public static class And extends Binary {
        public And() { super(AND); }
        @Override public Codon copy() { return new And(); }
        @Override int expr(int v1, int v2) {
            return v1&v2;
        }
    }

    public static class Or extends Binary {
        public Or() { super(OR); }
        @Override public Codon copy() { return new Or(); }
        @Override int expr(int v1, int v2) {
            return v1|v2;
        }
    }

    public static class Rotright extends Binary {
        public Rotright() { super(ROT_RIGHT); }
        @Override public Codon copy() { return new Rotright(); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateRight(v1,v2);
        }
    }

    public static class Rotleft extends Binary {
        public Rotleft() { super(ROT_LEFT); }
        @Override public Codon copy() { return new Rotleft(); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateLeft(v1,v2);
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

        @Override public void op(int[] p, Tape t) {
            int cond = t.pop();
            int fl = t.pop();
            int tr = t.pop();
            int res = cond!=0?tr:fl;
            t.push(res);
        }
    }

    public static class Duplicate implements Codon {
        @Override public Codon copy() { return new Duplicate(); }

        @Override public String code() {
            return DUPLICATE;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            int v = t.peek();
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

        @Override public void op(int[] p, Tape t) {
            int v = t.pop();
            t.push(-v);
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

        @Override public void op(int[] p, Tape t) {
            int v = t.pop();
            int r = _max-v;
            t.push(r);
        }
    }

    public static class Exclamatory implements Codon {
        @Override public Codon copy() { return new Exclamatory(); }

        @Override public String code() {
            return POW;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            int v = t.pop();
            int x = Maths.excl(v);
            t.push(x);
        }
    }

    public static class RotVecN implements Codon {
        private final int[] _v;
        private final int[] _vt;

        public RotVecN(int max) {
            _v = new int[max];
            _vt = new int[max];
        }

        @Override public Codon copy() { return new RotVecN(_v.length); }

        @Override public String code() {
            return ROT_VEC_N;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
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
    }

}
