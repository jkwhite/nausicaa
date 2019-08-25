package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class SequencePattern extends Enloggened implements Pattern, Mutatable, Humanizable, Genomic, Variables {
    private static final Logger LOG = LoggerFactory.getLogger(CA.class);
    private static final Random R = new Random();
    private static final int[] OFFSETS = new int[30059];
    static {
        for(int i=0;i<OFFSETS.length;i++) {
            OFFSETS[i] = R.nextInt(4);
        }
    }

    private final Sequence _s;
    private Pattern[] _p = new Pattern[2];
    private float _trans;
    private int _samples = 1;
    private int _idx;
    private int _offsetIdx;
    private int _thresh;


    public SequencePattern(Sequence s) {
        this(s, 0.1f);
        //Thread.dumpStack();
    }

    public SequencePattern(Sequence s, float trans) {
        _s = s;
        _p[0] = _s.pattern();
        _p[1] = _s.next();
        _trans = trans;
        _thresh = (int)(trans*_s.peek());
        //System.err.println("** created with threshold "+_thresh+" from "+trans);
        //Thread.dumpStack();
    }

    @Override public Archetype archetype() {
        return _p[0].archetype();
    }

    public float transition() {
        return _trans;
    }

    @Override public Double weight() {
        return _s.weight();
    }

    @Override public Mutatable mutate(MutationFactor m) {
        return new SequencePattern(_s.mutate(m), m.transition());
    }

    @Override public void tick() {
        //System.err.println("ratio: "+_all+" pats, "+_cnt+" new pats, "+_samples+" samples");
        //_all = 0;
        //_cnt = 0;
        int t = _s.tick();
        if(t<_thresh) {
            //_samples = t;
            //_samples = (int)Math.sqrt(t);
            float x = (float)t/_thresh;
            float s = _thresh*(1f+(float)Math.tanh(6f*x-3f));
            //System.err.println("samples: "+s);
            _samples = Math.max(2, (int)s);
        }
        if(t==0) {
            _p[0] = _s.pattern();
            _p[1] = _s.next();
            _thresh = (int)(_trans*_s.peek());
            _samples = 1;
            _idx = 0;
            //System.err.println("t0 for transition "+_trans);
            //LOG.debug("t0 for transition "+_trans);
            //_idx = OFFSETS[t%OFFSETS.length];
            //_idx = R.nextInt(OFFSETS.length);
        }
    }

    @Override public Pattern copy() {
        return new SequencePattern(_s.copy(), _trans);
    }

    @Override public byte next(int pattern, byte[] p2) {
        throw new UnsupportedOperationException();
    }

    private long _stats;
    private long _cnt;
    private long _all;
    @Override public int next(int pattern, int[] p2, Ctx ctx) {
        if(_samples!=1) {
            if(++_idx>=_samples) {
                _idx = OFFSETS[_offsetIdx];
                if(++_offsetIdx==OFFSETS.length) _offsetIdx = 0;
            }
        }
        final int ix = _idx==1?1:0;
        //++_all;
        //_cnt+=ix;
        //if(++_stats%100000==0) {
        //}
        return _p[ix].next(pattern, p2, ctx);
    }

    @Override public double next(int pattern, double[] p2, Ctx ctx) {
        if(_samples!=1) {
            if(++_idx>=_samples) {
                _idx = OFFSETS[_offsetIdx];
                if(++_offsetIdx==OFFSETS.length) _offsetIdx = 0;
            }
        }
        final int ix = _idx==1?1:0;
        //++_all;
        //_cnt+=ix;
        //if(++_stats%100000==0) {
        //}
        return _p[ix].next(pattern, p2, ctx);
    }

    @Override public boolean usesSource() {
        return _s.usesSource();
    }

    @Override public boolean usesContext() {
        return _s.usesContext();
    }

    @Override public String humanize() {
        return _s.humanize();
    }

    @Override public String genome() {
        return _s.genome();
    }

    @Override public String prettyGenome() {
        return _s.prettyGenome();
    }

    @Override public Varmap vars() {
        return _s.vars();
    }

    @Override public String toString() {
        return _s.humanize();
    }

    public static class Sequence implements Humanizable, Genomic {
        private final List<SEntry> _s = new ArrayList<>();
        private final List<DEntry> _d = new ArrayList<>();
        private int _t;
        private int _i;


        public Sequence() {
        }

        public Sequence s(int t, Double weight, ComputedPattern p) {
            _s.add(new SEntry(t, weight, p));
            _t = _s.get(0).t;
            _i = 0;
            return this;
        }

        public Sequence d(String n, Index i) {
            _d.add(new DEntry(n, i));
            return this;
        }

        public ComputedPattern pattern() {
            return _s.get(_i).p;
        }

        public ComputedPattern next() {
            return _s.get((_i+1)%_s.size()).p;
        }

        public Double weight() {
            return _s.get(_i).weight;
        }

        public int peek() {
            return _t;
        }

        public int tick() {
            pattern().tick();
            if(--_t==0) {
                ++_i;
                if(_i==_s.size()) {
                    _i = 0;
                }
                final SEntry s = _s.get(_i); 
                //System.err.println("switched to pattern "+_i+": "+s.p);
                //LOG.debug("switched to pattern "+_i+": "+s.p);
                LOG.trace("switched to pattern "+_i);
                _t = s.t;
                return 0;
            }
            return _t;
        }

        public boolean usesSource() {
            for(SEntry s:_s) {
                if(s.p.usesSource()) {
                    return true;
                }
            }
            return false;
        }

        public boolean usesContext() {
            for(SEntry s:_s) {
                if(s.p.usesContext()) {
                    return true;
                }
            }
            return false;
        }

        public Sequence copy() {
            Datamap dm = new Datamap();
            List<DEntry> nd = new ArrayList<>();
            for(DEntry d:_d) {
                nd.add(new DEntry(d.n, d.i));
                dm.index(d.n, d.i);
            }
            List<SEntry> ns = new ArrayList<>();
            for(SEntry s:_s) {
                ns.add(new SEntry(s.t, s.weight, (ComputedPattern)s.p.copy(dm)));
            }
            return new Sequence(ns, nd);
        }

        //public Sequence mutate(Random r) {
        public Sequence mutate(MutationFactor m) {
            List<SEntry> ns = new ArrayList<>();
            List<DEntry> nd = new ArrayList<>();
            Datamap dm = new Datamap();
            for(DEntry d:_d) {
                Index idx = (Index) d.i.mutate(m);
                dm.index(d.n, idx);
                nd.add(new DEntry(d.n, idx));
            }
            m = m.withDatamap(dm);
            switch(m.mode()) {
                case "normal":
                    //System.err.println("stage mutate: "+m.stage()+" of max "+_s.size());
                    LOG.debug("stage mutate: "+m.stage()+" of max "+(_s.size()-1));
                    for(int i=0;i<_s.size();i++) {
                        final SEntry s = _s.get(i);
                        ComputedPattern np;
                        Double nw = s.weight;
                        if(m.stage()==-1 || i==m.stage()) {
                            //System.err.println("MUTATING "+i);
                            boolean typ = false;
                            if(m.rule() && m.updateWeight()) {
                                typ = m.random().nextBoolean();
                            }
                            else if(m.rule()) {
                                typ = false;
                            }
                            else if(m.updateWeight()) {
                                typ = true;
                            }
                            if(typ) {
                                np = (ComputedPattern)s.p.copy(dm);
                                if(nw==null) {
                                    nw = m.random().nextDouble();
                                }
                                nw = UpdateWeightTransform.mutateWeight(nw, m.random());
                            }
                            else {
                                np = (ComputedPattern)s.p.mutate(m);
                                nw = s.weight;
                            }
                        }
                        else {
                            //System.err.println("COPYING "+i);
                            np = (ComputedPattern)s.p.copy(dm);
                        }
                        ns.add(new SEntry(s.t, nw, np));
                    }
                    break;
                case "add":
                    final Archetype a = _s.get(0).p.archetype();
                    for(SEntry s:_s) {
                        ns.add(new SEntry(s.t, s.weight, (ComputedPattern)s.p.copy(dm)));
                    }
                    ns.add(new SEntry(m.random().nextInt(70)+70, 1d, new ComputedPattern(a, ComputedPattern.random(a, dm, m.random()))));
                    break;
                case "add_data":
                    final Archetype ar = _s.get(0).p.archetype();
                    for(SEntry s:_s) {
                        ns.add(new SEntry(s.t, s.weight, (ComputedPattern)s.p.copy(dm)));
                    }
                    final String nm = Datamap.randomName(m.random());
                    final Index idx = new IndexGenerator().build(ar, m.random(), nm);
                    dm.index(nm, idx);
                    nd.add(new DEntry(nm, idx));
                    break;
                case "remove":
                    for(int i=0;i<_s.size();i++) {
                        if(i!=m.stage()) {
                            Archetype a2 = _s.get(i).p.archetype();
                            ns.add(new SEntry(m.random().nextInt(70)+70, _s.get(i).weight, (ComputedPattern)_s.get(i).p.copy(dm)));
                        }
                    }
                    break;
            }
            Sequence news = new Sequence(ns, nd);
            //System.err.println("GOT NEWS: "+news.humanize());
            return news;
        }

        public void clear() {
            _i = 0;
            _t = _s.get(0).t;
        }

        private Sequence(List<SEntry> s, List<DEntry> d) {
            _s.addAll(s);
            _d.addAll(d);
            clear();
        }

        @Override public String genome() {
            StringBuilder b = new StringBuilder();
            for(SEntry s:_s) {
                b.append(s.t);
                if(s.weight!=null) {
                    b.append("/").append(s.weight);
                }
                b.append(":");
                b.append(s.p.toString()).append(",");
            }
            for(DEntry d:_d) {
                b.append("da").append(d.n).append(":");
                b.append(d.i.genome()).append(",");
            }
            b.setLength(b.length()-1);
            return b.toString();
        }

        @Override public String prettyGenome() {
            return genome();
        }

        @Override public Varmap vars() {
            Varmap m = GenomeParser.createVarmap(genome());
            //System.err.println("## seq varmap: "+m+" for "+genome());
            return m;
        }

        @Override public String humanize() {
            StringBuilder b = new StringBuilder();
            for(SEntry s:_s) {
                b.append(s.t);
                if(s.weight!=null) {
                    b.append("/").append(s.weight);
                }
                b.append(":");
                b.append(s.p.toString()).append(",");
            }
            b.setLength(b.length()-1);
            return b.toString();
        }
    }

    private static class SEntry {
        public final int t;
        public final Double weight;
        public final ComputedPattern p;

        public SEntry(int t, Double weight, ComputedPattern p) {
            this.t = t;
            this.weight = weight;
            this.p = p;
        }
    }

    private static class DEntry {
        public final String n;
        public final Index i;

        public DEntry(String n, Index i) {
            this.n = n;
            this.i = i;
        }
    }
}
