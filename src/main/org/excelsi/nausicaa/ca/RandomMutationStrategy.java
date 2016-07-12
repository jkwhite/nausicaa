package org.excelsi.nausicaa.ca;


import java.util.Random;


public class RandomMutationStrategy implements MutationStrategy {
    private final MutatorFactory _factory;
    private final boolean _sym;


    public RandomMutationStrategy(MutatorFactory factory, boolean sym) {
        _factory = factory;
        _sym = sym;
    }

    @Override public CA mutate(final CA ca, Random rand, MutationFactor f) {
        return new RuleTransform(rand, createMutator(rand), f).transform(ca);
    }

    private Mutator createMutator(final Random rand) {
        final Mutator m = _factory.randomMutator(rand);
        return _sym ? new SymmetryForcer(m):m;
    }
}
