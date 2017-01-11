package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.Plane;
import org.excelsi.nausicaa.ca.BlockPlane;
import org.excelsi.nausicaa.ca.CA;
import org.excelsi.nausicaa.ca.Colors;

import java.util.Map;
import java.util.HashMap;

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
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
    }

    public void clear() {
        while(!getChildren().isEmpty()) {
            getChildren().remove(0);
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
                    if(v!=0) {
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
        //System.err.println("created "+count+" blocks, "+getChildren().size()+" children");
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

    private Box createBox(int v) {
        Box b = new Box(_scale, _scale, _scale);
        Material m = _materials.get(v);
        b.setMaterial(m);
        return b;
    }
}
