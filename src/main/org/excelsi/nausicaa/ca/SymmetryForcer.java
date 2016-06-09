package org.excelsi.nausicaa.ca;


import java.util.*;


public class SymmetryForcer extends AbstractMutator {
    private Mutator _m;
    private Symmetry _s = new Symmetry(true);


    public SymmetryForcer(Mutator m) {
        _m = m;
    }

    public String name() {
        return "i know a girl from a tribe so primitive";
    }

    public String description() {
        return "she can call me up without no telephone";
    }

    public void setRandom(Random r) {
        super.setRandom(r);
        _s.setRandom(r);
        _m.setRandom(r);
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        return _s.mutateIndexedRule(_m.mutateIndexedRule(r, f), f);
    }

    public Rule mutate(Rule r) throws MutationFailedException {
        long start1 = System.currentTimeMillis();
        r = _m.mutate(r);
        long start = System.currentTimeMillis();
        if(start-start1>10000) {
            System.err.println(_m.name()+" took "+(start-start1)+" msec");
        }
        Rule mu = _s.mutate(r);
        long end1 = System.currentTimeMillis();
        if(end1-start>10000) {
            System.err.println("symforce took "+(end1-start)+" msec");
        }
        return mu;
    }

    public Multirule mutate(Multirule r) throws MutationFailedException {
        long start1 = System.currentTimeMillis();
        r = _m.mutate(r);
        long start = System.currentTimeMillis();
        System.err.println(_m.name()+" took "+(start-start1)+" msec");
        Multirule mu = _s.mutate(r);
        System.err.println("symforce took "+(System.currentTimeMillis()-start)+" msec");
        return mu;
    }
}
