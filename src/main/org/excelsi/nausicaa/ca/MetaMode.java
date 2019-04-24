package org.excelsi.nausicaa.ca;


public enum MetaMode {
    depth, none;

    public static MetaMode from(String v) {
        return "depth".equals(v)?depth:none;
    }
};
