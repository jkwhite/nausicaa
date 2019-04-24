package org.excelsi.nausicaa.ca;


import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class ParameterTransform implements Transform {
    private static final Logger LOG = LoggerFactory.getLogger(ParameterTransform.class);
    private static final Mutator[] MUTATORS = {
        new UpdateModeMutator(),
        new ExternalForceMutator(),
        new EdgeModeMutator()
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
        LOG.info("transforming with param mutator "+m);
        return m.mutate(c, _r, _f);
    }

    @FunctionalInterface
    private static interface Mutator {
        CA mutate(CA c, Random r, MutationFactor f);
    }

    private static class UpdateModeMutator implements Mutator {
        @Override public CA mutate(CA c, Random r, MutationFactor f) {
            return c.updateMode(UpdateMode.createRandom(r));
        }

        @Override public String toString() { return "UpdateModeMutator"; }
    }

    private static class ExternalForceMutator implements Mutator {
        @Override public CA mutate(CA c, Random r, MutationFactor f) {
            return c.externalForce(ExternalForce.createRandom(r));
        }

        @Override public String toString() { return "ExternalForceMutator"; }
    }

    private static class EdgeModeMutator implements Mutator {
        @Override public CA mutate(CA c, Random r, MutationFactor f) {
            return c.edgeMode(EdgeMode.createRandom(r, f));
        }

        @Override public String toString() { return "EdgeModeMutator"; }
    }
}
