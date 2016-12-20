package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public final class ComputedPattern implements Pattern {
    public static final RuleLogic IDENTITY = new RARule("id", (p->(byte)(p[(p.length-1)/2])), b->b);
    public static final RuleLogic LIFE = new RARule("life",
        (p->{
            int t=PatternOp.sumo(p);
            int r;
            if(p[4]==0) {
                r = t>2&&t<=3?1:0;
            }
            else {
                r = t>=2&&t<=3?1:0;
            }
            return (byte)r;
        }),
        b->b);

    private final Archetype _a;
    private final RuleLogic _logic;


    public static RuleLogic cyclic(final Archetype a) {
        return new RARule("cyclic",
            (p->{
                int t = p[4]+1;
                if(t>=a.colors()) {
                    t=0;
                }
                return (byte)t;
            }),
            b->b);
    }

    public static RuleLogic random(final Archetype a, final Random r) {
        final PatternOp[] ops = new PatternOp[a.sourceLength()];
        for(int i=0;i<ops.length;i++) {
            final PatternOp op;
            switch(r.nextInt(2)) {
                case 0:
                    op = PatternOp.plus();
                    break;
                case 1:
                    op = PatternOp.minus();
                    break;
                case 2:
                    op = PatternOp.multiply();
                    break;
                case 3:
                default:
                    op = PatternOp.divide();
                    break;
            }
            ops[i] = op;
        }
        final PatternOp red = PatternOp.mod();
        return new RARule("rand",
            (p->{
                int t = p[0];
                for(int i=1;i<p.length;i++) {
                    t = ops[i-1].op(t, p[i]);
                }
                return (byte)t;
            }),
            b->(byte)red.op(b,a.colors()));
    }

    public ComputedPattern(Archetype a, RuleLogic logic) {
        _a = a;
        _logic = logic;
    }

    @Override public Archetype archetype() {
        return _a;
    }

    @Override public byte next(int pattern, final byte[] p2) {
        return _logic.next(p2);
    }

    @Override public void tick() {
    }

    @FunctionalInterface
    public interface RuleLogic {
        byte next(byte[] pattern);
    }

    public static class RARule implements RuleLogic {
        private final String _n;
        private final Reducer _r;
        private final Activator _a;


        public RARule(String name, Reducer r, Activator a) {
            _n = name;
            _r = r;
            _a = a;
        }

        @Override public byte next(byte[] pattern) {
            //System.err.print("pat: "+Patterns.formatPattern(pattern));
            //final byte b1 = _r.reduce(pattern);
            //System.err.print("\tred: "+(int)b1);
            //final byte b2 = _a.activate(b1);
            //System.err.println("\tact: "+(int)b2);
            return _a.activate(_r.reduce(pattern));
        }

        @Override public String toString() {
            return _n;
        }
    }

    @Override public String toString() {
        return _logic.toString();
    }

    @FunctionalInterface public interface Reducer {
        byte reduce(byte[] b);
    }

    @FunctionalInterface public interface Activator {
        byte activate(byte target);
    }
}
