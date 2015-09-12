package org.excelsi.ca;


public class World implements java.io.Serializable {
    private static int _size = 128;
    private Rule _r;
    private transient CA _c;
    private int _w;
    private int _h;


    public World(Rule r, int w, int h) {
        _r = r;
        _w = w;
        _h = h;
    }

    public static int getSize() {
        return _size;
    }

    public static void setSize(int size) {
        _size = size;
    }

    public World() {
    }

    public void setWidth(int w) {
        _w = w;
    }

    public void setHeight(int h) {
        _h = h;
    }

    public int getWidth() {
        return _w;
    }

    public int getHeight() {
        return _h;
    }

    public CA getCA() {
        if(_c==null) {
            _c = new CA(_w, _h);
            //_c.initRandom(_r.colors());
            _r.init(_c, Rule.Initialization.random);
            //System.err.println("calling generate on "+_r.getClass());
            int intr = _r.getSuggestedInterval(_c);
            _r.generate(_c, 1, intr-1, false, true, null);
        }
        return _c;
    }

    public void setCA(CA c) {
        _c = c;
    }

    public void clearCA() {
        _c = null;
    }

    public void setRule(Rule r) {
        _r = r;
    }

    public Rule getRule() {
        return _r;
    }

    public String toString() {
        return _r.toString();
    }
}
