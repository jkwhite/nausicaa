package org.excelsi.nausicaa.ca;


import java.util.List;
import java.util.ArrayList;
import com.google.gson.*;


public class Events {
    public static class Event {
        private final long _timestamp;
        private final String _desc;


        public Event(long timestamp, String desc) {
            _timestamp = timestamp;
            _desc = desc;
        }

        public long timestamp() { return _timestamp; }
        public String desc() { return _desc; }

        public Event copy() { return new Event(_timestamp, _desc); }

        public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("timestamp", _timestamp);
            o.addProperty("desc", _desc);
            return o;
        }

        public static Event fromJson(JsonElement e) {
            JsonObject o = (JsonObject) e;
            return new Event(
                Json.lng(o, "timestamp", 0L),
                Json.string(o, "desc", ""));
        }

        @Override public String toString() {
            return "Event::{timestamp:"+_timestamp+", desc:"+_desc+"}";
        }
    }


    private final List<Event> _es;


    public Events() {
        _es = new ArrayList<>();
    }

    public void add(Event e) {
        _es.add(e);
    }

    public List<Event> events() {
        return _es;
    }

    public Events fork() {
        Events evs = new Events();
        for(Event e:_es) {
            evs.add(e.copy());
        }
        return evs;
    }

    public JsonElement toJson() {
        JsonArray a = new JsonArray();
        for(Event e:_es) {
            a.add(e.toJson());
        }
        return a;
    }

    public static Events fromJson(JsonElement e) {
        JsonArray a = (JsonArray) e;
        Events evs = new Events();
        for(JsonElement ev:a) {
            evs.add(Event.fromJson(ev));
        }
        return evs;
    }

    @Override public String toString() {
        return "Events::"+_es;
    }
}
