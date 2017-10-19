package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;


public class SequencePattern implements Pattern, Mutatable {
    private static final Random R = new Random();
    private static final int[] OFFSETS = new int[4097];
    static {
        for(int i=0;i<OFFSETS.length;i++) {
            OFFSETS[i] = R.nextInt(4);
        }
    }

    private final Sequence _s;
    private Pattern[] _p = new Pattern[2];
    private int _samples = 1;
    private int _idx;
    private int _offsetIdx;
    private int _thresh;


    public SequencePattern(Sequence s) {
        _s = s;
        _p[0] = _s.pattern();
        _p[1] = _s.next();
        _thresh = _s.peek()/2;
    }

    @Override public Archetype archetype() {
        return _p[0].archetype();
    }

    @Override public Mutatable mutate(Random r) {
        return new SequencePattern(_s.mutate(r));
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
            _samples = Math.max(1, (int)s);
        }
        if(t==0) {
            _p[0] = _s.pattern();
            _p[1] = _s.next();
            _thresh = _s.peek()/2;
            _samples = 1;
            _idx = 0;
            //_idx = OFFSETS[t%OFFSETS.length];
            //_idx = R.nextInt(OFFSETS.length);
        }
    }

    @Override public Pattern copy() {
        return new SequencePattern(_s.copy());
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

        public Sequence mutate(Random r) {
            List<SEntry> ns = new ArrayList<>();
            for(SEntry s:_s) {
                ComputedPattern np;
                if(r.nextBoolean()) {
                    np = (ComputedPattern)s.p.mutate(r);
                }
                else {
                    np = (ComputedPattern)s.p.copy();
                }
                ns.add(new SEntry(s.t, np));
            }
            return new Sequence(ns);
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
