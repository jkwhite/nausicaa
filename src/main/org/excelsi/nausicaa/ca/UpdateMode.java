package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.*;


public interface UpdateMode {
    boolean simpleSynchronous();

    boolean update(Plane p, int x, int y, int z);

    JsonElement toJson();

    public static UpdateMode fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        String type = Json.string(o, "type");
        int chance = Json.integer(o, "chance", 0);
        return create(type, chance);
    }

    public static UpdateMode create(String type, int chance) {
        final Random r = new Random();
        switch(type) {
            case "sync":
                return new SimpleSynchronous();
            case "async":
                return new SimpleAsynchronous(r, chance);
            case "localasync":
                return new LocalAsynchronous(r, chance, new int[0]);
            default:
                throw new IllegalArgumentException("unknown type '"+type+"'");
        }
    }

    public static final class SimpleSynchronous implements UpdateMode {
        @Override public boolean simpleSynchronous() {
            return true;
        }

        @Override public boolean update(Plane p, int x, int y, int z) {
            return true;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","sync");
            return o;
        }
    }

    public static final class SimpleAsynchronous implements UpdateMode {
        private final Random _r;
        private final int _chance;


        public SimpleAsynchronous(Random r, int chance) {
            _r = r;
            _chance = chance;
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public boolean update(Plane p, int x, int y, int z) {
            return _r.nextInt(1000)<_chance;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","async");
            o.addProperty("chance",_chance);
            return o;
        }
    }

    public static final class LocalAsynchronous implements UpdateMode {
        private final Random _r;
        private final int _chance;
        private final int[] _center;


        public LocalAsynchronous(Random r, int chance, int[] center) {
            _r = r;
            _chance = chance;
            _center = center;
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public boolean update(Plane p, int x, int y, int z) {
            final int cx = p.getWidth()/2;
            final int cy = p.getHeight()/2;
            final int cz = p.getDepth()/2;
            final int d = (x-cx)*(x-cx)+(y-cy)*(y-cy)+(z-cz)*(z-cz);
            final int md = p.getWidth()*p.getWidth()+p.getHeight()*p.getHeight()+p.getDepth()*p.getDepth();
            return _r.nextInt(md+_chance)>d;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","localasync");
            o.addProperty("chance",_chance);
            return o;
        }
    }
}
