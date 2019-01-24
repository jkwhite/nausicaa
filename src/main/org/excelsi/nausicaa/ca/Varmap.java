package org.excelsi.nausicaa.ca;


import java.util.*;
import com.google.gson.*;


public class Varmap {
    public static final String P_START = "{";
    public static final String P_END = "}";

    private final Collection<String> _names;
    private final Map<String,String> _vars = new HashMap<>();


    public Varmap() {
        this(new String[0]);
    }

    public Varmap(Collection<String> names) {
        this(names.toArray(new String[0]));
    }

    public Varmap(String[] names) {
        _names = new ArrayList<>(Arrays.asList(names));
        for(String n:names) {
            _vars.put(n, null);
        }
    }

    public Varmap(Map<String,String> vars) {
        for(Map.Entry<String,String> v:vars.entrySet()) {
            _vars.put(v.getKey(), v.getValue());
        }
        _names = new ArrayList<>(vars.keySet());
    }

    public String[] names() {
        return _names.toArray(new String[0]);
    }

    public String[] getNames() { return names(); }

    public String get(String name) {
        String v = _vars.get(name);
        return v==null?"":v;
    }

    public Varmap put(String name, String value) {
        _vars.put(name, value);
        if(!_names.contains(name)) {
            _names.add(name);
        }
        return this;
    }

    public String getAt(String name) {
        return get(name);
    }

    public Varmap putAt(String name, String value) {
        return put(name, value);
    }

    public Varmap merge(Varmap o) {
        Collection<String> names = new ArrayList<>();
        names.addAll(_names);
        if(o!=null) {
            for(String on:o._names) {
                if(!names.contains(on)) names.add(on);
            }
        }
        Varmap merged = new Varmap(names);
        for(String n:names) {
            String v = _vars.get(n);
            if(v==null&&o!=null) {
                v = o._vars.get(n);
            }
            merged.put(n, v);
        }
        return merged;
    }

    @Override public String toString() {
        return "VarMap::"+_vars.toString();
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        for(Map.Entry<String,String> e:_vars.entrySet()) {
            o.addProperty(e.getKey(), e.getValue());
        }
        return o;
    }

    public static Varmap fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        Map<String,String> vs = new HashMap<>();
        for(Map.Entry<String,JsonElement> v:o.entrySet()) {
            vs.put(v.getKey(), Json.string(v.getValue()));
        }
        return new Varmap(vs);
    }

    public static final boolean containsVar(String s) {
        return s.indexOf(P_START)>=0;
    }
}
