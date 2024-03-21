package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class WorkerDiscrete implements Worker {
    private static final Logger LOG = LoggerFactory.getLogger(WorkerDiscrete.class);
    private final int _x1;
    private final int _y1;
    private final int _x2;
    private final int _y2;
    private final Pattern _wp;
    private final boolean _usesSource;
    private final int _size;
    private final int[] _prev;
    private final int[] _pattern;
    private final int[][] _chanpattern;
    private final int[] _pow;
    private final Variables _vars;
    private final Rogers _neighbors;
    private final boolean _useDepth;
    private final boolean _channels;
    private final UpdateMode _umode;
    private final ExternalForce _ef;
    private final Random _r;
    private final Pattern.Ctx _pctx;
    private double _weight;
    private double _oWeight;
    private final Stats _stats;

    public WorkerDiscrete(Pattern p, int x1, int y1, int x2, int y2, Variables vars, ComputeMode cmode, UpdateMode umode, ExternalForce ef, Random r) {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _wp = p;
        _vars = vars;
        _usesSource = _wp.usesSource();
        LOG.info("usesSource: "+_usesSource);
        _size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();
        _prev = new int[_wp.archetype().sourceLength()];
        _pattern = new int[_prev.length];
        _chanpattern = new int[4][_prev.length];
        _useDepth = p.archetype().dims()==3;
        _neighbors = Rogers.forPattern(p);
        _channels = cmode==ComputeMode.channel;
        _umode = umode.simpleSynchronous() ? null:umode.plan(_wp.archetype());
        _ef = ef;
        _r = r;
        _pctx = new Pattern.Ctx();
        _pctx.c = new int[3];
        _pctx.cr = new double[3];
        _pctx.r = r;
        _stats = new Stats();

        _pow = new int[_wp.archetype().sourceLength()];
        for(int i=0;i<_pow.length;i++) {
            _pow[_pow.length-1-i] = (int) Math.pow(colors, i);
        }
    }

    public Stats getStats() { return _stats; }

    private void validate(IntPlane p) {
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                p.getCell(i, j);
            }
        }
    }

    public void frame3d(final IntBlockPlane p1, final IntBlockPlane p2) {
        final int d = _size*2+1;
        for(int i=_y1;i<_y2;i++) {
            _pctx.c[1] = i-p1.getHeight()/2;
            _pctx.cr[1] = _pctx.c[1]/(double)p1.getHeight();
            for(int j=_x1;j<_x2;j++) {
                _pctx.c[0] = j-p1.getWidth()/2;
                _pctx.cr[0] = _pctx.c[0]/(double)p1.getWidth();
                for(int k=0;k<p1.getDepth();k++) {
                    if(_umode!=null&&!_umode.update(p1, j, i, k, _vars)) {
                        p2.setCell(j,i,k,p1.getCell(j,i,k));
                    }
                    else {
                        _pctx.c[2] = k-p1.getDepth()/2;
                        _pctx.cr[2] = _pctx.c[2]/Math.min(1,p1.getDepth());
                        if(!_vars.weightVaries()) {
                            _weight = _vars.weight();
                        }
                        else {
                            _weight = _vars.weight(p1, j, i, 0);
                        }
                        _oWeight = 1d - _weight;
                        if(_usesSource) {
                            _neighbors.getNeighborhood(p1, _pattern, j, i, k, 0);
                        }
                        if(_channels) {
                            p2.setCell(j, i, k, channels());
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

    private final int channels() {
        Colors.extractChannels(_pattern, _chanpattern);
        final int r = next(_chanpattern[0]);
        final int g = next(_chanpattern[1]);
        final int b = next(_chanpattern[2]);
        final int a = next(_chanpattern[3]);
        return Colors.packBounded(r,g,b,a);
    }

    private final int next(final int[] pattern) {
        final int v = _wp.next(0, pattern, _pctx);
        final int ov = pattern[pattern.length/2];
        final int nv = (int) ((_oWeight*ov)+(_weight*v));
        return nv;
    }

    private static void dump(int x, int y, int z, int[] p, int v) {
        StringBuilder b = new StringBuilder("("+x+","+y+","+z+") => ");
        for(int i=0;i<p.length;i++) {
            b.append(p[i]).append(" ");
        }
        b.append("=> ").append(v);
        System.err.println(b);
    }

    public void frame(final Plane ip1, final Plane ip2) {
        final long startTime = System.currentTimeMillis();
        if(_useDepth) {
            frame3d((IntBlockPlane)ip1, (IntBlockPlane)ip2);
            final long endTime = System.currentTimeMillis();
            _stats.timeMsec += (endTime-startTime);
            _stats.frames++;
            return;
        }
        final IntPlane p1 = (IntPlane) ip1;
        final IntPlane p2 = (IntPlane) ip2;
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
            _pctx.c[1] = i-p1.getHeight()/2;
            _pctx.cr[1] = _pctx.c[1]/(double)p1.getHeight();
            for(int j=_x1;j<_x2;j++) {
                if(!_vars.weightVaries()) {
                    _weight = _vars.weight();
                }
                else {
                    _weight = _vars.weight(ip1, j, i, 0);
                    //System.err.println("got w for "+j+","+i+":"+_weight);
                }
                _oWeight = 1d - _weight;
                if(_umode!=null&&!_umode.update(p1, j, i, 0, _vars)) {
                    p2.setCell(j,i,p1.getCell(j,i));
                }
                else {
                    _pctx.c[0] = j-p1.getWidth()/2;
                    _pctx.cr[0] = _pctx.c[0]/(double)p1.getWidth();
                    counts++;
                    if(_usesSource) {
                        _neighbors.getNeighborhood(p1, _pattern, j, i, 0);
                    }
                    if(_channels) {
                        p2.setCell(j, i, channels());
                    }
                    else {
                        p2.setCell(j, i, next(_pattern));
                    }
                }
            }
        }
        _ef.apply(p2, _r);
        final long endTime = System.currentTimeMillis();
        _stats.timeMsec += (endTime-startTime);
        _stats.frames++;
        //mutateRule();
        //System.err.println("set "+counts+" cells");
    }

    public void frame(final Plane ic) {
        if(_wp.archetype().dims()!=1) {
            throw new IllegalStateException("1-arity frame method only compatible with 1d rules");
        }
        final IntPlane c = (IntPlane) ic;
        final int w = c.getWidth();
        final int h = c.getHeight();
        final int size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();
        _weight = _vars.weight();
        _oWeight = 1d - _weight;

        int[] prev = new int[2*size+1];
        int[] pattern = new int[prev.length];

        //final Pattern p = createPattern(pool);
        //System.err.println("created pattern: "+p);
        for(int i=_y1;i<_y2;i++) {
            _pctx.c[1]=i-c.getHeight()/2;
            for(int j=0;j<w;j++) {
                _pctx.c[0]=j-c.getWidth()/2;
                if(_umode!=null&&!_umode.update(c, j, 0, 0, _vars)) {
                    c.setCell(j,i,c.getCell(j,i-1));
                }
                else {
                    c.getBlock(pattern, j-size, i-1, pattern.length, 1, 0);
                    final int v = _wp.next(0, pattern, _pctx);
                    final int ov = pattern[pattern.length/2];
                    final int nv = (int) ((_oWeight*ov)+(_weight*v));
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
        final IntPlane p1 = (IntPlane) ip1;
        final IntPlane p2 = (IntPlane) ip2;
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
                    _pattern[k] = (int) (_prev[k]);
                    idx += _prev[k] * _pow[k];
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
