package org.excelsi.nausicaa.ca;


import java.util.List;
import java.util.Random;


public class EvolverBuilder {
    private float _birthRate = 0.1f;
    private float _deathRate = 0.1f;
    private int _population = 10;
    private List<Initializer> _training;
    private Encoder _encoder;
    private Decoder _decoder;
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

    public EvolverBuilder withTraining(List<Initializer> training) {
        _training = training;
        return this;
    }

    public EvolverBuilder withEncoder(Encoder e) {
        _encoder = e;
        return this;
    }

    public EvolverBuilder withDecoder(Decoder d) {
        _decoder = d;
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
        return new Evolver(_training, _encoder, _fitness, _strategy, _birthRate, _deathRate, _population);
    }
}
