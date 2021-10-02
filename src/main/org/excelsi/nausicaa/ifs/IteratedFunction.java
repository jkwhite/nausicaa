package org.excelsi.nausicaa.ifs;


import java.util.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.paint.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.excelsi.nausicaa.ifs.Codons.Op;


public class IteratedFunction {
    private static final Logger LOG = LoggerFactory.getLogger(IteratedFunction.class);

    private final List<TreeTape.Op> _initial;
    private final List<TreeTape.Op> _iter;
    private final int _max;
    private TreeTape _tape;

    public IteratedFunction(TreeTape.Op[] initial, TreeTape.Op[] iter, int maxIter) {
        _initial = Arrays.asList(initial);
        _iter = Arrays.asList(iter);
        _max = maxIter;
        reset();
    }

    public int getMaxIter() { return _max; }

    public void iterate() {
        for(TreeTape.Op o:_iter) {
            Op op = (Op) o;
            List<TreeTape.TreeNode> leaves = _tape.getLeaves();
            LOG.info("leaves: "+leaves.size());
            for(TreeTape.TreeNode leaf:leaves) {
                if(op.isFork()) {
                    op.op(leaf.fork());
                }
                else {
                    op.op(leaf);
                }
            }
        }
    }

    public void reset() {
        _tape = new TreeTape(_initial);
    }

    public void initialize(GraphicsContext g, int w, int h) {
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.setStroke(javafx.scene.paint.Color.WHITE);
        Stop[] stops = new Stop[] { new Stop(0, Color.WHITE), new Stop(1, Color.RED)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        g.setFill(lg1);
        
        Affine a = null;
        a = g.getTransform(a);
        a.setToIdentity();
        g.setTransform(a);
        g.clearRect(0, 0, w, h);
        a = g.getTransform(a);
        a.appendTranslation(w/2, h/2);
        g.setTransform(a);
    }

    public void render(GraphicsContext g, int w, int h) {
        for(TreeTape.TreeNode leaf:_tape.getLeaves()) {
            g.save();
            for(TreeTape.Op op:leaf.getTape().getOps()) {
                ((Op)op).op(g);
            }
            g.restore();
        }
    }
}
