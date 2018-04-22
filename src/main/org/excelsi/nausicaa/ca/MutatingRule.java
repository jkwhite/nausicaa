package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Random;


public class MutatingRule extends AbstractRule implements Mutatable, Genomic {
    private final Random _r;
    private ComputedRule2d _rule;
    private Plane _last;
    private Iterator<Plane> _it;
    private int _doom;


    public MutatingRule(ComputedRule2d rule, Random r) {
        super(1,1);
        _rule = rule;
        _r = r;
    }

    //@Override public Mutatable mutate(Random r) {
    @Override public Mutatable mutate(MutationFactor m) {
        return new MutatingRule((ComputedRule2d)_rule.mutate(m), _r);
    }

    @Override public String humanize() {
        return _rule.humanize();
    }

    @Override public String toString() {
        return "MutatingRule::{rule:"+_rule.toString()+"}";
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
        init(c, pool, opt);
        return new Iterator<Plane>() {
            @Override public Plane next() {
                if(--_doom==0) {
                    System.err.println("DOOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                    internalMutate(_last, pool, opt);
                }
                _last = _it.next();
                System.err.print(".");
                return _last;
            }

            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
            }
        };
    }

    @Override public Plane generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u, GOptions opt) {
        return _rule.generate(c, start, end, pool, stopOnSame, overwrite, u, opt);
    }

    private void internalMutate(final Plane c, final ExecutorService pool, final GOptions opt) {
        _rule = (ComputedRule2d)_rule.mutate(new MutationFactor().withRandom(_r));
        init(c, pool, opt);
    }

    private void init(final Plane c, final ExecutorService pool, final GOptions opt) {
        _it = _rule.frameIterator(c, pool, opt);
        _doom = 50+_r.nextInt(50);
    }
}
