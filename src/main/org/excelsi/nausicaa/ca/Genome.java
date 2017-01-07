package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.ArrayList;
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
            (cs)->{
                int idx = r.nextInt(cs.size());
                cs.set(idx, gf.randomCodon(a, r));
            }
        };
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(a)));
        for(int i=0;i<1;i++) {
            final Mutator m = mutators[r.nextInt(mutators.length)];
            m.mutate(cs);
        }
        StringBuilder b = new StringBuilder();
        for(Codon c:cs) {
            System.err.println("appending code '"+c.code()+"' for "+c.getClass());
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
