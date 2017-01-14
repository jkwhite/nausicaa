package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.BlockPlane;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.SceneAntialiasing;
import javafx.scene.PerspectiveCamera;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.application.ConditionalFeature;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;


public class JfxCA extends Group {
    private static final double DEFAULT_SCALE = 4f;
    private static final int DEFAULT_DEPTH = 20;

    private final double _scale;
    private final int _depth;
    private final CA _ca;
    private final Map<Integer,Material> _materials = new HashMap<>();
    private List<Pool<Box>> _pools;


    public JfxCA(CA ca) {
        this(ca, DEFAULT_SCALE, DEFAULT_DEPTH);
    }

    public JfxCA(CA ca, double scale, int depth) {
        if(ca==null) {
            throw new IllegalArgumentException("null ca");
        }
        _ca = ca;
        _scale = scale;
        _depth = depth;
        int[] cols = _ca.getPalette().getColors();
        for(int i=0;i<cols.length;i++) {
            int[] c = Colors.unpack(cols[i]);
            Material m = new PhongMaterial(new Color(c[2]/255f, c[1]/255f, c[0]/255f, 1f));
            _materials.put(i, m);
        }
        _pools = new ArrayList<Pool<Box>>(ca.archetype().colors());
        for(int i=0;i<ca.archetype().colors();i++) {
            _pools.add(new Pool<Box>(new BoxFactory(i)));
        }
    }

    public void clear() {
        while(!getChildren().isEmpty()) {
            PooledBox b = (PooledBox) getChildren().remove(0);
            _pools.get(b.poolId()).checkin(b);
        }
        //getChildren().removeAll();
    }

    public void addPlane(Plane p) {
        if(p instanceof BlockPlane) {
            setBlocks((BlockPlane)p);
        }
        else {
            Layer layer = new Layer(p, _scale);
            if(getChildren().size()>_depth) {
                getChildren().remove(0);
                retranslate();
            }
            layer.setTranslateZ(-getChildren().size()*_scale);
            getChildren().add(layer);
        }
    }

    private void setBlocks(BlockPlane p) {
        clear();
        //System.err.println("setting blocks for "+p);
        int count = 0;
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                for(int k=0;k<p.getDepth();k++) {
                    int v = p.getCell(i,j,k);
                    if(shouldFill(p,i,j,k,v)) {
                        Box b = createBox(v);
                        b.setTranslateX(i*_scale);
                        b.setTranslateY(j*_scale);
                        b.setTranslateZ(k*_scale);
                        getChildren().add(b);
                        count++;
                    }
                }
            }
        }
        System.err.println("created "+count+" blocks, "+getChildren().size()+" children");
    }

    private static boolean shouldFill(final BlockPlane p, final int i, final int j, final int k, final int v) {
        if(v!=0&&i>0&&j>0&&k>0&&i<p.getWidth()-1&&j<p.getHeight()-1&&k<p.getDepth()-1) {
            return p.getCell(i-1,j,k)==0 || p.getCell(i+1,j,k)==0
                || p.getCell(i,j-1,k)==0 || p.getCell(i,j+1,k)==0
                || p.getCell(i,j,k-1)==0 || p.getCell(i,j,k+1)==0;
        }
        //return true;
        return v!=0;
    }

    private void retranslate() {
        final int size = getChildren().size();
        for(int i=0;i<size;i++) {
            getChildren().get(i).setTranslateZ(-i*_scale);
        }
    }

    class Layer extends Group {
        private final double _scale;


        public Layer(Plane p, double scale) {
            _scale = scale;
            final int[] row = new int[p.getWidth()];
            for(int i=0;i<p.getHeight();i++) {
                p.getRow(row, i, 0);
                for(int j=0;j<row.length;j++) {
                    final int v = row[j];
                    if(v!=0) {
                        Box b = new Box(_scale, _scale, _scale);
                        b.setTranslateX(i*_scale);
                        b.setTranslateY(j*_scale);
                        Material m = _materials.get(v);
                        b.setMaterial(m);
                        getChildren().add(b);
                    }
                }
            }
        }
    }

    class BoxFactory implements PoolFactory<Box> {
        private final int _c;

        public BoxFactory(int c) {
            _c = c;
        }

        @Override public PooledBox create() {
            PooledBox b = new PooledBox(_c, _scale, _scale, _scale);
            Material m = _materials.get(_c);
            b.setMaterial(m);
            //b.setDrawMode(DrawMode.LINE);
            return b;
        }
    }

    private Box createBox(int v) {
        /*
        Box b = new Box(_scale, _scale, _scale);
        Material m = _materials.get(v);
        b.setMaterial(m);
        b.setDrawMode(DrawMode.LINE);
        return b;
        */
        return _pools.get(v).checkout();
    }

    static class Pool<E> {
        private final PoolFactory<E> _pf;
        private final List<E> _p;


        public Pool(PoolFactory pf) {
            _pf = pf;
            _p = new LinkedList<>();
        }

        public void checkin(E e) {
            _p.add(e);
        }

        public E checkout() {
            if(!_p.isEmpty()) {
                return _p.remove(0);
            }
            else {
                return _pf.create();
            }
        }
    }

    static class PooledBox extends Box {
        private final int _poolId;

        public PooledBox(int p, double sx, double sy, double sz) {
            super(sx, sy, sz);
            _poolId = p;
        }

        public int poolId() {
            return _poolId;
        }
    }

    @FunctionalInterface
    interface PoolFactory<E> {
        E create();
    }
}
