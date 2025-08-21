package org.excelsi.nausicaa.ca;


public enum Values {
    discrete("Discrete"),
    continuous("Continuous");

    private final String _name;

    Values(String n) {
        _name = n;
    }

    public String getName() {
        return _name;
    }

    public static Values from(String s) {
        switch(s.toLowerCase()) {
            case "continuous":
                return Values.continuous;
            case "discrete":
                return Values.discrete;
            default:
                throw new IllegalArgumentException("unknown values '"+s+"'");
        }
    }
}
