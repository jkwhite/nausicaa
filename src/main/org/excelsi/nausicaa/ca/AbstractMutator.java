package org.excelsi.nausicaa.ca;


import java.util.*;


public abstract class AbstractMutator implements Mutator {
    public static final int ALPHA = 10;
    protected Random _om;


    public AbstractMutator() {
        _om = new Random();
    }

    public AbstractMutator(Random om) {
        _om = om;
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r) throws MutationFailedException {
        throw new UnsupportedOperationException();
    }

    @Override public Rule mutate(Rule r) {
        throw new UnsupportedOperationException();
    }

    @Override public Multirule mutate(Multirule r) {
        throw new UnsupportedOperationException();
    }

    public void setRandom(Random om) {
        _om = om;
    }

    public Random getRandom() {
        return _om;
    }

    public String toString() {
        return name();
    }
}
