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
    public static final String INTERSECT_S = "yo";
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

        @Override public String code() {
            return _c==-1?NON_ZERO:(NON_ZERO+_c);
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            if(_c==-1) {
                t.selectIdxAll();
            }
            else {
                t.selectIdx(_c);
            }
            t.apply(this, p);
            //int m = t.popAll(_t, _c);
            //for(int i=0;i<m;i++) {
                //if(_t[i]!=0) {
                    //t.push(_t[i]);
                //}
            //}
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

    /*
    public static class Nonzero implements Codon {
        private final int[] _t = new int[BUF_SIZE];
        private final int _c;

        public Nonzero(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?NON_ZERO:(NON_ZERO+_c);
        }

        @Override public void op(int[] p, Tape t) {
            int m = t.popAll(_t, _c);
            for(int i=0;i<m;i++) {
                if(_t[i]!=0) {
                    t.push(_t[i]);
                }
            }
        }
    }
    */

    public static final class Histo implements Codon {
        private final short[] _h;
        private final int[] _z;

        public Histo(int colors) {
            _h = new short[colors];
            _z = new int[8192];
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public void op(int[] p, Tape t) {
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
    }

    public static class Histold implements Codon {
        private final int[] _h;

        public Histold(int colors) {
            _h = new int[colors];
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public boolean usesPattern() {
            return true;
        }

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

        @Override public String code() {
            return SUM;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            //t.selectAgg(-1);
            t.selectAggAll();
            t.apply(this, p);
            //int m = t.popAll(_t, -1);
            //int s = 0;
            //for(int i=0;i<m;i++) {
                //s += _t[i];
            //}
            //t.push(s);
        }

        @Override public int op(int[] t, int st, int en, int[] p) {
            int s = 0;
            for(int i=st;i<=en;i++) {
                s += t[i];
            }
            return s;
        }
    }

    /*
    public static class OSum implements Codon {
        private final int[] _t = new int[BUF_SIZE];

        @Override public String code() {
            return SUM;
        }

        @Override public void op(int[] p, Tape t) {
            int m = t.popAll(_t, -1);
            int s = 0;
            for(int i=0;i<m;i++) {
                s += _t[i];
            }
            t.push(s);
        }
    }
    */

    public abstract static class NAggregate implements Codon, Tape.TapeOp {
        private final String _n;
        private final int _c;
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

        @Override public void op(int[] p, Tape t) {
            if(_c==-1) {
                t.selectAggAll();
            }
            else {
                t.selectAgg(_c);
            }
            t.apply(this, p);
            //t.op(this, p);
            //int m = t.popAll(_t, _c);
            //int a = agg(_t, m);
            //t.push(a);
        }

        /*
        @Override public int op(final int[] tape, final int start, final int end, final int[] pat) {
            //System.err.println("start="+start+", end="+end);
            if(end==start||end==-1) {
                return end;
            }
            int tl = 1+end-start;
            int s = _c==-1?tl:_c;
            //System.err.println("s="+s+", tl="+tl);
            if(s>tl) {
                s = s%tl;
            }
            else if(s<0) {
                s = (-s)%tl;
            }
            if(s==0) {
                return end;
            }
            int si = 1+end-s;
            //System.err.println("si="+si);
            if(si==end) {
                //tape[end]=0;
                return end;
            }
            int a = agg(tape, si, end);
            //System.err.println("a="+a);
            //tape[end] = a;
            //return end;
            tape[si] = a;
            return si;
        }
        */

        @Override public String generate(Random r) {
            return _c==-1?_n:(_n+(1+r.nextInt(9)));
        }

        //abstract int agg(int[] p, int m, int e);
    }

    /*
    public abstract static class Aggregate implements Codon {
        private final String _n;
        private final int _c;
        private final int[] _t = new int[BUF_SIZE];

        public Aggregate(String n, int c) {
            _n = n;
            _c = c;
        }

        @Override public String code() {
            return _c==-1?_n:(_n+_c);
        }

        @Override public void op(int[] p, Tape t) {
            int m = t.popAll(_t, _c);
            int a = agg(_t, m);
            t.push(a);
        }

        @Override public String generate(Random r) {
            return _c==-1?_n:(_n+(1+r.nextInt(9)));
        }

        abstract int agg(int[] p, int m);
    }
    */

    public abstract static class NAggregateN implements Codon, Tape.TapeOp {
        //private int dump = 0;
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

        @Override public void op(int[] p, Tape t) {
            int n = t.pop();
            //++dump;
            //if(dump<10) System.err.println("agg s="+n);
            t.selectAgg(n);
            t.apply(this, p);
            //t.op(this, p);
            /*
            int s = t.pop();
            if(s>=_t.length) {
                s = s%_t.length;
            }
            else if(s<0) {
                s = (-s)%_t.length;
            }
            int m = t.popAll(_t, s);
            int a = agg(_t, m);
            t.push(a);
            */
        }

        @Override public String generate(Random r) {
            return _n;
        }

        /*
        public int nop(final int[] tape, final int start, final int end, final int[] pat) {
            //System.err.println("start="+start+", end="+end);
            if(end==start||end==-1) {
                return end;
            }
            int s = tape[end];
            int tl = end-start;
            //System.err.println("s="+s+", tl="+tl);
            if(s>tl) {
                s = s%tl;
            }
            else if(s<0) {
                s = (-s)%tl;
            }
            if(s==0) {
                return end;
            }
            int si = end-s;
            //System.err.println("si="+si);
            if(si==end-1) {
                //tape[end]=0;
                return end;
            }
            int a = agg(tape, si, end-1);
            //System.err.println("a="+a);
            //tape[end] = a;
            //return end;
            tape[si] = a;
            return si;
        }
        */

        //abstract int agg(int[] p, int m, int e);
    }

    /*
    public abstract static class AggregateN implements Codon {
        //private int dump = 0;
        private final String _n;
        private final int[] _t = new int[BUF_SIZE];

        public AggregateN(String n) {
            _n = n;
        }

        @Override public String code() {
            return _n;
        }

        @Override public void op(int[] p, Tape t) {
            int s = t.pop();
            //++dump;
            //if(dump<10) System.err.println("agg s="+s);
            if(s>=_t.length) {
                s = s%_t.length;
            }
            else if(s<0) {
                s = (-s)%_t.length;
            }
            int m = t.popAll(_t, s);
            int a = agg(_t, m);
            //if(dump<10) System.err.println("agg m="+m+", a="+a);
            t.push(a);
        }

        @Override public String generate(Random r) {
            return _n;
        }

        abstract int agg(int[] p, int m);
    }
    */

    public static class Skip implements Codon {
        private final int _c;
        //private final int[] _t = new int[1024];

        public Skip(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?SKIP:(SKIP+_c);
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            //int m = t.popAll(_t, _c);
            t.skip(_c);
        }

        @Override public String generate(Random r) {
            return SKIP+(1+r.nextInt(9));
        }
    }

    public static class SkipN implements Codon {
        //private final int[] _t = new int[1024];

        @Override public String code() {
            return SKIP_N;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            int s = t.pop();
            //try {
                //int m = t.popAll(_t, s);
                t.skip(s);
            //}
            //catch(Exception e) {
                //System.err.println("FAILED popping "+s+" from "+t);
                //e.printStackTrace();
            //}
        }

        @Override public String generate(Random r) {
            return SKIP_N;
        }
    }

    public static final class Push implements Codon, Unstable {
        private final int _p;
        private final int _m;

        public Push(int p, int m) {
            _p = p;
            _m = m;
        }

        @Override public String code() {
            return PUSH+_p;
        }

        @Override public boolean usesPattern() {
            return true;
        }

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
    /*
    public static class Push implements Codon {
        private final int _p;

        public Push(int p) {
            _p = p;
        }

        @Override public String code() {
            return PUSH+_p;
        }

        @Override public void op(int[] p, Tape t) {
            t.push(p[_p]);
        }

        @Override public String generate(Random r) {
            return PUSH+(_p==-1?r.nextInt(9):_p);
        }
    }
    */

    public static class PushN implements Codon {
        public PushN() {
        }

        @Override public String code() {
            return PUSH_N;
        }

        @Override public boolean usesPattern() {
            return true;
        }

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

        @Override public String code() {
            return PUSH_S;
        }

        @Override public boolean usesPattern() {
            return true;
        }

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

        @Override public String code() {
            return CONS+_p;
        }

        @Override public boolean usesPattern() {
            return true;
        }

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

        @Override public boolean usesPattern() {
            return true;
        }

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

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public void op(int[] p, Tape t) {
            //t.pushAll(p, p.length);
            int n = p[p.length/2]%p.length; //t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
        }
    }

    public static class PushARot implements Codon {
        @Override public String code() {
            return PUSH_ALL_ROT;
        }

        @Override public boolean usesPattern() {
            return true;
        }

        @Override public void op(int[] p, Tape t) {
            int n = t.pop() % p.length;
            if(n<0) n=-n;
            t.pushAll(p, p.length-n, n);
            t.pushAll(p, n);
        }
    }

    public static class Intersects implements Codon {
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
            //int v1 = t.pop();
            //int v2 = t.pop();
            //int m = expr(v1, v2);
            //t.push(m);
        }

        @Override public int op(int[] t, int s, int e, int[] p) {
            //int te = s==e?0:t[e];
            //int m = expr(t[s], te);
            int ts = s==e?t[s]:t[s];
            int m = expr(t[e], ts);
            return m;
        }

        abstract int expr(int v1, int v2);
    }

    /*
    public abstract static class OBinary implements Codon {
        private final String _code;

        public OBinary(String code) {
            _code = code;
        }

        @Override public String code() {
            return _code;
        }

        @Override public void op(int[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int m = expr(v1, v2);
            t.push(m);
        }

        abstract int expr(int v1, int v2);
    }
    */

    public static class Subtract extends Binary {
        public Subtract() { super(SUBTRACT); }
        @Override int expr(int v1, int v2) {
            return v1-v2;
        }
    }

    public static class Multiply extends Binary {
        public Multiply() { super(MULTIPLY); }
        @Override int expr(int v1, int v2) {
            return v1*v2;
        }
    }

    public static class Divide extends Binary {
        public Divide() { super(DIVIDE); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1/v2;
        }
    }

    public static class Mod extends Binary {
        public Mod() { super(MOD); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1%v2;
        }
    }

    public static class Pow extends Binary {
        public Pow() { super(POW); }
        @Override int expr(int v1, int v2) {
            int v = Maths.pow(v1,Math.max(0,Math.abs(v2)));
            return v;
        }
    }

    public static class Sqrt implements Codon {
        @Override public String code() {
            return SQRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            //int v = Maths.pow(v1,Math.max(0,Math.abs(v2)));
            int v = (int) Math.sqrt(Math.abs(t.pop()));
            t.push(v);
        }
    }

    public static class Cbrt implements Codon {
        @Override public String code() {
            return CBRT;
        }

        @Override public boolean usesPattern() {
            return false;
        }

        @Override public void op(int[] p, Tape t) {
            //int v = Maths.pow(v1,Math.max(0,Math.abs(v2)));
            int v = (int) Math.cbrt(t.pop());
            t.push(v);
        }
    }

    public static class Sumn extends NAggregate {
        public Sumn() { super(SUM, -1); }
        public Sumn(int c) { super(SUM, c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }
        /*
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int s = vs[0];
            for(int i=1;i<m;i++) {
                s += vs[i];
            }
            return s;
        }
        */
    }

    public static class SumnN extends NAggregateN {
        public SumnN() { super(SUM_N); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int s = vs[m];
            for(int i=m+1;i<=e;i++) {
                s += vs[i];
            }
            return s;
        }
        //@Override int agg(int[] vs, int m) {
            //if(m==0) return 0;
            //int s = vs[0];
            //for(int i=1;i<m;i++) {
                //s += vs[i];
            //}
            //return s;
        //}
    }

    public static class Min extends NAggregate {
        public Min() { super(MIN, -1); }
        public Min(int c) { super(MIN, c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
        //@Override int agg(int[] vs, int m) {
            //if(m==0) return 0;
            //int min = vs[0];
            //for(int i=1;i<m;i++) {
                //if(vs[i]<min) min=vs[i];
            //}
            //return min;
        //}
    }

    public static final class MinN extends NAggregateN {
        public MinN() { super(MIN_N); }

        @Override public int op(int[] vs, int m, int e, int[] p) {
            int min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }

        /*
        @Override int agg(int[] vs, int m, int e) {
            int min = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
        */
    }

    /*
    public static class MinN extends AggregateN {
        public MinN() { super(MIN_N); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int min = vs[0];
            for(int i=1;i<m;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
    }
    */

    public static class Max extends NAggregate {
        public Max() { super(MAX, -1); }
        public Max(int c) { super(MAX, c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                final int t = vs[i];
                if(t>max) max=t;
            }
            return max;
        }
        /*
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int max = vs[0];
            for(int i=1;i<m;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
        */
    }

    public static final class MaxN extends NAggregateN {
        public MaxN() { super(MAX_N); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
/*
        @Override int agg(int[] vs, int m, int e) {
            int max = vs[m];
            for(int i=m+1;i<=e;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
    */
    }

    /*
    public static class MaxN extends AggregateN {
        public MaxN() { super(MAX_N); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int max = vs[0];
            for(int i=1;i<m;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
    }
    */

    public static final class Avg extends NAggregate {
        public Avg() { super(AVG, -1); }
        public Avg(int c) { super(AVG, c); }
        @Override public int op(int[] vs, int m, int e, int[] p) {
            int sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            return sum/(1+e-m);
        }
        //@Override int agg(int[] vs, int m, int e) {
            //int sum = vs[m];
            //for(int i=m+1;i<=e;i++) {
                //sum += vs[i];
            //}
            //return sum/(1+e-m);
        //}
    }

    /*
    public static class Avg extends Aggregate {
        public Avg() { super(AVG, -1); }
        public Avg(int c) { super(AVG, c); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int sum = vs[0];
            for(int i=1;i<m;i++) {
                sum += vs[i];
            }
            return sum/m;
        }
    }
    */

    private static void dump(int[] vs, int m, int e, int[] p, int sum, int res) {
        System.err.println("vs.length="+vs.length+", m="+m+", e="+e+", sum="+sum+", res="+res);
    }

    public static final class AvgN extends NAggregateN {
        //private int dump = 0;
//
        public AvgN() { super(AVG_N); }
        /*
        @Override int agg(int[] vs, int m, int e) {
            int sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            return sum/(1+e-m);
        }
        */

        @Override public int op(int[] vs, int m, int e, int[] p) {
            //if(e-m==0) return 0;
            int sum = vs[m];
            for(int i=m+1;i<=e;i++) {
                sum += vs[i];
            }
            int res = sum/(1+e-m);
            //if(dump++<10) {
                //dump(vs, m, e, p, sum, res);
            //}
            return res;
        }
    }

    /*
    public static class AvgN extends AggregateN {
        private int dump = 0;

        public AvgN() { super(AVG_N); }
        @Override int agg(int[] vs, int m) {
            if(m==0) {
                if(dump++<10) {
                    System.err.println("zero");
                }
                return 0;
            }
            int sum = vs[0];
            for(int i=1;i<m;i++) {
                sum += vs[i];
            }
            int res = sum/m;
            if(dump++<10) {
                dump(vs, 0, m, null, sum, res);
            }
            return res;
        }
    }
    */

    public static class Xor extends Binary {
        public Xor() { super(XOR); }
        @Override int expr(int v1, int v2) {
            return v1^v2;
        }
    }

    public static class GreaterThan extends Binary {
        public GreaterThan() { super(GT); }
        @Override int expr(int v1, int v2) {
            return v1>v2?1:0;
        }
    }

    public static class LessThan extends Binary {
        public LessThan() { super(LT); }
        @Override int expr(int v1, int v2) {
            return v1<v2?1:0;
        }
    }

    public static final class Greater extends Binary {
        public Greater() { super(GREATER); }
        @Override int expr(int v1, int v2) {
            return v1>=v2?v1:0;
        }
    }

    public static final class Lesser extends Binary {
        public Lesser() { super(LESSER); }
        @Override int expr(int v1, int v2) {
            return v1<=v2?v1:0;
        }
    }

    public static class And extends Binary {
        public And() { super(AND); }
        @Override int expr(int v1, int v2) {
            return v1&v2;
        }
    }

    public static class Or extends Binary {
        public Or() { super(OR); }
        @Override int expr(int v1, int v2) {
            return v1|v2;
        }
    }

    public static class Rotright extends Binary {
        public Rotright() { super(ROT_RIGHT); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateRight(v1,v2);
        }
    }

    public static class Rotleft extends Binary {
        public Rotleft() { super(ROT_LEFT); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateLeft(v1,v2);
        }
    }

    public static class If implements Codon {
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
