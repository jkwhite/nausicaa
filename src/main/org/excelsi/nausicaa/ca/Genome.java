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
        final WeightedFactory<GenomeMutator> mf = new WeightedFactory<>(
            new Weight<>(5,  GenomeMutators.jumble()),
            new Weight<>(20, GenomeMutators.replace()),
            new Weight<>(30, GenomeMutators.swap()),
            new Weight<>(40, GenomeMutators.insert()),
            new Weight<>(20, GenomeMutators.duplicate()),
            new Weight<>(20, GenomeMutators.remove()),
            new Weight<>(10, GenomeMutators.decimate()),
            new Weight<>(5,  GenomeMutators.repeat()),
            new Weight<>(10, GenomeMutators.symmetry()),
            new Weight<>(40, GenomeMutators.add()),
            new Weight<>(30, GenomeMutators.adjust())
        );
        int tries = 0;
        Genome child;
        do {
            child = replicate(im, mf, gf, m);
            if(tries==999) {
                System.err.println("failed to mutate "+this);
            }
        } while(child.equals(this) && ++tries<1000);
        System.err.println(this+" => "+child);
        return child;
    }

    private Genome replicate(final Implicate im, final WeightedFactory<GenomeMutator> mutators, final GenomeFactory gf, final MutationFactor mf) {
        final LinkedList<Codon> cs = new LinkedList(Arrays.asList(codons(im)));
        float mult = mf.alpha()/20f;
        int max = (int) (mult*(1+mf.random().nextInt(Math.max(1,cs.size()/3))));
        System.err.println("applying "+max+" mutators");
        for(int i=0;i<max;i++) {
            final GenomeMutator m = mf.genomeMutator()!=null?mf.genomeMutator():mutators.random(mf.random());
            m.mutate(cs, im, gf, mf);
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

    //@FunctionalInterface
    //interface Mutator {
        //void mutate(LinkedList<Codon> cs);
    //}

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
