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
                return new Min();
            case "mx":
                return new Max();
            case "av":
                return new Avg();
            case "hi":
                return new Histo(a.colors());
            default:
                throw new IllegalStateException("unknown opcode '"+code+"'");
        }
    }

    public static class Histo implements Codon {
        private final int[] _h;
        private final int[] _t = new int[100];

        public Histo(int colors) {
            _h = new int[colors];
        }

        @Override public String code() {
            return "hi";
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, -1);
            for(int i=0;i<_h.length;i++) {
                _h[i] = 0;
            }
            for(int i=0;i<m;i++) {
                _h[_t[i]]++;
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

    public static class Sumn implements Codon {
        private final int _c;
        private final int[] _t = new int[100];

        public Sumn(int c) {
            _c = c;
        }

        @Override public String code() {
            return _c==-1?"s":("s"+_c);
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
            int s = 0;
            for(int i=0;i<m;i++) {
                s += _t[i];
            }
            t.push(s);
        }

        @Override public String generate(Random r) {
            return "s"+r.nextInt(9);
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

    public static class Min extends Binary {
        public Min() { super("mn"); }
        @Override int expr(int v1, int v2) {
            return v1<v2?v1:v2;
        }
    }

    public static class Max extends Binary {
        public Max() { super("mx"); }
        @Override int expr(int v1, int v2) {
            return v1<v2?v2:v1;
        }
    }

    public static class Avg extends Binary {
        public Avg() { super("av"); }
        @Override int expr(int v1, int v2) {
            return (v1+v2)/2;
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
