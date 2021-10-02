package org.excelsi.nausicaa.ifs;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Affine;


public class Codons {
    private Codons() {}


    public static Op createOp(String name, double[] args, boolean fork) {
        Op op = createOp(name, args);
        return op.withFork(fork);
    }

    private static Op createOp(String name, double[] args) {
        switch(name) {
            case "circ":
                return new FillOval(args[0], args[1], args[2], args[3]);
            case "tran":
                return new Translate(args[0], args[1]);
            case "rot":
                return new Rotate(args[0]);
            case "scl":
                return new Scale(args[0], args[1]);
            default:
                throw new RuntimeException("unknown codon '"+name+"'");
        }
    }

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

    static class StrokeLine extends Op {
        private double _x1, _y1, _x2, _y2;
        public StrokeLine(double x1, double y1, double x2, double y2) {
            _x1 = x1;
            _y1 = y1;
            _x2 = x2;
            _y2 = y2;
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().append(this);
        }
        @Override public void op(GraphicsContext g) {
            g.strokeLine(_x1, _y1, _x2, _y2);
        }
    }

    static class FillOval extends Op {
        private double _x, _y, _w, _h;
        public FillOval(double x, double y, double w, double h) {
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

    static class FillRect extends Op {
        private double _x, _y, _w, _h;
        public FillRect(double x, double y, double w, double h) {
            _x = x;
            _y = y;
            _w = w;
            _h = h;
        }
        @Override public void op(TreeTape.TreeNode t) {
            t.getTape().append(this);
        }
        @Override public void op(GraphicsContext g) {
            g.fillRect(_x, _y, _w, _h);
        }
    }

    static class StrokeRect extends Op {
        private double _x, _y, _w, _h;
        public StrokeRect(double x, double y, double w, double h) {
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
        private double _x, _y;
        public Translate(double x, double y) {
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
        public Scale(double sxy) { this(sxy, sxy); }
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
