package org.excelsi.nausicaa.ca;


import java.util.Random;


public class MultiTransform implements Transform {
    private final Random _rand;
    private final Mutator _extraMutator;


    public MultiTransform(Random rand) {
        this(rand, null);
    }

    public MultiTransform(Random rand, Mutator extraMutator) {
        _rand = rand;
        _extraMutator = extraMutator;
    }

    public String name() {
        return "multitransform";
    }

    public CA transform(CA c) {
        for(int i=0;i<4;i++) {
            try {
                return runTransform(c);
            }
            catch(MutationFailedException e) {
                System.err.println("transform failed: "+e);
            }
        }
        return c;
    }

    private CA runTransform(CA c) throws MutationFailedException {
        Transform t;
        switch(_rand.nextInt(4)) {
            case 1:
                t = new HueTransform(_rand);
                break;
            default:
            case 0:
                t = new RuleTransform(_rand, createMutator(c));
                break;
        }
        return t.transform(c);
    }

    protected Mutator createMutator(CA c) {
        Mutator m = MutatorFactory.defaultMutators().randomMutator(c.getRandom());
        return _extraMutator!=null?Mutator.chain(m, _extraMutator):m;
    }
}
