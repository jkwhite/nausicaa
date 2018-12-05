package org.excelsi.nausicaa.ca;


public class GOptions {
    private final boolean _doubleBuffer;
    private final int _parallel;
    private final int _stride;
    @Deprecated private final float _weight;
    private final int _hd;
    @Deprecated private final ComputeMode _compMode;
    private final Variables _vars;


    public GOptions() {
        this(true, 1, 0, 1f, 0);
    }
 
    public GOptions(boolean db, int par, int s, float w) {
        this(db, par, s, w, 0);
    }
 
    public GOptions(boolean db, int par, int s, float w, int hd) {
        this(db, par, s, w, hd, ComputeMode.combined);
    }

    public GOptions(boolean db, int par, int s, float w, int hd, ComputeMode comp) {
        this(db, par, s, w, hd, comp, null);
    }

    public GOptions(boolean db, int par, int s, float w, int hd, ComputeMode comp, Variables vars) {
        _doubleBuffer = db;
        _parallel = par;
        _stride = s;
        _weight = w;
        _hd = hd;
        _compMode = comp;
        _vars = vars;
    }

    public boolean doubleBuffer() { return _doubleBuffer; }
    public int parallel() { return _parallel; }
    public int stride() { return _stride; }
    public float weight() { return _weight; }
    public int higherDim() { return _hd; }
    public ComputeMode computeMode() { return _compMode; }
    public Variables variables() { return _vars; }

    public GOptions stride(int s) {
        return new GOptions(_doubleBuffer, _parallel, s, _weight, _hd);
    }

    public GOptions weight(float w) {
        return new GOptions(_doubleBuffer, _parallel, _stride, w, _hd);
    }

    public GOptions higherDim(int hd) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, hd);
    }

    public GOptions computeMode(ComputeMode comp) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, _hd, comp);
    }

    public GOptions variables(Variables vars) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, _hd, _compMode, vars);
    }
}
