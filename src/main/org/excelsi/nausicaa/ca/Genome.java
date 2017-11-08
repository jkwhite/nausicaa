package org.excelsi.nausicaa.ca;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import static org.excelsi.nausicaa.ca.WeightedFactory.Weight;


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

    public Genome prune(Archetype a) {
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(a)));
        System.err.println("prune init codons: "+cs);
        while(cs.size()>1) {
            if(!cs.get(0).usesPattern()) {
                cs.remove(0);
            }
            else {
                break;
            }
        }
        System.err.println("prune final codons: "+cs);
        return fromCodons(cs);
    }

    public Genome mutate(final Archetype a, final GenomeFactory gf, final Random r) {
        final WeightedFactory<Mutator> mf = new WeightedFactory<>(
            new Weight<>(5,
                // jumble
                (cs)->{
                    Collections.shuffle(cs);
                }),
            new Weight<>(20,
                // replace
                (cs)->{
                    int idx = r.nextInt(cs.size());
                    cs.set(idx, gf.randomCodon(a, r));
                }),
            new Weight<>(30,
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
                }),
            new Weight<>(40,
                // insert
                (cs)->{
                    int idx = r.nextInt(cs.size());
                    cs.add(idx, gf.randomCodon(a, r));
                }),
            new Weight<>(20,
                // duplicate
                (cs)->{
                    int idx = r.nextInt(cs.size());
                    cs.add(idx, cs.get(idx).copy());
                }),
            new Weight<>(20,
                // remove
                (cs)->{
                    if(cs.size()>1) {
                        int idx = r.nextInt(cs.size());
                        cs.remove(idx);
                    }
                }),
            new Weight<>(10,
                // decimate
                (cs)->{
                    boolean first = true;
                    while(cs.size()>1 && (first || r.nextInt(3)==0)) {
                        int idx = r.nextInt(cs.size());
                        cs.remove(idx);
                        first = false;
                    }
                }),
            new Weight<>(10,
                // repeat
                (cs)->{
                    int st = r.nextInt(cs.size());
                    int en = st+r.nextInt(cs.size()-st);
                    int o = en;
                    for(int i=st;i<en;i++) {
                        cs.add(o++, cs.get(i).copy());
                    }
                }),
            new Weight<>(40,
                // add
                (cs)->{
                    cs.add(gf.randomCodon(a, r));
                }),
            new Weight<>(30,
                // adjust
                (cs)->{
                    boolean any = true;
                    boolean de = false;
                    while(any && !de) {
                        any = false;
                        for(int i=0;i<cs.size();i++) {
                            final Codon c = cs.get(i);
                            if(c instanceof Unstable) {
                                any = true;
                                if(r.nextInt(3)==0) {
                                    cs.set(i, ((Unstable)c).destabilize(r));
                                    de = true;
                                }
                            }
                        }
                    }
                })
        );
        int tries = 0;
        Genome child;
        do {
            child = replicate(a, mf, r);
            if(tries==999) {
                System.err.println("failed to mutate "+this);
            }
        } while(child.equals(this) && ++tries<1000);
        System.err.println(this+" => "+child);
        return child;
    }

    private Genome replicate(final Archetype a, final WeightedFactory<Mutator> mutators, final Random r) {
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(a)));
        int max = 1+r.nextInt(Math.max(1,cs.size()/3));
        for(int i=0;i<max;i++) {
            final Mutator m = mutators.random(r);
            m.mutate(cs);
        }
        StringBuilder b = new StringBuilder();
        for(Codon c:cs) {
            b.append(c.code());
            b.append("-");
        }
        b.setLength(b.length()-1);
        return fromCodons(cs).prune(a);
    }

    public String c() {
        return _c;
    }

    @Override public String toString() {
        return c();
    }

    @Override public boolean equals(Object o) {
        if(o==null||o.getClass()!=Genome.class) {
            return false;
        }
        return ((Genome)o).c().equals(c());
    }

    private static Genome fromCodons(List<Codon> cs) {
        StringBuilder b = new StringBuilder();
        for(Codon c:cs) {
            b.append(c.code());
            b.append("-");
        }
        b.setLength(b.length()-1);
        return new Genome(b.toString());
    }

    @FunctionalInterface
    interface Mutator {
        void mutate(LinkedList<Codon> cs);
    }
}
