package org.excelsi.nausicaa.ca;


import com.google.gson.*;


public final class Json {
    private Json() {}


    public static String string(JsonObject o, String name) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsString():null;
    }

    public static String string(JsonObject o, String name, String def) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsString():def;
    }

    public static int integer(JsonObject o, String name, int def) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsInt():def;
    }

    public static boolean bool(JsonObject o, String name, boolean def) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsBoolean():def;
    }

    public static long lng(JsonObject o, String name, long def) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsLong():def;
    }

    public static float flot(JsonObject o, String name, float def) {
        JsonPrimitive v = o.getAsJsonPrimitive(name);
        return v!=null?v.getAsFloat():def;
    }
}