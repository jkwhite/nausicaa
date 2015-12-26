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

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        switch(r.getPattern().archetype().dims()) {
            case 1:
                return mutateIndexedRule1d(r);
            case 2:
                return mutateIndexedRule2d(r);
            default:
                return r;
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

    public Rule omutate(Rule r) {
        int[][] pats = r.toPattern();
        int[] colors = r.colors();
        int bgr = r.background();
        //int[][] sym = new int[arch.length][arch[0].length];
        Map<Pattern,Pattern> s = new HashMap<Pattern,Pattern>();
        for(int i=0;i<pats.length;i++) {
            Pattern p = new Pattern(pats[i], r.dimensions());
            Pattern m = s.get(p.mirror());
            if(m!=null) {
                //System.err.println("found: "+p+" => "+m);
                pats[i][pats[i].length-1] = m.result();
                //s.put(p, p);
                //System.arraycopy(m.pattern(), 0, sym[i], 0, sym[i].length);
            }
            else {
                s.put(p,p);
                //System.arraycopy(arch[i], 0, sym[i], 0, sym[i].length);
            }
        }
        return r.origin().create(colors, pats, bgr);
    }

    public Rule mutate(Rule r) {
        long st = System.currentTimeMillis();
        int[][] pats = r.toPattern();
        long en = System.currentTimeMillis();
        //System.err.println("getp: "+(en-st));
        int[] colors = r.colors();
        int bgr = r.background();
        //int[][] sym = new int[arch.length][arch[0].length];
        long lst = System.currentTimeMillis();
        Map<Pattern,Pattern> s = new HashMap<Pattern,Pattern>();
        long mirrors = 0;
        for(int i=0;i<pats.length;i++) {
            long st1 = System.currentTimeMillis();
            Pattern p = new Pattern(pats[i], r.dimensions());
            Iterator<Pattern> ms = p.mirrors();
            Pattern found = null;
            while(ms.hasNext()) {
                mirrors++;
                Pattern mir = ms.next();
                Pattern m = s.get(mir);
                if(m!=null) {
                    //System.err.println("found: "+p+" => "+m);
                    found = m;
                    break;
                    //s.put(p, p);
                    //System.arraycopy(m.pattern(), 0, sym[i], 0, sym[i].length);
                }
            }
            if(found!=null) {
                pats[i][pats[i].length-1] = found.result();
            }
            else {
                s.put(p,p);
                //System.arraycopy(arch[i], 0, sym[i], 0, sym[i].length);
            }
            long en1 = System.currentTimeMillis();
            //System.err.println("iter: "+(en1-st1));
        }
        long len = System.currentTimeMillis();
        if(pats.length>1000000||len-lst>60000) {
            System.err.println("total pats: "+pats.length);
            System.err.println("total mirrors: "+mirrors);
            System.err.println("loop: "+(len-lst));
        }
        return r.origin().create(colors, pats, bgr);
    }

    public Multirule mutate(Multirule r) {
        //System.err.println("mutating "+r);
        Rule[] rules = r.rules();
        Rule[] nr = new Rule[rules.length];
        int si = _om.nextInt(rules.length);
        boolean left = _om.nextBoolean();
        for(int i=0;i<rules.length;i++) {
            if(_all||i==si) {
                nr[i] = mutate(rules[i]);
            }
            else {
                nr[i] = rules[i];
            }
        }
        //return new Multirule1D(nr);
        return (Multirule) r.origin().create((Object[])nr);
    }
}
