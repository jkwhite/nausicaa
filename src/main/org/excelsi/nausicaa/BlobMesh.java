package org.excelsi.nausicaa;


import java.util.*;

import org.excelsi.nausicaa.ca.*;
import static org.excelsi.nausicaa.ca.Blobs.Blob;
import static org.excelsi.nausicaa.ca.Blobs.Point;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.MeshView;
import javafx.scene.Group;
import javafx.scene.Node;

import org.fxyz3d.shapes.primitives.ScatterMesh;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.CubeMesh;
import org.fxyz3d.shapes.primitives.TetrahedraMesh;


public class BlobMesh extends Group {
    private final Blob _b;
    private final double _scale;


    public BlobMesh(final Blob b, final double scale) {
        _b = b;
        _scale = scale;
        buildMesh();
    }

    public MeshView getMeshView() {
        return (MeshView)getChildren().get(0);
    }

    private void buildMesh() {
        final Plane p = new BitSetBlockPlane(null, _b.xExtent(), _b.yExtent(), _b.zExtent());
        for(Point pnt:_b.points()) {
            p.setCell(pnt.x, pnt.y, pnt.z, 1);
        }
        final List<Face> faces = new ArrayList<>();
        for(Point pnt:_b.points()) {
            buildSurface(p,faces,pnt);
        }
        final Map<Point,Integer> pmap = new HashMap<>();
        final List<Point> plist = new ArrayList<>();

        TriangleMesh m = new TriangleMesh();
        m.getTexCoords().addAll(0,0);
        for(Face f:faces) {
            int p1 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][0][0],
                f.p.y+P_EXP[f.dir][0][1],
                f.p.z+P_EXP[f.dir][0][2]
            ));
            int p2 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][1][0],
                f.p.y+P_EXP[f.dir][1][1],
                f.p.z+P_EXP[f.dir][1][2]
            ));
            int p3 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][2][0],
                f.p.y+P_EXP[f.dir][2][1],
                f.p.z+P_EXP[f.dir][2][2]
            ));

            int p4 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][2][0],
                f.p.y+P_EXP[f.dir][2][1],
                f.p.z+P_EXP[f.dir][2][2]
            ));
            int p5 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][3][0],
                f.p.y+P_EXP[f.dir][3][1],
                f.p.z+P_EXP[f.dir][3][2]
            ));
            int p6 = pIdx(pmap, plist, new Point(
                f.p.x+P_EXP[f.dir][0][0],
                f.p.y+P_EXP[f.dir][0][1],
                f.p.z+P_EXP[f.dir][0][2]
            ));
            m.getFaces().addAll(
                p1,0,p2,0,p3,0,
                p4,0,p5,0,p6,0
            );
        }
        for(Point pnt:plist) {
            m.getPoints().addAll(
                (pnt.x-_b.x1)*(float)_scale,
                (pnt.y-_b.y1)*(float)_scale,
                (pnt.z-_b.z1)*(float)_scale
            );
        }
        MeshView mv = new MeshView(m);
        getChildren().add(mv);
        /*
        List<Point3D> pnts = new ArrayList<>();
        for(Face f:faces) {
            pnts.add(new Point3D(
                (f.p.x-_b.x1+f.dir[0])*(float)_scale,
                (f.p.y-_b.y1+f.dir[1])*(float)_scale,
                (f.p.z-_b.z1+f.dir[2])*(float)_scale
            ));
        }
        ScatterMesh m = new ScatterMesh(pnts);
        getChildren().add(m);
        */
        /*
        for(int i=0;i<p.getWidth()) {
            for(int j=0;j<p.getHeight()) {
                for(int k=0;k<p.getDepth()) {
                    buildSurface(p,surfaceCells,i,j,k);
                }
            }
        }
        */
    }

    private static int pIdx(Map<Point,Integer> pmap, List<Point> plist, Point p) {
        Integer idx = pmap.get(p);
        if(idx==null) {
            plist.add(p);
            pmap.put(p, plist.size()-1);
            idx = plist.size()-1;
        }
        return idx;
    }

    private static final int[][] EXP = {
        {-1, 0, 0},
        { 1, 0, 0},
        { 0,-1, 0},
        { 0, 1, 0},
        { 0, 0,-1},
        { 0, 0, 1}
    };
    private static final int[][][] P_EXP = {
        { {0,0,0}, {0,0,1}, {0,1,1}, {0,1,0} },
        { {1,0,0}, {1,1,0}, {1,1,1}, {1,0,1} },
        { {0,0,0}, {1,0,0}, {1,0,1}, {0,0,1} },
        { {0,1,0}, {0,1,1}, {1,1,1}, {1,1,0} },
        { {0,0,0}, {0,1,0}, {1,1,0}, {1,0,0} },
        { {0,0,1}, {1,0,1}, {1,1,1}, {0,1,1} }
    };

    private void buildSurface(Plane p, List<Face> faces, Point pnt /*int i, int j, int k*/) {
        for(int w=0;w<EXP.length;w++) {
            int di = pnt.x+EXP[w][0];
            int dj = pnt.y+EXP[w][1];
            int dk = pnt.z+EXP[w][2];
            boolean escape = true;
            while(di>=0&&dj>=0&&dk>=0&&di<p.getWidth()&&dj<p.getHeight()&&dk<p.getDepth()) {
                if(p.getCell(di,dj,dk)!=0) {
                    escape = false;
                    break;
                }
                di+=EXP[w][0];
                dj+=EXP[w][1];
                dk+=EXP[w][2];
            }
            if(escape) {
                faces.add(new Face(pnt, w));
            }
        }
    }

    static private class Face /*implements Comparable*/ {
        public final Point p;
        public final int dir;


        public Face(Point p, int dir) {
            this.p = p;
            this.dir = dir;
        }

        /*
        @Override public int compareTo(Object o) {
            Face f = (Face)o;
            int k1 = p.z - f.p.z;
            if(k1!=0) return k1;
            if(p.y==f.p.y) {
            }
            else if(p.y>f.p.y) {
            }
            else {
            }
        }
        */
    }
}
