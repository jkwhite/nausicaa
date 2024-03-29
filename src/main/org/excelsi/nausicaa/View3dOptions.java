package org.excelsi.nausicaa;


public class View3dOptions {
    private boolean _root;
    private boolean _animate;
    private float _scale;


    public View3dOptions() {
    }

    public View3dOptions root(boolean r) {
        _root = r;
        return this;
    }

    public View3dOptions animate(boolean a) {
        _animate = a;
        return this;
    }

    public View3dOptions scale(float s) {
        _scale = s;
        return this;
    }

    public boolean root() {
        return _root;
    }

    public boolean animate() {
        return _animate;
    }

    public float scale() {
        return _scale;
    }
}
