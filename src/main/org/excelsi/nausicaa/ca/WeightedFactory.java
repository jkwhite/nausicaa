package org.excelsi.nausicaa.ca;


import java.util.Random;


public class WeightedFactory<E> {
    private Weight<E>[] _mutators;
    private int _sumWeight;


    public WeightedFactory(Weight<E>... mutators) {
        _mutators = mutators;
        reweight();
        //int sum = 0;
        //for(Weight<E> w:_mutators) {
            //sum += w.weight();
        //}
        //_sumWeight = sum;
    }

    public void add(Weight<E> m) {
        Weight<E>[] ms = new Weight[_mutators.length+1];
        System.arraycopy(_mutators, 0, ms, 0, _mutators.length);
        ms[ms.length-1] = m;
        _mutators = ms;
        reweight();
    }

    public Weight<E>[] all() {
        return _mutators;
    }

    public E random(final Random rand) {
        E m = null;
        int tries = 0;
        do {
            int v = rand.nextInt(_sumWeight);
            for(Weight<E> w:_mutators) {
                v -= w.weight();
                if(v<=0) {
                    m = w.e();
                    break;
                }
            }
        } while(++tries<1000&&m==null);
        if(m!=null) {
            return m;
        }
        throw new IllegalStateException("impossible weight range for "+_sumWeight);
    }

    private void reweight() {
        int sum = 0;
        for(Weight<E> w:_mutators) {
            sum += w.weight();
        }
        _sumWeight = sum;
    }

    public static <E> Weight<E> weight(int weight, E m) {
        return new Weight<E>(weight, m);
    }

    public static class Weight<E> {
        private final int _weight;
        private final E _m;


        public Weight(int weight, E m) {
            _weight = weight;
            _m = m;
        }

        public int weight() {
            return _weight;
        }

        public E e() {
            return _m;
        }
    }
}
