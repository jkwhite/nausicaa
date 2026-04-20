package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class SequencePattern extends Enloggened implements Pattern, Mutatable, Humanizable, Genomic, Variables {
    private static final Logger LOG = LoggerFactory.getLogger(SequencePattern.class);
    private static final Random R = new Random();
    private static final double[] PCT = new double[30059];
    static {
        for(int i=0;i<PCT.length;i++) {
            PCT[i] = R.nextDouble();
        }
    }

    private final Sequence _s;
    private Pattern[] _p = new Pattern[2];
    private float _trans;
    private int _thresh;
    private int _pctIdx;
    private double _chance;
    private boolean _inTrans;


    public SequencePattern(Sequence s) {
        this(s, 0f);
    }

    public SequencePattern(Sequence s, float trans) {
        _s = s;
        _p[0] = _s.pattern();
        _p[1] = _s.next();
        _trans = trans;
        _thresh = (int)(trans*_s.peek());
        _chance = 1f;
        _inTrans = false;
        LOG.debug("sequence pattern created with threshold "+_thresh+" from "+trans);
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
        return new SequencePattern(_s.mutate(m), _trans);
    }

    // private int zeroes, ones;
    @Override public void tick() {
        final int t = _s.tick();
        if(t<_thresh) {
            double pct = (double)t/(double)_thresh;
            // this sigmoid gives a nice smooth transition with long
            // flat ends, the 1.01 and 0.005 are to make sure it goes
            // inclusively from 0-100%
            _chance = 1.01d*1d/(1d+Math.exp(-10d*(pct-0.5d)))-0.005d;
            // LOG.debug("last 0,1:"+zeroes+", "+ones+", chance: "+_chance);

            _inTrans = true;
            // zeroes = 0; ones = 0;
        }
        if(t==0) {
            _p[0] = _s.pattern();
            _p[1] = _s.next();
            _thresh = (int)(_trans*_s.peek());
            _chance = 1f;
            _inTrans = false;
        }
    }

    @Override public Pattern copy() {
        return new SequencePattern(_s.copy(), _trans);
    }

    @Override public byte next(int pattern, byte[] p2) {
        throw new UnsupportedOperationException();
    }

    @Override public int next(int pattern, int[] p2, Ctx ctx) {
        int ix=0;
        if(_inTrans) {
            final double pct = PCT[_pctIdx];
            if(++_pctIdx==PCT.length) _pctIdx = 0;
            ix = pct<_chance?0:1;
        }
        return _p[ix].next(pattern, p2, ctx);
    }

    @Override public double next(int pattern, double[] p2, Ctx ctx) {
        int ix=0;
        if(_inTrans) {
            final double pct = PCT[_pctIdx];
            if(++_pctIdx==PCT.length) _pctIdx = 0;
            ix = pct<_chance?0:1;
            // debug stuff
            // if(ix==0) {
                // zeroes++;
            // }
            // else {
                // ones++;
            // }
        }
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

        public Sequence s(int t, Double weight, Double decay, ComputedPattern p) {
            _s.add(new SEntry(t, weight, decay, p));
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

        public SEntry sentry() {
            return _s.get(_i);
        }

        public ComputedPattern next() {
            return _s.get((_i+1)%_s.size()).p;
        }

        public Double weight() {
            return _s.get(_i).weight();
        }

        public int peek() {
            return _t;
        }

        public int tick() {
            sentry().tick();
            if(--_t==0) {
                ++_i;
                if(_i==_s.size()) {
                    _i = 0;
                }
                final SEntry s = _s.get(_i); 
                LOG.trace("switched to pattern "+_i);
                _t = s.t;
                s.reset();
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
                ns.add(new SEntry(s.t, s.weight, s.decay, (ComputedPattern)s.p.copy(dm)));
            }
            return new Sequence(ns, nd);
        }

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
                    LOG.debug("stage mutate: "+m.stage()+" of max "+(_s.size()-1));
                    for(int i=0;i<_s.size();i++) {
                        final SEntry s = _s.get(i);
                        ComputedPattern np;
                        Double nw = s.weight;
                        Double ndec = s.decay;
                        if(m.stage()==-1 || i==m.stage()) {
                            boolean doWeight = false;
                            if(m.updateArchetype()) {
                                doWeight = false;
                            }
                            else if(m.rule() && m.updateWeight()) {
                                doWeight = m.random().nextBoolean();
                            }
                            else if(m.rule()) {
                                doWeight = false;
                            }
                            else if(m.updateWeight()) {
                                doWeight = true;
                            }
                            if(doWeight) {
                                np = (ComputedPattern)s.p.copy(dm);
                                if(nw==null) {
                                    nw = m.random().nextDouble();
                                }
                                if(ndec==null) {
                                    ndec = m.random().nextDouble();
                                }
                                nw = UpdateWeightTransform.mutateWeight(nw, m.random());
                                ndec = UpdateWeightTransform.mutateDecay(ndec, m.random(), nw);
                            }
                            else {
                                np = (ComputedPattern)s.p.mutate(m);
                                nw = s.weight;
                                ndec = s.decay;
                            }
                        }
                        else {
                            np = (ComputedPattern)s.p.copy(dm);
                        }
                        ns.add(new SEntry(s.t, nw, ndec, np));
                    }
                    break;
                case "add":
                    final Archetype a = _s.get(0).p.archetype();
                    for(SEntry s:_s) {
                        ns.add(new SEntry(s.t, s.weight, s.decay, (ComputedPattern)s.p.copy(dm)));
                    }
                    ns.add(new SEntry(m.random().nextInt(70)+70, 1d, null, new ComputedPattern(a, ComputedPattern.random(a, dm, m.random()))));
                    break;
                case "add_data":
                    final Archetype ar = _s.get(0).p.archetype();
                    for(SEntry s:_s) {
                        ns.add(new SEntry(s.t, s.weight, s.decay, (ComputedPattern)s.p.copy(dm)));
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
                            ns.add(new SEntry(_s.get(i).t, _s.get(i).weight, _s.get(i).decay, (ComputedPattern)_s.get(i).p.copy(dm)));
                        }
                    }
                    break;
            }
            Sequence news = new Sequence(ns, nd);
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
                if(s.decay!=null) {
                    b.append(";").append(s.decay);
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
            return m;
        }

        @Override public String humanize() {
            StringBuilder b = new StringBuilder();
            for(SEntry s:_s) {
                b.append(s.t);
                if(s.weight!=null) {
                    b.append("/").append(s.weight);
                }
                if(s.decay!=null) {
                    b.append(";").append(s.decay);
                }
                b.append(":");
                b.append(s.p.humanize()).append(",");
            }
            b.setLength(b.length()-1);
            return b.toString();
        }
    }

    private static class SEntry {
        public final int t;
        public final Double decay;
        public final ComputedPattern p;
        public final Double weight;
        public Double currentWeight;

        public SEntry(int t, Double weight, Double decay, ComputedPattern p) {
            this.t = t;
            this.weight = weight;
            this.decay = decay;
            this.p = p;
            this.currentWeight = weight;
        }

        public void tick() {
            p.tick();
            if(decay!=null&&weight!=null) {
                currentWeight = currentWeight*decay;
                if(currentWeight>1.0) {
                    currentWeight = weight;
                }
            }
        }

        public void reset() {
            currentWeight = weight;
        }

        public Double weight() {
            return currentWeight;
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
