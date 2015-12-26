package org.excelsi.nausicaa.ca;


import java.util.Random;


public class EvolverBuilder {
    private float _birthRate = 0.1f;
    private float _deathRate = 0.1f;
    private int _population = 10;
    private Random _random;
    private Initializer _initializer;
    private Encoder _encoder;
    private Fitness _fitness;
    private MutationStrategy _strategy;


    public EvolverBuilder withBirthRate(float b) {
        _birthRate = b;
        return this;
    }

    public EvolverBuilder withDeathRate(float d) {
        _deathRate = d;
        return this;
    }

    public EvolverBuilder withPopulation(int p) {
        _population = p;
        return this;
    }

    public EvolverBuilder withRandom(Random r) {
        _random = r;
        return this;
    }

    public EvolverBuilder withInitializer(Initializer i) {
        _initializer = i;
        return this;
    }

    public EvolverBuilder withEncoder(Encoder e) {
        _encoder = e;
        return this;
    }

    public EvolverBuilder withFitness(Fitness f) {
        _fitness = f;
        return this;
    }

    public EvolverBuilder withMutationStrategy(MutationStrategy s) {
        _strategy = s;
        return this;
    }

    public Evolver build() {
        return new Evolver(_random, _initializer, _encoder, _fitness, _strategy, _birthRate, _deathRate, _population);
    }
}
