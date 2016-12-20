package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.BitSet;


public class Symmetry extends AbstractMutator {
    public String name() { return "Symmetry"; }
    public String description() { return "Introduces symmetry to a rule"; }

    private boolean _all = false;


    public Symmetry() {
        this(false);
    }

    public Symmetry(boolean all) {
        _all = all;
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        //System.err.println("*********************** running symmetry");
        switch(r.getPattern().archetype().dims()) {
            case 1:
                return mutateIndexedRule1d(r);
            case 2:
                return mutateIndexedRule2d(r);
            default:
                throw new MutationFailedException("unsupported dims: "+r.getPattern().archetype().dims());
        }
    }

    private IndexedRule mutateIndexedRule1d(IndexedRule r) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        final long max = a.totalPatterns();
        final byte[] base = new byte[a.sourceLength()];
        final byte[] sym = new byte[a.sourceLength()];
        final int[] coefficients = a.sourceCoefficients();
        return r.derive(r.getPattern().transform((arch, target)->{
            for(int i=0;i<max;i++) {
                Patterns.expandSourceIndex(arch, i, base);
                Patterns.mirror(base, sym);
                int midx = Patterns.indexForSource(coefficients, sym);
                //System.err.println(i+" => "+midx+": "+Patterns.formatPattern(base)+" => "+Patterns.formatPattern(sym));
                target[midx] = target[i];
            }
        }));
    }

    private IndexedRule mutateIndexedRule2d(IndexedRule r) throws MutationFailedException {
        final Archetype a = r.getPattern().archetype();
        final long max = a.totalPatterns();
        final byte[] base = new byte[a.sourceLength()];
        final byte[] sym = new byte[a.sourceLength()];
        final byte[][] syms = new byte[][]{
            new byte[a.sourceLength()],
            new byte[a.sourceLength()],
            new byte[a.sourceLength()],
            new byte[a.sourceLength()]
        };
        final int[] coefficients = a.sourceCoefficients();
        return r.derive(r.getPattern().transform((arch, target)->{
            final BitSet done = new BitSet(target.length);
            for(int i=0;i<max;i++) {
                if(!done.get(i)) {
                    Patterns.expandSourceIndex(arch, i, syms[0]);
                    for(int k=0;k<3;k++) {
                        Patterns.rotate(syms[k], syms[k+1]);
                        int nidx = Patterns.indexForSource(coefficients, syms[k+1]);
                        //System.err.println(i+" => "+nidx+": "+Patterns.formatPattern(base)+" => "+Patterns.formatPattern(sym));
                        target[nidx] = target[i];
                        done.set(nidx);
                    }
                    done.set(i);
                }
            }
        }));
    }
}
