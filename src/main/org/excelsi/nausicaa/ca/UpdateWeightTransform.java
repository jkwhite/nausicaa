package org.excelsi.nausicaa.ca;


import java.util.Random;


public class UpdateWeightTransform implements Transform {
    private static final float MIN_WEIGHT = 0.0001f;
    private final Random _rand;


    public UpdateWeightTransform(Random rand) {
        _rand = rand;
    }

    public String name() { return "UpdateWeight"; }

    public CA transform(CA c) {
        float a = (float)_rand.nextGaussian()/8f;
        float ow = c.getWeight();
        float nw = Math.max(MIN_WEIGHT,Math.min(1f,a+ow));
        System.err.println("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return c.weight(nw);
    }

    public static float mutateWeight(float ow, Random r) {
        float a = (float)r.nextGaussian()/8f;
        float nw = Math.max(MIN_WEIGHT,Math.min(1f,a+ow));
        System.err.println("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return nw;
    }
}
