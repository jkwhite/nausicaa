package org.excelsi.nausicaa.ca;


import java.util.Random;
import com.google.gson.*;


public interface ExternalForce {
    void apply(Plane p, Random r);
    JsonElement toJson();


    public static ExternalForce nop() {
        return new NopExternalForce();
    }

    public static ExternalForce fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        String type = Json.string(o, "type");
        switch(type) {
            case "random":
                return new RandomExternalForce(Json.flot(o, "amount", 0.01f));
            case "nop":
                return new NopExternalForce();
        }
        throw new IllegalArgumentException("unknown type '"+type+"'");
    }

    public static class NopExternalForce implements ExternalForce {
        @Override public void apply(final Plane p, final Random r) {
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type", "nop");
            return o;
        }
    }

    public static class RandomExternalForce implements ExternalForce {
        private final float _amount;


        public RandomExternalForce(float amount) {
            _amount = amount;
        }

        @Override public void apply(final Plane p, final Random r) {
            final float max = p.getWidth()*p.getHeight()*Math.max(1f,p.getDepth());
            final int pnts = (int) (_amount*max);
            final int cols = p.creator().archetype().colors();
            for(int i=0;i<pnts;i++) {
                if(p.getDepth()>0) {
                    p.setCell(
                        r.nextInt(p.getWidth()),
                        r.nextInt(p.getHeight()),
                        r.nextInt(p.getDepth()),
                        r.nextInt(cols));
                }
                else {
                    p.setCell(
                        r.nextInt(p.getWidth()),
                        r.nextInt(p.getHeight()),
                        r.nextInt(cols));
                }
            }
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type", "random");
            o.addProperty("amount", _amount);
            return o;
        }
    }
}
