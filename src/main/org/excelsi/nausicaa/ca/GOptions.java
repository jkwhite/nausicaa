package org.excelsi.nausicaa.ca;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class GOptions {
    private static final Logger LOG = LoggerFactory.getLogger(GOptions.class);
    private final boolean _doubleBuffer;
    private final int _parallel;
    private final int _stride;
    @Deprecated private final double _weight;
    private final int _hd;
    @Deprecated private final ComputeMode _compMode;
    private final Variables _vars;
    private final MetaMode _meta;


    public GOptions() {
        this(true, 1, 0, 1f, 0);
    }
 
    public GOptions(boolean db, int par, int s, double w) {
        this(db, par, s, w, 0);
    }
 
    public GOptions(boolean db, int par, int s, double w, int hd) {
        this(db, par, s, w, hd, ComputeMode.combined);
    }

    public GOptions(boolean db, int par, int s, double w, int hd, ComputeMode comp) {
        this(db, par, s, w, hd, comp, null);
    }

    public GOptions(boolean db, int par, int s, double w, int hd, ComputeMode comp, Variables vars) {
        this(db, par, s, w, hd, comp, vars, MetaMode.depth);
    }

    public GOptions(boolean db, int par, int s, double w, int hd, ComputeMode comp, Variables vars, MetaMode meta) {
        _doubleBuffer = db;
        _parallel = par;
        _stride = s;
        _weight = w;
        _hd = hd;
        _compMode = comp;
        _vars = vars;
        _meta = meta;
        if(_parallel==1) {
            LOG.debug("GOptions with parallel 1", new Exception());
        }
    }

    public boolean doubleBuffer() { return _doubleBuffer; }
    public int parallel() { return _parallel; }
    public int stride() { return _stride; }
    public double weight() { return _weight; }
    public int higherDim() { return _hd; }
    public ComputeMode computeMode() { return _compMode; }
    public Variables variables() { return _vars; }
    public MetaMode metaMode() { return _meta; }

    public GOptions stride(int s) {
        return new GOptions(_doubleBuffer, _parallel, s, _weight, _hd, _compMode, _vars, _meta);
    }

    public GOptions weight(double w) {
        return new GOptions(_doubleBuffer, _parallel, _stride, w, _hd, _compMode, _vars, _meta);
    }

    public GOptions higherDim(int hd) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, hd, _compMode, _vars, _meta);
    }

    public GOptions computeMode(ComputeMode comp) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, _hd, comp, _vars, _meta);
    }

    public GOptions metaMode(MetaMode meta) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, _hd, _compMode, _vars, meta);
    }

    public GOptions variables(Variables vars) {
        return new GOptions(_doubleBuffer, _parallel, _stride, _weight, _hd, _compMode, vars, _meta);
    }
}
