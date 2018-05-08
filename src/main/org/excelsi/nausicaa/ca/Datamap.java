package org.excelsi.nausicaa.ca;


import java.util.*;


public class Datamap {
    private final Map<String,Index> _m = new HashMap<>();


    public Datamap() {
    }

    public Datamap index(String name, Index idx) {
        _m.put(name, idx);
        return this;
    }

    public Index find(String name) {
        return _m.get(name);
    }
}
