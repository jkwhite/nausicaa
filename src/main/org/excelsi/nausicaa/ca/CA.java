package org.excelsi.nausicaa.ca;


import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import java.awt.image.*;
import org.excelsi.rlyehian.Codec;


public final class CA {
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    private Rule _r;
    private Palette _p;
    private Initializer _i;
    private int _w;
    private int _h;
    private int _d;
    private int _prelude;
    private int _coda;
    private Random _rand;
    private long _seed;
    private float _weight;
    private ComputeMode _cmode;
    private UpdateMode _umode;
    private static final byte VERSION = 5;


    public CA(Rule r, Palette p, Initializer i, Random rand, long seed, int w, int h, int d, int prelude, float weight, int coda, ComputeMode cmode, UpdateMode umode) {
        if(i==null) {
            throw new IllegalArgumentException("null initializer");
        }
        _r = r;
        _p = p;
        _i = i;
        _rand = rand;
        _seed = seed;
        _w = w;
        _h = h;
        _d = d;
        _prelude = prelude;
        _weight = weight;
        _coda = coda;
        _cmode = cmode;
        _umode = umode;
    }

    public Archetype archetype() {
        return _r.archetype();
    }

    public Plane createPlane() {
        return createPlane(POOL, new GOptions(true, 1, 0, 1f));
    }

    public Plane createPlane(ExecutorService pool, GOptions opt) {
        if(archetype().dims()==3) {
            //BlockPlane p = new BlockPlane(this, getWidth(), getHeight(), getDepth(), _p, BlockPlane.Mode.argb);
            Plane p = new IntBlockPlane(this, getWidth(), getHeight(), getDepth(), _p);
            p = populatePlane(p, pool, opt);
            return p;
        }
        else {
            if(true||_p.getColorCount()>=127) {
                Plane p = new IntBlockPlane2d(this, getWidth(), getHeight(), 1, _p);
                p = populatePlane(p, pool, opt);
                return p;
            }
            else {
                return createBufferedImagePlane(pool, opt);
                //return createWritableImagePlane(pool, opt);
            }
        }
    }

    public Plane pooledPlane(Plane p, ExecutorService pool, GOptions opt) {
        return populatePlane(p, pool, opt);
    }

    private Plane createBufferedImagePlane(ExecutorService pool, GOptions opt) {
        BufferedImagePlane p = new BufferedImagePlane(this, _w, _h, _p);
        return populatePlane(p, pool, opt);
    }


    private Plane populatePlane(Plane p, ExecutorService pool, GOptions opt) {
        _rand.setSeed(_seed);
        _i.init(p, _r, _rand);
        //if(_r instanceof Multirule1D) {
            //((Multirule1D)_r).generate2(p, 1, _h, false, true, null);
        //}
        //else {
            switch(_r.dimensions()) {
                case 1:
                    _r.generate(p, 1, _h, pool, false, true, null, opt);
                    break;
                case 3:
                    System.err.println("generating for "+_prelude);
                    final long st = System.currentTimeMillis();
                    _r.generate(p, 1, _prelude, pool, false, true, null, opt);
                    final long en = System.currentTimeMillis();
                    System.err.println("generation took "+(en-st)+" millis");
                    break;
                case 2:
                default:
                    //_r.generate(p, 1, _d, pool, false, true, null, opt);
                    _r.generate(p, 1, _prelude, pool, false, true, null, opt);
                    if(_d>1) {
                        p = _r.generate(p, 1, _d, pool, false, true, null, opt.higherDim(_d));
                        //System.err.println("****** expanding dimension result: "+p);
                    }
                    break;
            }
        //}
        return p;
    }

    private Plane createWritableImagePlane(ExecutorService pool, GOptions opt) {
        WritableImagePlane p = new WritableImagePlane(this, _w, _h, _p);
        populatePlane(p, pool, opt);
        /*
        _rand.setSeed(_seed);
        _i.init(p, _r, _rand);
        if(_r instanceof Multirule1D) {
            ((Multirule1D)_r).generate2(p, 1, _h, false, true, null);
        }
        else {
            _r.generate(p, 1, _h, POOL, false, true, null);
        }
        */
        return p;
    }

    public Rule getRule() {
        return _r;
    }

    public Palette getPalette() {
        return _p;
    }

    public Random getRandom() {
        return _rand;
    }

    public long getSeed() {
        return _seed;
    }

    public int getWidth() {
        return _w;
    }

    public int getHeight() {
        return _h;
    }

    public int getDepth() {
        return _d;
    }

    public int getPrelude() {
        return _prelude;
    }

    public int getCoda() {
        return _coda;
    }

    public float getWeight() {
        return _weight;
    }

    public ComputeMode getComputeMode() {
        return _cmode;
    }

    public UpdateMode getUpdateMode() {
        return _umode;
    }

    public void resize(int w, int h) {
        _w = w;
        _h = h;
    }

    public void reseed(long seed) {
        _seed = seed;
    }

    public CA mutate(Rule r, Random om) {
        return new CA(r, _p.matchCapacity(r.colorCount(), om), _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA size(int w, int h) {
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA size(int w, int h, int d) {
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA size(int w, int h, int d, int prelude) {
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, d, prelude, _weight, _coda, _cmode, _umode);
    }

    public CA copy() {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA seed() {
        return seed(_rand.nextLong());
    }

    public CA seed(long seed) {
        return new CA(_r, _p, _i, branchRandom(), seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA palette(Palette p) {
        return new CA(_r, p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA initializer(Initializer i) {
        return new CA(_r, _p, i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode);
    }

    public CA prelude(int pre) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, pre, _weight, _coda, _cmode, _umode);
    }

    public CA coda(int coda) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, coda, _cmode, _umode);
    }

    public CA weight(float weight) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, weight, _coda, _cmode, _umode);
    }

    public CA computeMode(ComputeMode cmode) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, cmode, _umode);
    }

    public CA updateMode(UpdateMode umode) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, umode);
    }

    public Initializer getInitializer() {
        return _i;
    }

    public void setInitializer(Initializer i) {
        if(i==null) {
            throw new IllegalArgumentException("null initializer");
        }
        _i = i;
    }

    public byte[] toBinary() {
        // version width height seed palette initializer rule 
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            write(dos);
            return bos.toByteArray();
        }
        catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String toBase64() {
        return Base64.encodeObject(toBinary());
    }

    public String toIncantation() {
        Codec c = Codec.get();
        return c.encode(toBinary());
    }

    public void save(String filename, String format) throws IOException {
        if("binary".equals(format)) {
            if(!filename.endsWith(".ca.gz")) {
                filename = filename+".ca.gz";
            }
            try(DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))))) {
                write(dos);
            }
        }
        else if("text".equals(format)) {
            OutputStream os = null;
            if(filename.endsWith(".ca")) {
                os = new BufferedOutputStream(new FileOutputStream(filename));
            }
            else if(filename.endsWith(".ca.gz")) {
                os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            }
            else {
                filename = filename+".ca";
                os = new BufferedOutputStream(new FileOutputStream(filename));
            }
            try(PrintWriter pw=new PrintWriter(new OutputStreamWriter(os))) {
                write(pw);
                if(pw.checkError()) {
                    throw new IOException("Failed to save "+filename);
                }
            }
        }
    }

    public static CA fromImage(String filename, String paletteMode) throws IOException {
        BufferedImage i = ImageIO.read(new File(filename));
        Palette p;
        switch(paletteMode) {
            case "rgb":
                p = new RGBPalette();
                break;
            case "rgba":
                p = new RGBAPalette();
                break;
            case "indexed":
            default:
                p = Palette.fromImage(i);
        }
        int w = i.getWidth();
        int h = i.getHeight();
        int d = 0;
        int colors = p.getColorCount();
        int size = 1;
        int dims = 2;
        Archetype a = new Archetype(dims, size, colors);
        Ruleset rs = new ComputedRuleset(a);
        Random rand = new Random();
        Rule rule = rs.random(rand).next();
        ImageInitializer init = new ImageInitializer(new File(filename));
        CA ca = new CA(rule, p, init, rand, 0, w, h, d, 0, 1f, 0, ComputeMode.combined, new UpdateMode.SimpleSynchronous());
        return ca;
    }

    public static CA fromFile(String filename, String format) throws IOException {
        if("binary".equals(format)) {
            return fromBinaryFile(filename);
        }
        else {
            return fromTextFile(filename);
        }
    }

    private static CA fromBinaryFile(String filename) throws IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filename));
            if(filename.endsWith(".gz")) {
                in = new GZIPInputStream(in);
            }
            DataInputStream dis=new DataInputStream(in);
            byte version = dis.readByte();
            int w = dis.readInt();
            int h = dis.readInt();
            long seed = dis.readLong();
            Palette p = Palette.read(dis);
            Initializer i = Initializers.read(dis);
            Rule r = new IndexedRuleReader(dis).read();
            return new CA(r, p, i, new Random(), seed, w, h, 10, 10, 1f, 0, ComputeMode.combined, new UpdateMode.SimpleSynchronous());
        }
        finally {
            if(in!=null) {
                try {
                    in.close();
                }
                catch(IOException e) {
                }
            }
        }
    }
    
    static private class Header {
        int version;
        int w;
        int h;
        int d;
        int prelude;
        long seed;
        float weight;
        int coda;
        ComputeMode computeMode;
    }

    private static CA fromTextFile(String filename) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            if(filename.endsWith(".gz")) {
                in = new GZIPInputStream(in);
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            final Header h = readHeader(r);
            Palette p = null;
            Initializer i = null;
            Rule rule = null;
            String line;
            while((line=r.readLine())!=null) {
                switch(line) {
                    case "palette {":
                        p = Palette.read(r, h.version);
                        break;
                    case "initializer {":
                        i = Initializers.read(r, h.version);
                        break;
                    case "rule {":
                        rule = new ComputedRuleReader(r, h.version).readRule();
                        break;
                    default:
                        throw new IOException("unknown section '"+line+"'");
                }
                line = r.readLine();
                if(!"}".equals(line)) {
                    throw new IOException("did not find end of section marker: '"+line+"'");
                }
            }
            if(p==null) {
                throw new IOException("missing palette");
            }
            if(rule==null) {
                throw new IOException("missing rule");
            }
            if(i==null) {
                throw new IOException("missing initializer");
            }
            return new CA(rule, p, i, new Random(), h.seed, h.w, h.h, h.d, h.prelude, h.weight, h.coda, ComputeMode.combined, new UpdateMode.SimpleSynchronous());
        }
        finally {
            if(in!=null) {
                try {
                    in.close();
                }
                catch(IOException e) {
                }
            }
        }
    }

    private static Header readHeader(BufferedReader r) throws IOException {
        Header h = new Header();
        String line = r.readLine();
        if(!"ca {".equals(line)) {
            throw new IOException("corrupt header; not a CA: '"+line+"'");
        }
        h.version = Integer.parseInt(r.readLine());
        switch(h.version) {
            case 1:
                h.w = Integer.parseInt(r.readLine());
                h.h = Integer.parseInt(r.readLine());
                h.d = Integer.parseInt(r.readLine());
                h.prelude = Integer.parseInt(r.readLine());
                h.seed = Long.parseLong(r.readLine());
                h.weight = 1f;
                h.coda = 0;
                break;
            case 2:
                h.w = Integer.parseInt(r.readLine());
                h.h = Integer.parseInt(r.readLine());
                h.d = Integer.parseInt(r.readLine());
                h.prelude = Integer.parseInt(r.readLine());
                h.seed = Long.parseLong(r.readLine());
                h.weight = Float.parseFloat(r.readLine());
                h.coda = 0;
                break;
            case 3:
                h.w = Integer.parseInt(r.readLine());
                h.h = Integer.parseInt(r.readLine());
                h.d = Integer.parseInt(r.readLine());
                h.prelude = Integer.parseInt(r.readLine());
                h.seed = Long.parseLong(r.readLine());
                h.weight = Float.parseFloat(r.readLine());
                h.coda = Integer.parseInt(r.readLine());
                h.computeMode = ComputeMode.combined;
                break;
            case 4:
            case 5:
                h.w = Integer.parseInt(r.readLine());
                h.h = Integer.parseInt(r.readLine());
                h.d = Integer.parseInt(r.readLine());
                h.prelude = Integer.parseInt(r.readLine());
                h.seed = Long.parseLong(r.readLine());
                h.weight = Float.parseFloat(r.readLine());
                h.coda = Integer.parseInt(r.readLine());
                h.computeMode = ComputeMode.from(r.readLine());
                break;
            default:
                throw new IOException("unsupported version "+h.version);
        }
        line = r.readLine();
        if(!"}".equals(line)) {
            throw new IOException("corrupt trailer; not a CA: "+line);
        }
        return h;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(VERSION);
        dos.writeInt(_w);
        dos.writeInt(_h);
        dos.writeLong(_seed);
        _p.write(dos);
        _i.write(dos);
        _r.write(dos);
    }

    public void write(PrintWriter w) throws IOException {
        w.println("ca {");
        w.println(VERSION);
        w.println(_w);
        w.println(_h);
        w.println(_d);
        w.println(_prelude);
        w.println(_seed);
        w.println(_weight);
        w.println(_coda);
        w.println(_cmode);
        w.println("}");
        w.println("palette {");
        _p.write(w);
        w.println("}");
        w.println("initializer {");
        _i.write(w);
        w.println("}");
        w.println("rule {");
        new ComputedRuleWriter(w).writeRule(_r);
        w.println("}");
    }

    @Override public String toString() {
        return "ca::{w:"+_w+", h:"+_h+", c:"+_r.archetype().colors()+"}";
    }

    private Random branchRandom() {
        return new Random();
    }
}
