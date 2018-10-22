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
