package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;


public class WorkerContinuous implements Worker {
    private final int _x1;
    private final int _y1;
    private final int _x2;
    private final int _y2;
    private final Pattern _wp;
    private final int _size;
    private final float[] _prev;
    private final float[] _pattern;
    private final float[][] _chanpattern;
    //private final int[] _pow;
    private final float _weight;
    private final float _oWeight;
    private final boolean _moore;
    private final boolean _useDepth;
    private final boolean _channels;
    private final UpdateMode _umode;
    private final ExternalForce _ef;
    private final Random _r;

    public WorkerContinuous(Pattern p, int x1, int y1, int x2, int y2, float weight, ComputeMode cmode, UpdateMode umode, ExternalForce ef, Random r) {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _wp = p;
        _weight = weight;
        _oWeight = 1f - weight;
        _size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();
        _prev = new float[_wp.archetype().sourceLength()];
        _pattern = new float[_prev.length];
        _chanpattern = new float[4][_prev.length];
        _useDepth = p.archetype().dims()==3;
        _moore = p.archetype().neighborhood()==Archetype.Neighborhood.moore;
        _channels = cmode==ComputeMode.channel;
        _umode = umode.simpleSynchronous() ? null:umode.plan(_wp.archetype());
        _ef = ef;
        _r = r;

        //_pow = new int[_wp.archetype().sourceLength()];
        //for(int i=0;i<_pow.length;i++) {
            //_pow[_pow.length-1-i] = (int) Math.pow(colors, i);
        //}
    }

    private void validate(IntPlane p) {
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                p.getCell(i, j);
            }
        }
    }

    public void frame3d(final FloatBlockPlane p1, final FloatBlockPlane p2) {
        final int d = _size*2+1;
        for(int i=_y1;i<_y2;i++) {
            for(int j=_x1;j<_x2;j++) {
                for(int k=0;k<p1.getDepth();k++) {
                    if(_umode!=null&&!_umode.update(p1, j, i, k)) {
                        p2.setCell(j,i,k,p1.getCell(j,i,k));
                    }
                    else {
                        if(_moore) {
                            p1.getBlock(_pattern, j-_size, i-_size, k-_size, /*dx*/ d, /*dy*/ d, /*dz*/ d, 0);
                        }
                        else {
                            p1.getCardinal(_pattern, j, i, k, /*dx*/ _size, /*dy*/ _size, /*dz*/ _size, 0);
                        }
                        if(_channels) {
                            //p2.setCell(j, i, k, channels());
                        }
                        else {
                            p2.setCell(j, i, k, next(_pattern));
                        }
                    }
                }
            }
        }
        _ef.apply(p2, _r);
    }

    /*TODO
    private final int channels() {
        Colors.extractChannels(_pattern, _chanpattern);
        final float r = next(_chanpattern[0]);
        final float g = next(_chanpattern[1]);
        final float b = next(_chanpattern[2]);
        final float a = next(_chanpattern[3]);
        return Colors.packBounded(r,g,b,a);
    }
    */

    private final float next(final float[] pattern) {
        final float v = _wp.next(0, pattern);
        final float ov = pattern[pattern.length/2];
        final float nv = (float) ((_oWeight*ov)+(_weight*v));
        return nv;
    }

    private static void dump(int x, int y, int z, float[] p, float v) {
        StringBuilder b = new StringBuilder("("+x+","+y+","+z+") => ");
        for(int i=0;i<p.length;i++) {
            b.append(p[i]).append(" ");
        }
        b.append("=> ").append(v);
        System.err.println(b);
    }

    public void frame(final Plane ip1, final Plane ip2) {
        if(_useDepth) {
            frame3d((FloatBlockPlane)ip1, (FloatBlockPlane)ip2);
            return;
        }
        final FloatPlane p1 = (FloatPlane) ip1;
        final FloatPlane p2 = (FloatPlane) ip2;
        //validate(p1);
        int counts = 0;
        //System.err.println(String.format("dims: %d,%d,%d,%d", _y1, _y2, _x1, _x2));
        int mw = p1.getWidth()/2;
        int mh = p1.getHeight()/2;
        int tw = p1.getWidth();
        int th = p1.getHeight();
        final int d = _size*2+1;
        final int mx = (_y2-_y1)*(_x2-_x1);
        for(int i=_y1;i<_y2;i++) {
            for(int j=_x1;j<_x2;j++) {
                //if(RAND.nextInt(1000)>=100) {
                final int self = i*j;
                //if(RAND.nextInt(mx)>=self) {
                if(_umode!=null&&!_umode.update(p1, j, i, 0)) {
                    p2.setCell(j,i,p1.getCell(j,i));
                }
                else {
                    counts++;
                    if(_moore) {
                        p1.getBlock(_pattern, j-_size, i-_size, /*dx*/ d, /*dy*/ d, 0);
                    }
                    else {
                        p1.getCardinal(_pattern, j, i, _size, _size, 0);
                    }
                    if(_channels) {
                        //p2.setCell(j, i, channels());
                    }
                    else {
                        p2.setCell(j, i, next(_pattern));
                    }
                }
            }
        }
        _ef.apply(p2, _r);
        //mutateRule();
        //System.err.println("set "+counts+" cells");
    }

    public void frame(final Plane ic) {
        if(_wp.archetype().dims()!=1) {
            throw new IllegalStateException("1-arity frame method only compatible with 1d rules");
        }
        final FloatPlane c = (FloatPlane) ic;
        final int w = c.getWidth();
        final int h = c.getHeight();
        final int size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();

        float[] prev = new float[2*size+1];
        float[] pattern = new float[prev.length];

        //final Pattern p = createPattern(pool);
        //System.err.println("created pattern: "+p);
        for(int i=_y1;i<_y2;i++) {
            for(int j=0;j<w;j++) {
                if(_umode!=null&&!_umode.update(c, j, 0, 0)) {
                    c.setCell(j,i,c.getCell(j,i-1));
                }
                else {
                    c.getBlock(pattern, j-size, i-1, pattern.length, 1, 0);
                    final float v = _wp.next(0, pattern);
                    final float ov = pattern[pattern.length/2];
                    final float nv = (float) ((_oWeight*ov)+(_weight*v));
                    c.setCell(j, i, nv);
                    //System.err.print(".");
                }
            }
            //mutateRule(p);
            _wp.tick();
            _ef.apply(c, _r);
        }
    }

    public void frame(final Plane ip1, final Plane ip2, final Plane imeta) {
        final FloatPlane p1 = (FloatPlane) ip1;
        final FloatPlane p2 = (FloatPlane) ip2;
        final IntPlane meta = (IntPlane) imeta;
        int counts = 0;
        //System.err.println(String.format("dims: %d,%d,%d,%d", _y1, _y2, _x1, _x2));
        int mw = p1.getWidth()/2;
        int mh = p1.getHeight()/2;
        int tw = p1.getWidth();
        int th = p1.getHeight();
        int toff = 0;
        for(int i=_y1;i<_y2;i++) {
            for(int j=_x1;j<_x2;j++) {
                counts++;
                //System.err.println(String.format("working: %d,%d", j, i));
                p1.getBlock(_prev, j-_size, i-1, /*dx*/ 3, /*dy*/ 3, 0);
                int idx = 0;
                for(int k=0;k<_prev.length;k++) {
                    _pattern[k] = (float) (_prev[k]);
                    //TODO: ???
                    //idx += _prev[k] * _pow[k];
                    //System.err.println(String.format("prev[%d]=%d, pow[%d]=%d", k, _prev[k], k, _pow[k]));
                }
                //System.err.println(idx+" ");
                //p2.setCell(j, i, _wp.next(idx));
                //TODO HERE
                //p2.setCell(j, i, _wp.next(idx, distance(tw, th, mw, mh, i, j)));
                //int off = meta.getCell(j, i);
                //toff += off;
                //p2.setCell(j, i, _wp.next(idx, off));
            }
        }
        mutateRule();
        //System.err.println("set "+counts+" cells");
        //System.err.println("total offsets: "+toff);
    }

    private final int distance(int tw, int th, int mw, int mh, int i, int j) {
        return (int) (Math.sqrt((mw-i)*(mw-i) + (mh-j)*(mh-j))/10);
    }

    private void mutateRule() {
        //if(mutagen()!=null) {
            //_wp.mutate(mutagen());
        //}
    }
}
