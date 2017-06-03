package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.Arrays;


public class Codons {
    //public static final String SUM = "s";
    //public static final String PUSH = "p";
    //public static final String PUSH_SURROUND = "pO";
    //public static final String MOD = "mo";
    //public static final String INTERSECT = "i";
    //public static final String CONS = "c";
    //public static final String EQUAL = "eq";
    //public static final String NOT_EQUAL = "ne";
    //public static final String IF = "if";
    //public static final String SUBTRACT = "su";
    //public static final String MULTIPLY = "mu";
    //public static final String DIVIDE = "dv";
    //public static final String POW = "po";
    //public static final String MIN = "mn";
    //public static final String MAX = "mx";
    //public static final String AVG = "av";
    //public static final String XOR = "xo";
    //public static final String AND = "an";
    //public static final String OR = "or";
    //public static final String ROT_RIGHT = "rl";
    //public static final String ROT_LEFT = "rl";
    //public static final String SKIP = "x";
    //public static final String NON_ZERO = "nz";
    //public static final String TIME = "t";
    //public static final String HISTO = "hi";
    //public static final String DUPLICATE = "du";
    //public static final String EXCLAMATORY = "ex";

    public static final String SUM = "mi";
    public static final String SUM_N = "me";
    public static final String PUSH = "o";
    public static final String PUSH_N = "no";
    public static final String PUSH_S = "ya";
    public static final String PUSH_SURROUND = "ki";
    public static final String PUSH_ALL = "go";
    public static final String MOD = "mo";
    public static final String INTERSECT = "u";
    public static final String CONS = "a";
    public static final String EQUAL = "ma";
    public static final String NOT_EQUAL = "ne";
    public static final String IF = "ra";
    public static final String SUBTRACT = "su";
    public static final String MULTIPLY = "mu";
    public static final String DIVIDE = "ho";
    public static final String POW = "i";
    public static final String MIN = "chi";
    public static final String MAX = "ta";
    public static final String AVG = "gi";
    public static final String MIN_N = "jo";
    public static final String MAX_N = "ri";
    public static final String AVG_N = "e";
    public static final String XOR = "tsu";
    public static final String AND = "to";
    public static final String OR = "ka";
    public static final String ROT_RIGHT = "ku";
    public static final String ROT_LEFT = "ko";
    public static final String SKIP = "ro";
    public static final String SKIP_N = "ji";
    public static final String NON_ZERO = "zu";
    public static final String TIME = "te";
    public static final String HISTO = "hi";
    public static final String DUPLICATE = "do";
    public static final String EXCLAMATORY = "ha";
    public static final String SUPERSYMMETRY = "hu";
    public static final String ROT_VEC_N = "ba";

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
                return new Push(p);
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
            case INTERSECT:
                return new Intersects();
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
            default:
                throw new IllegalStateException("unknown opcode '"+code+"'");
        }
    }

    public static class Nonzero implements Codon {
        private final int[] _t = new int[256];
        private final int _c;

        public Nonzero(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?NON_ZERO:(NON_ZERO+_c);
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
            for(int i=0;i<m;i++) {
                if(_t[i]!=0) {
                    t.push(_t[i]);
                }
            }
        }
    }

    public static class Histo implements Codon {
        private final int[] _h;

        public Histo(int colors) {
            _h = new int[colors];
        }

        @Override public String code() {
            return HISTO;
        }

        @Override public void op(byte[] p, Tape t) {
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

    public static class Sum implements Codon {
        private final int[] _t = new int[256];

        @Override public String code() {
            return SUM;
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, -1);
            int s = 0;
            for(int i=0;i<m;i++) {
                s += _t[i];
            }
            t.push(s);
        }
    }

    public abstract static class Aggregate implements Codon {
        private final String _n;
        private final int _c;
        private final int[] _t = new int[256];

        public Aggregate(String n, int c) {
            _n = n;
            _c = c;
        }

        @Override public String code() {
            return _c==-1?_n:(_n+_c);
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
            int a = agg(_t, m);
            t.push(a);
        }

        @Override public String generate(Random r) {
            return _c==-1?_n:(_n+(1+r.nextInt(9)));
        }

        abstract int agg(int[] p, int m);
    }

    public abstract static class AggregateN implements Codon {
        private final String _n;
        private final int[] _t = new int[1024];

        public AggregateN(String n) {
            _n = n;
        }

        @Override public String code() {
            return _n;
        }

        @Override public void op(byte[] p, Tape t) {
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
        }

        @Override public String generate(Random r) {
            return _n;
        }

        abstract int agg(int[] p, int m);
    }

    public static class Skip implements Codon {
        private final int _c;
        private final int[] _t = new int[100];

        public Skip(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?SKIP:(SKIP+_c);
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
        }

        @Override public String generate(Random r) {
            return SKIP+(1+r.nextInt(9));
        }
    }

    public static class SkipN implements Codon {
        private final int[] _t = new int[256];

        @Override public String code() {
            return SKIP_N;
        }

        @Override public void op(byte[] p, Tape t) {
            int s = t.pop();
            try {
                //int m = t.popAll(_t, s);
                t.skip(s);
            }
            catch(Exception e) {
                System.err.println("FAILED popping "+s+" from "+t);
                e.printStackTrace();
            }
        }

        @Override public String generate(Random r) {
            return SKIP_N;
        }
    }

    public static class Push implements Codon {
        private final int _p;

        public Push(int p) {
            _p = p;
        }

        @Override public String code() {
            return PUSH+_p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(p[_p]);
        }

        @Override public String generate(Random r) {
            return PUSH+(_p==-1?r.nextInt(9):_p);
        }
    }

    public static class PushN implements Codon {
        public PushN() {
        }

        @Override public String code() {
            return PUSH_N;
        }

        @Override public void op(byte[] p, Tape t) {
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

        @Override public void op(byte[] p, Tape t) {
            t.push(p[p.length/2]);
        }

        @Override public String generate(Random r) {
            return PUSH_S;
        }
    }

    public static class Constant implements Codon {
        private final int _p;

        public Constant(int p) {
            _p = p;
        }

        @Override public String code() {
            return CONS+_p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(_p);
        }

        @Override public String generate(Random r) {
            return CONS+(_p==-1?r.nextInt(27):_p);
        }
    }

    public static class PushO implements Codon {
        @Override public String code() {
            return PUSH_SURROUND;
        }

        @Override public void op(byte[] p, Tape t) {
            int mid = p.length/2;
            for(int i=0;i<mid;i++) {
                t.push(p[i]);
            }
            for(int i=mid+1;i<p.length;i++) {
                t.push(p[i]);
            }
        }
    }

    public static class PushA implements Codon {
        @Override public String code() {
            return PUSH_ALL;
        }

        @Override public void op(byte[] p, Tape t) {
            for(int i=0;i<p.length;i++) {
                t.push(p[i]);
            }
        }
    }

    public static class Intersects implements Codon {
        @Override public String code() {
            return INTERSECT;
        }

        @Override public void op(byte[] p, Tape t) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?1:0;
            t.push(in);
        }
    }

    public static class Equals implements Codon {
        @Override public String code() {
            return EQUAL;
        }

        @Override public void op(byte[] p, Tape t) {
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

        @Override public void op(byte[] p, Tape t) {
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

        @Override public void op(byte[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1!=v2)?1:0;
            t.push(eq);
        }
    }

    public abstract static class Binary implements Codon {
        private final String _code;

        public Binary(String code) {
            _code = code;
        }

        @Override public String code() {
            return _code;
        }

        @Override public void op(byte[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int m = expr(v1, v2);
            t.push(m);
        }

        abstract int expr(int v1, int v2);
    }

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
            long st = System.currentTimeMillis();
            int v = Maths.pow(v1,Math.abs(v2));
            long en = System.currentTimeMillis();
            if(en-st>2000) {
                System.err.println("took "+(en-st)+" for pow("+v1+","+v2+")");
            }
            return v;
        }
    }

    public static class Sumn extends Aggregate {
        public Sumn() { super(SUM, -1); }
        public Sumn(int c) { super(SUM, c); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int s = vs[0];
            for(int i=1;i<m;i++) {
                s += vs[i];
            }
            return s;
        }
    }

    public static class SumnN extends AggregateN {
        public SumnN() { super(SUM_N); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int s = vs[0];
            for(int i=1;i<m;i++) {
                s += vs[i];
            }
            return s;
        }
    }

    public static class Min extends Aggregate {
        public Min() { super(MIN, -1); }
        public Min(int c) { super(MIN, c); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int min = vs[0];
            for(int i=1;i<m;i++) {
                if(vs[i]<min) min=vs[i];
            }
            return min;
        }
    }

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

    public static class Max extends Aggregate {
        public Max() { super(MAX, -1); }
        public Max(int c) { super(MAX, c); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int max = vs[0];
            for(int i=1;i<m;i++) {
                if(vs[i]>max) max=vs[i];
            }
            return max;
        }
    }

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

    public static class AvgN extends AggregateN {
        public AvgN() { super(AVG_N); }
        @Override int agg(int[] vs, int m) {
            if(m==0) return 0;
            int sum = vs[0];
            for(int i=1;i<m;i++) {
                sum += vs[i];
            }
            return sum/m;
        }
    }

    public static class Xor extends Binary {
        public Xor() { super(XOR); }
        @Override int expr(int v1, int v2) {
            return v1^v2;
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

        @Override public void op(byte[] p, Tape t) {
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

        @Override public void op(byte[] p, Tape t) {
            int v = t.pop();
            t.push(v);
            t.push(v);
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

        @Override public void op(byte[] p, Tape t) {
            int v = t.pop();
            int r = _max-v;
            t.push(r);
        }
    }

    public static class Exclamatory implements Codon {
        @Override public String code() {
            return POW;
        }

        @Override public void op(byte[] p, Tape t) {
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

        @Override public void op(byte[] p, Tape t) {
            int dist = t.pop();
            int len = Math.min(t.pop(), _v.length);
            //int ipeek = t.peek();
            int m = t.popAll(_v, len);
            for(int i=0;i<len;i++) {
                int dest = (i+dist)%len;
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
