package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;


public class SequencePattern implements Pattern, Mutatable {
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
        this(s, 0.5f);
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
    @Override public int next(int pattern, int[] p2) {
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
        return _p[ix].next(pattern, p2);
    }

    @Override public String toString() {
        return _s.humanize();
    }

    public static class Sequence {
        private final List<SEntry> _s = new ArrayList<>();
        private int _t;
        private int _i;


        public Sequence() {
        }

        public Sequence s(int t, ComputedPattern p) {
            _s.add(new SEntry(t, p));
            _t = _s.get(0).t;
            _i = 0;
            return this;
        }

        public ComputedPattern pattern() {
            return _s.get(_i).p;
        }

        public ComputedPattern next() {
            return _s.get((_i+1)%_s.size()).p;
        }

        public int peek() {
            return _t;
        }

        public int tick() {
            if(--_t==0) {
                ++_i;
                if(_i==_s.size()) {
                    _i = 0;
                }
                System.err.println("switch to pattern "+_i);
                final SEntry s = _s.get(_i); 
                _t = s.t;
                return 0;
            }
            return _t;
        }

        public Sequence copy() {
            List<SEntry> ns = new ArrayList<>();
            for(SEntry s:_s) {
                ns.add(new SEntry(s.t, (ComputedPattern)s.p.copy()));
            }
            return new Sequence(ns);
        }

        //public Sequence mutate(Random r) {
        public Sequence mutate(MutationFactor m) {
            List<SEntry> ns = new ArrayList<>();
            switch(m.mode()) {
                case "normal":
                    System.err.println("stage mutate: "+m.stage()+" of max "+_s.size());
                    for(int i=0;i<_s.size();i++) {
                        final SEntry s = _s.get(i);
                        ComputedPattern np;
                        if(m.stage()==-1 || i==m.stage()) {
                            System.err.println("MUTATING "+i);
                            np = (ComputedPattern)s.p.mutate(m);
                        }
                        else {
                            System.err.println("COPYING "+i);
                            np = (ComputedPattern)s.p.copy();
                        }
                        ns.add(new SEntry(s.t, np));
                    }
                    break;
                case "add":
                    final Archetype a = _s.get(0).p.archetype();
                    for(SEntry s:_s) {
                        ns.add(new SEntry(s.t, (ComputedPattern)s.p.copy()));
                    }
                    ns.add(new SEntry(m.random().nextInt(70)+70, new ComputedPattern(a, ComputedPattern.random(a, m.random()))));
                    break;
                case "remove":
                    for(int i=0;i<_s.size();i++) {
                        if(i!=m.stage()) {
                            ns.add(new SEntry(m.random().nextInt(70)+70, (ComputedPattern)_s.get(i).p.copy()));
                        }
                    }
                    break;
            }
            Sequence news = new Sequence(ns);
            System.err.println("GOT NEWS: "+news.humanize());
            return news;
        }

        public void clear() {
            _i = 0;
            _t = _s.get(0).t;
        }

        private Sequence(List<SEntry> s) {
            _s.addAll(s);
            clear();
        }

        public String humanize() {
            StringBuilder b = new StringBuilder();
            for(SEntry s:_s) {
                b.append(s.t).append(":");
                b.append(s.p.toString()).append(",");
            }
            b.setLength(b.length()-1);
            return b.toString();
        }
    }

    private static class SEntry {
        public final int t;
        public final ComputedPattern p;

        public SEntry(int t, ComputedPattern p) {
            this.t = t;
            this.p = p;
        }
    }
}
