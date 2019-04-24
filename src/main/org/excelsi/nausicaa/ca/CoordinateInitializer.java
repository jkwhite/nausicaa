package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


public class CoordinateInitializer implements Initializer {
    public CoordinateInitializer() {
    }

    @Override public String humanize() {
        return "Coordinate";
    }

    public void init(Plane plane, Rule rule, Random random) {
        final int colors = rule.archetype().colors();
        final Pen pen = plane.pen();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    pen.setCell(x, 0, x-plane.getWidth()/2);
                    //plane.setCell(x, 0, computeColor(r, colors));
                }
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        //pen.setCell(x, y, x-plane.getWidth()/2);
                        //plane.setCell(x, y, computeColor(r, colors));
                    }
                }
                break;
            case 3:
                IntBlockPlane bp = (IntBlockPlane) plane;
                for(int y=0;y<bp.getHeight();y++) {
                    for(int x=0;x<bp.getWidth();x++) {
                        for(int z=0;z<bp.getDepth();z++) {
                            //bp.setCell(x, y, z, computeColor(r, colors));
                        }
                    }
                }
                break;
            default:
        }
    }

    /*
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
    */

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.coordinate.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.coordinate.name());
        //w.println(_seed);       
        //_params.write(w);
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","coordinate");
        return o;
    }

    @Override public Mutatable mutate(MutationFactor m) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean supportsMutation() {
        return false;
    }

    //public static RandomInitializer read(BufferedReader r, int version) throws IOException {
        //return new RandomInitializer(
            //null,
            //Long.parseLong(r.readLine()),
            //new Params(Float.parseFloat(r.readLine()))
        //);
    //}

    public static CoordinateInitializer fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        return new CoordinateInitializer();
    }
}
