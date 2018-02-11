package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.List;
import java.util.Comparator;
import java.util.Random;


public class Evolver {
    private final double _birthRate;
    private final double _deathRate;
    private final int _population;
    private final List<Initializer> _training;
    private final Encoder _encoder;
    private final Fitness _fitness;
    private final MutationStrategy _strategy;
    private boolean _stop;


    public Evolver(final List<Initializer> training, final Encoder encoder, final Fitness fitness,
            final MutationStrategy strategy, final double birthRate, final double deathRate, final int population) {
        _training = training;
        _encoder = encoder;
        _fitness = fitness;
        _strategy = strategy;
        _birthRate = birthRate;
        _deathRate = deathRate;
        _population = population;
    }

    public void requestCancel() {
        _stop = true;
    }

    public CA run(final CA initial, final Random random, final int epicycles, final int iterations, final int subiterations, final int colorLimit, ExecutorService pool) {
        MutationFactor mf = new MutationFactor(5, (a)->{return a.colors() <= colorLimit && a.totalPatterns()<50000000;});
        final Runtime runtime = new Runtime(random, iterations, subiterations, mf, pool, new ThisThreadExecutorService());
        System.err.println("starting "+this);

        List<Choice> pop = new ArrayList<>();
        pop.add(computeFitness(initial, runtime));

        for(int i=0;i<epicycles;i++) {
            List<Choice> ends = runCycle(pop, runtime, i);
            while(ends.size()>_population) {
                ends.remove(ends.size()-1);
            }
            pop = ends;
            if(_stop||Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        return pop.get(0).getCA();
        /*
        int step = 0;
        double max = 0;
        for(int i=0;i<iterations;i++) {
            runIteration(pop, runtime);
            step++;
            double nmax = pop.get(0).getFitness();
            if(nmax>max) {
                max = nmax;
                step = 0;
            }
            mf.withAlpha(50+step);
            if(step>50) {
                ends.add(pop.get(0));
                pop.clear();
                step = 0;
                max = 0;
                mf.withAlpha(50);
                pop.add(computeFitness(initial, runtime));
                System.err.println("******************************* APOCALYPSE ******************************");
            }
            System.err.println(i+" =====> p100: "+pop.get(0)+", p50: "+pop.get(pop.size()/2)+", p0: "+pop.get(pop.size()-1)+", size: "+pop.size());
        }
        ends.add(pop.get(0));
        Collections.sort(ends);
        System.err.println("ENDS: "+ends);
        return ends.get(0).getCA();
        */
    }

    private List<Choice> runCycle(final List<Choice> initial, final Runtime runtime, int epicycle) {
        final List<Choice> ends = new ArrayList<>();
        final List<Choice> pop = new ArrayList<>();
        pop.addAll(initial);
        int step = 0;
        double max = 0;
        for(int i=0;i<runtime.iterations;i++) {
            runIteration(pop, runtime);
            step++;
            double nmax = pop.get(0).getFitness();
            if(nmax>max) {
                max = nmax;
                step = 0;
            }
            runtime.mutationFactor.withAlpha(5+2*step);
            if(step>50) {
                ends.add(pop.get(0));
                pop.clear();
                step = 0;
                max = 0;
                runtime.mutationFactor.withAlpha(5);
                //pop.add(computeFitness(initial, runtime));
                pop.addAll(initial);
                System.err.println("******************************* APOCALYPSE ******************************");
            }
            System.err.println(epicycle+"."+i+" =====> p100: "+pop.get(0)+", p50: "+pop.get(pop.size()/2)+", p0: "+pop.get(pop.size()-1)+", size: "+pop.size());
            if(_stop||Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        ends.add(pop.get(0));
        Collections.sort(ends);
        return ends;
    }

    private void runIteration(final List<Choice> pop, final Runtime runtime) {
        while(pop.size()<_population) {
            //System.err.println("creating "+pop.size()+" / "+_population);
            final int children = _population - pop.size();
            final List<Future<Choice>> tasks = new ArrayList<>(children);
            final int repr = (int) Math.max(1f, pop.size()*_birthRate);
            for(int i=0;i<children;i++) {
                tasks.add(runtime.pool.<Choice>submit(()->{
                    return buildChoice(pop.get(runtime.random.nextInt(repr)), runtime);
                }));
            }
            for(int i=0;i<tasks.size();i++) {
                try {
                    pop.add(tasks.get(i).get());
                }
                catch(Exception e) {
                    System.err.println("mutation failed: "+e);
                }
            }
        }
        Collections.sort(pop);
        //System.err.println(pop);
        int deaths = (int) Math.max(1f, pop.size()*_deathRate);
        //System.err.println("killing "+deaths);
        while(deaths-->0) {
            pop.remove(pop.size()-1);
        }
        //System.err.println(pop);
    }

    @Override public String toString() {
        return "evolver::{population:"+_population+", birthRate:"+_birthRate+", deathRate:"+_deathRate+", fitness:"+_fitness+", strategy:"+_strategy+", trainingSize:"+_training.size()+"}";
    }

    private Choice buildChoice(final Choice parent, final Runtime runtime) {
        final CA ca = _strategy.mutate(parent.getCA(), runtime.random, runtime.mutationFactor);
        return computeFitness(ca, runtime);
    }

    private Choice computeFitness(final CA ca, final Runtime runtime) {
        double[] fitness = new double[_training.size()];
        for(int i=0;i<_training.size();i++) {
            final CA ica = ca.initializer(_training.get(i));
            final Plane[] ps = new Plane[runtime.subiterations];
            ps[0] = ica.createPlane();
            if(runtime.subiterations>1) {
                final Iterator<Plane> ips = ica.getRule().frameIterator(ps[0], runtime.subpool, new GOptions(false, 1, 1, 1f));
                for(int j=0;j<runtime.subiterations-1;j++) {
                    ps[j+1] = ips.next();
                }
            }
            fitness[i] = _fitness.evaluate(ica.archetype(), ps);
        }
        return new Choice(ca, computeOverallFitness(fitness));
    }

    private double computeOverallFitness(double[] fitness) {
        double t = 0;
        for(int i=0;i<fitness.length;i++) {
            t += fitness[i] * fitness[i];
        }
        return t/fitness.length;
    }

    private static final class Choice implements Comparable {
        private final CA _ca;
        private final double _fitness;


        public Choice(CA ca, double fitness) {
            _ca = ca;
            _fitness = fitness;
        }

        public CA getCA() {
            return _ca;
        }

        public double getFitness() {
            return _fitness;
        }

        @Override public int compareTo(Object o) {
            final double of = ((Choice)o)._fitness;
            return _fitness>of?1:_fitness<of?-1:0;
        }

        @Override public String toString() {
            return "future::{fitness:"+_fitness+", ca:"+_ca+"}";
        }
    }

    private static final class Runtime {
        public final Random random;
        public final int iterations;
        public final int subiterations;
        public final ExecutorService pool;
        public final ExecutorService subpool;
        public final MutationFactor mutationFactor;


        public Runtime(Random random, int iterations, int subiterations, MutationFactor mutationFactor, ExecutorService pool, ExecutorService subpool) {
            this.random = random;
            this.iterations = iterations;
            this.subiterations = subiterations;
            this.mutationFactor = mutationFactor;
            this.pool = pool;
            this.subpool = subpool;
        }
    }
}
