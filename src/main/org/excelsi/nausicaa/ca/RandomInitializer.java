package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public class RandomInitializer implements Initializer {
    private final Random _random;
    private final long _seed;
    private final Params _params;


    public RandomInitializer() {
        this(null, 0);
    }

    public RandomInitializer(long seed) {
        this(new Random(), seed);
    }

    public RandomInitializer(Random random, long seed) {
        this(random, seed, new Params());
    }

    public RandomInitializer(Random random, long seed, Params params) {
        _random = random;
        _seed = seed;
        _params = params;
    }

    public void init(Plane plane, Rule rule, Random random) {
        final Random r;
        if(_random!=null) {
            _random.setSeed(_seed);
            r = _random;
        }
        else {
            r = random;
        }
        int[] colors = rule.colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, colors[computeColor(r, colors.length)]);
                }
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, colors[computeColor(r, colors.length)]);
                    }
                }
                break;
            case 3:
                IntBlockPlane bp = (IntBlockPlane) plane;
                for(int y=0;y<bp.getHeight();y++) {
                    for(int x=0;x<bp.getWidth();x++) {
                        for(int z=0;z<bp.getDepth();z++) {
                            bp.setCell(x, y, z, colors[computeColor(r, colors.length)]);
                        }
                    }
                }
                break;
            default:
        }
    }

    private int computeColor(Random random, int colors) {
        if(_params.zeroWeight>0f && random.nextInt(1000)<=1000f*_params.zeroWeight) {
            return 0;
        }
        else {
            return random.nextInt(colors);
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.random.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.random.name());
        w.println(_seed);       
        _params.write(w);
    }

    public static RandomInitializer read(BufferedReader r, int version) throws IOException {
        return new RandomInitializer(
            null,
            Long.parseLong(r.readLine()),
            new Params(Float.parseFloat(r.readLine()))
        );
    }

    public static final class Params {
        public final float zeroWeight;


        public Params() {
            this(0f);
        }

        public Params(float zeroWeight) {
            this.zeroWeight = zeroWeight;
        }

        public void write(PrintWriter w) {
            w.println(zeroWeight);
        }
    }
}
