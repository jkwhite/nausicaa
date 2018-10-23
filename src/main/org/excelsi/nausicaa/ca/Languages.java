package org.excelsi.nausicaa.ca;


import java.util.*;


public class Languages {
    private Languages() {
    }

    public static Language universal() {
        Archetype a = new Archetype(2, 1, 2);
        List<String> cs = new GenomeFactory().allCodons(a);
        Language lang = new Language();
        for(String c:cs) {
            lang.add(c, c);
        }
        return lang;
    }

    public static Language simpleSymmetry() {
        return new Language()
            .add("life", "ki","mi", "a2", "a3", "u", "ki", "mi8", "a3", "ma", "ya", "ra")
            .add("edg1","a0","a1","a0","a1","a-4","a1","a0","a1","a0","he")
            .add("bri","a0","a1","a0","ki","a1","pi8","a2","ma","ra","a2","a0","ya","ma","ra","a2","ya","ma","ra")
            .add("wire","a2","ya","mu","ja","a0","za","a2","za","a3","za","a1","a3","a2","a1","a1","gu","u","ra")
            .add("if", "ra")
            .add("mod", "mo")
            .add("max", "ta")
            .add("min", "chi")
            .add("xor", "tsu")
            .add("mul", "mu")
            .add("avg", "gi")
            .add("add", "mi")
            .add("and", "to")
            .add("or", "ka")
            .add("pavg", "go", "gi9")
            .add("pmax", "go", "ta9")
            .add("padd", "go", "mi9")
            .add("pmul", "go", "mu9")
            .add("pcavg", "pa", "gi9")
            .add("pcmax", "pa", "ta9")
            .add("pcadd", "pa", "mi9")
            .add("pcmul", "pa", "mu9");
    }
}
