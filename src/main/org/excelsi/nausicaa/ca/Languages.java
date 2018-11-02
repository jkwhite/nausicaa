package org.excelsi.nausicaa.ca;


import java.util.*;


public class Languages {
    private Languages() {
    }

    public static Language named(String name) {
        switch(name) {
            case "Universal":
                return universal();
            case "Symmetric2d":
                return simpleSymmetry();
        }
        throw new IllegalArgumentException("no such language '"+name+"'");
    }

    public static Language universal() {
        Archetype a = new Archetype(2, 1, 2);
        List<String> cs = new GenomeFactory().allCodons(a);
        Language lang = new Language("Universal");
        for(String c:cs) {
            lang.add(c, c);
        }
        return lang;
    }

    public static Language simpleSymmetry() {
        return new Language("Symmetric2d")
            .add("life", "ki","mi", "a2", "a3", "u", "ki", "mi8", "a3", "ma", "ya", "ra")
            .add("edge","a0","a1","a0","a1","a-4","a1","a0","a1","a0","he")
            .add("bri","a0","a1","a0","ki","a1","pi8","a2","ma","ra","a2","a0","ya","ma","ra","a2","ya","ma","ra")
            .add("wire","a2","ya","mu","ja","a0","za","a2","za","a3","za","a1","a3","a2","a1","a1","gu","u","ra")
            .add("star",s("go do do ja tsu gi za ni ta"))
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

    private static String[] s(String p) {
        return p.split(" ");
    }
}
