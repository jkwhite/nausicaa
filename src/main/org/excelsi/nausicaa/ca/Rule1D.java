package org.excelsi.nausicaa.ca;


//import javassist.*;
import java.util.*;
import java.math.BigInteger;
import java.io.*;
import java.util.zip.*;
import java.util.concurrent.ExecutorService;
import org.excelsi.rlyehian.Codec;


public class Rule1D extends AbstractRule implements Rule, java.io.Serializable {
    protected int _bgr;
    private transient Selector _sel;
    protected int _len;
    private String _name;
    protected int[] _colors;
    private Ruleset _origin;
    private int[][] _patterns;
    private int _background;
    private byte[] _binary;
    private long _seed;

    private int[] _row;
    private int[] _next;
    private int[] _last;
    private int[] _existing;
    private int[] _totals;
    private int[] _lastTotals;
    private int[] _lastTotals2;
    private boolean _haslast;
    private transient boolean _debug;

    protected transient Random _om = new Random(Rand.seed());


    public Rule1D(Ruleset origin, int[] colors, int[][] patterns, int background) {
        super(patterns.length*patterns[0].length, 1);
        _origin = origin;
        _len = patterns[0].length;
        _colors = colors;
        _patterns = patterns;
        _background = background;
        int i = 0;
        long seed = patterns.length;
        for(int[] p:patterns) {
            if(p.length!=_len) {
                throw new IllegalArgumentException("length mismatch");
            }
            for(int j=0;j<p.length;j++) {
                //TODO
                //setCell(i++, 0, p[j]);
                seed = Long.rotateLeft(seed, 1);
                seed ^= p[j];
            }
        }
        _bgr = background;
        _sel = new TroveSelector(_patterns, _colors);
        _seed = seed;
    }

    private void computeName() {
        int i = 0;
        StringBuilder str = new StringBuilder();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(bos, 16384);
            DataOutputStream dos = new DataOutputStream(gos);
            dos.writeByte(dimensions());
            dos.writeByte(_colors.length);
            for(int color:_colors) {
                dos.writeInt(color);
            }
            dos.writeByte(index(_colors, _background));

            //System.err.println("_len="+_len);
            dos.writeInt(_patterns.length);
            dos.writeInt(_patterns[0].length);
            for(int[] p:_patterns) {
                if(p.length!=_len) {
                    throw new IllegalArgumentException("length mismatch");
                }
                for(int j=0;j<p.length;j++) {
                    //set(i++, 0, p[j]);
                    dos.writeByte(index(_colors, p[j]));
                }
            }
            //_bgr = background;
            //_sel = compile2(patterns);
            //_sel = new TroveSelector(_patterns, _colors);
            _options.write(dos);
            dos.close();
            _binary = bos.toByteArray();
            _name = Base64.encodeBytes(_binary);
        }
        catch(IOException e) {
            throw new Error(e);
        }
    }

    public int dimensions() { return 1; }

    public void setDebug(boolean debug) {
        _debug = debug;
    }

    private int _debugColumn = -1;
    public void setDebug(int column) {
        _debugColumn = column;
    }

    public Rule1D copy() {
        return new Rule1D(_origin, _colors, _patterns, _background);
    }

    public int length() {
        return _len-1;
    }

    public int background() {
        return _background;
    }

    public Ruleset origin() {
        return _origin;
    }

    public int[][] toPattern() {
        // getCell
        throw new UnsupportedOperationException();
        /*
        int[][] ps = new int[width()/_len][_len];
        int pos = 0;
        for(int i=0;i<ps.length;i++) {
            ps[i] = new int[_len];
            for(int j=0;j<ps[i].length;j++) {
                ps[i][j] = getCell(pos, 0);
                pos++;
            }
        }
        return ps;
        */
    }

    public int[] colors() {
        int[] colors = new int[_colors.length];
        System.arraycopy(_colors, 0, colors, 0, _colors.length);
        return colors;
    }

    public String toString() {
        if(_name==null) {
            computeName();
        }
        return _name;
    }

    public String toIncantation() {
        Codec c = Codec.get();
        return c.encode(binary());
    }

    public byte[] binary() {
        if(_binary==null) {
            computeName();
        }
        return _binary;
    }

    public long toSeed() {
        return _seed;
    }

    public static Object deepCopy(Serializable s) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(s);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return ois.readObject();
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    public static int[][] archetype(int[] colors, int length) {
        int[] idx = new int[length];

        //System.err.println("building archetype of dim "+Math.pow(colors.length, length)+" x "+(1+length));
        int[][] pats = new int[(int)Math.pow(colors.length, length)][length+1];
        for(int i=0;i<pats.length;i++) {
            int[] p = pats[pats.length-i-1];
            for(int j=0;j<p.length-1;j++) {
                p[j] = colors[idx[j]];
            }
            incArray(idx, colors.length);
        }
        return pats;
    }

    public static void incArray(int[] idx, int max) {
        for(int j=idx.length-1;j>=0;j--) {
            if(++idx[j]==max) {
                idx[j] = 0;
            }
            else {
                break;
            }
        }
    }

    public void uncache() {
        _next=null;
    }

    public void generate2(int[] next, int[] row, int[] existing, boolean over) {
        final int offset = (_len-2)/2;
        int failed = 0;
        for(int i=0;i<next.length;i++) {
            int colidx = _sel.next(row, i);
            if(colidx==-1) {
                // rule cannot exist in this space
                // use existing value (possibly from another rule)
                //if(_debug&&!printed) { System.err.println("no space for rule "+toSeed()+" at "+i); printed=true; }
                if(i==_debugColumn) {
                    System.err.println(toSeed()+" skipping, over="+over+", prev alpha: "+Colors.alpha(row[i+offset])+" bgra="+Colors.alpha(_bgr)+" exalpha: "+Colors.alpha(existing[i]));
                }
                next[i] = over?combineAlpha(row[i+offset], _bgr):existing[i];
                if(next[i]==0) {
                    next[i] = _bgr;
                }
                failed++;
                continue;
            }
            //spaceCount++;
            //_totals[colidx]++;
            if(i==_debugColumn) {
                System.err.println(toSeed()+" prev alpha: "+Colors.alpha(row[i+offset])+" colalpha: "+Colors.alpha(_colors[colidx]));
            }
            if(existing[i]==_bgr||existing[i]==0) {
                //if(i%100==0) System.err.print(_existing[i]==_bgr?"b":"z");
                next[i] = combineAlpha(row[i+offset], _colors[colidx]);
            }
            else {
                //if(i%100==0) System.err.print("H");
                //if(Rand.om.nextInt(100)>=50) {
                    //_next[i] = combineAlpha(_row[i+offset], _colors[colidx]);
                //}
            }
            //if(same&&next[i]!=row[i+offset]) {
                //if(_haslast&&last[i+offset]!=next[i]) {
                    //same = false;
                //}
            //}
        }
    }

    public float generate(Plane c, int start, int end, final ExecutorService pool, boolean stopOnSame, boolean over, Updater u) {
        final int offset = (_len-2)/2;
        if(_next==null||_next.length!=c.getWidth()) {
            _row = new int[c.getWidth()+2*offset];
            _next = new int[c.getWidth()];
            _last = new int[c.getWidth()+2*offset];
            _existing = new int[c.getWidth()];
            _totals = new int[_colors.length];
            _lastTotals = new int[_colors.length];
            _lastTotals2 = new int[_colors.length];
            _haslast = false;
        }

        int crow = start;
        int steps = end-start;
        int diff = 0;
        Selector sel = _sel;
        for(int gen=0;gen<steps;gen++) {
            //System.err.print("#");
            c.getRow(_row, crow-1, offset);
            c.getRow(_existing, crow, 0);
            for(int i=0;i<offset;i++) {
                _row[i] = _row[c.getWidth()+i];
                _row[c.getWidth()+offset+i] = _row[i+offset];
                //_next[i] = _existing[i];
                _next[i] = _bgr;
                _next[_next.length-i-1] = _bgr;
            }
            boolean same = true;
            int failed = 0;
            boolean printed = false;
            boolean printed2 = false;
            long spaceCount = 0;
            for(int i=0;i<_next.length;i++) {
                int colidx = sel.next(_row, i);
                if(colidx==-1) {
                    // rule cannot exist in this space
                    // use existing value (possibly from another rule)
                    //if(_debug&&!printed) { System.err.println("no space for rule "+toSeed()+" at "+i); printed=true; }
                    if(i==_debugColumn) {
                        System.err.println(toSeed()+" skipping, over="+over+", prev alpha: "+Colors.alpha(_row[i+offset])+" bgra="+Colors.alpha(_bgr)+" exalpha: "+Colors.alpha(_existing[i]));
                    }
                    _next[i] = over?combineAlpha(_row[i+offset], _bgr):_existing[i];
                    if(_next[i]==0) {
                        _next[i] = _bgr;
                    }
                    failed++;
                    continue;
                }
                //spaceCount++;
                _totals[colidx]++;
                if(i==_debugColumn) {
                    System.err.println(toSeed()+" prev alpha: "+Colors.alpha(_row[i+offset])+" colalpha: "+Colors.alpha(_colors[colidx]));
                }
                if(_existing[i]==_bgr||_existing[i]==0) {
                    //if(i%100==0) System.err.print(_existing[i]==_bgr?"b":"z");
                    _next[i] = combineAlpha(_row[i+offset], _colors[colidx]);
                }
                else {
                    //if(i%100==0) System.err.print("H");
                    //if(Rand.om.nextInt(100)>=50) {
                        //_next[i] = combineAlpha(_row[i+offset], _colors[colidx]);
                    //}
                }
                if(same&&_next[i]!=_row[i+offset]) {
                    if(_haslast&&_last[i+offset]!=_next[i]) {
                        same = false;
                    }
                }
            }
            //System.err.println(toSeed()+" spaceCount: "+spaceCount);
            diff = 0;
            c.setRow(_next, crow);
            int tot = _next.length;
            if(!same&&failed<_next.length/2) {
                for(int i=1;i<_totals.length;i++) {
                    if(crow>1) {
                        diff += Math.abs(_totals[i] - _lastTotals[i]);
                        if(crow>2) {
                            diff += Math.abs(_totals[i] - _lastTotals2[i]);
                        }
                    }
                    _lastTotals2[i] = _lastTotals[i];
                    _lastTotals[i] = _totals[i];
                    _totals[i] = 0;
                }
            }
            System.arraycopy(_row, 0, _last, 0, _row.length);
            _haslast = true;
            if(same&&stopOnSame) {
                return diff;
            }
            crow++;
            if(u!=null&&crow%u.interval()==0) {
                u.update(this, start, crow, end);
            }
            if(Thread.currentThread().isInterrupted()) {
                return diff;
            }
        }
        int contig = 0;
        int maxc = 0;
        for(int i=1;i<_next.length;i++) {
            if(_next[i]==_next[i-1]) {
                contig++;
                if(contig>maxc) {
                    maxc = contig;
                }
                break;
            }
            else {
                contig = 0;
            }
        }
        boolean same = contig < 4;
        return diff;
    }

    private int _minAlpha = Integer.MAX_VALUE;
    private int combineAlpha(int a, int b) {
        int aa = Colors.alpha(a);
        int ab = Colors.alpha(b);
        //System.err.println("srcalphas: "+aa+", "+ab);
        int an = aa+ab; //-255;
        //an -= 255;
        //if(a+b!=-19192912) an-=255;
        if(an>255) an-=254;
        if(an>255) an=255;
        //if(an<0) an=0;
        if(an<128) an=128;
        an = 255;
        //if(a+b!=-1191191) an=255;
        //System.out.print(an);
        //System.out.print(" ");
        _minAlpha = Math.min(_minAlpha, an);
        a = (b&Colors.COLOR_MASK)|(an<<24);
        //System.err.println("aa="+aa+", ab="+ab+", an="+an+", an<<24="+(an<<24)+", a="+a);
        return a;
    }

    private int colorIndexOf(int color) {
        for(int i=0;i<_colors.length;i++) {
            if(_colors[i]==color) {
                return i;
            }
        }
        return -1;
    }

    public static final int get(Plane c, int x, int y) {
        if(x<0) {
            x = c.getWidth()+x;
        }
        else if(x>=c.getWidth()) {
            x -= c.getWidth();
        }
        if(y<0) {
            y = c.getHeight()+y;
        }
        else if(y>=c.getHeight()) {
            y -= c.getHeight();
        }
        try {
            return c.getCell(x, y);
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("coordinate out of range: ("+x+","+y+")");
        }
    }

    public static final void set(Plane c, int x, int y, int v) {
        if(x<0) {
            x = c.getWidth()+x;
        }
        else if(x>=c.getWidth()) {
            x -= c.getWidth();
        }
        if(y<0) {
            y = c.getHeight()+y;
        }
        else if(y>=c.getHeight()) {
            y -= c.getHeight();
        }
        try {
            c.setCell(x, y, v);
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("coordinate out of range: ("+x+","+y+")");
        }
    }

    private static byte index(int[] colors, int p) {
        for(int i=0;i<colors.length;i++) {
            if(colors[i]==p) {
                return (byte) i;
            }
        }
        throw new IllegalArgumentException("no such color "+p+" in "+Arrays.toString(colors));
    }

}
