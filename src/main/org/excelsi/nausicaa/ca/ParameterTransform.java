package org.excelsi.nausicaa.ca;


import java.util.Random;


public class ParameterTransform implements Transform {
    private static final Mutator[] MUTATORS = {
        new UpdateModeMutator()
    };

    private final Random _r;
    private final MutationFactor _f;


    public ParameterTransform(Random r, MutationFactor f) {
        _r = r;
        _f = f;
    }

    @Override public String name() {
        return "Parameters";
    }

    @Override public CA transform(CA c) {
        Mutator m = MUTATORS[_r.nextInt(MUTATORS.length)];
        //System.err.println("PARAM MUTATOR: "+m);
        return m.mutate(c, _r);
    }

    @FunctionalInterface
    private static interface Mutator {
        CA mutate(CA c, Random r);
    }

    private static class UpdateModeMutator implements Mutator {
        @Override public CA mutate(CA c, Random r) {
            return c.updateMode(UpdateMode.createRandom(r));
        }
    }
}
