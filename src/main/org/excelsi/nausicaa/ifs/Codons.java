package org.excelsi.nausicaa.ifs;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Affine;


public class Codons {
    private Codons() {}


    abstract static class Op implements TreeTape.Op {
        private boolean _fork;
        abstract void op(GraphicsContext g);
        public void tick() {
        }
        public Op withFork(boolean fork) {
            _fork = fork;
            return this;
        }
        public boolean isFork() { return _fork; }
        @Override public String toString() {
            return getClass().getSimpleName();
        }
    }

    static class FillOval extends Op {
        private int _x, _y, _w, _h;
        public FillOval(int x, int y, int w, int h) {
            _x = x;
            _y = y;
            _w = w;
            _h = h;
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().append(this);
        }
        @Override public void op(GraphicsContext g) {
            g.fillOval(_x, _y, _w, _h);
        }
    }

    static class StrokeRect extends Op {
        private int _x, _y, _w, _h;
        public StrokeRect(int x, int y, int w, int h) {
            _x = x;
            _y = y;
            _w = w;
            _h = h;
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().append(this);
        }
        @Override public void op(GraphicsContext g) {
            g.strokeRect(_x, _y, _w, _h);
        }
    }

    static class Translate extends Op {
        private int _x, _y;
        public Translate(int x, int y) {
            _x = x;
            _y = y;
        }
        @Override public void op(GraphicsContext g) {
            Affine a = g.getTransform();
            a.appendTranslation(_x, _y);
            g.setTransform(a);
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().prepend(this);
        }
    }

    static class Rotate extends Op {
        private double _r;
        private double _inc;
        public Rotate(double r) {
            this(r, 0);
        }
        public Rotate(double r, double inc) {
            _r = r;
            _inc = inc;
        }
        @Override public void op(GraphicsContext g) {
            Affine a = g.getTransform();
            //a.appendRotation(_r, _w/2, _h/2);
            a.appendRotation(_r, 0, 0);
            g.setTransform(a);
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().prepend(this);
        }
        @Override public void tick() {
            _r += _inc;
        }
    }

    static class Scale extends Op {
        private double _sx, _sy;
        public Scale(double sx, double sy) {
            _sx = sx;
            _sy = sy;
        }
        @Override public void op(GraphicsContext g) {
            g.scale(_sx, _sy);
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().prepend(this);
        }
    }
}
