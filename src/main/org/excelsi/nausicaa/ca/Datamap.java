package org.excelsi.nausicaa.ca;


import java.util.*;


public class Datamap {
    private static final String NS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
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

    public Datamap copy() {
        Datamap d = new Datamap();
        Map<String,Index> m = new HashMap<>();
        for(Map.Entry<String,Index> e:_m.entrySet()) {
            d.index(e.getKey(), e.getValue().copy());
        }
        return d;
    }

    public static String randomName(Random r) {
        String b = "";
        for(int i=0;i<3;i++) {
            b = b+NS.charAt(r.nextInt(NS.length()));
        }
        return b;
    }
}
