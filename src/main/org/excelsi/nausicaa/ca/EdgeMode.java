package org.excelsi.nausicaa.ca;


import com.google.gson.*;


public class EdgeMode {
    public enum Type {
        toroidal, zero, constant
    };

    private final Type _t;
    private final int _constant;


    public EdgeMode(Type t) {
        _t = t;
        _constant = 0;
    }

    public EdgeMode(Type t, int constant) {
        _t = t;
        _constant = constant;
    }

    public Type type() {
        return _t;
    }

    public int constant() {
        return _constant;
    }

    public Integer oobValue() {
        switch(_t) {
            case toroidal:
            default:
                return null;
            case zero:
                return 0;
            case constant:
                return _constant;
        }
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", _t.toString());
        if(_t==Type.constant) {
            o.addProperty("constant", _constant);
        }
        return o;
    }

    public static EdgeMode fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        String t = Json.string(o, "type");
        switch(t) {
            case "toroidal":
                return new EdgeMode(Type.toroidal);
            case "zero":
                return new EdgeMode(Type.zero);
            case "constant":
                return new EdgeMode(Type.constant,
                    Json.integer(o, "constant", 0));
        }
        throw new IllegalArgumentException("unknown type '"+t+"'");
    }

    public static EdgeMode defaultMode() {
        return new EdgeMode(Type.toroidal);
    }
}
