package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public final class ComputedPattern implements Pattern, Mutatable, Humanizable {
    private static final Logger LOG = LoggerFactory.getLogger(ComputedPattern.class);
    private static final boolean ENABLE_CACHE = true;
    private final Archetype _a;
    private final RuleLogic _logic;
    private final IO _io;


    public ComputedPattern(Archetype a, RuleLogic logic) {
        _a = a;
        _logic = logic;
        int size = _a.sourceLength();
        _lastP = new int[size];
        //_lastP = new int[(int)Math.pow(2*_a.size()+1, _a.dims())];

        //int size = (int)Math.pow(2*_a.size()+1, _a.dims());

        // based on 12g heap
        // 3375
        // 1=>100000
        // 7=>29000
        // -11833
        int csize = Math.max(5000, -11833*_a.size()+110000);
        _io = new IO(a.values());
        LOG.debug("cacheable machine: "+logic.isCacheable());
        if(ENABLE_CACHE && logic.isCacheable()) {
            if(a.isDiscrete()) {
                if(a.sourceLength()<9) {
                    _cache = new ShortPCache(csize, size);
                }
                else {
                    _cache = new PCache(csize, size);
                }
                _fcache = null;
            }
            else {
                if(a.sourceLength()<9) {
                    _fcache = new ShortFPCache(csize, size);
                }
                else {
                    _fcache = new FPCache(csize, size);
                }
                _cache = null;
            }
        }
        else {
            _fcache = null;
            _cache = null;
        }
    }

    public ComputedPattern copy(Datamap dm) {
        return new ComputedPattern(_a, _logic.copy(dm));
    }

    @Override public Archetype archetype() {
        return _a;
    }

    @Override public byte next(int pattern, final byte[] p2) {
        throw new UnsupportedOperationException();
    }

    static class ShortPCache extends PCache {
        public ShortPCache(int csize, int psize) {
            super(csize, psize);
        }

        public int find(int[] p) {
            h = false;
            return 0;
        }

        protected int key(int[] p) {
            int k = (p[0]<<24^p[1]<<16^p[2]<<8);
            k = k % _p.length;
            if(k<0) k=-k;
            k = k % _csize;
            return k;
        }
    }

    static class PCache {
        public boolean h;
        public int lk;
        private final int _psize;
        protected final int _csize;
        private final int _bsize;
        private final int[] _c;
        protected final int[] _p;
        private final int[] _hot;

        public PCache(int csize, int psize) {
            _c = new int[csize];
            _hot = new int[csize];
            _csize = csize;
            _psize = psize;
            _bsize = _psize + 1;
            _p = new int[_bsize*csize];
        }

        public int find(int[] p) {
            int i = key(p);
            lk = i;
            i=0;
            h = true;
            int j = 0;
            int idx = i*_psize;
            if(_p[idx++]==0) {
                h = false;
                return 0;
            }
            for(;idx<=(i+1)*_psize;idx++) {
                if(p[j++]!=_p[idx]) {
                    h = false;
                    break;
                }
            }
            return _c[i];
        }

        public void put(int[] p, int r) {
            final int i = lk;
            _hot[i]++;
            _p[i*_bsize] = 1;
            System.arraycopy(p, 0, _p, 1+i*_bsize, p.length);
            _c[i] = r;
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
                }
            }
            LOG.info("total writes: "+w+" total buckets in use: "+b+" / "+_hot.length+" max: "+max+"("+mb+") min: "+min);
        }

        protected int key(int[] p) {
            int k = (p[0]<<24^p[1]<<16^p[2]<<8^p[3]^p[4]<<8^p[5]<<16^p[6]<<24);
            k = k^(p[7]<<24^p[8]); //<<16^p[9]<<8^p[10]^p[11]<<8^p[12]<<16^p[13]<<24);
            k = k % _p.length;
            if(k<0) k=-k;
            k = k % _csize;
            return k;
        }
    }

    static class FPCache {
        public boolean h;
        public int lk;
        private final int _psize;
        protected final int _csize;
        private final int _bsize;
        private final double[] _c;
        protected final double[] _p;
        private final int[] _hot;

        public FPCache(int csize, int psize) {
            _c = new double[csize];
            _hot = new int[csize];
            _csize = csize;
            _psize = psize;
            _bsize = _psize + 1;
            _p = new double[_bsize*csize];
        }

        public double find(double[] p) {
            int i = key(p);
            lk = i;
            i=0;
            h = true;
            int j = 0;
            int idx = i*_psize;
            if(_p[idx++]==0d) {
                h = false;
                return 0d;
            }
            for(;idx<=(i+1)*_psize;idx++) {
                if(p[j++]!=_p[idx]) {
                    h = false;
                    break;
                }
            }
            return _c[i];
        }

        public void put(double[] p, double r) {
            final int i = lk;
            _hot[i]++;
            _p[i*_bsize] = 1f;
            System.arraycopy(p, 0, _p, 1+i*_bsize, p.length);
            _c[i] = r;
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
                }
            }
            LOG.info("total writes: "+w+" total buckets in use: "+b+" / "+_hot.length+" max: "+max+"("+mb+") min: "+min);
        }

        protected static final int f(double f) {
            return (int) Double.doubleToLongBits(f);
        }

        protected int key(double[] p) {
            int k = (f(p[0])<<24^f(p[1])<<16^f(p[2])<<8^f(p[3])^f(p[4])<<8^f(p[5])<<16^f(p[6])<<24);
            k = k^(f(p[7])<<24^f(p[8])); //<<16^p[9]<<8^p[10]^p[11]<<8^p[12]<<16^p[13]<<24);
            k = k % _p.length;
            if(k<0) k=-k;
            k = k % _csize;
            return k;
        }
    }

    static class ShortFPCache extends FPCache {
        public ShortFPCache(int csize, int psize) {
            super(csize, psize);
        }

        protected int key(double[] p) {
            int k = (f(p[0])<<24^f(p[1])<<16^f(p[2])<<8);
            if(p.length>3) {
                k = k^(f(p[3])^f(p[4])<<8);
            }
            if(p.length>6) {
                k = k^f(p[5])<<16^f(p[6])<<24;
            }
            k = k % _p.length;
            if(k<0) k=-k;
            k = k % _csize;
            return k;
        }
    }

    private PCache _cache;
    private FPCache _fcache;
    private int[] _lastP;
    private int _lastN;
    private long _hits;
    private long _misses;
    private static final int[] ZEROS = new int[9];

    @Override public int next(int pattern, final int[] p2, Ctx ctx) {
        int r;
        if(ENABLE_CACHE && _cache!=null) {
            r = _cache.find(p2);
            if(_cache.h) {
                _hits++;
                //if(_hits%1000000==0) {
                    //System.err.println("hits: "+_hits+" misses: "+_misses+" ratio: "+(_hits/((float)_hits+_misses)));
                    //if(_hits%5000000==0) {
                        //_cache.dump();
                    //}
                //}
                //dumpres(p2, r);
                return r;
            }
        }
        _misses++;
        _io.ii = p2;
        _io.ctx = ctx;
        _logic.next(_io);
        r = _io.io;
        if(ENABLE_CACHE && _cache!=null) {
            _cache.put(p2, r);
        }
        return r;
    }

    @Override public double next(int pattern, final double[] p2, Pattern.Ctx ctx) {
        double r;
        if(ENABLE_CACHE && _fcache!=null) {
            r = _fcache.find(p2);
            if(_fcache.h) {
                _hits++;
                if(_hits%1000000==0) {
                    LOG.info("hits: "+_hits+" misses: "+_misses+" ratio: "+(_hits/((float)_hits+_misses)));
                    //if(_hits%5000000==0) {
                        //_cache.dump();
                    //}
                }
                return r;
            }
        }
        _misses++;
        _io.fi = p2;
        _io.ctx = ctx;
        _logic.next(_io);
        r = _io.fo;
        if(ENABLE_CACHE && _fcache!=null) {
            _fcache.put(p2, r);
        }
        return r;
    }

    @Override public boolean usesSource() {
        return _logic.usesSource();
    }

    @Override public boolean usesContext() {
        return _logic.usesContext();
    }

    private static String fmt(int[] p) {
        StringBuilder b = new StringBuilder();
        for(int i:p) {
            b.append(i).append(" ");
        }
        String key = b.toString();
        return key;
    }

    private java.util.Map<String,Integer> _dumpmap = new java.util.HashMap<>();
    private void dumpres(int[] p, int r) {
        StringBuilder b = new StringBuilder();
        for(int i:p) {
            b.append(i).append(" ");
        }
        String key = b.toString();
        Integer res = _dumpmap.get(key);
        if(res!=null) {
            if(r!=res.intValue()) {
                LOG.error("!!!!!!!!! BUG!!!!!!!!!! "+key+"=> res: "+res+" r: "+r);
                Thread.dumpStack();
            }
        }
        else {
            _dumpmap.put(key, r);
            if(true||Arrays.equals(ZEROS, p)) {
                LOG.info("PUTTING "+key+" => "+r);
            }
        }
        _io.ii = p;
        _logic.next(_io);
        int rp = _io.io;
        if(r!=rp) {
            LOG.error("!!!!!!!!! BUG!!!!!!!!!!"+key+"=> said: "+r+" real: "+rp);
            Thread.dumpStack();
        }
    }

    @Override public void tick() {
        _logic.tick();
    }

    @Override public ComputedPattern mutate(MutationFactor m) {
        GenomeFactory gf = new GenomeFactory();
        return new ComputedPattern(_a, _logic.mutate(new Implicate(_a, m.datamap(), m.language(), m.vars()), gf, m));
    }

    public interface RuleLogic extends Humanizable {
        void next(IO io);
        RuleLogic copy(Datamap dm);
        default RuleLogic mutate(Implicate im, GenomeFactory gf, MutationFactor mf) {
            return this;
        }
        default void tick() { }
        default boolean isDeterministic() { return true; }
        default boolean isCacheable() { return isDeterministic() && ! usesContext(); }
        default boolean usesSource() { return true; }
        default boolean usesContext() { return false; }
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

        @Override public void next(IO io) {
            io.io = _a.activate(_r.reduce(io.ii));
        }

        @Override public RARule copy(Datamap dm) {
            return new RARule(_n, _r.copy(), _a.copy());
        }

        @Override public String humanize() {
            return toString();
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

        @Override public boolean isDeterministic() {
            return _m.isDeterministic();
        }

        @Override public boolean usesSource() {
            return _m.usesSource();
        }

        @Override public boolean usesContext() {
            return _m.usesContext();
        }

        @Override public void next(IO io) {
            _m.compute(io);
        }

        @Override public RuleLogic copy(Datamap dm) {
            return new MachineElf(_m.copy(dm));
        }

        @Override public MachineElf mutate(Implicate im, GenomeFactory gf, MutationFactor m) {
            return new MachineElf(_m.mutate(im, gf, m));
        }

        @Override public void tick() {
            _m.tick();
        }

        @Override public String humanize() {
            return _m.humanize();
        }

        @Override public String toString() {
            return _m.toString();
        }
    }

    @Override public String humanize() {
        return _logic.humanize();
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

    @Deprecated
    public static RuleLogic random(final Archetype a, final Datamap d, final Random r) {
        GenomeFactory gf = new GenomeFactory();
        final Implicate im = new Implicate(a, d, Languages.universal());
        return new MachineElf(new Machine(im, gf.generate(im, r)));
    }

    public static RuleLogic random(final Implicate i, final Random r) {
        //GenomeFactory gf = new GenomeFactory();
        return new MachineElf(new Machine(i, i.language().generate(i.archetype(), r)));
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
}
