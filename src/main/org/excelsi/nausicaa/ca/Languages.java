package org.excelsi.nausicaa.ca;


import java.util.*;
import com.google.gson.*;


public class Languages {
    private Languages() {
    }

    public static String[] catalog() {
        return new String[]{
            "Universal",
            "Classic",
            "Circular",
            "Symmetric2d"
            //"Symmetric3d",
            //"Menagerie"
        };
    }

    public static Language named(String name) {
        switch(name) {
            case "Universal":
                return universal();
            case "Classic":
                return classic();
            case "Circular":
                return circular();
            case "Symmetric2d":
                return simpleSymmetry();
            case "Menagerie":
                return menagerie();
            case "Symmetric3d":
                return simpleSymmetry3d();
        }
        throw new IllegalArgumentException("no such language '"+name+"'");
    }

    public static Language universal() {
        return new Universal();
    }

    public static Language classic() {
        return new Language("Classic")
            .deterministic(true)
            .nondeterministic(false)
            .contextual(false);
    }

    public static Language circular() {
        return new Language("Circular")
            .deterministic(true)
            .nondeterministic(true)
            .contextual(true)
            .add("circle","kya0+kya0+mu2+kya1+kya1+mu2+mi2+ni")
            .add("blitx","jya0+a200+mu2+re+a5+mu2")
            .add("blity","jya1+a200+mu2+re+a5+mu2")
            .add("blitxy","jya0+a200+mu2+re+a5+mu2+jya1+a200+mu2+re+a5+mu2+mi3")
            ;
    }

    public static Language simpleSymmetry() {
        return base("Symmetric2d")
            .add("life", "ki","mi", "a2", "a3", "u", "ki", "mi8", "a3", "ma", "ya", "ra")
            .add("edge","a0","a1","a0","a1","a-4","a1","a0","a1","a0","he")
            .add("bri","a0","a1","a0","ki","a1","pi8","a2","ma","ra","a2","a0","ya","ma","ra","a2","ya","ma","ra")
            .add("wire","a2","ya","mu","ja","a0","za","a2","za","a3","za","a1","a3","a2","a1","a1","gu","u","ra")
            .add("star",s("go do do ja tsu gi za ni ta"))
            .add("pavg", "go", "gi9")
            .add("pmax", "go", "ta9")
            .add("pmin", "go", "chi9")
            .add("padd", "go", "mi9")
            .add("pmul", "go", "mu9")
            .add("psavg", "ki", "gi9")
            .add("psmax", "ki", "ta9")
            .add("psmin", "ki", "chi9")
            .add("psadd", "ki", "mi9")
            .add("psmul", "ki", "mu9")
            .add("pcavg", "pa", "gi5")
            .add("pcmax", "pa", "ta5")
            .add("pcmin", "pa", "chi5")
            .add("pcadd", "pa", "mi5")
            .add("pcmul", "pa", "mu5");
    }

    public static Language simpleSymmetry3d() {
        return base("Symmetric3d")
            .add("pavg", "go", "gi27")
            .add("pmax", "go", "ta27")
            .add("pmin", "go", "chi27")
            .add("padd", "go", "mi27")
            .add("pmul", "go", "mu27")
            .add("psavg", "ki", "gi27")
            .add("psmax", "ki", "ta27")
            .add("psmin", "ki", "chi27")
            .add("psadd", "ki", "mi27")
            .add("psmul", "ki", "mu27")
            .add("pcavg", "pa", "gi7")
            .add("pcmax", "pa", "ta7")
            .add("pcmin", "pa", "chi7")
            .add("pcadd", "pa", "mi7")
            .add("pcmul", "pa", "mu7");
    }

    public static Language menagerie() {
        return base("Menagerie")
            .add("sier", s("o8 o6 ma ma go zu ne"))
            .add("ts1", s("o8 o6 ma ma o8 o6 ma ma go zu ne na"))
            .add("ts2", s("go ma ma ma ma ma ma zu8 ne ne"))
            .add("ts1", s("go zu to zu zu bo yu gi nu"))
            .add("ua", s("go ma zu6 ma ma ma ya mi5"))
            .add("ub", s("go tsu zu ni chi1 ne"))
            .add("uc", s("go zu zu zu bo yu gi nu"))
            .add("ud", s("go zu zu zu bo gi nu"))
            .add("ue", s("go zu to zu zu bo yu mu gi nu"))
            .add("uf", s("go zu zu7 zu2 bo bo mu mu na ke nu mu"))
            .add("ug", s("yu bo o8 nu ka o10 ka mu bo bo na ki zu ya ho ya mi6 ka chi11"))
            .add("uh", s("yu bo o8 nu ka o10 ka bo bo na ki zu ya ho ya mi6 ka chi11 yu"))
            ;
    }

    private static Language base(final String name) {
        return new Language(name)
            .add("dup", "do")
            .add("if", "ra")
            .add("mod", "mo")
            .add("max", "ta")
            .add("min", "chi")
            .add("xor", "tsu")
            .add("mul", "mu")
            .add("add", "mi2")
            .add("sub", "su")
            .add("avg", "gi")
            .add("and", "to")
            .add("or", "ka")
            .add("ya", "ya")
            .add("jmp", "ja")
            .add("eq", "ma")
            .add("ne", "ne")
            .add("gt", "ke")
            .add("lt", "na")
            .add("cnz", s("a0 gu"))
            .add("cno", s("a1 gu"))
            ;
    }

    private static String[] s(String p) {
        return p.split(" ");
    }

    public static class Universal extends Language {
        public Universal() {
            super("Universal");
        }

        @Override public boolean accept(Codon c) {
            return true;
        }

        @Override public String[] chains() {
            return new String[0];
        }

        public String[] phonemes(String word) {
            return new String[]{word};
        }

        public String word(String phonemes) {
            return phonemes;
        }

        public String expand(String word) {
            return word;
        }

        public Genome generate(final Archetype a, final Random r) {
            GenomeFactory f = new GenomeFactory();
            //return f.generate(a, r);
            return f.generate(new Implicate(a, new Datamap(), this), r);
        }

        public String randomCodon(Archetype a, Random r) {
            GenomeFactory f = new GenomeFactory();
            return f.randomCodon(new Implicate(a, null, null), r).code();
        }

        public JsonElement toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("name", "Universal");
            return o;
        }
    }
}
