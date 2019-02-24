package org.excelsi.nausicaa.ca;


import java.util.Random;


public class UpdateWeightTransform implements Transform {
    private static final double MIN_WEIGHT = 0.0001f;
    private final Random _rand;


    public UpdateWeightTransform(Random rand) {
        _rand = rand;
    }

    public String name() { return "UpdateWeight"; }

    public CA transform(CA c) {
        double a = (double)_rand.nextGaussian()/8f;
        double ow = c.getWeight();
        double nw = Math.max(MIN_WEIGHT,Math.min(1d,a+ow));
        System.err.println("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return c.weight(nw);
    }

    public static double mutateWeight(double ow, Random r) {
        double a = (double)r.nextGaussian()/8f;
        double nw = Math.max(MIN_WEIGHT,Math.min(1d,a+ow));
        System.err.println("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return nw;
    }
}
