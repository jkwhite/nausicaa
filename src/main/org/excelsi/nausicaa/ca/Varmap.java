package org.excelsi.nausicaa.ca;


import java.util.*;


public class Varmap {
    public static final String P_START = "{";
    public static final String P_END = "}";

    private final Set<String> _names;
    private final Map<String,String> _vars = new HashMap<>();


    public Varmap() {
        this(new String[0]);
    }

    public Varmap(Set<String> names) {
        this(names.toArray(new String[0]));
    }

    public Varmap(String[] names) {
        _names = new HashSet<>(Arrays.asList(names));
        for(String n:names) {
            _vars.put(n, null);
        }
    }

    public String[] names() {
        return _names.toArray(new String[0]);
    }

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

    public Varmap merge(Varmap o) {
        Set<String> names = new HashSet<>();
        names.addAll(_names);
        if(o!=null) {
            names.addAll(o._names);
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

    public static final boolean containsVar(String s) {
        return s.indexOf(P_START)>=0;
    }
}
