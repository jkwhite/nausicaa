package org.excelsi.nausicaa;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class Config {
    private List<ConfigListener> _listeners = new ArrayList<>();
    private int _w;
    private int _h;
    private int _d;
    private int _prelude = 0;
    private long _seed = System.currentTimeMillis(); //19450806L;
    private float _scale = 1f;
    private long _animationDelay = 100;
    private boolean _forceSymmetry = true;
    private boolean _hueVariations = false;
    private final Map<String,Object> _variables = new HashMap<>();
    private String _dir = System.getProperty("user.home");


    public Config(int w, int h, int d) {
        _w = w;
        _h = h;
        _d = d;
    }

    public void addListener(ConfigListener l) {
        _listeners.add(l);
    }

    public void removeListener(ConfigListener l) {
        _listeners.remove(l);
    }

    public void setSize(int w, int h) {
        _w = w;
        _h = h;
        notify("size");
    }

    public void setSize(int w, int h, int d) {
        _w = w;
        _h = h;
        _d = d;
        notify("size");
    }

    public void setSize(int w, int h, int d, int prelude) {
        _w = w;
        _h = h;
        _d = d;
        _prelude = prelude;
        notify("size");
    }

    public void setScale(float s) {
        _scale = s;
        notify("scale");
    }

    public void setPrelude(int prelude) {
        _prelude = prelude;
    }

    public int getWidth() {
        return _w;
    }

    public int getHeight() {
        return _h;
    }

    public int getDepth() {
        return _d;
    }

    public float getScale() {
        return _scale;
    }

    public int getPrelude() {
        return _prelude;
    }

    public void setSeed(long seed) {
        _seed = seed;
        notify("seed");
    }

    public long getSeed() {
        return _seed;
    }

    public void setAnimationDelay(long delay) {
        if(_animationDelay!=delay) {
            _animationDelay = delay;
            notify("animationDelay");
        }
    }

    public long getAnimationDelay() {
        return _animationDelay;
    }

    public void setForceSymmetry(boolean forceSymmetry) {
        _forceSymmetry = forceSymmetry;
        notify("forceSymmetry");
    }

    public boolean getForceSymmetry() {
        return _forceSymmetry;
    }

    public void setHueVariations(boolean hueVariations) {
        _hueVariations = hueVariations;
        notify("hueVariations");
    }

    public boolean getHueVariations() {
        return _hueVariations;
    }

    public void setDir(String dir) {
        _dir = dir;
        notify("dir");
    }

    public String getDir() {
        return _dir;
    }

    public void setVariable(String name, Object o) {
        _variables.put(name, o);
    }

    public <T> T getVariable(String name, T dvalue) {
        T t = (T) _variables.get(name);
        return t!=null?t:dvalue;
    }

    public int getIntVariable(String name, int dvalue) {
        Object v = (Object) _variables.get(name);
        if(v instanceof String) {
            return Integer.parseInt((String)v);
        }
        else if(v instanceof Integer) {
            return ((Integer)v).intValue();
        }
        else {
            return dvalue;
        }
    }

    public float getFloatVariable(String name, float dvalue) {
        Object v = (Object) _variables.get(name);
        if(v instanceof String) {
            return Float.parseFloat((String)v);
        }
        else if(v instanceof Float) {
            return ((Float)v).floatValue();
        }
        else if(v instanceof Integer) {
            return (float) ((Integer)v).intValue();
        }
        else {
            return dvalue;
        }
    }

    public void notify(final String p) {
        for(ConfigListener l:new ArrayList<>(_listeners)) {
            l.configChanged(this, p);
        }
    }
}
