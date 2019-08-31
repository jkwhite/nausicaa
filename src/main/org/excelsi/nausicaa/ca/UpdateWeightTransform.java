package org.excelsi.nausicaa.ca;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Random;


public class UpdateWeightTransform implements Transform {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateWeightTransform.class);
    private static final double MIN_WEIGHT = 0.0001;
    private static final double MAX_WEIGHT = 1.0;
    private static final double MIN_DECAY = 0.9;
    private static final double MAX_DECAY = 1.0;
    private final Random _rand;


    public UpdateWeightTransform(Random rand) {
        _rand = rand;
    }

    public String name() { return "UpdateWeight"; }

    public CA transform(CA c) {
        double a = (double)_rand.nextGaussian()/8f;
        double ow = c.getWeight();
        double nw = Math.max(MIN_WEIGHT,Math.min(1d,a+ow));
        LOG.debug("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return c.weight(nw);
    }

    public static double mutateWeight(double ow, Random r) {
        double a = (double)r.nextGaussian()/8f;
        double nw = Math.max(MIN_WEIGHT,Math.min(MAX_WEIGHT,a+ow));
        LOG.debug("old weight: "+ow+", new weight: "+nw+", a: "+a);
        return nw;
    }

    public static double mutateDecay(double od, Random r, double w) {
        double a = (double)r.nextGaussian()/8f;
        double nd = Math.max(MIN_DECAY, a+od);
        if(w==1.0 && nd>1.0) {
            nd = 1.0;
        }
        LOG.debug("old decay: "+od+", new decay: "+nd+", a: "+a);
        return nd;
    }
}
