package org.excelsi.nausicaa.ca;


import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class InitializerTransform implements Transform {
    private static final Logger LOG = LoggerFactory.getLogger(InitializerTransform.class);

    private final Random _r;
    private final MutationFactor _f;


    public InitializerTransform(Random r, MutationFactor f) {
        _r = r;
        _f = f;
    }

    @Override public String name() {
        return "Initializers";
    }

    @Override public CA transform(CA c) {
        return c.initializer((Initializer)c.getInitializer().mutate(_f));
    }
}
