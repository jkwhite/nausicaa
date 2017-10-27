package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public class ClusteredGaussianInitializer implements Initializer {
    private final Random _random;
    private final long _seed;
    private final Params _params;


    public ClusteredGaussianInitializer() {
        this(null, 0);
    }

    public ClusteredGaussianInitializer(long seed) {
        this(new Random(), seed);
    }

    public ClusteredGaussianInitializer(Random random, long seed) {
        this(random, seed, new Params());
    }

    public ClusteredGaussianInitializer(Random random, long seed, Params params) {
        _random = random;
        _seed = seed;
        _params = params;
    }

    public void init(Plane plane, Rule rule, Random random) {
        final Random r;
        if(_random!=null) {
            _random.setSeed(_seed);
            r = _random;
        }
        else {
            r = random;
        }
        final int[] colors = rule.colors();
        final int maxrad = (int) (((plane.getWidth()+plane.getHeight())/2)*_params.maxRadius);
        final int[] pnt = new int[rule.dimensions()];
        final int[] ctr = new int[rule.dimensions()];
        final int[] max = new int[rule.dimensions()];
        switch(rule.dimensions()) {
            case 3:
                max[2] = ((IntBlockPlane)plane).getDepth();
            case 2:
                max[1] = plane.getHeight();
            case 1:
                max[0] = plane.getWidth();
            default:
                break;
        }

        for(int i=0;i<_params.maxPoints;i++) {
            final int rad = r.nextInt(maxrad/2)+maxrad/2;
            switch(rule.dimensions()) {
                case 1:
                    ctr[0] = r.nextInt(plane.getWidth());
                    final int points1 = (int)(rad*_params.density);
                    for(int x=0;x<points1;x++) {
                        point(r, ctr, max, rad, pnt);
                        plane.setCell(pnt[0], 0, colors[computeColor(r, colors.length, i, _params.maxPoints)]);
                    }
                    break;
                case 2:
                    ctr[0] = r.nextInt(plane.getWidth());
                    ctr[1] = r.nextInt(plane.getHeight());
                    final int points2 = (int)(rad*rad*_params.density);
                    for(int j=0;j<points2;j++) {
                        point(r, ctr, max, rad, pnt);
                        plane.setCell(pnt[0], pnt[1], colors[computeColor(r, colors.length, i, _params.maxPoints)]);
                    }
                    break;
                case 3:
                    IntBlockPlane bp = (IntBlockPlane) plane;
                    ctr[0] = r.nextInt(bp.getWidth());
                    ctr[1] = r.nextInt(bp.getHeight());
                    ctr[2] = r.nextInt(bp.getHeight());
                    final int points3 = (int)(rad*rad*rad*_params.density);
                    for(int j=0;j<points3;j++) {
                        point(r, ctr, max, rad, pnt);
                        //System.err.println("("+pnt[0]+","+pnt[1]+","+pnt[2]+")");
                        bp.setCell(pnt[0], pnt[1], pnt[2], colors[computeColor(r, colors.length, i, _params.maxPoints)]);
                    }
                    break;
                default:
            }
        }
    }

    private void point(Random r, int[] c, int[] max, int rad, int[] pnt) {
        final float h = (float)Math.abs(rad*r.nextGaussian());
        final float t1 = r.nextFloat()*2f*Maths.PI2;
        final float t2 = r.nextFloat()*2f*Maths.PI2;
        final float t3 = r.nextFloat()*2f*Maths.PI2;
        pnt[0] = wrap(c[0]+(int)(h*Math.cos(t1)),max[0]);
        if(pnt.length>1) {
            pnt[1] = wrap(c[1]+(int)(h*Math.sin(t1)), max[1]);
            if(pnt.length>2) {
                pnt[2] = wrap(c[2]+(int)(h*Math.cos(t3)), max[2]);
            }
        }
    }

    private int wrap(int v, int m) {
        //return v<0 ? (m+v)%m : v>m ? v%m : v;
        return v<0 ? Math.abs((m+v)%m) : v>=m ? v%m : v;
    }

    private int computeColor(Random random, int colors, int idx, int maxPoints) {
        if(_params.zeroWeight>0f && random.nextInt(1000)<=1000f*_params.zeroWeight) {
            return 0;
        }
        else {
            int low = idx * (colors / maxPoints);
            int high = (1+idx) * (colors / maxPoints)-1;
            return random.nextInt(high-low)+low;
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.gaussian.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.clusteredgaussian.name());
        w.println(_seed);
        w.println(_params.zeroWeight);
        w.println(_params.maxPoints);
        w.println(_params.maxRadius);
        w.println(_params.density);
        w.println(_params.skew);
    }

    public static ClusteredGaussianInitializer read(BufferedReader r, int version) throws IOException {
        return new ClusteredGaussianInitializer(
            null,
            Long.parseLong(r.readLine()),
            new Params(
                Float.parseFloat(r.readLine()),
                Integer.parseInt(r.readLine()),
                Float.parseFloat(r.readLine()),
                Float.parseFloat(r.readLine()),
                Float.parseFloat(r.readLine())
            )
        );
    }

    public static final class Params {
        public final float zeroWeight;
        public final int maxPoints;
        public final float maxRadius;
        public final float density;
        public final float skew;


        public Params() {
            this(0f, 5, 0.1f, 0.2f, 0.2f);
        }

        public Params(float zeroWeight, int maxPoints, float maxRadius, float density, float skew) {
            this.zeroWeight = zeroWeight;
            this.maxPoints = maxPoints;
            this.maxRadius = maxRadius;
            this.density = density;
            this.skew = skew;
        }
    }
}
