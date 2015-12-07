package org.excelsi.nausicaa.ca;


import java.util.Random;


public class MutatorFactory {
    private static final MutatorFactory DEFAULT_FACTORY = 
        new MutatorFactory(
            weight(20, new Noise()),
            weight(5, new Symmetry()),
            weight(8, new Color()),
            weight(8, new Collapse()),
            weight(10, new ThinOne()),
            weight(5, new ThinAll()),
            weight(10, new ThickenOne()),
            weight(5, new ThickenAll()),
            weight(10, new Fork()),
            weight(5, new Segregate()),
            weight(5, new Tangle()),
            weight(1, new Life())
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

    public Mutator randomMutator(final Random rand) {
        int v = rand.nextInt(_sumWeight);
        for(Weight w:_mutators) {
            v -= w.weight();
            if(v<=0) {
                return w.mutator();
            }
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
