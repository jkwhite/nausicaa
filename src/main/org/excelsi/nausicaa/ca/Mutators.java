package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public enum Mutators {
    collapse,
    order,
    cull,
    fork,
    hue,
    color,
    expand,
    noise,
    tangle,
    diverge,
    thin,
    thicken,
    symmetry,
    transpose,
    stabilize,
    destabilize,
    random,
    life;


    public static Iterable<Mutator> createAllIndexed() {
        List<Mutator> all = new ArrayList<>();
        all.add(thicken.createIndexed());
        all.add(thin.createIndexed());
        all.add(noise.createIndexed());
        all.add(color.createIndexed());
        all.add(collapse.createIndexed());
        return all;
    }

    public static Iterable<Mutator> createAll() {
        List<Mutator> all = new ArrayList<>();
        for(Mutators m:EnumSet.allOf(Mutators.class)) {
            all.add(m.create());
        }
        return all;
    }

    public Mutator createIndexed() {
        switch(this) {
            case color:
                return new Color();
            case collapse:
                return new Collapse();
            case noise:
                return new Noise();
            case thin:
                return new ThinAll();
            case thicken:
            default:
                return new ThickenAll();
        }
    }

    public static Mutators randomIndexed(Random rand) {
        switch(rand.nextInt(10)) {
            case 4:
                return color;
            case 3:
                return collapse;
            case 2:
                return thicken;
            case 1:
                return thin;
            case 0:
            default:
                return noise;
        }
    }

    public Mutator create() {
        switch(this) {
            case random:
                return new RandomMutator();
            case tangle:
                return new Tangle();
            case cull:
                return new Cull();
            case fork:
                return new Fork();
            case symmetry:
                return new Symmetry();
            case thicken:
                return new ThickenAll();
            case thin:
                return new ThinAll();
            case hue:
                return new Hue();
            case noise:
                return new Noise();
            case color:
            default:
                return new Color();
        }
        /*
        switch(this) {
            case collapse:
                return new Collapse();
            case order:
                return new Order();
            case cull:
                return new Cull();
            case clone:
                return new Clone();
            case hue:
                return new Hue();
            case color:
                return new Color();
            case expand:
                return new Expand();
            case noise:
                return new Noise();
            case tangle:
                return new Tangle();
            case diverge:
                return new Diverge();
            case thin:
                return new Thin();
            case thicken:
                return new Thicken();
            case symmetry:
                return new Symmetry();
            case transpose:
                return new Transpose();
            case stabilize:
                return new Stability(1);
            case destabilize:
                return new Stability(-1);
            case life:
                return new Life();
        }
        */
    }
}
