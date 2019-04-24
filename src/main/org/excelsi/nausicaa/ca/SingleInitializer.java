package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


public class SingleInitializer implements Initializer {
    private final int _x;
    private final int _y;
    private final int _z;
    private final float _color;
    private final int _size;


    public SingleInitializer() {
        this(-1, -1, -1, 1, 1);
    }

    public SingleInitializer(float color, int x, int y, int z, int size) {
        _color = color;
        _x = x;
        _y = y;
        _z = z;
        _size = size;
    }

    @Override public String humanize() {
        return "Fixed (color="+_color+", x="+_x+", y="+_y+", z="+_z+")";
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
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, 0);
                }
                plane.setCell(coordX(plane, _x), 0, color(colors, random, _color));
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, 0);
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), color(colors, random, _color));
                break;
            case 3:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        for(int z=0;z<plane.getDepth();z++) {
                            plane.setCell(x, y, z, 0);
                        }
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), coordZ(plane, _z), color(colors, random, _color));
                break;
            default:
        }
    }

    private void initCont(FloatPlane plane, Rule rule, Random random) {
        int colors = rule.archetype().colors();
        switch(rule.dimensions()) {
            case 1:
                for(int x=0;x<plane.getWidth();x++) {
                    plane.setCell(x, 0, 0);
                }
                plane.setCell(coordX(plane, _x), 0, colorFloat(colors, random, _color));
                break;
            case 2:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        plane.setCell(x, y, 0);
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), colorFloat(colors, random, _color));
                break;
            case 3:
                for(int y=0;y<plane.getHeight();y++) {
                    for(int x=0;x<plane.getWidth();x++) {
                        for(int z=0;z<plane.getDepth();z++) {
                            plane.setCell(x, y, z, 0);
                        }
                    }
                }
                plane.setCell(coordX(plane, _x), coordY(plane, _y), coordZ(plane, _z), colorFloat(colors, random, _color));
                break;
            default:
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.single.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.single.name());
        w.println(_color);
        w.println(_x);
        w.println(_y);
        w.println(_z);
        w.println(_size);
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","single");
        o.addProperty("color",_color);
        o.addProperty("x",_x);
        o.addProperty("y",_y);
        o.addProperty("z",_z);
        o.addProperty("size",_size);
        return o;
    }

    @Override public Mutatable mutate(MutationFactor m) {
        float nc;
        if(m.r().nextBoolean()) {
            nc = -1;
        }
        else {
            nc = m.r().nextFloat()*(m.archetype().colors()-1f);
        }
        return new SingleInitializer(nc, _x, _y, _z, _size);
    }

    @Override public boolean supportsMutation() {
        return true;
    }

    private int coordX(Plane p, int v) {
        return v==-1?p.getWidth()/2:v%p.getWidth();
    }

    private int coordY(Plane p, int v) {
        return v==-1?p.getHeight()/2:v%p.getHeight();
    }

    private int coordZ(Plane p, int v) {
        return v==-1?p.getDepth()/2:v%p.getDepth();
    }

    private float colorFloat(int colors, Random r, float v) {
        return v==-1?(((float)(colors-1))*r.nextFloat()):v%colors;
    }

    private int color(int colors, Random r, float v) {
        return v==-1?(1+r.nextInt(colors-1)):((int)v)%colors;
    }

    public static SingleInitializer read(BufferedReader r, int version) throws IOException {
        if(version<4) {
            return new SingleInitializer();
        }
        else {
            return new SingleInitializer(
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine())
            );
        }
    }

    public static SingleInitializer fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        return new SingleInitializer(
            Json.flot(o, "color", -1),
            Json.integer(o, "x", -1),
            Json.integer(o, "y", -1),
            Json.integer(o, "z", -1),
            Json.integer(o, "size", 1)
        );
    }
}
