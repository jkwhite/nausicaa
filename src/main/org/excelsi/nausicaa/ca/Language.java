package org.excelsi.nausicaa.ca;


import java.util.*;
import com.google.gson.*;


public class Language {
    public static final boolean FAIL = false;
    private final String _name;
    private final Map<String,String[]> _lang;
    private final Map<String,String> _rev;


    public Language(String name) {
        _name = name;
        _lang = new HashMap<>();
        _rev = new HashMap<>();
    }

    public String name() {
        return _name;
    }

    public Language add(String word, String... phonemes) {
        _lang.put(key(word), phonemes);
        _rev.put(combine(phonemes), word);
        return this;
    }

    public String[] phonemes(String word) {
        String[] ph = _lang.get(key(word));
        if(ph==null) {
            if(FAIL) {
                throw new IllegalArgumentException("no such word '"+word+"'; language contains: "+Arrays.toString(words()));
            }
            else {
                System.err.println("no such word '"+word+"'; language contains: "+Arrays.toString(words()));
                return new String[]{word};
            }
        }
        return ph;
    }

    public String word(String phonemes) {
        String w = _rev.get(phonemes);
        if(w==null) {
            if(FAIL)
                throw new IllegalArgumentException("no matching word for phonemes '"+phonemes+"'; language contains: "+_rev.keySet());
            else
                return phonemes;
        }
        return w;
    }

    public String expand(String word) {
        String[] phon = phonemes(word);
        StringBuilder b = new StringBuilder(12);
        for(String p:phon) {
            b.append(p).append("+");
        }
        b.setLength(b.length()-1);
        return b.toString();
    }

    public String randomCodon(Random r) {
        String[] cs = _lang.keySet().toArray(new String[0]);
        return cs[r.nextInt(cs.length)];
    }

    public String[] words() {
        String[] cs = _lang.keySet().toArray(new String[0]);
        return cs;
    }

    public Genome generate(final Archetype a, final Random r) {
        return new Genome(randomCodon(r)+" "
                +randomCodon(r)+" "
                +randomCodon(r)+" "
                +randomCodon(r)
            );
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("name", _name);
        JsonObject dict = new JsonObject();
        o.add("dict", dict);
        for(Map.Entry<String,String[]> e:_lang.entrySet()) {
            dict.add(e.getKey(), Json.toArray(e.getValue()));
        }
        return o;
    }

    private static String key(String word) {
        return word.replaceAll("[0-9]+", "");
    }

    public static Language fromJson(JsonElement el) {
        JsonObject o = (JsonObject) el;
        Language lang = new Language(Json.string(o, "name", "Unknown"));
        JsonObject dict = (JsonObject) o.get("dict");
        for(Map.Entry<String,JsonElement> e:dict.entrySet()) {
            lang.add(e.getKey(), Json.sarray(e.getValue()));
        }
        return lang;
    }

    private static String combine(String... phonemes) {
        StringBuilder b = new StringBuilder(12);
        for(String ph:phonemes) {
            b.append(ph).append('+');
        }
        b.setLength(b.length()-1);
        return b.toString();
    }
}
