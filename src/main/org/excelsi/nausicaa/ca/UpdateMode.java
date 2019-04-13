package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.*;


public interface UpdateMode extends Plannable<UpdateMode,Archetype>, Humanizable {
    boolean simpleSynchronous();

    String name();

    boolean update(Plane p, int x, int y, int z, Variables vars);

    JsonElement toJson();

    @Override default public UpdateMode plan(Archetype a) {
        return this;
    }

    public static UpdateMode fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        String type = Json.string(o, "type");
        float chance = Json.flot(o, "chance", 0);
        int size = Json.integer(o, "size", 1);
        return create(type, chance, size);
    }

    public static UpdateMode createRandom(Random r) {
        final String[] types = {"sync","async","variable","localasync","energy"};
        return create(types[r.nextInt(5)], r.nextFloat(), 1);
    }

    public static UpdateMode create(String type, float chance, int size) {
        final Random r = new Random();
        switch(type) {
            case "sync":
                return new SimpleSynchronous();
            case "async":
                return new SimpleAsynchronous(r, chance);
            case "variable":
                return new VariableAsynchronous(chance);
            case "localasync":
                return new LocalAsynchronous(r, chance, new int[0]);
            case "energy":
                return new EnergyAsynchronous(r, chance, size);
            default:
                throw new IllegalArgumentException("unknown type '"+type+"'");
        }
    }

    public static final class SimpleSynchronous implements UpdateMode {
        @Override public String name() { return "Synchronous"; }

        @Override public String humanize() {
            return name();
        }

        @Override public boolean simpleSynchronous() {
            return true;
        }

        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
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
        private final float _chance;


        public SimpleAsynchronous(Random r, float chance) {
            _r = r;
            _chance = chance;
        }

        @Override public String name() { return "Asynchronous"; }

        @Override public String humanize() {
            return name()+" ("+_chance+")";
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
            return _r.nextFloat()<_chance;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","async");
            o.addProperty("chance",_chance);
            return o;
        }
    }

    public static final class VariableAsynchronous implements UpdateMode {
        private final float _chance;


        public VariableAsynchronous(float chance) {
            _chance = chance;
        }

        @Override public String name() { return "Variable Async"; }

        @Override public String humanize() {
            return name()+" ("+_chance+")";
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
            boolean u = vars.update(p, x, y, z, _chance);
            //System.err.println("upd: "+u+", vars: "+vars);
            return u;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","variable");
            o.addProperty("chance",_chance);
            return o;
        }
    }

    public static final class LocalAsynchronous implements UpdateMode {
        private final Random _r;
        private final float _chance;
        private final int[] _center;


        public LocalAsynchronous(Random r, float chance, int[] center) {
            _r = r;
            _chance = chance;
            _center = center;
        }

        @Override public String name() { return "Local Async"; }

        @Override public String humanize() {
            return name()+" ("+_chance+")";
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
            final int cx = p.getWidth()/2;
            final int cy = p.getHeight()/2;
            final int cz = p.getDepth()/2;
            final int d = (x-cx)*(x-cx)+(y-cy)*(y-cy)+(z-cz)*(z-cz);
            final int md = p.getWidth()/2*p.getWidth()/2+p.getHeight()/2*p.getHeight()/2+p.getDepth()/2*p.getDepth()/2;
            return _r.nextInt((int)(md*_chance))>=d;
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","localasync");
            o.addProperty("chance",_chance);
            return o;
        }
    }

    public static class EnergyAsynchronous implements UpdateMode {
        private final Random _r;
        private final float _chance;
        private final int _size;


        public EnergyAsynchronous(Random r, float chance, int size) {
            _r = r;
            _chance = chance;
            _size = size;
        }

        @Override public String name() { return "Energy Async"; }

        @Override public String humanize() {
            return name()+" ("+_chance+", "+_size+")";
        }

        @Override public boolean simpleSynchronous() {
            return false;
        }

        @Override public UpdateMode plan(final Archetype a) {
            switch(a.values()) {
                default:
                case discrete:
                    return new EnergyAsynchronous(_r, _chance, _size) {
                        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
                            final IntPlane qp = (IntPlane) p;
                            int c;
                            switch(_size) {
                                case 1:
                                    c = 1+qp.getCell(x,y,z);
                                    break;
                                default:
                                    int m = 0;
                                    for(int i=x-_size;i<=x+_size;i++) {
                                        for(int j=y-_size;j<=y+_size;j++) {
                                            for(int k=z-_size;k<=z+_size;k++) {
                                                int t = qp.getCell(i,j,k);
                                                if(t>m) m=t;
                                            }
                                        }
                                    }
                                    c = m;
                                    break;
                            }
                            int m = qp.creator().archetype().colors();
                            float e = _chance*(float)c/(float)m;
                            return _r.nextFloat()<=e;
                        }
                    };
                case continuous:
                    return new EnergyAsynchronous(_r, _chance, _size) {
                        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
                            final FloatPlane qp = (FloatPlane) p;
                            double c;
                            switch(_size) {
                                case 1:
                                    c = 1+qp.getCell(x,y,z);
                                    break;
                                default:
                                    double m = 0;
                                    for(int i=x-_size;i<=x+_size;i++) {
                                        for(int j=y-_size;j<=y+_size;j++) {
                                            for(int k=z-_size;k<=z+_size;k++) {
                                                double t = qp.getCell(i,j,k);
                                                if(t>m) m=t;
                                            }
                                        }
                                    }
                                    c = m;
                                    break;
                            }
                            int m = qp.creator().archetype().colors();
                            double e = _chance*(double)c/(double)m;
                            return _r.nextDouble()<=e;
                        }
                    };
            }
        }

        @Override public boolean update(Plane p, int x, int y, int z, Variables vars) {
            throw new UnsupportedOperationException("requires plan");
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type","energy");
            o.addProperty("chance",_chance);
            o.addProperty("size",_size);
            return o;
        }
    }
}
