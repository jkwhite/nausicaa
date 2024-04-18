package org.excelsi.nausicaa.ca;


import java.util.*;
import com.google.gson.*;


public class Language {
    public static final boolean FAIL = false;
    private final String _name;
    private final Map<String,String> _lang;
    private final Map<String,String> _rev;
    private boolean _deterministic = false;
    private boolean _nondeterministic = false;
    private boolean _context = false;
    private boolean _positioning = false;
    private boolean _tape = false;


    public Language() {
        this("Default");
        deterministic(true);
    }

    public Language(String name) {
        _name = name;
        _lang = new HashMap<>();
        _rev = new HashMap<>();
    }

    public String name() {
        return _name;
    }

    public Language add(String word, String... phonemes) {
        _lang.put(key(word), combine(phonemes));
        _rev.put(combine(phonemes), word);
        return this;
    }

    public Map<String,String> dict() {
        return _lang;
    }

    public boolean deterministic() {
        return _deterministic;
    }

    public Language deterministic(boolean d) {
        _deterministic = d;
        return this;
    }

    public boolean nondeterministic() {
        return _nondeterministic;
    }

    public Language nondeterministic(boolean nd) {
        _nondeterministic = nd;
        return this;
    }

    public boolean contextual() {
        return _context;
    }

    public Language contextual(boolean c) {
        _context = c;
        return this;
    }

    public boolean positioning() {
        return _positioning;
    }

    public Language positioning(boolean p) {
        _positioning = p;
        return this;
    }

    public boolean tape() {
        return _tape;
    }

    public Language tape(boolean t) {
        _tape = t;
        return this;
    }

    public boolean accept(Codon c) {
        if(!_deterministic && c.deterministic()) return false;
        if(!_nondeterministic && !c.deterministic()) return false;
        if(!_context && c.usesContext()) return false;
        if(!_positioning && c.positioning()) return false;
        if(!_tape && c.usesTape()) return false;

        return true;
    }

    public String[] chains() {
        return _rev.keySet().toArray(new String[0]);
    }

    public static Language union(Language... ls) {
        final StringBuilder n = new StringBuilder();
        for(Language l:ls) {
            n.append(l.name()).append("+");
        }
        n.setLength(n.length()-1);
        final Language u = new Language(n.toString());
        for(Language l:ls) {
            u.positioning(u.positioning()||l.positioning())
            .contextual(u.contextual()||l.contextual())
            .deterministic(u.deterministic()||l.deterministic())
            .nondeterministic(u.nondeterministic()||l.nondeterministic());
            u._lang.putAll(l._lang);
            u._rev.putAll(l._rev);
        }
        return u;
    }

    /*
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
    */

    //public String randomCodon(Archetype a, Random r) {
        //String[] cs = _lang.keySet().toArray(new String[0]);
        //return cs[r.nextInt(cs.length)];
        //String[] cs = _rev.keySet().toArray(new String[0]);
        //return cs[r.nextInt(cs.length)];
    //}

    //public String[] words() {
        //String[] cs = _lang.keySet().toArray(new String[0]);
        //return cs;
    //}

    public Genome generate(final Archetype a, final Random r) {
        GenomeFactory f = new GenomeFactory();
        return f.generate(new Implicate(a, new Datamap(), this), r);
    }

    public String randomCodon(Archetype a, Random r) {
        GenomeFactory f = new GenomeFactory();
        return f.randomCodon(new Implicate(a, new Datamap(), this), r).code();
    }

    public String toDescription() {
        StringBuilder b = new StringBuilder(name()).append(" (");
        if(deterministic()) b.append("deterministic, ");
        if(nondeterministic()) b.append("nondeterministic, ");
        if(contextual()) b.append("contextual, ");
        if(positioning()) b.append("positioning, ");
        if(tape()) b.append("tape, ");
        b.setLength(b.length()-2);
        b.append(")");
        return b.toString();
    }

    //public Genome generate(final Archetype a, final Random r) {
        //return new Genome(randomCodon(a, r)+" "
                //+randomCodon(a, r)+" "
                //+randomCodon(a, r)+" "
                //+randomCodon(a, r)
            //);
    //}

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("name", _name);
        o.addProperty("deterministic", _deterministic);
        o.addProperty("nondeterministic", _nondeterministic);
        o.addProperty("context", _context);
        o.addProperty("positioning", _positioning);
        o.addProperty("tape", _tape);
        JsonObject dict = new JsonObject();
        o.add("dict", dict);
        for(Map.Entry<String,String> e:_lang.entrySet()) {
            dict.addProperty(e.getKey(), e.getValue());
        }
        return o;
    }

    private static String key(String word) {
        return word;
        //return word.replaceAll("[0-9]+", "");
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
        Language lang = new Language(name);
        lang._deterministic = Json.bool(o, "deterministic", true);
        lang._nondeterministic = Json.bool(o, "nondeterministic", true);
        lang._context = Json.bool(o, "context", true);
        lang._positioning = Json.bool(o, "positioning", true);
        lang._tape = Json.bool(o, "tape", true);
        JsonObject dict = (JsonObject) o.get("dict");
        for(Map.Entry<String,JsonElement> e:dict.entrySet()) {
            lang.add(e.getKey(), Json.string(e.getValue()));
        }
        return lang;
    }

    private static String combine(String... phonemes) {
        StringBuilder b = new StringBuilder();
        for(String ph:phonemes) {
            b.append(ph).append('+');
        }
        if(b.length()>0) {
            b.setLength(b.length()-1);
        }
        return b.toString();
    }
}
