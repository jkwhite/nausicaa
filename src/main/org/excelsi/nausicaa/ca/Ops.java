package org.excelsi.nausicaa.ca;


public class Ops {
    public static Op op(final String s) {
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
                return new Sum(p);
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
            default:
                throw new IllegalStateException("unknown opcode '"+code+"'");
        }
    }

    static class Sum implements Op {
        private final int _c;
        private final int[] _t = new int[100];

        public Sum(int c) {
            _c = c;
        }

        @Override public void op(byte[] p, Tape t) {
            int m = t.popAll(_t, _c);
            int s = 0;
            //System.err.print("m="+m+", [");
            //for(int i=0;i<m;i++) {
                //System.err.print(_t[i]+", ");
            //}
            //System.err.println("]");
            for(int i=0;i<m;i++) {
                s += _t[i];
            }
            t.push(s);
        }
    }

    static class Push implements Op {
        private final int _p;

        public Push(int p) {
            _p = p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(p[_p]);
        }
    }


    static class Constant implements Op {
        private final int _p;

        public Constant(int p) {
            _p = p;
        }

        @Override public void op(byte[] p, Tape t) {
            t.push(_p);
        }
    }

    static class PushO implements Op {
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

    static class Intersects implements Op {
        @Override public void op(byte[] p, Tape t) {
            int up = t.pop();
            int low = t.pop();
            int mid = t.pop();
            int in = (mid >= low && mid <= up)?1:0;
            t.push(in);
        }
    }

    static class Equals implements Op {
        @Override public void op(byte[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int eq = (v1==v2)?1:0;
            t.push(eq);
        }
    }

    static class Mod implements Op {
        @Override public void op(byte[] p, Tape t) {
            int v1 = t.pop();
            int v2 = t.pop();
            int m = v1%v2;
            t.push(m);
        }
    }

    static class If implements Op {
        @Override public void op(byte[] p, Tape t) {
            int cond = t.pop();
            int fl = t.pop();
            int tr = t.pop();
            int res = cond!=0?tr:fl;
            t.push(res);
        }
    }
}
