package org.excelsi.nausicaa.ca;


public final class Parameters {
    private Varmap _vars;
    private boolean _trace;
    private float _transition;


    public Parameters vars(Varmap m) {
        _vars = m;
        return this;
    }

    public Varmap vars() {
        return _vars;
    }

    public Parameters trace(boolean t) {
        _trace = t;
        return this;
    }

    public boolean trace() {
        return _trace;
    }

    public Parameters transition(float t) {
        _transition = t;
        return this;
    }

    public float transition() {
        return _transition;
    }

    @Override public String toString() {
        return "Parameters::{transition:"+_transition+", trace:"+_trace+", vars:"+_vars+"}";
    }
}
