package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public final class ComputedPattern implements Pattern, Mutatable {
    public static final RuleLogic IDENTITY = new RARule("id",
        new Reducer() {
            @Override public int reduce(int[] p) {
                return (int)(p[(p.length-1)/2]);
            }
            @Override public Reducer copy() { return this; }
        },
        new Activator() {
            @Override public int activate(int b) { return b; }
            @Override public Activator copy() { return this; }
        });

    public static final RuleLogic LIFE = new RARule("life",
        new Reducer() {
            @Override public int reduce(int[] p) {
                int t=PatternOp.sumo(p);
                int r;
                if(p[4]==0) {
                    r = t>2&&t<=3?1:0;
                }
                else {
                    r = t>=2&&t<=3?1:0;
                }
                return (int)r;
            }
            @Override public Reducer copy() { return this; }
        },
        new Activator() {
            @Override public int activate(int b) { return b; }
            @Override public Activator copy() { return this; }
        });

    private final Archetype _a;
    private final RuleLogic _logic;


    public static RuleLogic cyclic(final Archetype a) {
        return new RARule("cyclic",
            new Reducer() {
                @Override public int reduce(int[] p) {
                    int t = p[4]+1;
                    if(t>=a.colors()) {
                        t=0;
                    }
                    return (int)t;
                }
                @Override public Reducer copy() { return this; }
            },
            new Activator() {
                @Override public int activate(int b) { return b; }
                @Override public Activator copy() { return this; }
            });
    }

    public static RuleLogic random(final Archetype a, final Random r) {
        //return new MachineElf(new Machine(new Genome("pO-s-c2-c3-i-pO-s8-c3-eq-p4-if")));
        GenomeFactory gf = new GenomeFactory();
        return new MachineElf(new Machine(a, gf.generate(a, r)));
        //return new MachineElf(new Machine(new Genome("pO-s-c2-c3-i-pO-s8-c3-eq-p3-if")));
        //return new MachineElf(new Machine(new Genome("pO-s-c4-c7-i")));
    }

    public static RuleLogic serial_random(final Archetype a, final Random r) {
        final ArrayOp aop = ArrayOp.histo(a.colors());
        int len = 1+r.nextInt(a.colors());
        len = 9;
        int streak = 0;
        final PatternOp[] ops = new PatternOp[len];
        final StringBuilder n = new StringBuilder();
        int opi = -1;
        for(int i=0;i<ops.length;i++) {
            final PatternOp op;
            final char c;
            if(streak==0) {
                streak = 1+r.nextInt(9);
                opi = r.nextInt(19);
            }
            streak--;
            switch(opi) {
                case 0:
                    op = PatternOp.plus();
                    c = '+';
                    break;
                case 1:
                    op = PatternOp.minus();
                    c = '-';
                    break;
                case 2:
                    op = PatternOp.multiply();
                    c = '*';
                    break;
                case 3:
                    op = PatternOp.mod();
                    c = '%';
                    break;
                case 4:
                    op = PatternOp.max();
                    c = 'M';
                    break;
                case 5:
                    op = PatternOp.min();
                    c = 'm';
                    break;
                case 6:
                    op = PatternOp.pow();
                    c = 'p';
                    break;
                case 7:
                    op = PatternOp.minusa();
                    c = '_';
                    break;
                case 8:
                    op = PatternOp.xor();
                    c = '^';
                    break;
                case 9:
                    op = PatternOp.and();
                    c = '&';
                    break;
                case 10:
                    op = PatternOp.or();
                    c = '|';
                    break;
                case 11:
                    op = PatternOp.rotr();
                    c = '>';
                    break;
                case 12:
                    op = PatternOp.rotl();
                    c = '<';
                    break;
                case 13:
                    op = PatternOp.avg();
                    c = 'a';
                    break;
                case 14:
                    op = PatternOp.first();
                    c = 'f';
                    break;
                case 15:
                    op = PatternOp.last();
                    c = 'l';
                    break;
                case 16:
                    op = PatternOp.eq();
                    c = '=';
                    break;
                case 17:
                    op = PatternOp.neq();
                    c = '!';
                    break;
                case 18:
                default:
                    op = PatternOp.divide();
                    c = '/';
                    break;
            }
            ops[i] = op;
            n.append(c);
        }
        final PatternOp red;
        switch(r.nextInt(2)) {
            case 0:
                red = PatternOp.mod();
                n.append('%');
                break;
            default:
            case 1:
                red = PatternOp.mino();
                n.append('m');
                break;
        }
        return new RARule(n.toString(),
            new HiddenLayerReducer(a, aop, ops),
            new BoundedActivator(red, a)
        );
        /*
        return new RARule(n.toString(),
            (p->{
                final int[] q = aop.op(p);
                int t = q[0];
                int opidx = 0;
                for(int i=1;i<q.length;i++) {
                    t = ops[opidx].op(t, q[i]);
                    if(++opidx==ops.length) {
                        opidx = 0;
                    }
                }
                if(t<0) t=0;
                return (byte)t;
            }),
            b->{
                int v = red.op(b,a.colors());
                if(v<0) v=0;
                if(v>=a.colors()) v=a.colors()-1;
                return (byte) v;
            });
            */
    }

    private static class HiddenLayerReducer implements Reducer {
        private final Archetype _a;
        private final ArrayOp _aop;
        private final PatternOp[] _ops;

        public HiddenLayerReducer(Archetype a, ArrayOp aop, PatternOp[] ops) {
            _a = a;
            _aop = aop;
            _ops = ops;
        }

        @Override public int reduce(int[] p) {
            final int[] q = _aop.op(p);
            int t = q[0];
            int opidx = 0;
            for(int i=1;i<q.length;i++) {
                t = _ops[opidx].op(t, q[i]);
                if(++opidx==_ops.length) {
                    opidx = 0;
                }
            }
            if(t<0) t=0;
            return (int)t;
        }

        @Override public HiddenLayerReducer copy() {
            return new HiddenLayerReducer(
                _a,
                ArrayOp.histo(_a.colors()),
                _ops);
        }
    }

    private static class BoundedActivator implements Activator {
        private final PatternOp _red;
        private final Archetype _a;

        public BoundedActivator(PatternOp red, Archetype a) {
            _red = red;
            _a = a;
        }

        @Override public int activate(int b) {
            int v = _red.op(b,_a.colors());
            if(v<0) v=0;
            if(v>=_a.colors()) v=_a.colors()-1;
            return (int) v;
        }

        @Override public BoundedActivator copy() {
            return new BoundedActivator(_red, _a);
        }
    }

    public static RuleLogic orandom(final Archetype a, final Random r) {
        int len = 1+r.nextInt(a.sourceLength());
        len = 5;
        final PatternOp[] ops = new PatternOp[len];
        final StringBuilder n = new StringBuilder();
        for(int i=0;i<ops.length;i++) {
            final PatternOp op;
            final char c;
            switch(r.nextInt(17)) {
                case 0:
                    op = PatternOp.plus();
                    c = '+';
                    break;
                case 1:
                    op = PatternOp.minus();
                    c = '-';
                    break;
                case 2:
                    op = PatternOp.multiply();
                    c = '*';
                    break;
                case 3:
                    op = PatternOp.mod();
                    c = '%';
                    break;
                case 4:
                    op = PatternOp.max();
                    c = 'M';
                    break;
                case 5:
                    op = PatternOp.min();
                    c = 'm';
                    break;
                case 6:
                    op = PatternOp.pow();
                    c = 'p';
                    break;
                case 7:
                    op = PatternOp.minusa();
                    c = '_';
                    break;
                case 8:
                    op = PatternOp.xor();
                    c = '^';
                    break;
                case 9:
                    op = PatternOp.and();
                    c = '&';
                    break;
                case 10:
                    op = PatternOp.or();
                    c = '|';
                    break;
                case 11:
                    op = PatternOp.rotr();
                    c = '>';
                    break;
                case 12:
                    op = PatternOp.rotl();
                    c = '<';
                    break;
                case 13:
                    op = PatternOp.avg();
                    c = 'a';
                    break;
                case 14:
                    op = PatternOp.first();
                    c = 'f';
                    break;
                case 15:
                    op = PatternOp.last();
                    c = 'l';
                    break;
                case 16:
                default:
                    op = PatternOp.divide();
                    c = '/';
                    break;
            }
            ops[i] = op;
            n.append(c);
        }
        final PatternOp red;
        switch(r.nextInt(2)) {
            case 0:
                red = PatternOp.mod();
                n.append('%');
                break;
            default:
            case 1:
                red = PatternOp.min();
                n.append('m');
                break;
        }
        //0 1 2
        //3 4 5
        //6 7 8
        final int[] order = {1, 7, 3, 5, 0, 2, 6, 8, 4};
        return new RARule(n.toString(),
            new Reducer() {
                @Override public int reduce(int[] p) {
                    int t = p[order[0]];
                    int opidx = 0;
                    for(int i=1;i<p.length;i++) {
                        t = ops[opidx].op(t, p[order[i]]);
                        if(++opidx==ops.length) {
                            opidx = 0;
                        }
                    }
                    if(t<0) t=0;
                    return (int)t;
                }
                @Override public Reducer copy() { return this; }
            },
            new Activator() {
                @Override public int activate(int b) {
                    return (int)red.op(b,a.colors());
                }
                @Override public Activator copy() { return this; }
            });       
    }

    public ComputedPattern(Archetype a, RuleLogic logic) {
        _a = a;
        _logic = logic;
        _lastP = new int[(int)Math.pow(2*_a.size()+1, _a.dims())];
        int size = (int)Math.pow(2*_a.size()+1, _a.dims());
        _cache = new PCache(300000, size);
    }

    public ComputedPattern copy() {
        return new ComputedPattern(_a, _logic.copy());
    }

    @Override public Archetype archetype() {
        return _a;
    }

    @Override public byte next(int pattern, final byte[] p2) {
        //return _logic.next(p2);
        throw new UnsupportedOperationException();
    }

    static final class PCache {
        public boolean h;
        public int lk;
        private final int _psize;
        private final int _csize;
        //private final int _bsize;
        private final int[] _c;
        //private final int[][] _p;
        private final int[] _p;
        private final int[] _hot;

        public PCache(int csize, int psize) {
            _c = new int[csize];
            _hot = new int[csize];
            //_p = new int[csize][];
            _csize = csize;
            _psize = psize;
            //_bsize = _psize + 1;
            _p = new int[csize*psize];
            //_p = new int[_bsize*csize];
            System.err.println("plen: "+_p.length);
            //for(int i=0;i<_p.length;i++) {
                //_p[i] = new int[psize];
            //}
        }

        public int find(int[] p) {
            final int i = key(p);
            lk = i;
            h = true;
            int j = 0;
            //if(i<0) throw new IllegalStateException("negative i: "+i);
            for(int idx = i*_psize;idx<(i+1)*_psize;idx++) {
            //int idx;
            //for(idx = i*_bsize;idx<(i+1)*_bsize-1;idx++) {
                //if(j<0) throw new IllegalStateException("negative j: "+j+" idx: "+idx+" i: "+i+" psize: "+_psize);
                //if(idx<0) throw new IllegalStateException("negative idx: "+idx+" j: "+j+" i: "+i+" psize: "+_psize);
                if(p[j++]!=_p[idx]) {
                    h = false;
                    break;
                }
            }
            //h = Arrays.equals(p, _p[i]);
            return _c[i];
            //return _p[idx];
        }

        public void put(int[] p, int r) {
            //final int i = key(p);
            final int i = lk;
            _hot[i]++;
            System.arraycopy(p, 0, _p, i*_psize, p.length);
            //System.arraycopy(p, 0, _p, i*_bsize, p.length);
            _c[i] = r;
            //_p[i*_bsize+p.length] = r;
        }

        public void dump() {
            int w = 0;
            int b = 0;
            int max = 0;
            int min = 99999999;
            int mb = 0;
            for(int i=0;i<_csize;i++) {
                if(_hot[i]>0) {
                    w+=_hot[i];
                    b++;
                    if(max<_hot[i]) {
                        max = _hot[i];
                        mb = i;
                    }
                    if(min>_hot[i]) min = _hot[i];
                    //System.err.print(i+": "+_hot[i]);
                    //System.err.print(" ");
                }
                //if(i%10==0) {
                    //System.err.println();
                //}
            }
            System.err.println("total writes: "+w+" total buckets in use: "+b+" / "+_hot.length+" max: "+max+"("+mb+") min: "+min);
        }

        private int key(int[] p) {
            int k = (p[0]<<24^p[1]<<16^p[2]<<8^p[3]^p[4]<<8^p[5]<<16^p[6]<<24);
            k = k^(p[7]<<24^p[8]); //<<16^p[9]<<8^p[10]^p[11]<<8^p[12]<<16^p[13]<<24);
            k = k % _p.length;
            //if(k<0) k=-k;
            //return k;
            //int k = p[0];
            //final int l = p.length;
            //for(int i=1;i<l;i+=4) {
                //k = 7*k + p[i];
            //}
            //int k = 7*p[0]+31*p[1]+113*p[2]+7*p[3]+31*p[4]+113*p[5]+11*p[6];
            //k = k * 7*p[7]+31*p[8]+113*p[9]+7*p[10]+31*p[11]+113*p[12]+11*p[13];
            //k = k * 7*p[14]+31*p[15]+113*p[16]+7*p[17]+31*p[18]+113*p[19]+11*p[20];
            //k = k * 7*p[21]+31*p[22]+113*p[23]+7*p[24]+31*p[25]+113*p[26]+11*p[27];
            if(k<0) k=-k;
            //k = k % _p.length;
            //k = k % _csize;
            k = k % _csize;
            return k;
        }
    }

    private PCache _cache;
    private int[] _lastP;
    private int _lastN;
    private long _hits;
    private long _misses;
    @Override public int next(int pattern, final int[] p2) {
        /*
        if(Arrays.equals(p2, _lastP)) {
            _hits++;
            if(_hits%1000000==0) System.err.println("hits: "+_hits);
            return _lastN;
        }
        */
        //_lastP = p2;
        //System.arraycopy(p2, 0, _lastP, 0, p2.length);
        int r = _cache.find(p2);
        if(_cache.h) {
            _hits++;
            if(_hits%1000000==0) {
                System.err.println("hits: "+_hits+" misses: "+_misses+" ratio: "+(_hits/((float)_hits+_misses)));
                if(_hits%5000000==0) {
                    _cache.dump();
                }
            }
            return r;
        }
        _misses++;
        r = _logic.next(p2);
        //if(_cache.lk!=0) {
            _cache.put(p2, r);
        //}
        return r;
    }

    @Override public void tick() {
        _logic.tick();
    }

    @Override public ComputedPattern mutate(Random r) {
        GenomeFactory gf = new GenomeFactory();
        return new ComputedPattern(_a, _logic.mutate(_a, gf, r));
    }

    public interface RuleLogic {
        int next(int[] pattern);
        RuleLogic copy();
        default RuleLogic mutate(Archetype a, GenomeFactory gf, Random r) {
            return this;
        }
        default void tick() {
        }
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

        @Override public int next(int[] pattern) {
            //System.err.print("pat: "+Patterns.formatPattern(pattern));
            //final byte b1 = _r.reduce(pattern);
            //System.err.print("\tred: "+(int)b1);
            //final byte b2 = _a.activate(b1);
            //System.err.println("\tact: "+(int)b2);
            return _a.activate(_r.reduce(pattern));
        }

        @Override public RARule copy() {
            return new RARule(_n, _r.copy(), _a.copy());
        }

        @Override public String toString() {
            return _n;
        }
    }

    public static class MachineElf implements RuleLogic {
        private final Machine _m;
        private long _c;


        public MachineElf(Machine m) {
            _m = m;
        }

        @Override public int next(int[] pattern) {
            if(++_c%1000000==0) _m.dump();
            return _m.compute(pattern);
        }

        @Override public RuleLogic copy() {
            return new MachineElf(_m.copy());
        }

        @Override public MachineElf mutate(Archetype a, GenomeFactory gf, Random r) {
            return new MachineElf(_m.mutate(a, gf, r));
        }

        @Override public void tick() {
            _m.tick();
        }

        @Override public String toString() {
            return _m.toString();
        }
    }

    @Override public String toString() {
        return _logic.toString();
    }

    public interface Reducer {
        int reduce(int[] b);
        Reducer copy();
    }

    public interface Activator {
        int activate(int target);
        Activator copy();
    }
}
