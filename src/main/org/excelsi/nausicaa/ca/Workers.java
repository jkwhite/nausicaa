package org.excelsi.nausicaa.ca;


import java.util.Random;


public class Workers {
    private Workers() {}


    public static Worker create(Pattern p, int x1, int y1, int x2, int y2, float weight, ComputeMode cmode, UpdateMode umode, ExternalForce ef, Random r) {
        switch(p.archetype().values()) {
            case discrete:
            default:
                return new WorkerDiscrete(p, x1, y1, x2, y2, weight, cmode, umode, ef, r);
            case continuous:
                return new WorkerContinuous(p, x1, y1, x2, y2, weight, cmode, umode, ef, r);
        }
    }
}
