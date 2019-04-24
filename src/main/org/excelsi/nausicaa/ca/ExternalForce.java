package org.excelsi.nausicaa.ca;


import java.util.Random;
import com.google.gson.*;


public interface ExternalForce extends Humanizable {
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

    public static ExternalForce createRandom(Random r) {
        if(r.nextBoolean()) {
            return new NopExternalForce();
        }
        else {
            return new RandomExternalForce(r.nextFloat());
        }
    }

    public static class NopExternalForce implements ExternalForce {
        @Override public String humanize() {
            return "None";
        }

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

        @Override public String humanize() {
            return "Random ("+_amount+")";
        }

        @Override public void apply(final Plane p, final Random r) {
            switch(p.type()) {
                case discrete:
                default:
                    applyDisc((IntPlane)p, r);
                    break;
                case continuous:
                    applyCont((FloatPlane)p, r);
                    break;
            }
        }

        @Override public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("type", "random");
            o.addProperty("amount", _amount);
            return o;
        }

        private void applyDisc(final IntPlane p, final Random r) {
            final float max = p.getWidth()*p.getHeight()*Math.max(1f,p.getDepth());
            //final int pnts = (int) (_amount*max);
            if(Math.abs(r.nextFloat())<_amount) {
                final int pnts = 1;
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
        }

        private void applyCont(final FloatPlane p, final Random r) {
            final float max = p.getWidth()*p.getHeight()*Math.max(1f,p.getDepth());
            final int pnts = (int) (_amount*max);
            final int cols = p.creator().archetype().colors();
            for(int i=0;i<pnts;i++) {
                if(p.getDepth()>0) {
                    p.setCell(
                        r.nextInt(p.getWidth()),
                        r.nextInt(p.getHeight()),
                        r.nextInt(p.getDepth()),
                        r.nextFloat()*(cols-1));
                }
                else {
                    p.setCell(
                        r.nextInt(p.getWidth()),
                        r.nextInt(p.getHeight()),
                        r.nextFloat()*(cols-1));
                }
            }
        }
    }
}
