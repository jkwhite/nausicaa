package org.excelsi.ca;


import java.util.Random;


public class MutatorFactory {
    private static MutatorFactory _f;
    private boolean _forceSymmetry = false;


    private MutatorFactory() {
    }

    public static synchronized MutatorFactory instance() {
        if(_f==null) {
            _f = new MutatorFactory();
        }
        return _f;
    }

    public void setForceSymmetry(boolean f) {
        _forceSymmetry = f;
    }

    public boolean getForceSymmetry() {
        return _forceSymmetry;
    }

    public Mutator createHue(Random om, boolean useBackground, int color) {
        Mutator m = new Hue(useBackground, color);
        m.setRandom(om);
        return m;
    }

    public Mutator createMutator(Mutators m) {
        return createMutator(m, Rand.om);
    }

    public Mutator createMutator(Mutators m, Random om) {
        return internalCreateMutator(m, om);
    }

    public Mutator createRandomMutator(Random om) {
        return internalCreateMutator(internalCreateRandomMutator(om), om);
    }

    private Mutator internalCreateMutator(Mutators m, Random om) {
        Mutator mu = m.create();
        mu.setRandom(om);
        if(_forceSymmetry) {
            SymmetryForcer f = new SymmetryForcer(mu);
            f.setRandom(om);
            return f;
        }
        else {
            return mu;
        }
    }

    private Mutators internalCreateRandomMutator(Random om) {
        switch(om.nextInt(18)) {
            case 15:
                return Mutators.destabilize;
            case 14:
                return Mutators.stabilize;
            case 13:
                return Mutators.symmetry;
            case 12:
                return Mutators.thicken;
            case 11:
                return Mutators.thin;
            case 10:
                return Mutators.diverge;
            case 9:
                return Mutators.collapse;
            case 8:
                return Mutators.order;
            case 7:
                return Mutators.cull;
            case 6:
                return Mutators.clone;
            case 5:
                return Mutators.hue;
            case 4:
                return Mutators.color;
            case 3:
                return Mutators.expand;
            case 2:
                return Mutators.tangle;
            default:
                return Mutators.noise;
        }
    }
}
