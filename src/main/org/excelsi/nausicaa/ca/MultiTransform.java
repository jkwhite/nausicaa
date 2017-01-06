package org.excelsi.nausicaa.ca;


import java.util.Random;


public class MultiTransform implements Transform {
    private final Random _rand;
    private final MutationFactor _factor;
    private final Mutator _extraMutator;
    private boolean _hueVariations;


    public MultiTransform(Random rand, MutationFactor f) {
        this(rand, f, null);
    }

    public MultiTransform(Random rand, MutationFactor f, Mutator extraMutator) {
        _rand = rand;
        _factor = f;
        _extraMutator = extraMutator;
    }

    public String name() {
        return "multitransform";
    }

    public MultiTransform hueVariations(boolean v) {
        _hueVariations = v;
        return this;
    }

    public CA transform(CA c) {
        for(int i=0;i<4;i++) {
            try {
                return runTransform(c);
            }
            catch(MutationFailedException e) {
                System.err.println("transform failed: "+e);
                e.printStackTrace();
            }
        }
        return c;
    }

    private CA runTransform(CA c) throws MutationFailedException {
        Transform t;
        if(_hueVariations) {
            switch(_rand.nextInt(6)) {
                case 1:
                    t = new HueTransform(_rand);
                    break;
                default:
                case 0:
                    t = new RuleTransform(_rand, createMutator(c), _factor);
                    break;
            }
        }
        else {
            t = new RuleTransform(_rand, createMutator(c), _factor);
        }
        return t.transform(c);
    }

    protected Mutator createMutator(CA c) {
        Mutator m = MutatorFactory.defaultMutators().randomMutator(c.getRandom(), c.getRule());
        return _extraMutator!=null?Mutator.chain(m, _extraMutator):m;
    }
}
