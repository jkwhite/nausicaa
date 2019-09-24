package org.excelsi.nausicaa.ca;


public class Animated {
    private final CA _ca;
    private final double _scale;
    private final int _frames;
    private final int _rate;


    public Animated(CA ca, double scale, int frames, int rate) {
        _ca = ca;
        _scale = scale;
        _frames = frames;
        _rate = rate;
    }

    public CA getCA() {
        return _ca;
    }

    public double getScale() { return _scale; }
    public int getFrames() { return _frames; }
    public int getRate() { return _rate; }
}
