package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.IntBlockPlane;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javafx.scene.shape.DrawMode;
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
    private enum Strategy { all, delta };
    private static final double DEFAULT_SCALE = 2f;
    private static final int DEFAULT_DEPTH = 20;

    private final double _scale;
    private final int _depth;
    private final CA _ca;
    private final Map<Integer,Material> _materials = new HashMap<>();
    private final Map<PooledBox,Integer> _boxes = new HashMap<>();
    private final PooledBox[] _boxindex;
    private List<Pool<PooledBox>> _pools;
    private int _lastNops;
    private long _lastTime = 0;
    private Strategy _strat = Strategy.delta;


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
            if(c[0]>0||c[1]>0||c[2]>0) {
                Material m = new PhongMaterial(new Color(c[2]/255f, c[1]/255f, c[0]/255f, 1f));
                _materials.put(i, m);
            }
        }
        _pools = new ArrayList<Pool<PooledBox>>(ca.archetype().colors());
        for(int i=0;i<ca.archetype().colors();i++) {
            _pools.add(new Pool<PooledBox>(new BoxFactory(i)));
        }
        _boxindex = new PooledBox[ca.getWidth()*ca.getHeight()*ca.getDepth()];
    }

    public void clear() {
        while(!getChildren().isEmpty()) {
            PooledBox b = (PooledBox) getChildren().remove(0);
            _pools.get(b.poolId()).checkin(b);
        }
        /*
        for(Object o:getChildren()) {
            PooledBox b = (PooledBox) o;
            _pools.get(b.poolId()).checkin(b);
        }
        getChildren().removeAll();
        */
    }

    public void addPlane(Plane p) {
        if(p instanceof IntBlockPlane) {
            setBlocks((IntBlockPlane)p);
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

    private void setBlocks(IntBlockPlane p) {
        if(_lastTime>=1000) {
            if(_strat==Strategy.delta) {
                _strat = Strategy.all;
            }
            else {
                _strat = Strategy.delta;
            }
        }
        long start = System.currentTimeMillis();
        if(_strat==Strategy.all) {
            clear();
        }
        int count = 0;
        int created = 0;
        int destroyed = 0;
        int nops = 0;
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                for(int k=0;k<p.getDepth();k++) {
                    if(_strat==Strategy.all) {
                        _boxindex[count] = null;
                    }
                    int v = p.getCell(i,j,k);
                    //boolean shouldExist = _materials.containsKey(v) && shouldFill(p,i,j,k,v);
                    boolean shouldExist = _materials.containsKey(v) && shouldFillFrozen(p,i,j,k,v);
                    PooledBox existing = _boxindex[count];
                    boolean same = existing!=null && existing.poolId() == v;
                    if(shouldExist) {
                        if(existing!=null) {
                            if(same) {
                                //nop
                                nops++;
                            }
                            else {
                                _pools.get(existing.poolId()).checkin(existing);
                                _boxindex[count] = null;
                                //int idx = _boxes.get(existing);
                                //_boxes.remove(existing);
                                getChildren().remove(existing);
                                //create
                                PooledBox b = createBox(v);
                                b.setTranslateX(i*_scale);
                                b.setTranslateY(j*_scale);
                                b.setTranslateZ(k*_scale);
                                getChildren().add(b);
                                //_boxes.put(b, count);
                                _boxindex[count] = b;
                                created++;
                            }
                        }
                        else {
                            //create
                            PooledBox b = createBox(v);
                            b.setTranslateX(i*_scale);
                            b.setTranslateY(j*_scale);
                            b.setTranslateZ(k*_scale);
                            getChildren().add(b);
                            //_boxes.put(b, count);
                            _boxindex[count] = b;
                            created++;
                        }
                    }
                    else {
                        if(existing!=null) {
                            // destroy
                            _pools.get(existing.poolId()).checkin(existing);
                            _boxindex[count] = null;
                            //int idx = _boxes.get(existing);
                            //_boxes.remove(existing);
                            getChildren().remove(existing);
                            destroyed++;
                        }
                        else {
                            //nop
                            nops++;
                        }
                    }
                    /*
                    if(_materials.containsKey(v) && shouldFill(p,i,j,k,v)) {
                        PooledBox b = createBox(v);
                        b.setTranslateX(i*_scale);
                        b.setTranslateY(j*_scale);
                        b.setTranslateZ(k*_scale);
                        getChildren().add(b);
                        //_boxes.put(b, count);
                        _boxindex[count] = b;
                        created++;
                    }
                    */
                    count++;
                }
            }
        }
        _lastNops = nops;
        long end = System.currentTimeMillis();
        _lastTime = end-start;
        System.err.println("took "+(end-start)+", created "+created+", destroyed "+destroyed+", nop on "+nops+", "+getChildren().size()+" children");
    }

    private final boolean shouldFill(final IntBlockPlane p, final int i, final int j, final int k, final int v) {
        if(v!=0&&i>0&&j>0&&k>0&&i<p.getWidth()-1&&j<p.getHeight()-1&&k<p.getDepth()-1) {
            /*
            return p.getCell(i-1,j,k)==0 || p.getCell(i+1,j,k)==0
                || p.getCell(i,j-1,k)==0 || p.getCell(i,j+1,k)==0
                || p.getCell(i,j,k-1)==0 || p.getCell(i,j,k+1)==0;
                */
            return nzrgb(p,i-1,j,k) || nzrgb(p,i+1,j,k)
                || nzrgb(p,i,j-1,k) || nzrgb(p,i,j+1,k)
                || nzrgb(p,i,j,k-1) || nzrgb(p,i,j,k+1);
        }
        return true;
        //return v!=0;
    }

    private static final int[] UP = new int[]{0,1,0};
    private static final int[] DOWN = new int[]{0,-1,0};
    private static final int[] LEFT = new int[]{1,0,0};
    private static final int[] RIGHT = new int[]{-1,0,0};
    private static final int[] FRONT = new int[]{0,0,1};
    private static final int[] BACK = new int[]{0,0,-1};
    private final boolean shouldFillFrozen(final IntBlockPlane p, final int i, final int j, final int k, final int v) {
        if(v!=0&&i>0&&j>0&&k>0&&i<p.getWidth()-1&&j<p.getHeight()-1&&k<p.getDepth()-1) {
            return
                clearToEdge(p,i,j,k,UP) ||
                clearToEdge(p,i,j,k,DOWN) ||
                clearToEdge(p,i,j,k,LEFT) ||
                clearToEdge(p,i,j,k,RIGHT) ||
                clearToEdge(p,i,j,k,FRONT) ||
                clearToEdge(p,i,j,k,BACK);
        }
        return true;
        //return v!=0;
    }

    private final boolean clearToEdge(final IntBlockPlane p, int i, int j, int k, final int[] dir) {
        int v = p.getCell(i,j,k);
        i+=dir[0];
        j+=dir[1];
        k+=dir[2];
        while(v!=0&&i>0&&j>0&&k>0&&i<p.getWidth()-1&&j<p.getHeight()-1&&k<p.getDepth()-1) {
            if(nzrgb(p,i,j,k)) {
                return false;
            }
            i+=dir[0];
            j+=dir[1];
            k+=dir[2];
        }
        return true;
    }

    private final boolean nzrgb(final IntBlockPlane p, final int i, final int j, final int k) {
        final int v = p.getCell(i,j,k);
        return _materials.containsKey(v);
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

    private PooledBox createBox(int v) {
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
            //setDrawMode(DrawMode.LINE);
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
