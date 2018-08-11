package org.excelsi.nausicaa.ca;


public enum Values {
    discrete,
    continuous;


    public static Values from(String s) {
        switch(s) {
            case "continuous":
                return Values.continuous;
            case "discrete":
                return Values.discrete;
            default:
                throw new IllegalArgumentException("unknown values '"+s+"'");
        }
    }
}
