package org.excelsi.nausicaa;


import java.util.*;


public class Config {
    private List<ConfigListener> _listeners = new ArrayList<>();
    private int _w;
    private int _h;
    private long _seed = System.currentTimeMillis(); //19450806L;
    private float _scale = 1f;
    private long _animationDelay = 100;
    private boolean _forceSymmetry = true;
    private String _dir = System.getProperty("user.home");


    public Config(int w, int h) {
        _w = w;
        _h = h;
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

    public void setScale(float s) {
        _scale = s;
        notify("scale");
    }

    public int getWidth() {
        return _w;
    }

    public int getHeight() {
        return _h;
    }

    public float getScale() {
        return _scale;
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
            System.err.println("setting animationDelay="+delay);
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

    public void setDir(String dir) {
        _dir = dir;
        notify("dir");
    }

    public String getDir() {
        return _dir;
    }

    private void notify(final String p) {
        for(ConfigListener l:new ArrayList<>(_listeners)) {
            l.configChanged(this, p);
        }
    }
}
