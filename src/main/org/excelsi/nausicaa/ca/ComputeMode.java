package org.excelsi.nausicaa.ca;


public enum ComputeMode {
    combined, channel;

    public static ComputeMode from(String v) {
        return "combined".equals(v)?combined:channel;
    }
};
