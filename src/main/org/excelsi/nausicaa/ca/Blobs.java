package org.excelsi.nausicaa.ca;


import java.util.*;


public class Blobs {
    public enum Mode { infinite, finite };

    public List<Blob> blobs(final Plane p, final Mode mode) {
        if(mode==Mode.infinite) {
            throw new UnsupportedOperationException("not yet");
        }
        final Palette pal = p.creator().getPalette();
        final List<Blob> bs = new ArrayList<>();
        final BitSet v = new BitSet();
        final int[] block = new int[6];
        final Set<Point> visited = new HashSet<>();

        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                for(int k=0;k<p.getDepth();k++) {
                    if(!visited(p,v,i,j,k)) {
                        final int c = p.getCell(i,j,k);
                        //System.err.print("testing: "+c+" ");
                        if(pal.isBlack(c)) {
                            //System.err.println("black");
                            markVisited(p,v,i,j,k);
                        }
                        else {
                            //System.err.println("not black");
                            final Blob b = explore(p, v, mode, i, j, k);
                            if(b!=null) {
                                bs.add(b);
                            }
                            markVisited(p,v,i,j,k);
                        }
                    }
                }
            }
        }
        return bs;
    }

    private static final int[][] EXP = { {-1,0,0}, {1,0,0}, {0,-1,0}, {0,1,0}, {0,0,-1}, {0,0,1} };

    private final List<Point> _frontier = new ArrayList<>();

    private Blob explore(final Plane p, final BitSet v, final Mode mode, int i, int j, int k) {
        final int c = p.getCell(i,j,k);
        final Blob b = new Blob(c);
        _frontier.add(new Point(i,j,k));

        //b.points.add(new Point(i,j,k));
        while(!_frontier.isEmpty()) {
            final Point pnt = _frontier.remove(0);
            b.addPoint(pnt);
            for(int w=0;w<EXP.length;w++) {
                final int[] d = EXP[w];
                final int di = pnt.x+d[0];
                final int dj = pnt.y+d[1];
                final int dk = pnt.z+d[2];
                if(di>=0&&dj>=0&&dk>=0&&di<p.getWidth()&&dj<p.getHeight()&&dk<p.getDepth()) {
                    if(!visited(p, v, di, dj, dk)) {
                        final int dc = p.getCell(di,dj,dk);
                        if(dc==c) {
                            _frontier.add(new Point(di,dj,dk));
                        }
                        markVisited(p, v, di, dj, dk);
                    }
                }
            }
        }
        return b;
    }

    private static final boolean visited(Plane p, BitSet v, int i, int j, int k) {
        return v.get(idx(p,i,j,k));
    }

    private static final void markVisited(Plane p, BitSet v, int i, int j, int k) {
        v.set(idx(p,i,j,k));
    }

    private static int idx(Plane p, int x, int y, int z) {
        return x+p.getWidth()*y+p.getWidth()*p.getHeight()*z;
    }

    public static class Blob {
        public final int c;
        private final List<Point> _points = new ArrayList<>();
        public int x1=-1, y1=-1, z1=-1, x2, y2, z2;

        public Blob(int c) {
            this.c = c;
        }

        public void addPoint(Point p) {
            _points.add(p);
            if(x1==-1||p.x<x1) x1 = p.x;
            if(y1==-1||p.y<y1) y1 = p.y;
            if(z1==-1||p.z<z1) z1 = p.z;

            if(p.x>x2) x2 = p.x;
            if(p.y>y2) y2 = p.y;
            if(p.z>z2) z2 = p.z;
        }

        public List<Point> points() {
            return _points;
        }

        public int volume() {
            return (1+x2-x1)*(1+y2-y1)*(1+z2-z1);
        }

        public int xExtent() { return x2-x1; }
        public int yExtent() { return y2-y1; }
        public int zExtent() { return z2-z1; }

        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("{col:"+c+", vol:"+volume()+", b:("+x1+","+y1+","+z1+")-("+x2+","+y2+","+z2+")");
            /*
            b.append(", p:[");
            for(Point p:points) {
                b.append("("+p.x+","+p.y+","+p.z+"), ");
            }
            b.setLength(b.length()-2);
            b.append("]");
            */
            b.append("}");
            return b.toString();
        }
    }

    public static class Point {
        //public final int[] p;
        public final int x;
        public final int y;
        public final int z;

        //public Point(int[] p) {
            //this.p = p;
        //}
        public Point(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean equals(Object o) {
            Point p = (Point)o;
            /*
            //return Arrays.equals(p, p.p);
            return p[0]==p.p[0]
                && p[1]==p.p[1]
                && p[2]==p.p[2];
            */
            return p.x==x && p.y==y && p.z==z;
        }

        public int hashCode() {
            return 7*x+31*y+57*z;
        }
    }
}
