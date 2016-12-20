package org.excelsi.nausicaa.ca;


import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.excelsi.rlyehian.Codec;


public final class CA {
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
    private Rule _r;
    private Palette _p;
    private Initializer _i;
    private int _w;
    private int _h;
    private int _d;
    private Random _rand;
    private long _seed;
    private static final byte VERSION = 1;


    public CA(Rule r, Palette p, Initializer i, Random rand, long seed, int w, int h, int d) {
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
    }

    public Archetype archetype() {
        return _r.archetype();
    }

    public Plane createPlane() {
        return createBufferedImagePlane();
        //return createWritableImagePlane();
    }

    public Plane pooledPlane(Plane p) {
        return populatePlane(p);
    }

    private Plane createBufferedImagePlane() {
        BufferedImagePlane p = new BufferedImagePlane(this, _w, _h, _p);
        return populatePlane(p);
    }

    private Plane populatePlane(Plane p) {
        _rand.setSeed(_seed);
        _i.init(p, _r, _rand);
        //if(_r instanceof Multirule1D) {
            //((Multirule1D)_r).generate2(p, 1, _h, false, true, null);
        //}
        //else {
            switch(_r.dimensions()) {
                case 1:
                    _r.generate(p, 1, _h, POOL, false, true, null);
                    break;
                case 2:
                default:
                    _r.generate(p, 1, _d, POOL, false, true, null);
                    break;
            }
        //}
        return p;
    }

    private Plane createWritableImagePlane() {
        WritableImagePlane p = new WritableImagePlane(this, _w, _h, _p);
        populatePlane(p);
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

    public void resize(int w, int h) {
        _w = w;
        _h = h;
    }

    public void reseed(long seed) {
        _seed = seed;
    }

    public CA mutate(Rule r, Random om) {
        return new CA(r, _p.matchCapacity(r.colorCount(), om), _i, branchRandom(), _seed, _w, _h, _d);
    }

    public CA size(int w, int h) {
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, _d);
    }

    public CA size(int w, int h, int d) {
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, d);
    }

    public CA seed() {
        return seed(_rand.nextLong());
    }

    public CA seed(long seed) {
        return new CA(_r, _p, _i, branchRandom(), seed, _w, _h, _d);
    }

    public CA palette(Palette p) {
        return new CA(_r, p, _i, branchRandom(), _seed, _w, _h, _d);
    }

    public CA initializer(Initializer i) {
        return new CA(_r, _p, i, branchRandom(), _seed, _w, _h, _d);
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

    public void save(String filename) throws IOException {
        if(!filename.endsWith(".ca.gz")) {
            filename = filename+".ca.gz";
        }
        try(DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))))) {
            write(dos);
            dos.close();
        }
    }

    public static CA fromFile(String filename) throws IOException {
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
            return new CA(r, p, i, new Random(), seed, w, h, 10);
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

    public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(VERSION);
        dos.writeInt(_w);
        dos.writeInt(_h);
        dos.writeLong(_seed);
        _p.write(dos);
        _i.write(dos);
        _r.write(dos);
    }

    @Override public String toString() {
        return "ca::{w:"+_w+", h:"+_h+", c:"+_r.archetype().colors()+"}";
    }

    private Random branchRandom() {
        return new Random();
    }
}
