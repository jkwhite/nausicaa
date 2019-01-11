package org.excelsi.nausicaa.ca;


import com.google.gson.*;


public class EdgeMode implements Humanizable {
    public enum Type {
        toroidal, zero, constant
    };

    private final Type _t;
    private final int _iconstant;
    private final float _fconstant;


    public EdgeMode(Type t) {
        _t = t;
        _iconstant = 0;
        _fconstant = 0;
    }

    public EdgeMode(Type t, int constant) {
        _t = t;
        _iconstant = constant;
        _fconstant = 0;
    }

    public EdgeMode(Type t, float constant) {
        _t = t;
        _iconstant = 0;
        _fconstant = constant;
    }

    public EdgeMode(Type t, int iconstant, float fconstant) {
        _t = t;
        _iconstant = iconstant;
        _fconstant = fconstant;
    }

    @Override public String humanize() {
        switch(_t) {
            case toroidal:
                return "Toroidal";
            case zero:
                return "Zero";
            case constant:
                return "Constant ("+_iconstant+", "+_fconstant+")";
            default:
                return "???";
        }
    }

    public Type type() {
        return _t;
    }

    public int intConstant() {
        return _iconstant;
    }

    public float floatConstant() {
        return _fconstant;
    }

    public Integer oobValue() {
        switch(_t) {
            case toroidal:
            default:
                return null;
            case zero:
                return 0;
            case constant:
                return _iconstant;
        }
    }

    public Integer intOobValue() {
        return oobValue();
    }

    public Float floatOobValue() {
        switch(_t) {
            case toroidal:
            default:
                return null;
            case zero:
                return 0f;
            case constant:
                return _fconstant;
        }
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", _t.toString());
        if(_t==Type.constant) {
            o.addProperty("constant", _iconstant);
            o.addProperty("floatConstant", _fconstant);
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
                    Json.integer(o, "constant", 0),
                    Json.flot(o, "floatConstant", 0f));
        }
        throw new IllegalArgumentException("unknown type '"+t+"'");
    }

    public static EdgeMode defaultMode() {
        return new EdgeMode(Type.toroidal);
    }
}
