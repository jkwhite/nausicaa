package org.excelsi.nausicaa.ca;


import java.util.*;


public abstract class AbstractMutator implements Mutator {
    public static final int ALPHA = 10;
    protected Random _om;
    private final int _alpha;


    public AbstractMutator() {
        this(new Random());
    }

    public AbstractMutator(Random om) {
        _om = om;
        _alpha = findAlpha();
    }

    protected final int alpha() {
        return _alpha;
    }

    protected final int findAlpha() {
        return _om.nextInt(20)+1;
    }

    protected final boolean chance() {
        return _om.nextInt(100)<_alpha;
    }

    protected final boolean chance(MutationFactor f) {
        return _om.nextInt(100)<f.alpha();
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
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
