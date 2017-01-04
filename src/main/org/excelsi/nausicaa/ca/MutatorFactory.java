package org.excelsi.nausicaa.ca;


import java.util.Random;


public class MutatorFactory {
    private static final MutatorFactory DEFAULT_FACTORY = 
        new MutatorFactory(
            weight(20, new Noise()),
            weight(10, new ColoredNoise()),
            weight(5, new Skew("Skew linear", Probability.linear())),
            weight(5, new Skew("Skew gaussian", Probability.gaussian())),
            weight(5, new Skew("Skew lowpass", Probability.lowpass())),
            weight(5, new Skew("Skew highpass", Probability.highpass())),
            weight(5, new Symmetry()),
            weight(5, new Color()),
            weight(3, new Collapse()),
            weight(10, new ThinOne()),
            weight(5, new ThinAll()),
            weight(10, new ThickenOne()),
            weight(5, new ThickenAll()),
            weight(3, new Fork()),
            weight(3, new Splice()),
            weight(7, new Segregate()),
            weight(8, new Tangle()),
            weight(5, new Grow()),
            weight(1, new Grow(true)),
            weight(0, new Life()),
            weight(0, new Wireworld()),
            weight(0, new Self()),
            weight(0, new Deeper()),
            weight(0, new Shallower()),
            weight(0, new Dimensionality(Dimensionality.Direction.up)),
            weight(0, new Dimensionality(Dimensionality.Direction.down))
        );
    private final Weight[] _mutators;
    private final int _sumWeight;


    public MutatorFactory(Weight... mutators) {
        _mutators = mutators;
        int sum = 0;
        for(Weight w:_mutators) {
            sum += w.weight();
        }
        _sumWeight = sum;
    }

    public Mutator[] getAll() {
        Mutator[] ms = new Mutator[_mutators.length];
        for(int i=0;i<_mutators.length;i++) {
            ms[i] = _mutators[i].mutator();
        }
        return ms;
    }

    public static MutatorFactory defaultMutators() {
        return DEFAULT_FACTORY;
    }

    public Mutator randomMutator(final Random rand, final Rule rule) {
        Mutator m = null;
        int tries = 0;
        do {
            int v = rand.nextInt(_sumWeight);
            for(Weight w:_mutators) {
                v -= w.weight();
                if(v<=0) {
                    m = w.mutator();
                    break;
                }
            }
        } while(++tries<1000&&(m==null||!m.supports(rule)));
        if(m!=null) {
            return m;
        }
        throw new IllegalStateException("impossible weight range for "+_sumWeight);
    }

    public static Weight weight(int weight, Mutator m) {
        return new Weight(weight, m);
    }

    public static class Weight {
        private final int _weight;
        private final Mutator _m;


        public Weight(int weight, Mutator m) {
            _weight = weight;
            _m = m;
        }

        public int weight() {
            return _weight;
        }

        public Mutator mutator() {
            return _m;
        }
    }
}
