package org.excelsi.nausicaa.ca;


import java.util.Random;


public final class MutationStrategies {
    public static MutationStrategy noise() {
        return (ca, rand, f)->new RuleTransform(rand, new Noise(), f).transform(ca);
    }

    private MutationStrategies() {}
}
