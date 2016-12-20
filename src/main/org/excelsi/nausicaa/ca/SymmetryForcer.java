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
}
