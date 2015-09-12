package org.excelsi.ca;


import javassist.*;
import java.util.*;
import java.math.BigInteger;
import java.io.*;
import java.util.zip.*;
import org.excelsi.rlyehian.Codec;


public class Rule1D extends AbstractRule implements Rule, java.io.Serializable {
    private static int _unique = 0;
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
    protected BitSet _mask;
    protected boolean _evaporate;
    protected boolean _phaseTransition;
    protected boolean _whimsical;
    protected boolean _useMask = false;

    private int[] _row;
    private int[] _next;
    private int[] _last;
    private int[] _existing;
    private int[] _totals;
    private int[] _lastTotals;
    private int[] _lastTotals2;
    private boolean _haslast;
    private transient boolean _debug;

    protected transient Interceptor _interceptor;
    private transient Fabric _fabric;
    protected transient Random _om = new Random(Rand.seed());

    private static byte index(int[] colors, int p) {
        for(int i=0;i<colors.length;i++) {
            if(colors[i]==p) {
                return (byte) i;
            }
        }
        throw new IllegalArgumentException("no such color "+p+" in "+Arrays.toString(colors));
    }

    public Rule1D(Ruleset origin, int[] colors, int[][] patterns, int background) {
        super(patterns.length*patterns[0].length, 1);
        _origin = origin;
        _len = patterns[0].length;
        _colors = colors;
        _patterns = patterns;
        _background = background;
        int i = 0;
        long seed = patterns.length;
        //StringBuilder str = new StringBuilder();
        //try {
            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //GZIPOutputStream gos = new GZIPOutputStream(bos, 16384);
            //DataOutputStream dos = new DataOutputStream(gos);
            //dos.writeByte(dimensions());
            //dos.writeByte(_colors.length);
            //for(int color:_colors) {
                //dos.writeInt(color);
            //}
            //dos.writeByte(index(_colors, _background));
//
            //dos.writeInt(patterns.length);
            //dos.writeInt(patterns[0].length);
            for(int[] p:patterns) {
                if(p.length!=_len) {
                    throw new IllegalArgumentException("length mismatch");
                }
                for(int j=0;j<p.length;j++) {
                    set(i++, 0, p[j]);
                    seed = Long.rotateLeft(seed, 1);
                    seed ^= p[j];
                    //dos.writeByte(index(_colors, p[j]));
                }
            }
            _bgr = background;
            _sel = new TroveSelector(_patterns, _colors);
            _seed = seed;
            //dos.close();
            //_binary = bos.toByteArray();
            //_name = Base64.encodeBytes(_binary);
        //}
        //catch(IOException e) {
            //throw new Error(e);
        //}
    }

    public void setFabric(Fabric f) {
        _fabric = f;
    }

    public void setRandom(Random om) {
        _om = om;
    }

    public void setEvaporate(boolean evap) {
        _evaporate = evap;
    }

    public void setPhaseTransition(boolean phase) {
        _phaseTransition = phase;
    }

    public void setWhimsical(boolean whimsical) {
        _whimsical = whimsical;
    }

    public void setMask(BitSet mask) {
        _mask = mask;
        _useMask = _mask!=null;
        //Thread.currentThread().dumpStack();
    }

    public BitSet getMask() {
        return _mask;
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

    public void setInterceptor(Interceptor i) {
        _interceptor = i;
    }

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

    public int getSuggestedInterval(CA c) {
        return c.getHeight();
    }

    public Ruleset origin() {
        return _origin;
    }

    protected Selector selector() {
        if(_sel==null) {
            _sel = new TroveSelector(_patterns, _colors);
        }
        return _sel;
    }

    public int[][] toPattern() {
        int[][] ps = new int[getWidth()/_len][_len];
        int pos = 0;
        for(int i=0;i<ps.length;i++) {
            ps[i] = new int[_len];
            for(int j=0;j<ps[i].length;j++) {
                ps[i][j] = get(pos, 0);
                pos++;
            }
        }
        return ps;
        //return (int[][]) deepCopy(_patterns);
        //return _patterns;
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
        //return toString().hashCode();
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

    public static Rule fromIncantation(String s) {
        Codec c = Codec.get();
        return fromBytes(c.decode(s));
    }

    public static Rule fromString(String s) {
        byte[] bytes = Base64.decode(s);
        return fromBytes(bytes);
    }

    public static Rule fromBytes(byte[] bytes) {
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            int dims = dis.readByte();
            int cols = dis.readByte();
            int[] colors = new int[cols];
            for(int i=0;i<colors.length;i++) {
                colors[i] = dis.readInt();
            }
            int bgr = colors[dis.readByte()];
            int plen = dis.readInt();
            int p0len = dis.readInt();
            int[][] patterns = new int[plen][p0len];
            for(int i=0;i<patterns.length;i++) {
                for(int j=0;j<patterns[i].length;j++) {
                    patterns[i][j] = colors[dis.readByte()];
                }
            }
            Rule r = null;
            switch(dims) {
                case 1:
                    r = new Rule1D(null, colors, patterns, bgr);
                    break;
                case 2:
                    r = new Rule2D(null, colors, patterns, bgr);
                    break;
                default:
                    throw new IllegalArgumentException("unsupported dimensionality: "+dims);
            }
            ((Rule1D)r)._options.read(dis);
            return r;
        }
        catch(IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Rule1D ofromString(int[] colors, int bgr, int width, String s) {
        System.err.println("str: "+s);
        //long i = Long.parseLong(s);
        //String ex = Long.toString(i, colors.length);
        BigInteger i = new BigInteger(s);
        String ex = i.toString(colors.length);
        System.err.println("ex: "+ex);
        int[][] ps = archetype(colors, width);
        while(ex.length()<ps.length) {
            ex = "0"+ex;
        }
        int pos = 0;
        for(int[] p:ps) {
            p[p.length-1] = colors[Integer.parseInt(""+ex.charAt(pos++))];
        }
        return new Rule1D(null, colors, ps, bgr);
    }

    public Rule mutate(Mutator m) throws MutationFailedException {
        return m.mutate(this);
    }

    public void init(CA c, Initialization in) {
        switch(in) {
            case single:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[0]);
                }
                c.set(c.getWidth()/2, 0, _colors[_om.nextInt(_colors.length-1)+1]);
                break;
            case random:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[_om.nextInt(_colors.length)]);
                }
                break;
            default:
        }
    }

    public void uncache() {
        _next=null;
    }

    public float generate(CA c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
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
        Selector sel = selector();
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
            boolean expandMask = false;
            boolean lastExpanded = false;
            long spaceCount = 0;
            for(int i=0;i<_next.length;i++) {
                if(_useMask) {
                    if(!_mask.get(i)) {
                        //System.err.println(toSeed()+" masked for "+i);
                        _next[i] = _existing[i];
                        continue;
                    }
                    if(lastExpanded) {
                        lastExpanded = false;
                        _next[i] = _existing[i];
                        //System.err.println("lastExpanded for "+i);
                        continue;
                    }
                    int before = i-1, after = i+1;
                    if(before==-1) before = _next.length-1;
                    if(after==_next.length) after = 0;
                    if(!_mask.get(before)) {
                        _mask.set(before);
                        expandMask = true;
                    }
                    if(!_mask.get(after)) {
                        _mask.set(after);
                        expandMask = true;
                        lastExpanded = true;
                    }
                }
                int colidx = sel.next(_row, i);
                if(colidx==-1) {
                    // rule cannot exist in this space
                    // use existing value (possibly from another rule)
                    //if(_debug&&!printed) { System.err.println("no space for rule "+toSeed()+" at "+i); printed=true; }
                    if(i==_debugColumn) {
                        System.err.println(toSeed()+" skipping, over="+over+", prev alpha: "+CA.alpha(_row[i+offset])+" bgra="+CA.alpha(_bgr)+" exalpha: "+CA.alpha(_existing[i]));
                    }
                    _next[i] = over?combineAlpha(_row[i+offset], _bgr):_existing[i];
                    if(_next[i]==0) {
                        _next[i] = _bgr;
                    }
                    failed++;
                    continue;
                }
                spaceCount++;
                _totals[colidx]++;
                if(i==_debugColumn) {
                    System.err.println(toSeed()+" prev alpha: "+CA.alpha(_row[i+offset])+" colalpha: "+CA.alpha(_colors[colidx]));
                }
                if(_existing[i]==_bgr||_existing[i]==0) {
                    if(i%100==0) System.err.print(_existing[i]==_bgr?"b":"z");
                    _next[i] = combineAlpha(_row[i+offset], _colors[colidx]);
                }
                else {
                    if(i%100==0) System.err.print("H");
                    if(Rand.om.nextInt(100)>=50) {
                        _next[i] = combineAlpha(_row[i+offset], _colors[colidx]);
                    }
                }
                //if(!printed2) { System.err.println(toSeed()+" alpha: "+CA.alpha(_next[i])); printed2=true; }
                if(CA.alpha(_next[i])<129) {
                    //if(!printed2) { System.err.println("destabilizing"); printed2=true; }
                    if(_fabric!=null) {
                        _fabric.destabilized(new VacuumMetastabilityDisaster(this, i, crow));
                    }
                }
                if(same&&_next[i]!=_row[i+offset]) {
                    if(_haslast&&_last[i+offset]!=_next[i]) {
                        same = false;
                    }
                }
            }
            //System.err.println(toSeed()+" spaceCount: "+spaceCount);
            if(_useMask&&!expandMask) {
                _useMask = false; // field is everywhere, no longer needed
                if(Options.instantaneousMaskReset.get()) {
                    _mask = null;
                }
            }
            if(spaceCount==0&&_mask==null) {
                //evaporated = true;
                if(_fabric!=null) {
                    _fabric.evaporated(this);
                }
            }
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
        //System.err.println("diff="+diff+", /wid="+(diff/c.getWidth()/_colors.length/_len));
        //return diff/c.getWidth()/_colors.length/_len < 15;
        //c.setGoodness(diff/c.getWidth()/_colors.length/_len);
        //return diff/c.getWidth()/_colors.length/_len;
        //System.err.println("diff="+diff);
        c.setGoodness(diff);
        //System.err.println(toSeed()+": minAlpha: "+_minAlpha);
        return diff;
        //return diff<10;
    }

    private int _minAlpha = Integer.MAX_VALUE;
    private int combineAlpha(int a, int b) {
        int aa = CA.alpha(a);
        int ab = CA.alpha(b);
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
        a = (b&CA.COLOR_MASK)|(an<<24);
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

    protected Selector compile(int[][] patterns) {
        Level root = new Level(0);
        for(int[] p:patterns) {
            Level lev = root;
            for(int i=0;i<p.length-1;i++) {
                int sw = p[i];
                Level ch = lev.getChild(sw);
                if(ch==null) {
                    ch = new Level(sw);
                    lev.addChild(ch);
                }
                lev = ch;
            }
            //lev.val(p[p.length-1]);
            lev.val(colorIndexOf(p[p.length-1]));
        }
        StringBuilder bigsw = new StringBuilder("{ ");
        buildSwitch(bigsw, root, 0);
        //bigsw.append("throw new Error(\"unmatched pattern '\"+java.util.Arrays.toString($1)+\"' at \"+$2); }");
        bigsw.append("return -1; }");
        //System.err.println(bigsw.toString());
        try {
            ClassPool p = ClassPool.getDefault();
            CtClass sel = p.get(Selector.class.getName());
            CtClass s = p.makeClass("rule"+_unique);
            s.addInterface(sel);
            CtMethod n = new CtMethod(CtClass.intType, "next", new CtClass[]{p.get("int[]"), p.get("int")}, s);
            n.setBody(bigsw.toString());
            n.setModifiers(Modifier.PUBLIC);
            s.addMethod(n);
            _unique++;
            return (Selector) s.toClass().newInstance();
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    private void buildSwitch(StringBuilder bigsw, Level lev, int i) {
        if(lev.isLeaf()) {
            bigsw.append("return "+lev.val()+";\n");
        }
        else {
            bigsw.append("switch($1[$2+"+i+"]) {\n");
            for(Level ch:lev.getChildren()) {
                bigsw.append("case "+ch.sw()+":\n");
                buildSwitch(bigsw, ch, i+1);
            }
            bigsw.append("}\n");
        }
    }

    protected Selector compile2(int[][] patterns) {
        Level root = new Level(0);
        for(int[] p:patterns) {
            Level lev = root;
            for(int i=0;i<p.length-1;i++) {
                int sw = p[i];
                Level ch = lev.getChild(sw);
                if(ch==null) {
                    ch = new Level(sw);
                    lev.addChild(ch);
                }
                lev = ch;
            }
            //lev.val(p[p.length-1]);
            lev.val(colorIndexOf(p[p.length-1]));
        }
        try {
            ClassPool p = ClassPool.getDefault();
            CtClass sel = p.get(Selector.class.getName());
            CtClass s = p.makeClass("rule"+_unique);
            s.addInterface(sel);
            _unique++;
            buildSwitchMethod("next", s, root, 0, new Unique());
            return (Selector) s.toClass().newInstance();
        }
        catch(Exception e) {
            throw new Error(e);
        }

    }

    private void buildSwitchMethod(String mname, CtClass cl, Level lev, int i, Unique u) {
        StringBuilder bigsw = new StringBuilder(128);
        bigsw.append("{ ");
        if(lev.isLeaf()) {
            bigsw.append("return ");
            bigsw.append(lev.val());
            bigsw.append(";\n");
        }
        else {
            bigsw.append("switch($1[$2+");
            bigsw.append(i);
            bigsw.append("]) {\n");
            for(Level ch:lev.getChildren()) {
                bigsw.append("case ");
                bigsw.append(ch.sw());
                bigsw.append(":\n");
                if(ch.isLeaf()) {
                    bigsw.append("return ");
                    bigsw.append(ch.val());
                    bigsw.append(";\n");
                }
                else {
                    bigsw.append("switch($1[$2+"+(i+1)+"]) {\n");
                    for(Level ch2:ch.getChildren()) {
                        bigsw.append("case "+ch2.sw()+":\n");
                        if(ch2.isLeaf()) {
                            bigsw.append("return "+ch2.val()+";\n");
                        }
                        else {
                            bigsw.append("return sel");
                            bigsw.append(u.unique(ch2));
                            bigsw.append("($1, $2);");
                            buildSwitchMethod(null, cl, ch2, i+2, u);
                        }
                    }
                    bigsw.append("}\n");
                }
                /*
                else {
                    bigsw.append("return sel");
                    bigsw.append(u.unique(ch));
                    bigsw.append("($1, $2);");
                    buildSwitchMethod(null, cl, ch, i+1, u);
                }
                */
            }
            bigsw.append("}\n");
        }
        bigsw.append("return -1; }");
        if(mname==null) {
            mname = "sel"+u.unique(lev);
        }
        //System.err.println("============================");
        //System.err.println( "generated: "+mname+":\n"+bigsw);
        //long start = System.currentTimeMillis();
        try {
            ClassPool p = ClassPool.getDefault();
            CtMethod m = new CtMethod(CtClass.intType, mname, new CtClass[]{p.get("int[]"), p.get("int")}, cl);
            m.setBody(bigsw.toString());
            m.setModifiers(Modifier.PUBLIC);
            cl.addMethod(m);
        }
        catch(Exception e) {
            throw new Error(e);
        }
        //long end = System.currentTimeMillis();
        //System.err.println("method compile time: "+(end-start)+" ("+bigsw.length()+")");
    }

    private static class Unique {
        private int _n = 0;
        private Map<Object,Integer> _u = new HashMap<Object,Integer>();

        public Unique() {
        }

        public int unique(Object o) {
            Integer i = _u.get(o);
            if(i==null) {
                i = new Integer(_n++);
                _u.put(o, i);
            }
            return i.intValue();
        }
    }

    private static class Level {
        private int _sw;
        private int _val;
        private boolean _hasVal = false;
        private List<Level> _sub;


        public Level(int sw) {
            _sw = sw;
        }

        public Level(int sw, int val) {
            _sw = sw;
            _val = val;
        }

        public boolean isLeaf() {
            return _sub==null;
        }

        public int sw() {
            return _sw;
        }

        public void val(int v) {
            if(_hasVal) {
                throw new IllegalStateException("duplicate pattern "+this+": "+v);
            }
            _val = v;
            _hasVal = true;
        }

        public int val() {
            return _val;
        }

        public int numChildren() {
            return _sub!=null?_sub.size():0;
        }

        public Level getChild(int sw) {
            if(_sub!=null) {
                for(Level lev:_sub) {
                    if(lev.sw()==sw) {
                        return lev;
                    }
                }
            }
            return null;
        }

        public List<Level> getChildren() {
            return _sub;
        }

        public void addChild(Level lev) {
            if(_sub==null) {
                _sub = new ArrayList<Level>();
            }
            _sub.add(lev);
        }

        public String toString() {
            String s = _sw+"";
            if(_sub!=null) {
                s += " "+_sub;
            }
            return s;
        }
    }

    /*
    private int[] column(int column) {
        int[] col = new int[_patterns.length];
        for(int i=0;i<_patterns.length;i++) {
            col[i] = _patterns[i][column];
        }
        return col;
    }
    */

    public interface Selector {
        int next(int[] input, int offset);
    }

    public static final int get(CA c, int x, int y) {
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
            return c.get(x, y);
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("coordinate out of range: ("+x+","+y+")");
        }
    }

    public static final void set(CA c, int x, int y, int v) {
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
            c.set(x, y, v);
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("coordinate out of range: ("+x+","+y+")");
        }
    }
}
