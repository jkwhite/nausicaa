package org.excelsi.nausicaa.ca;


public enum Options {
    phaseTransitions("Phase transitions"),
    evaporation("Evaporation"),
    instantaneousMaskReset("Instantaneous null mask propagation"),
    instantaneousMutationProp("Instantaneous mutation propagation"),
    stabilityReset("Reset stability on phase transition");


    private boolean _state;
    private String _desc;


    private Options(String desc) {
        this(desc, false);
    }

    private Options(String desc, boolean state) {
        _desc = desc;
        _state = state;
    }

    public String description() { return _desc; }

    public boolean get() {
        return _state;
    }

    public void set(boolean s) {
        _state = s;
    }

    public boolean toggle() {
        _state = !_state;
        return _state;
    }
}
