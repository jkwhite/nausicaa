package org.excelsi.nausicaa.ca;


import java.util.*;
import java.io.*;
import java.util.zip.*;


public class History {
    private static final Map<String,History> _instances = new HashMap<>();

    private List<String> _past = new ArrayList<>();
    private Set<String> _hashes = new HashSet<>();


    public static synchronized History named(String name) {
        History h = _instances.get(name);
        if(h==null) {
            h = new History();
            _instances.put(name, h);
        }
        return h;
    }

    public void push(CA c) {
        String enc = encode(c);
        //ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //GZIPOutputStream gos = new GZIPOutputStream(bos);
        //PrintWriter pw = new PrintWriter(new OutputStreamWriter(gos));
        //pw.write(s);
        //pw.close();
        System.err.println(enc);
        _past.add(enc);
        _hashes.add(enc);
    }

    public boolean contains(CA c) {
        return _hashes.contains(encode(c));
    }

    private static String encode(CA c) {
        return Base64.encodeObject(c.toJson().toString(), Base64.GZIP | Base64.DONT_BREAK_LINES);
    }
}
