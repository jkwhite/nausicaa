package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javafx.scene.shape.DrawMode;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Node;
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

import org.fxyz3d.shapes.primitives.ScatterMesh;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.CubeMesh;
import org.fxyz3d.shapes.primitives.TetrahedraMesh;


public class JfxCA extends Group {
    public enum Render { cells, bounds, mesh, single_mesh, cube_mesh, blob_mesh, best };
    private enum Strategy { all, delta };

    private static final double DEFAULT_SCALE = 2f;
    private static final int DEFAULT_DEPTH = 20;
    private static final boolean FROZEN = true;

    private static final Render DEFAULT_RENDER = Render.best;

    private double _scale;
    private final int _depth;
    private final CA _ca;
    private final boolean _stack;
    private final Map<Integer,Color> _colors = new HashMap<>();
    private final Map<Integer,Material> _materials = new HashMap<>();
    private final Map<PooledBox,Integer> _boxes = new HashMap<>();
    private final PooledBox[] _boxindex;
    private Render _render;
    private List<Pool<PooledBox>> _pools;
    private int _lastNops;
    private long _lastTime = 0;
    private Plane _lastPlane;
    private Strategy _strat = Strategy.delta;


    public JfxCA(CA ca) {
        this(ca, DEFAULT_SCALE, DEFAULT_DEPTH, DEFAULT_RENDER);
    }

    public JfxCA(CA ca, double scale, int depth, Render render) {
        if(ca==null) {
            throw new IllegalArgumentException("null ca");
        }
        _ca = ca;
        _scale = scale;
        _depth = depth;
        _render = render;
        _stack = ca.getRule().archetype().dims()<3;
        int[] cols = _ca.getPalette().getColors();
        for(int i=0;i<cols.length;i++) {
            int[] c = Colors.unpack(cols[i]);
            if(c[0]>0||c[1]>0||c[2]>0) {
                Color col = new Color(c[2]/255f, c[1]/255f, c[0]/255f, 1f);
                PhongMaterial m = new PhongMaterial(col);
                m.setSpecularPower(64d);
                _colors.put(i, col);
                _materials.put(i, m);
            }
        }
        _pools = new ArrayList<Pool<PooledBox>>(ca.archetype().colors());
        for(int i=0;i<ca.archetype().colors();i++) {
            _pools.add(new Pool<PooledBox>(new BoxFactory(i)));
        }
        _boxindex = new PooledBox[ca.getWidth()*ca.getHeight()*ca.getDepth()];
        getTransforms().add(new Rotate(-180, new javafx.geometry.Point3D(0,1,0)));
    }

    public CA getCA() {
        return _ca;
    }

    public Rule getRule() {
        return _ca.getRule();
    }

    public Plane getLastPlane() {
        return _lastPlane;
    }

    public void setRender(Render render) {
        clear(true);
        _render = render;
        addPlane(_lastPlane);
    }

    public void setScale(double scale) {
        clear(true);
        _scale = scale;
        addPlane(_lastPlane);
    }

    public double getScale() {
        return _scale;
    }

    public void clear() {
        clear(false);
    }

    public void clear(boolean raze) {
        switch(_render) {
            case cells:
                int i = 0;
                while(!getChildren().isEmpty()) {
                    Node nc = getChildren().remove(0);
                    if(nc instanceof PooledBox) {
                        PooledBox b = (PooledBox) nc;
                        _pools.get(b.poolId()).checkin(b);
                    }
                    //if(raze) {
                        //_boxindex[i++] = null;
                    //}
                }
                if(raze) {
                    _strat = Strategy.all;
                }
                break;
            default:
                while(!getChildren().isEmpty()) {
                    getChildren().remove(0);
                }
                break;
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
        _lastPlane = p;
        if(p.getDepth()>1 /*!_stack*/ /*p instanceof IntBlockPlane*/) {
            IntBlockPlane bp = (IntBlockPlane) p;
            List<Blobs.Blob> blobs = null;
            if(_render==Render.best) {
                blobs = new Blobs().blobs(p, Blobs.Mode.finite);
                if(blobs.size()<90000) {
                    _render = Render.blob_mesh;
                }
                else {
                    _render = Render.cells;
                }
                //_render = Render.blob_mesh;
            }
            switch(_render) {
                case cells:
                    setBlocks(bp);
                    break;
                case bounds:
                    renderBounds(p);
                    break;
                case mesh:
                    renderMesh(p, blobs);
                    break;
                case single_mesh:
                    renderSingleMesh(p);
                    break;
                case blob_mesh:
                    renderBlobMesh(p, blobs);
                    break;
                case cube_mesh:
                    renderCubeMesh(p);
                    break;
            }
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

    private void renderBounds(Plane p) {
        clear();
        final List<Blobs.Blob> blobs = new Blobs().blobs(p, Blobs.Mode.finite);
        for(Blobs.Blob blob:blobs) {
            Box b = createBox(blob);
            b.setTranslateX(blob.x1*_scale);
            b.setTranslateY(blob.y1*_scale);
            b.setTranslateZ(blob.z1*_scale);
            getChildren().add(b);
        }
    }

    private void renderMesh(Plane p, List<Blobs.Blob> blobs) {
        clear();
        if(blobs==null) {
            blobs = new Blobs().blobs(p, Blobs.Mode.finite);
        }
        int count = 0;
        System.err.println("creating "+blobs.size()+" blobs");
        // TODO: autoselect best render
        // TODO: render local changes faster - relative to current pos
        for(Blobs.Blob blob:blobs) {
            Group g = createMesh(blob);
            count += blob.points().size();
            g.setTranslateX(blob.x1*_scale);
            g.setTranslateY(blob.y1*_scale);
            g.setTranslateZ(blob.z1*_scale);
            getChildren().add(g);
        }
        System.err.println("created "+blobs.size()+" blobs, "+count+" points");
    }

    private void renderBlobMesh(Plane p, List<Blobs.Blob> blobs) {
        clear();
        if(blobs==null) {
            blobs = new Blobs().blobs(p, Blobs.Mode.finite);
        }
        int count = 0;
        System.err.println("creating "+blobs.size()+" blobs");
        for(Blobs.Blob blob:blobs) {
            Group g = createBlobMesh(blob);
            count += blob.points().size();
            g.setTranslateX(blob.x1*_scale);
            g.setTranslateY(blob.y1*_scale);
            g.setTranslateZ(blob.z1*_scale);
            getChildren().add(g);
        }
        System.err.println("created "+blobs.size()+" blobs, "+count+" points");
    }

    private void renderSingleMesh(Plane p) {
        clear();
        Group g = createMesh(p);
        getChildren().add(g);
    }

    private void renderCubeMesh(Plane p) {
        clear();
        final Palette pal = p.creator().getPalette();
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                for(int k=0;k<p.getDepth();k++) {
                    int c = p.getCell(i,j,k);
                    if(!pal.isBlack(c)) {
                        //CubeMesh m = new CubeMesh(0.5);
                        TetrahedraMesh m = new TetrahedraMesh(2d);
                        m.setTranslateX(i*_scale);
                        m.setTranslateY(j*_scale);
                        m.setTranslateZ(k*_scale);
                        getChildren().add(m);
                    }
                }
            }
        }
        //getChildren().add(g);
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
                    boolean shouldExist;
                    if(FROZEN) {
                        shouldExist = _materials.containsKey(v) && shouldFillFrozen(p,i,j,k,v);
                    }
                    else {
                        shouldExist = _materials.containsKey(v) && shouldFill(p,i,j,k,v);
                    }
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

    private Box createBox(Blobs.Blob b) {
        Box box = new Box(_scale*(b.x2-b.x1), _scale*(b.y2-b.y1), _scale*(b.z2-b.z1));
        Material m = _materials.get(b.c);
        box.setMaterial(m);
        return box;
    }

    private Group createMesh(Blobs.Blob b) {
        List<Point3D> pnts = new ArrayList<>(b.points().size());
        for(Blobs.Point p:b.points()) {
            pnts.add(new Point3D((float)_scale*(p.x-b.x1), (float)_scale*(p.y-b.y1), (float)_scale*(p.z-b.z1)));
        }
        ScatterMesh mesh = new ScatterMesh(pnts, true, 2d, 2);
        //Material m = _materials.get(b.c);
        //mesh.setMaterial(m);
        mesh.setTextureModeNone(_colors.get(b.c));
        return mesh;
    }

    private Group createBlobMesh(Blobs.Blob b) {
        BlobMesh m = new BlobMesh(b, _scale);
        Material mat = _materials.get(b.c);
        m.getMeshView().setMaterial(mat);
        return m;
    }

    private Group createMesh(Plane p) {
        final Palette pal = p.creator().getPalette();
        List<Point3D> pnts = new LinkedList<>();
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                for(int k=0;k<p.getDepth();k++) {
                    int c = p.getCell(i,j,k);
                    if(!pal.isBlack(c)) {
                        pnts.add(new Point3D((float)_scale*i, (float)_scale*j, (float)_scale*k));
                    }
                }
            }
        }
        ScatterMesh mesh = new ScatterMesh(pnts, true, 2d, 1);
        //Material m = _materials.get(b.c);
        //mesh.setMaterial(m);
        return mesh;
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
            //b.setCache(true);
            //b.setCacheHint(CacheHint.SCALE_AND_ROTATE);
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
