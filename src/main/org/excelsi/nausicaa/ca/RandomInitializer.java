package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


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

    @Override public String humanize() {
        return "Random (zeroWeight="+_params.zeroWeight+")";
    }

    public void init(Plane plane, Rule rule, Random random) {
        if(rule.archetype().isDiscrete()) {
            initDisc((IntPlane)plane, rule, random);
        }
        else {
            initCont((FloatPlane)plane, rule, random);
        }
    }

    private void initDisc(IntPlane plane, Rule rule, Random random) {
        final Random r;
        if(_random!=null) {
            _random.setSeed(_seed);
            r = _random;
        }
        else {
            r = random;
        }
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, computeColor(r, colors));
                }
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, computeColor(r, colors));
                    }
                }
                break;
            case 3:
                IntBlockPlane bp = (IntBlockPlane) plane;
                for(int y=0;y<bp.getHeight();y++) {
                    for(int x=0;x<bp.getWidth();x++) {
                        for(int z=0;z<bp.getDepth();z++) {
                            bp.setCell(x, y, z, computeColor(r, colors));
                        }
                    }
                }
                break;
            default:
        }
    }

    private void initCont(FloatPlane plane, Rule rule, Random random) {
        final Random r;
        if(_random!=null) {
            _random.setSeed(_seed);
            r = _random;
        }
        else {
            r = random;
        }
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, computeFloatColor(r, colors));
                }
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, computeFloatColor(r, colors));
                    }
                }
                break;
            case 3:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        for(int z=0;z<plane.getDepth();z++) {
                            plane.setCell(x, y, z, computeFloatColor(r, colors));
                        }
                    }
                }
                break;
            default:
        }
    }

    private int computeColor(Random random, int colors) {
        if(_params.zeroWeight>0f && random.nextInt(100000)<=100000f*_params.zeroWeight) {
            return 0;
        }
        else {
            return random.nextInt(colors);
        }
    }

    private float computeFloatColor(Random random, int colors) {
        if(_params.zeroWeight>0f && random.nextInt(100000)<=100000f*_params.zeroWeight) {
            return 0;
        }
        else {
            float v = ((float)(colors-1))*random.nextFloat();
            return v;
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

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","random");
        o.addProperty("seed", _seed);
        o.addProperty("zero_weight", _params.zeroWeight);
        return o;
    }

    public static RandomInitializer read(BufferedReader r, int version) throws IOException {
        return new RandomInitializer(
            null,
            Long.parseLong(r.readLine()),
            new Params(Float.parseFloat(r.readLine()))
        );
    }

    public static RandomInitializer fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        return new RandomInitializer(
            null,
            Json.lng(o, "seed", 0),
            new Params(
                Json.flot(o, "zero_weight", 0)
            )
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
