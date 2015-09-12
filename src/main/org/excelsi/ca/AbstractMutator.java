package org.excelsi.ca;


import java.util.*;


public abstract class AbstractMutator implements Mutator {
    protected Random _om;


    public AbstractMutator() {
        _om = new Random();
    }

    public AbstractMutator(Random om) {
        _om = om;
    }

    public void setRandom(Random om) {
        _om = om;
    }

    public Random getRandom() {
        return _om;
    }
}
