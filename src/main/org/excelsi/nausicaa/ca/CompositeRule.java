package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class CompositeRule implements Rule {
    private final Rule[] _rs;


    public CompositeRule(Rule[] rs) {
        _rs = rs;
    }

    @Override public Archetype archetype() {
        throw new UnsupportedOperationException();
    }

    @Override public Ruleset origin() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule getHyperrule() {
        throw new UnsupportedOperationException();
    }

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public int[] colors() {
        throw new UnsupportedOperationException();
    }

    @Override public int background() {
        throw new UnsupportedOperationException();
    }

    @Override public int length() {
        throw new UnsupportedOperationException();
    }

    @Override public int dimensions() {
        //throw new UnsupportedOperationException();
        return _rs[0].dimensions();
    }

    @Override public int colorCount() {
        throw new UnsupportedOperationException();
    }

    @Override public int width() {
        throw new UnsupportedOperationException();
    }

    @Override public int height() {
        throw new UnsupportedOperationException();
    }

    @Override public String id() {
        throw new UnsupportedOperationException();
    }

    @Override public void copy(Plane p) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
        throw new UnsupportedOperationException();
    }

    @Override public com.google.gson.JsonElement toJson() {
        throw new UnsupportedOperationException();
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public void write(PrintWriter w) {
        throw new UnsupportedOperationException();
    }

    @Override public String humanize() {
        return Arrays.toString(_rs);
    }

    @Override public Plane generate(Plane c, int start, int end, ExecutorService pool, boolean stopOnSame, boolean overwrite, Updater u, GOptions opt) {
        Sliceable p = (Sliceable) c;
        for(int i=_rs.length-1;i>=0;i--) {
            System.err.println("compositing at depth "+i);
            p.setReadDepth(i);
            p.setWriteDepth(i);
            _rs[i].generate(c, start, end, pool, stopOnSame, overwrite, u, opt);
        }
        return c;
    }

    @Override public Iterator<Plane> frameIterator(Plane initial, ExecutorService pool, GOptions opt) {
        CompositePlane p1 = (CompositePlane) initial;
        final Plane[] ps1 = p1.planes();

        //final IntPlane[] ps2 = new IntPlane[ps1.length];
        //final CompositeIntPlane p2 = new CompositeIntPlane(ps2);
        final CompositePlane p2 = p1.emptyCopy();
        final Plane[] ps2 = p2.planes();

        final DepthVariables dv = new DepthVariables(ps1, ps2, false);

        final Iterator[] its = new Iterator[ps1.length];
        for(int i=0;i<its.length;i++) {
            GOptions vs = i<its.length-1?opt.variables(dv):opt;
            its[i] = _rs[i].frameIterator(ps1[i], pool, vs);
        }

        return new Iterator<Plane>() {
            CompositePlane pread = p1; // reading plane
            CompositePlane pwrite = p2; // writing plane
            boolean first = true;
            int stack = 0;
            @Override public boolean hasNext() { return true; }
            @Override public void remove() { }
            @Override public Plane next() {
                pwrite.lockWrite();
                //IntPlane[] pr = p.planes();
                for(int i=_rs.length-1;i>=0;i--) {
                    dv.setIndex(i+1);
                    //System.err.println("compositing at depth "+i);
                    Plane n = (Plane) its[i].next();
                    if(first) {
                        ps2[i] = n;
                    }
                }
                pwrite.unlockWrite();
                first = false;
                stack = (stack==0?1:0);
                dv.setStack(stack);
                CompositePlane tmp = pread;
                pread = pwrite;
                pwrite = tmp;
                return pread;
            }
        };
    }

    private static class DepthVariables implements Variables {
        private final Plane[][] _ps;
        private final boolean _wrap;

        private int _i;
        private int _s;


        public DepthVariables(Plane[] ps1, Plane[] ps2, boolean wrap) {
            _ps = new Plane[2][];
            _ps[0] = ps1;
            _ps[1] = ps2;
            _wrap = wrap;
        }

        public void setIndex(int i) { _i = i; }
        public void setStack(int s) { _s = s; }

        @Override public Double weight() { return null; }

        @Override public boolean weightVaries() { return true; }

        @Override public Double weight(Plane p, int x, int y, int z) {
            double w = _ps[_s][_i].probe().probeNorm(x,y,z);
            //System.err.println("w: "+w);
            return w;
        }

        @Override public boolean update(Plane p, int x, int y, int z, float chance) {
            float v = _ps[_s][_i].probe().probeNorm(x,y,z);
            //System.err.println("varp: "+x+","+y+","+z+":"+chance+", "+v);
            return v>=chance;
        }
    }
}
