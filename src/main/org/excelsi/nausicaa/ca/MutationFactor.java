package org.excelsi.nausicaa.ca;


import java.util.function.Predicate;


public final class MutationFactor {
    private int _alpha;
    private Predicate<Archetype> _validator;


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
