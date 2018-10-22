package org.excelsi.nausicaa.ca;


import java.util.*;


public class Language {
    private Map<String,String[]> _lang;
    private Map<String,String> _rev;


    public Language() {
        _lang = new HashMap<>();
        _rev = new HashMap<>();
    }

    public Language add(String word, String... phonemes) {
        _lang.put(word, phonemes);
        _rev.put(combine(phonemes), word);
        return this;
    }

    public String[] phonemes(String word) {
        String[] ph = _lang.get(word);
        if(ph==null) {
            throw new IllegalArgumentException("no such word '"+word+"'; language contains: "+Arrays.toString(words()));
        }
        return ph;
    }

    public String word(String phonemes) {
        String w = _rev.get(phonemes);
        if(w==null) {
            throw new IllegalArgumentException("no matching word for phonemes '"+phonemes+"'; language contains: "+_rev.keySet());
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

    private static String combine(String... phonemes) {
        StringBuilder b = new StringBuilder(12);
        for(String ph:phonemes) {
            b.append(ph).append('+');
        }
        b.setLength(b.length()-1);
        return b.toString();
    }
}
