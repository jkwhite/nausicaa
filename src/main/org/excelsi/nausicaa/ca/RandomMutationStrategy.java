package org.excelsi.nausicaa.ca;


import java.util.Random;


public class RandomMutationStrategy implements MutationStrategy {
    private final MutatorFactory _factory;


    public RandomMutationStrategy(MutatorFactory factory) {
        _factory = factory;
    }

    @Override public CA mutate(final CA ca, Random rand) {
        return new RuleTransform(rand, new SafeMutator(_factory.randomMutator(rand))).transform(ca);
    }
}
