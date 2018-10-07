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
    private final int _v;


    public Genome(String c) {
        this(c, 2);
    }

    public Genome(String c, int version) {
        if("".equals(c)) {
            throw new IllegalArgumentException("illegal genome '"+c+"'");
        }
        _c = c;
        _v = version;
    }

    public Codon[] codons(Implicate i) {
        final List<Codon> ops = new ArrayList<>();
        final String sep = _v==1?"-":" ";
        for(final String op:_c.split(sep)) {
            ops.add(Codons.codon(op, i));
        }
        return ops.toArray(new Codon[0]);
    }

    public Genome prune(Implicate im) {
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(im)));
        //System.err.println("prune init codons: "+cs);
        while(cs.size()>1) {
            if(!cs.get(0).usesPattern()) {
                cs.remove(0);
            }
            else {
                break;
            }
        }
        //System.err.println("prune final codons: "+cs);
        return fromCodons(cs);
    }

    public Genome mutate(final Implicate im, final GenomeFactory gf, final MutationFactor m) {
        final Random r = m.random();
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
                    cs.set(idx, gf.randomCodon(im, r));
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
                    cs.add(idx, gf.randomCodon(im, r));
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
                    if(cs.size()>2) {
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
            new Weight<>(5,
                // repeat
                (cs)->{
                    int st = r.nextInt(cs.size());
                    int en = st+r.nextInt(cs.size()-st);
                    int o = en;
                    for(int i=st;i<en;i++) {
                        cs.add(o++, cs.get(i).copy());
                    }
                }),
            new Weight<>(10,
                // symmetry
                (cs)->{
                    symmetry(im.archetype(), cs);
                    /*
                    System.err.println("before sym: "+cs);
                    for(int i=0;i<cs.size()&&cs.size()>1;i++) {
                        Codon c = cs.get(i);
                        if(!c.symmetric()) {
                            cs.remove(i);
                            i--;
                        }
                    }
                    boolean pat = false;
                    for(Codon c:cs) {
                        if(c.usesPattern()) {
                            pat = true;
                            break;
                        }
                    }
                    if(!pat) {
                        cs.add(0, new Codons.Histo(a.colors()));
                    }
                    System.err.println("after sym: "+cs);
                    */
                }),
            new Weight<>(40,
                // add
                (cs)->{
                    cs.add(gf.randomCodon(im, r));
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
            child = replicate(im, mf, m);
            if(tries==999) {
                System.err.println("failed to mutate "+this);
            }
        } while(child.equals(this) && ++tries<1000);
        System.err.println(this+" => "+child);
        return child;
    }

    private Genome replicate(final Implicate im, final WeightedFactory<Mutator> mutators, final MutationFactor mf) {
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(im)));
        int max = 1+mf.random().nextInt(Math.max(1,cs.size()/3));
        for(int i=0;i<max;i++) {
            final Mutator m = mutators.random(mf.random());
            m.mutate(cs);
        }
        if(mf.symmetry()) {
            symmetry(im.archetype(), cs);
        }
        StringBuilder b = new StringBuilder();
        for(Codon c:cs) {
            b.append(c.code());
            b.append("-");
        }
        b.setLength(b.length()-1);
        try {
            return fromCodons(cs).prune(im);
        }
        catch(Exception e) {
            throw new IllegalStateException("illegal genome '"+b+"': "+e, e);
        }
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
            b.append(" ");
        }
        b.setLength(b.length()-1);
        return new Genome(b.toString());
    }

    @FunctionalInterface
    interface Mutator {
        void mutate(LinkedList<Codon> cs);
    }

    private static void symmetry(final Archetype a, final List<Codon> cs) {
        //System.err.println("before sym: "+cs);
        for(int i=0;i<cs.size()&&cs.size()>1;i++) {
            Codon c = cs.get(i);
            if(!c.symmetric()) {
                cs.remove(i);
                i--;
            }
        }
        boolean pat = false;
        for(Codon c:cs) {
            if(c.usesPattern()) {
                pat = true;
                break;
            }
        }
        if(!pat) {
            //cs.add(0, new Codons.Histo(a.colors()));
            cs.add(0, new Codons.PushA());
        }
        //System.err.println("after sym: "+cs);
    }
}
