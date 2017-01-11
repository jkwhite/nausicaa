package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public final class Genome {
    private final String _c;


    public Genome(String c) {
        _c = c;
    }

    public Codon[] codons(Archetype a) {
        final List<Codon> ops = new ArrayList<>();
        for(final String op:_c.split("-")) {
            ops.add(Codons.codon(op, a));
        }
        return ops.toArray(new Codon[0]);
    }

    public Genome mutate(final Archetype a, final GenomeFactory gf, final Random r) {
        final Mutator[] mutators = new Mutator[]{
            // jumble
            (cs)->{
                Collections.shuffle(cs);
            },
            // replace
            (cs)->{
                int idx = r.nextInt(cs.size());
                cs.set(idx, gf.randomCodon(a, r));
            },
            // swap
            (cs)->{
                if(cs.size()>1) {
                    int idx1 = r.nextInt(cs.size());
                    boolean d = r.nextBoolean();
                    int idx2 = d&&idx1<cs.size()-1?idx1+1:idx1>1?idx1-1:r.nextInt(cs.size());
                    Codon c1 = cs.get(idx1);
                    Codon c2 = cs.get(idx2);
                    cs.set(idx1, c2);
                    cs.set(idx2, c1);
                }
            },
            // insert
            (cs)->{
                int idx = r.nextInt(cs.size());
                cs.add(idx, gf.randomCodon(a, r));
            },
            // remove
            (cs)->{
                if(cs.size()>1) {
                    int idx = r.nextInt(cs.size());
                    cs.remove(idx);
                }
            },
            // decimate
            (cs)->{
                while(cs.size()>1 && r.nextInt(3)==0) {
                    int idx = r.nextInt(cs.size());
                    cs.remove(idx);
                }
            },
            // add
            (cs)->{
                cs.add(gf.randomCodon(a, r));
            }
        };
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(a)));
        int max = 1+r.nextInt(3);
        for(int i=0;i<max;i++) {
            final Mutator m = mutators[r.nextInt(mutators.length)];
            m.mutate(cs);
        }
        StringBuilder b = new StringBuilder();
        for(Codon c:cs) {
            //System.err.println("appending code '"+c.code()+"' for "+c.getClass());
            b.append(c.code());
            b.append("-");
        }
        b.setLength(b.length()-1);
        System.err.println(this+" => "+b.toString());
        return new Genome(b.toString());
    }

    public String c() {
        return _c;
    }

    @Override public String toString() {
        return c();
    }

    @FunctionalInterface
    interface Mutator {
        void mutate(LinkedList<Codon> cs);
    }
}
