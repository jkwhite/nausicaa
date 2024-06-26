package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import com.google.gson.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Sequence {
    private static final Logger LOG = LoggerFactory.getLogger(CA.class);

    private List<Segment> _ord = new ArrayList<>();
    private String _name;


    public Sequence(String name) {
        _name = name;
    }

    private Sequence(String name, List<Segment> ord) {
        _name = name;
        _ord = ord;
    }

    public Sequence add(CA ca, int gens) {
        _ord.add(new Segment(ca, gens));
        return this;
    }

    public String name() { return _name; }
    public void name(String name) { _name = name; }
    public Segment[] segments() { return _ord.toArray(new Segment[0]); }

    public Iterator<Plane> frameIterator(int segment, ExecutorService pool, GOptions opt, SequenceRepo repo) throws IOException {
        final Plane initial;
        if(segment==0) {
            initial = _ord.get(0).ca().createPlane(pool, opt);
        }
        else {
            Segment prev = _ord.get(segment-1);
            Plane p = repo.readPlane(prev, segment-1, prev.gens()-1, pool, opt);
            initial = p;
        }
        final Iterator<Plane> it = _ord.get(segment).ca().compileRule().frameIterator(initial, pool, opt);
        return new Iterator<Plane>() {
            boolean first = true;

            @Override public Plane next() {
                if(first) {
                    first = false;
                    return initial;
                }
                else {
                    return it.next();
                }
            }

            @Override public boolean hasNext() {
                return it.hasNext();
            }

            @Override public void remove() {}
        };
    }

    public Iterator<Plane> frameIterator(ExecutorService pool, GOptions opt) {
        Iterator<Segment> ss = _ord.iterator();
        return new Iterator<Plane>() {
            boolean first = true;
            Plane p;
            long curgens = 0;
            Segment curslice = ss.next();
            Iterator<Plane> curp;

            @Override public Plane next() {
                if(first) {
                    LOG.info("creating initial sequence plane");
                    p = curslice.ca().createPlane(pool, opt);
                    curp = curslice.ca().compileRule().frameIterator(p, pool, opt);
                    first = false;
                    return p;
                }
                else {
                    LOG.info("generations: "+curgens+"/"+curslice.gens());
                    if(++curgens<curslice.gens()) {
                        p = curp.next();
                        return p;
                    }
                    else {
                        LOG.info("moving to next slice");
                        curslice = ss.next();
                        curgens=0;
                        curp = curslice.ca().compileRule().frameIterator(p, pool, opt);
                        return next();
                    }
                }
            }

            @Override public boolean hasNext() {
                return ss.hasNext() || curgens<curslice.gens()-1;
            }

            @Override public void remove() { }
        };
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("name", _name);
        JsonArray a = new JsonArray();
        for(Segment s:_ord) {
            a.add(s.toJson());
        }
        o.add("segments", a);
        return o;
    }

    public static Sequence fromJson(JsonElement e) throws IOException {
        JsonObject o = (JsonObject) e;
        String name = Json.string(o, "name", "Nameless");
        JsonArray a = (JsonArray) o.get("segments");
        List<Segment> segs = new ArrayList<>();
        for(int i=0;i<a.size();i++) {
            segs.add(Segment.fromJson(a.get(i), name+"_"+i));
        }
        return new Sequence(name, segs);
    }

    public static class Segment {
        private final CA _ca;
        private int _gens;


        public Segment(CA ca, int gens) {
            _ca = ca;
            _gens = gens;
        }

        public CA ca() { return _ca; }
        public int gens() { return _gens; }
        public void gens(int gens) { _gens = gens; }

        public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("generations", _gens);
            o.add("ca", _ca.toJson());
            return o;
        }

        public static Segment fromJson(JsonElement e, String name) throws IOException {
            JsonObject o = (JsonObject) e;
            int gens = Json.integer(o, "generations", 100);
            CA c = CA.fromJson(o.get("ca"), name);
            return new Segment(c, gens);
        }
    }
}
