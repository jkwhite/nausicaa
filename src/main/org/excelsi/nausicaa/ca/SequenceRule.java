package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;


public class SequenceRule extends AbstractRule implements Mutatable, Genomic {
    private final Sequence _s;
    private ComputedRule2d _rule;
    private Plane _last;
    private Iterator<Plane> _it;


    public SequenceRule(Sequence s) {
        super(1,1);
        _s = s;
        _rule = _s.rule();
    }

    //@Override public Mutatable mutate(Random r) {
    @Override public Mutatable mutate(MutationFactor m) {
        return new SequenceRule(_s.mutate(m));
    }

    @Override public String humanize() {
        return _s.humanize();
    }

    @Override public String toString() {
        return "SequenceRule::{s:"+_s.toString()+"}";
    }

    @Override public Archetype archetype() {
        return _rule.archetype();
    }

    @Override public int dimensions() {
        return _rule.dimensions();
    }

    @Override public int background() {
        return _rule.background();
    }

    @Override public int length() {
        return _rule.length();
    }

    @Override public int[] colors() {
        return _rule.colors();
    }

    @Override public int[][] toPattern() {
        return _rule.toPattern();
    }

    @Override public Ruleset origin() {
        return _rule.origin();
    }

    @Override public String genome() {
        return _rule.genome();
    }

    @Override public String prettyGenome() {
        return _rule.prettyGenome();
    }

    @Override public Iterator<Plane> frameIterator(final Plane c, final ExecutorService pool, final GOptions opt) {
        _s.clear();
        _rule = _s.rule();
        init(c, pool, opt);
        _last = c;
        return new Iterator<Plane>() {
            @Override public Plane next() {
                int rem = _s.tick();
                if(rem==0) {
                    ComputedRule2d r = _s.rule();
                    System.err.println("DOOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                    _rule = r;
                    init(_last, pool, opt);
                }
                _last = _it.next();
                return _last;
            }

            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
            }
        };
    }

    @Override public float generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u, GOptions opt) {
        return _rule.generate(c, start, end, pool, stopOnSame, overwrite, u, opt);
    }

    private void init(final Plane c, final ExecutorService pool, final GOptions opt) {
        _it = _rule.frameIterator(c, pool, opt);
    }

    public static class Sequence {
        private final List<SEntry> _s = new ArrayList<>();
        private int _t;
        private int _i;


        public Sequence() {
        }

        public Sequence s(int t, ComputedRule2d r) {
            _s.add(new SEntry(t, r));
            _t = _s.get(0).t;
            _i = 0;
            return this;
        }

        public ComputedRule2d rule() {
            return _s.get(_i).r;
        }

        public ComputedRule2d next() {
            return _s.get((_i+1)%_s.size()).r;
        }

        public int tick() {
            if(--_t==0) {
                ++_i;
                if(_i==_s.size()) {
                    _i = 0;
                }
                final SEntry s = _s.get(_i); 
                //ComputedRule2d r = s.r;
                _t = s.t;
                return 0;
            }
            return _t;
        }

        public Sequence copy() {
            return new Sequence(_s);
        }

        public Sequence mutate(MutationFactor m) {
            List<SEntry> ns = new ArrayList<>();
            for(SEntry s:_s) {
                ns.add(new SEntry(m.random().nextInt(40)+30, (ComputedRule2d)s.r.mutate(m)));
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
                b.append(s.r.humanize()).append(" ");
            }
            return b.toString();
        }
    }

    private static class SEntry {
        public final int t;
        public final ComputedRule2d r;

        public SEntry(int t, ComputedRule2d r) {
            this.t = t;
            this.r = r;
        }
    }
}
