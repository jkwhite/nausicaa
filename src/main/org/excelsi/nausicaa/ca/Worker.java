package org.excelsi.nausicaa.ca;


public class Worker {
    private final int _x1;
    private final int _y1;
    private final int _x2;
    private final int _y2;
    private final Pattern _wp;
    private final int _size;
    private final int[] _prev;
    private final int[] _pattern;
    private final int[][] _chanpattern;
    private final int[] _pow;
    private final float _weight;
    private final float _oWeight;
    private final boolean _useDepth;
    private final boolean _channels;

    public Worker(Pattern p, int x1, int y1, int x2, int y2, float weight, ComputeMode cmode) {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _wp = p;
        _weight = weight;
        _oWeight = 1f - weight;
        _size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();
        _prev = new int[(int)Math.pow(2*_size+1, _wp.archetype().dims())];
        _pattern = new int[_prev.length];
        _chanpattern = new int[4][_prev.length];
        _useDepth = p.archetype().dims()==3;
        _channels = cmode==ComputeMode.channel;
        if(_channels) System.err.println("using by-channel compute mode");

        //_pow = new int[_wp.length()];
        _pow = new int[_wp.archetype().sourceLength()];
        for(int i=0;i<_pow.length;i++) {
            _pow[_pow.length-1-i] = (int) Math.pow(colors, i);
            if(Rand.om.nextInt(100)<0) {
                _pow[_pow.length-1-i] = _pow[_pow.length-1-i] + (int) (Rand.om.nextGaussian()*_pow[_pow.length-1-i]);
            }
        }
        //System.err.println(String.format("worker dims: %dx%d+%dx%d", _x1, _y1, _x2, _y2));
        //System.err.println(String.format("prev array size: %d, pow array size: %d", _prev.length, _pow.length));
    }

    private void validate(Plane p) {
        for(int i=0;i<p.getWidth();i++) {
            for(int j=0;j<p.getHeight();j++) {
                p.getCell(i, j);
            }
        }
    }

    //private static final float WEIGHT = 0.05f;
    //private static final float WEIGHT = 1.0f;
    //private static final float O_WEIGHT = 1.0f - WEIGHT;
    public void frame3d(final IntBlockPlane p1, final IntBlockPlane p2) {
        final int d = _size*2+1;
        for(int i=_y1;i<_y2;i++) {
            for(int j=_x1;j<_x2;j++) {
                for(int k=0;k<p1.getDepth();k++) {
                    p1.getBlock(_pattern, j-_size, i-_size, k-_size, /*dx*/ d, /*dy*/ d, /*dz*/ d, 0);
                    //final int v = _wp.next(0, _pattern);
                    //final int ov = _pattern[_pattern.length/2];
                    //final int nv = (int) ((_oWeight*ov)+(_weight*v));
                    //p2.setCell(j, i, k, nv);
                    if(_channels) {
                        p2.setCell(j, i, k, channels());
                    }
                    else {
                        p2.setCell(j, i, k, next(_pattern));
                    }
                }
            }
        }
        //System.err.println("-----");
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
        final int v = _wp.next(0, pattern);
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

    public void frame(final Plane p1, final Plane p2) {
        if(_useDepth /*p1 instanceof IntBlockPlane*/) {
            frame3d((IntBlockPlane)p1, (IntBlockPlane)p2);
            return;
        }
        //validate(p1);
        int counts = 0;
        //System.err.println(String.format("dims: %d,%d,%d,%d", _y1, _y2, _x1, _x2));
        int mw = p1.getWidth()/2;
        int mh = p1.getHeight()/2;
        int tw = p1.getWidth();
        int th = p1.getHeight();
        final int d = _size*2+1;
        for(int i=_y1;i<_y2;i++) {
            for(int j=_x1;j<_x2;j++) {
                counts++;
                //System.err.println(String.format("working: %d,%d", j, i));
                //p1.getBlock(_prev, j-_size, i-1, /*dx*/ 3, /*dy*/ 3, 0);
                p1.getBlock(_prev, j-_size, i-_size, /*dx*/ d, /*dy*/ d, 0);
                int idx = 0;
                for(int k=0;k<_prev.length;k++) {
                    _pattern[k] = (int) (_prev[k]);
                    //idx += _prev[k] * _pow[k];
                    //System.err.println(String.format("prev[%d]=%d, pow[%d]=%d", k, _prev[k], k, _pow[k]));
                }
                if(_channels) {
                    p2.setCell(j, i, channels());
                }
                else {
                    //final int v = _wp.next(0, _pattern);
                    //final int ov = _pattern[_pattern.length/2];
                    //final int nv = (int) ((_oWeight*ov)+(_weight*v));
                    //p2.setCell(j, i, nv);
                    p2.setCell(j, i, next(_pattern));
                }
            }
        }
        //mutateRule();
        //System.err.println("set "+counts+" cells");
    }

    public void frame(final Plane c) {
        if(_wp.archetype().dims()!=1) {
            throw new IllegalStateException("1-arity frame method only compatible with 1d rules");
        }
        final int w = c.getWidth();
        final int h = c.getHeight();
        final int size = _wp.archetype().size();
        final int colors = _wp.archetype().colors();

        int[] prev = new int[2*size+1];
        int[] pattern = new int[prev.length];

        //int[] pow = new int[_wp.length()];
        //for(int i=0;i<pow.length;i++) {
            //pow[pow.length-1-i] = (int) Math.pow(colors, i);
        //}

        //final Pattern p = createPattern(pool);
        //System.err.println("created pattern: "+p);
        for(int i=_y1;i<_y2;i++) {
            for(int j=0;j<w;j++) {
                c.getBlock(prev, j-size, i-1, prev.length, 1, 0);
                //int idx = 0;
                for(int k=0;k<prev.length;k++) {
                    pattern[k] = (int) (prev[k]);
                    //idx += prev[k] * pow[k];
                }
                final int v = _wp.next(0, pattern);
                final int ov = pattern[pattern.length/2];
                final int nv = (int) ((_oWeight*ov)+(_weight*v));
                c.setCell(j, i, nv);
                //System.err.print(".");
            }
            //mutateRule(p);
            _wp.tick();
        }
    }

    public void frame(final Plane p1, final Plane p2, final Plane meta) {
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
