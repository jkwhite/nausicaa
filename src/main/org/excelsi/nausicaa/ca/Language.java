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
        String[] parts = parts(word);
        String[] ph = _lang.get(parts[0]);
        System.err.println("word: '"+word+"', parts[0]='"+parts[0]+"', parts[1]='"+parts[1]+"', ph="+Arrays.toString(ph));
        if(ph==null) {
            if(FAIL) {
                throw new IllegalArgumentException("no such word '"+word+"'; language contains: "+Arrays.toString(words()));
            }
            else {
                System.err.println("no such word '"+word+"'; language contains: "+Arrays.toString(words()));
                System.err.println("m0: "+word+" => "+word);
                return new String[]{word};
            }
        }
        if(ph.length==1) {
            System.err.println("m1: '"+word+"' => '"+ph[0]+"','"+parts[1]+"'");
            if(Character.isDigit(ph[0].charAt(ph[0].length()-1))) {
                return new String[]{ph[0]};
            }
            else {
                return new String[]{ph[0] + parts[1]}; // mi3
            }
        }
        else {
            System.err.println("m2: "+word+" => "+ph);
            return ph; //go mi
        }
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

    public String randomCodon(Archetype a, Random r) {
        String[] cs = _lang.keySet().toArray(new String[0]);
        return cs[r.nextInt(cs.length)];
    }

    public String[] words() {
        String[] cs = _lang.keySet().toArray(new String[0]);
        return cs;
    }

    public Genome generate(final Archetype a, final Random r) {
        return new Genome(randomCodon(a, r)+" "
                +randomCodon(a, r)+" "
                +randomCodon(a, r)+" "
                +randomCodon(a, r)
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

    private static String[] parts(String word) {
        String[] ps = new String[2];
        ps[0] = word.replaceAll("[0-9]+", "");
        ps[1] = word.substring(ps[0].length());
        System.err.println("m4: '"+word+"' => '"+ps[0]+"','"+ps[1]+"'");
        return ps;
    }

    public static Language fromJson(JsonElement el) {
        JsonObject o = (JsonObject) el;
        final String name = Json.string(o, "name", "Unknown");
        if("Universal".equals(name)) {
            return new Languages.Universal();
        }
        else {
            Language lang = new Language(name);
            JsonObject dict = (JsonObject) o.get("dict");
            for(Map.Entry<String,JsonElement> e:dict.entrySet()) {
                lang.add(e.getKey(), Json.sarray(e.getValue()));
            }
            return lang;
        }
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
