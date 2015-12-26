package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Random;


public class Evolver {
    private final float _birthRate;
    private final float _deathRate;
    private final int _population;
    private final Random _random;
    private final List<Initializer> _training;
    private final Encoder _encoder;
    private final Fitness _fitness;
    private final MutationStrategy _strategy;


    public Evolver(final Random random, final List<Initializer> training, final Encoder encoder, final Fitness fitness,
            final MutationStrategy strategy, final float birthRate, final float deathRate, final int population) {
        _random = random;
        _training = training;
        _encoder = encoder;
        _fitness = fitness;
        _strategy = strategy;
        _birthRate = birthRate;
        _deathRate = deathRate;
        _population = population;
    }

    public CA run(final CA initial, final int iterations) {
        List<Choice> pop = new ArrayList<>();
        pop.add(new Choice(initial, _fitness.evaluate(initial.archetype(), initial.createPlane())));
        for(int i=0;i<iterations;i++) {
            runIteration(pop);
        }
        return pop.get(0).getCA();
    }

    private void runIteration(final List<Choice> pop) {
        while(pop.size()<_population) {
            int repr = (int) Math.max(1f, pop.size()*_birthRate);
            System.err.println("mutating from "+repr);
            pop.add(buildChoice(pop.get(_random.nextInt(repr))));
        }
        Collections.sort(pop);
        System.err.println(pop);
        int deaths = (int) Math.max(1f, pop.size()*_deathRate);
        System.err.println("killing "+deaths);
        while(deaths-->0) {
            pop.remove(pop.size()-1);
        }
        //System.err.println(pop);
    }

    @Override public String toString() {
        return "evolver::{population:"+_population+", birthRate:"+_birthRate+", deathRate:"+_deathRate+", fitness:"+_fitness+", strategy:"+_strategy+"}";
    }

    private Choice buildChoice(final Choice parent) {
        final CA ca = _strategy.mutate(parent.getCA(), _random);
        float[] fitness = new float[_training.size()];
        for(int i=0;i<_training.size();i++) {
            final CA ica = ca.initializer(_training.get(i));
            fitness[i] = _fitness.evaluate(ica.archetype(), ica.createPlane());
        }
        return new Choice(ca, computeOverallFitness(fitness));
    }

    private float computeOverallFitness(float[] fitness) {
        float t = 0;
        for(int i=0;i<fitness.length;i++) {
            t += fitness[i];
        }
        return t/fitness.length;
    }

    private static final class Choice implements Comparable {
        private final CA _ca;
        private final float _fitness;


        public Choice(CA ca, float fitness) {
            _ca = ca;
            _fitness = fitness;
        }

        public CA getCA() {
            return _ca;
        }

        public float getFitness() {
            return _fitness;
        }

        @Override public int compareTo(Object o) {
            final float of = ((Choice)o)._fitness;
            return _fitness>of?-1:_fitness<of?1:0;
        }

        @Override public String toString() {
            return "future::{fitness:"+_fitness+", ca:"+_ca+"}";
        }
    }
}
