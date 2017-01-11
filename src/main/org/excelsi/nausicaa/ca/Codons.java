package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Codons {
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
            case "s":
                return p==-1?new Sum():new Sumn(p);
            case "p":
                return new Push(p);
            case "mo":
                return new Mod();
            case "pO":
                return new PushO();
            case "i":
                return new Intersects();
            case "c":
                return new Constant(p);
            case "eq":
                return new Equals();
            case "ne":
                return new NotEquals();
            case "if":
                return new If();
            case "su":
                return new Subtract();
            case "mu":
                return new Multiply();
            case "dv":
                return new Divide();
            case "po":
                return new Pow();
            case "mn":
                return new Min(p);
            case "mx":
                return new Max(p);
            case "av":
                return new Avg(p);
            case "xo":
                return new Xor();
            case "an":
                return new And();
            case "or":
                return new Or();
            case "rr":
                return new Rotright();
            case "rl":
                return new Rotleft();
            case "x":
                return new Skip(p);
            case "nz":
                return new Nonzero(p);
            case "t":
                return new Time();
            case "hi":
                return new Histo(a.colors());
            default:
                throw new IllegalStateException("unknown opcode '"+code+"'");
        }
    }

    public static class Nonzero implements Codon {
        private final int[] _t = new int[100];
        private final int _c;

        public Nonzero(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?"nz":("nz"+_c);
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
            return "hi";
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
        private final int[] _t = new int[100];

        @Override public String code() {
            return "s";
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
        private final int[] _t = new int[100];

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

    public static class Skip implements Codon {
        private final int _c;
        private final int[] _t = new int[100];

        public Skip(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?"x":("x"+_c);
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
        }

        @Override public String generate(Random r) {
            return "x"+r.nextInt(9);
        }
    }

    public static class Push implements Codon {
        private final int _p;

        public Push(int p) {
            _p = p;
        }

        @Override public String code() {
            return "p"+_p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(p[_p]);
        }

        @Override public String generate(Random r) {
            return "p"+(_p==-1?r.nextInt(9):_p);
        }
    }


    public static class Constant implements Codon {
        private final int _p;

        public Constant(int p) {
            _p = p;
        }

        @Override public String code() {
            return "c"+_p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(_p);
        }

        @Override public String generate(Random r) {
            return "c"+(_p==-1?r.nextInt(9):_p);
        }
    }

    public static class PushO implements Codon {
        @Override public String code() {
            return "pO";
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

    public static class Intersects implements Codon {
        @Override public String code() {
            return "i";
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
            return "eq";
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
            return "t";
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
            return "ne";
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
        public Subtract() { super("su"); }
        @Override int expr(int v1, int v2) {
            return v1-v2;
        }
    }

    public static class Multiply extends Binary {
        public Multiply() { super("mu"); }
        @Override int expr(int v1, int v2) {
            return v1*v2;
        }
    }

    public static class Divide extends Binary {
        public Divide() { super("dv"); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1/v2;
        }
    }

    public static class Mod extends Binary {
        public Mod() { super("mo"); }
        @Override int expr(int v1, int v2) {
            return v2==0?v1:v1%v2;
        }
    }

    public static class Pow extends Binary {
        public Pow() { super("po"); }
        @Override int expr(int v1, int v2) {
            return (int)Math.pow(v1,v2);
        }
    }

    public static class Sumn extends Aggregate {
        public Sumn() { super("s", -1); }
        public Sumn(int c) { super("s", c); }
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
        public Min() { super("mn", -1); }
        public Min(int c) { super("mn", c); }
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
        public Max() { super("mx", -1); }
        public Max(int c) { super("mx", c); }
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
        public Avg() { super("av", -1); }
        public Avg(int c) { super("av", c); }
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
        public Xor() { super("xo"); }
        @Override int expr(int v1, int v2) {
            return v1^v2;
        }
    }

    public static class And extends Binary {
        public And() { super("an"); }
        @Override int expr(int v1, int v2) {
            return v1&v2;
        }
    }

    public static class Or extends Binary {
        public Or() { super("or"); }
        @Override int expr(int v1, int v2) {
            return v1|v2;
        }
    }

    public static class Rotright extends Binary {
        public Rotright() { super("rr"); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateRight(v1,v2);
        }
    }

    public static class Rotleft extends Binary {
        public Rotleft() { super("rl"); }
        @Override int expr(int v1, int v2) {
            return Integer.rotateLeft(v1,v2);
        }
    }

    public static class If implements Codon {
        @Override public String code() {
            return "if";
        }

        @Override public void op(byte[] p, Tape t) {
            int cond = t.pop();
            int fl = t.pop();
            int tr = t.pop();
            int res = cond!=0?tr:fl;
            t.push(res);
        }
    }
}
