package org.excelsi.nausicaa.ca;


import java.util.function.Predicate;
import java.util.Random;


public final class MutationFactor {
    private int _alpha;
    private Random _r;
    private Predicate<Archetype> _validator;
    private String _mode = "normal";
    private int _stage;


    public MutationFactor() {
        this(20, (a)->{return true;});
    }

    public MutationFactor(final int alpha, final Predicate<Archetype> validator) {
        _alpha = alpha;
        _validator = validator;
    }

    public int alpha() {
        return _alpha;
    }

    public MutationFactor withAlpha(int alpha) {
        _alpha = alpha;
        return this;
    }

    public Predicate<Archetype> validator() {
        return _validator;
    }

    public MutationFactor withValidator(Predicate<Archetype> validator) {
        _validator = validator;
        return this;
    }

    public MutationFactor withRandom(Random r) {
        _r = r;
        return this;
    }

    public Random random() {
        return _r;
    }

    public MutationFactor withMode(String m) {
        _mode = m;
        return this;
    }

    public String mode() {
        return _mode;
    }

    public MutationFactor withStage(int stage) {
        _stage = stage;
        return this;
    }

    public int stage() {
        //if(_stage==0) {
            //System.err.println("************* ZERO STAGE ***********");
            //Thread.dumpStack();
        //}
        return _stage;
    }

    public String toString() {
        return "{alpha:"+_alpha+"}";
    }

    public static MutationFactor defaultFactor() {
        return new MutationFactor(
            20,
            (a)->{return a.totalPatterns()<400000000;}
        );
    }
}
