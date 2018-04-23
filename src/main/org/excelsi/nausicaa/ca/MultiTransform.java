package org.excelsi.nausicaa.ca;


import java.util.Random;


public class MultiTransform implements Transform {
    private final Random _rand;
    private final MutationFactor _factor;
    private final Mutator _extraMutator;
    private boolean _ruleVariations;
    private boolean _hueVariations;
    private boolean _weightVariations;


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

    public MultiTransform ruleVariations(boolean v) {
        _ruleVariations = v;
        return this;
    }

    public MultiTransform hueVariations(boolean v) {
        _hueVariations = v;
        return this;
    }

    public MultiTransform weightVariations(boolean v) {
        _weightVariations = v;
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
        //System.err.println("multi: hv: "+_hueVariations+", wv: "+_weightVariations);
        Transform t = null;
        int tries = 0;
        do {
            switch(_rand.nextInt(6)) {
                case 2:
                    if(_weightVariations) {
                        t = new UpdateWeightTransform(_rand);
                        break;
                    }
                case 1:
                    if(_hueVariations) {
                        t = new HueTransform(_rand);
                        break;
                    }
                default:
                case 0:
                    if(_ruleVariations) {
                        t = new RuleTransform(_rand, createMutator(c), _factor);
                    }
                    break;
            }
        } while(++tries<10 && t==null);
        return t!=null?t.transform(c):c.copy();
    }

    protected Mutator createMutator(CA c) {
        Mutator m = MutatorFactory.defaultMutators().randomMutator(c.getRandom(), c.getRule());
        return _extraMutator!=null?Mutator.chain(m, _extraMutator):m;
    }
}
