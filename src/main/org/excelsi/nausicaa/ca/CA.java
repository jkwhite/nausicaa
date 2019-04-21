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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import java.awt.image.*;
//import org.excelsi.rlyehian.Codec;
import com.google.gson.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public final class CA {
    private static final Logger LOG = LoggerFactory.getLogger(CA.class);
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
    private double _weight;
    private ComputeMode _cmode;
    private UpdateMode _umode;
    private EdgeMode _emode;
    private ExternalForce _ef;
    private Varmap _vars;
    private CA _meta;
    private static final byte VERSION = 6;


    public CA(Rule r,
            Palette p,
            Initializer i,
            Random rand,
            long seed,
            int w,
            int h,
            int d,
            int prelude,
            double weight,
            int coda,
            ComputeMode cmode,
            UpdateMode umode,
            EdgeMode emode,
            ExternalForce ef,
            Varmap vars,
            CA meta) {
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
        _emode = emode;
        _ef = ef;
        _meta = meta;
        //System.err.println("rule: "+r);
        //System.err.println("rule vars: "+r.vars());
        //System.err.println("inherit vars: "+vars);
        _vars = r.vars().merge(vars);
        //System.err.println("vars: "+_vars);
    }

    public Archetype archetype() {
        return _r.archetype();
    }

    public Plane createPlane() {
        return createPlane(POOL, new GOptions(true, 1, 0, 1f));
    }

    public Plane createPlane(ExecutorService pool, GOptions opt) {
        if(archetype().isContinuous()) {
            if(_meta!=null) {
                return createMetaPlane(pool, opt);
            }
            else {
                Plane p = new FloatBlockPlane(this, getWidth(), getHeight(), getDepth(), _p, _emode.floatOobValue());
                return populatePlane(p, pool, opt);
            }
        }
        else if(archetype().dims()==3) {
            //BlockPlane p = new BlockPlane(this, getWidth(), getHeight(), getDepth(), _p, BlockPlane.Mode.argb);
            Plane p = new IntBlockPlane(this, getWidth(), getHeight(), getDepth(), _p, _emode.intOobValue());
            p = populatePlane(p, pool, opt);
            return p;
        }
        if(archetype().dims()==2) {
            if(_meta!=null) {
                return createMetaPlane(pool, opt);
            }
            else {
                Plane p = new IntBlockPlane2d(this, getWidth(), getHeight(), 1, _p, _emode.intOobValue());
                p = populatePlane(p, pool, opt);
                return p;
            }
        }
        else {
            if(_meta!=null) {
                return createMetaPlane(pool, opt);
            }
            else if(true||_p.getColorCount()>=127) {
                Plane p = new IntBlockPlane2d(this, getWidth(), getHeight(), 1, _p, _emode.intOobValue());
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

    private Plane createMetaPlane(ExecutorService pool, GOptions opt) {
        List<CA> chain = buildCAChain();
        LOG.debug("creating meta plane over chain "+chain);
        Plane[] ps = new Plane[chain.size()];
        for(int i=0;i<ps.length;i++) {
            CA ca = chain.get(i);
            if(ca.archetype().isContinuous()) {
                ps[i] = new FloatBlockPlane(ca, ca.getWidth(), ca.getHeight(), 1 /*ca.getDepth()*/, ca.getPalette(), ca.getEdgeMode().floatOobValue());
            }
            else {
                ps[i] = new IntBlockPlane2d(ca, ca.getWidth(), ca.getHeight(), 1 /*ca.getDepth()*/, ca.getPalette(), ca.getEdgeMode().intOobValue());
            }
        }
        CompositePlane p;
        if(archetype().isContinuous()) {
            p = new CompositeFloatPlane(ps);
        }
        else {
            p = new CompositeIntPlane(ps);
        }
        //CompositeIntPlane p = new CompositeIntPlane(ps);
        for(int i=0;i<ps.length;i++) {
            p.setReadDepth(i);
            p.setWriteDepth(i);
            chain.get(i).populatePlane0(p, pool, opt);
        }
        p.setReadDepth(0);
        p.setWriteDepth(0);
        return populatePlane1(p, pool, opt);
    }

    private Plane createBufferedImagePlane(ExecutorService pool, GOptions opt) {
        BufferedImagePlane p = new BufferedImagePlane(this, _w, _h, _p, _emode.oobValue());
        return populatePlane(p, pool, opt);
    }

    public Rule compileRule() {
        List<CA> chain = buildCAChain();
        if(chain.size()==1) {
            return chain.get(0).getRule();
        }
        else {
            return new CompositeRule(chain
                .stream()
                .map((c)->{return c.getRule();})
                .collect(Collectors.toList()).toArray(new Rule[0]));
        }
    }

    private List<CA> buildCAChain() {
        List<CA> chain = new ArrayList<>();
        CA n = this;
        while(n!=null) {
            if(chain.contains(n)) {
                throw new IllegalStateException("strange loop: "+chain);
            }
            chain.add(n);
            n = n.getMeta();
        }
        return chain;
    }

    private Plane populatePlane(Plane p, ExecutorService pool, GOptions opt) {
        return populatePlane1(populatePlane0(p, pool, opt), pool, opt);
    }

    private Plane populatePlane0(Plane p, ExecutorService pool, GOptions opt) {
        _rand.setSeed(_seed);
        _i.init(p, _r, _rand);
        return p;
    }

    private Plane populatePlane1(Plane p, ExecutorService pool, GOptions opt) {
        //_rand.setSeed(_seed);
        //_i.init(p, _r, _rand);
        Rule r = compileRule();
        switch(r.dimensions()) {
            case 1:
                r.generate(p, 1, _h, pool, false, true, null, opt);
                break;
            case 3:
                LOG.debug("prelude generating for "+_prelude);
                final long st = System.currentTimeMillis();
                r.generate(p, 1, _prelude, pool, false, true, null, opt);
                final long en = System.currentTimeMillis();
                LOG.debug("prelude generation took "+(en-st)+" millis");
                break;
            case 2:
            default:
                LOG.debug("prelude generating for "+_prelude);
                final long st2 = System.currentTimeMillis();
                r.generate(p, 1, _prelude, pool, false, true, null, opt);
                if(_d>1) {
                    p = r.generate(p, 1, _d, pool, false, true, null, opt.higherDim(_d));
                    //System.err.println("****** expanding dimension result: "+p);
                }
                final long en2 = System.currentTimeMillis();
                LOG.debug("prelude generation took "+(en2-st2)+" millis");
                break;
        }
        return p;
    }

    private Plane createWritableImagePlane(ExecutorService pool, GOptions opt) {
        WritableImagePlane p = new WritableImagePlane(this, _w, _h, _p, _emode.oobValue());
        populatePlane(p, pool, opt);
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

    public double getWeight() {
        return _weight;
    }

    public ComputeMode getComputeMode() {
        return _cmode;
    }

    public UpdateMode getUpdateMode() {
        return _umode;
    }

    public EdgeMode getEdgeMode() {
        return _emode;
    }

    public ExternalForce getExternalForce() {
        return _ef;
    }

    public Varmap getVars() {
        return _vars;
    }

    public CA getMeta() {
        return _meta;
    }

    public void resize(int w, int h) {
        _w = w;
        _h = h;
        if(_meta!=null) {
            _meta.resize(w,h);
        }
    }

    public void reseed(long seed) {
        _seed = seed;
    }

    public CA mutate(Rule r, Random om) {
        if(r instanceof IndexedRule) {
            return new CA(r, _p.matchCapacity(r.colorCount(), om), _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
        }
        else {
            return new CA(r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
        }
    }

    public CA size(int w, int h) {
        CA meta = _meta!=null?_meta.size(w,h):null;
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, meta);
    }

    public CA size(int w, int h, int d) {
        CA meta = _meta!=null?_meta.size(w,h,d):null;
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, meta);
    }

    public CA size(int w, int h, int d, int prelude) {
        CA meta = _meta!=null?_meta.size(w,h,d,prelude):null;
        return new CA(_r, _p, _i, branchRandom(), _seed, w, h, d, prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, meta);
    }

    public CA copy() {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA seed() {
        return seed(_rand.nextLong());
    }

    public CA seed(long seed) {
        return new CA(_r, _p, _i, branchRandom(), seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA palette(Palette p) {
        return new CA(_r, p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA initializer(Initializer i) {
        return new CA(_r, _p, i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA prelude(int pre) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, pre, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA coda(int coda) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA weight(double weight) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, weight, _coda, _cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA computeMode(ComputeMode cmode) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, cmode, _umode, _emode, _ef, _vars, _meta);
    }

    public CA updateMode(UpdateMode umode) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, umode, _emode, _ef, _vars, _meta);
    }

    public CA edgeMode(EdgeMode emode) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, emode, _ef, _vars, _meta);
    }

    public CA externalForce(ExternalForce ef) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, ef, _vars, _meta);
    }

    public CA vars(Varmap v) {
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, v, _meta);
    }

    public CA meta(CA meta) {
        if(meta==this) {
            throw new IllegalArgumentException("strange loop");
        }
        return new CA(_r, _p, _i, branchRandom(), _seed, _w, _h, _d, _prelude, _weight, _coda, _cmode, _umode, _emode, _ef, _vars, meta);
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

    //public String toIncantation() {
        //Codec c = Codec.get();
        //return c.encode(toBinary());
    //}

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

    public static CA fromImage(String filename, String paletteMode, String lang) throws IOException {
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
            case "continuous":
            case "continuous-channels":
            default:
                p = Palette.fromImage(i);
        }
        Values v = "continuous".equals(paletteMode) || "continuous-channels".equals(paletteMode)
            ? Values.continuous : Values.discrete;
        int w = i.getWidth();
        int h = i.getHeight();
        int d = "continuous-channels".equals(paletteMode) ? 4 : 1;
        int colors = v==Values.discrete ? p.getColorCount() : 2;
        LOG.debug("image with "+p.getColorCount()+" colors");
        int size = 1;
        int dims = "continuous-channels".equals(paletteMode) ? 3 : 2;
        Archetype a = new Archetype(dims, size, colors, Archetype.Neighborhood.moore, v);
        Ruleset rs = new ComputedRuleset(a, Languages.named(lang));
        Random rand = new Random();
        Rule rule = rs.random(rand).next();
        ImageInitializer init = new ImageInitializer(new File(filename));
        CA ca = new CA(rule, p, init, rand, 0, w, h, d, 0, 1d, 0, ComputeMode.combined, new UpdateMode.SimpleSynchronous(), EdgeMode.defaultMode(), ExternalForce.nop(), new Varmap(), null);
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
            return new CA(r, p, i, new Random(), seed, w, h, 10, 10, 1d, 0, ComputeMode.combined, new UpdateMode.SimpleSynchronous(), EdgeMode.defaultMode(), ExternalForce.nop(), new Varmap(), null);
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

    private static String readTextFileType(String filename) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            if(filename.endsWith(".gz")) {
                in = new GZIPInputStream(in);
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line = r.readLine();
            if(line.startsWith("ca {")) {
                return "nausicaa_legacy";
            }
            else if(line.startsWith("{")) {
                return "nausicaa_json";
            }
            else {
                return "unknown";
            }
        }
        finally {
            try {
                if(in!=null) {
                    in.close();
                }
            }
            catch(IOException e) {
            }
        }
    }

    private static CA fromTextFile(String filename) throws IOException {
        switch(readTextFileType(filename)) {
            case "nausicaa_json":
                return fromJsonTextFile(filename);
            case "nausicaa_legacy":
                return fromLegacyTextFile(filename);
        }
        throw new UnsupportedOperationException("unsupported file '"+filename+"'");
    }

    private static CA fromJsonTextFile(String filename) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            if(filename.endsWith(".gz")) {
                in = new GZIPInputStream(in);
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            JsonElement e = new JsonParser().parse(r);
            return fromJson(e);
        }
        finally {
            try { in.close(); } catch(IOException e) {}
        }
    }

    private static CA fromJson(JsonElement e) throws IOException {
        JsonObject o = (JsonObject) e;
        int version = Json.integer(o, "version", 6);
        int w = Json.integer(o, "width", 100);
        int h = Json.integer(o, "height", 100);
        int d = Json.integer(o, "depth", 1);
        int pre = Json.integer(o, "prelude", 0);
        int seed = Json.integer(o, "seed", 0);
        double weight = Json.dobl(o, "weight", 1d);
        int coda = Json.integer(o, "coda", 0);
        ComputeMode cmode = ComputeMode.from(Json.string(o, "compute_mode", "combined"));
        UpdateMode umode = UpdateMode.fromJson(o.get("update_mode"));
        EdgeMode emode = o.has("edge_mode") ? EdgeMode.fromJson(o.get("edge_mode")) : EdgeMode.defaultMode();
        Initializer i = Initializers.fromJson(o.get("initializer"));
        Varmap vars = o.has("vars") ? Varmap.fromJson(o.get("vars")) : new Varmap();
        Rule r = ComputedRuleReader.fromJson(o.get("rule"), vars);
        Palette p = Palette.fromJson(o.get("palette"));
        ExternalForce ef = o.has("external_force") ? ExternalForce.fromJson(o.get("external_force")) : ExternalForce.nop();
        CA meta = o.has("meta") ? fromJson(o.get("meta")):null;
        return new CA(r, p, i, new Random(), seed, w, h, d, pre, weight, coda, cmode, umode, emode, ef, vars, meta);
    }

    private static CA fromLegacyTextFile(String filename) throws IOException {
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
            return new CA(rule, p, i, new Random(), h.seed, h.w, h.h, h.d, h.prelude, h.weight, h.coda, ComputeMode.combined, new UpdateMode.SimpleSynchronous(), EdgeMode.defaultMode(), ExternalForce.nop(), new Varmap(), null);
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

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("version", VERSION);
        o.addProperty("width", _w);
        o.addProperty("height", _h);
        o.addProperty("depth", _d);
        o.addProperty("prelude", _prelude);
        o.addProperty("seed", _seed);
        o.addProperty("weight", _weight);
        o.addProperty("coda", _coda);
        o.addProperty("compute_mode", _cmode.toString());
        o.add("update_mode", _umode.toJson());
        o.add("edge_mode", _emode.toJson());
        o.add("external_force", _ef.toJson());
        o.add("initializer", _i.toJson());
        o.add("rule", _r.toJson());
        o.add("palette", _p.toJson());
        o.add("vars", _vars.toJson());
        if(_meta!=null) {
            o.add("meta", _meta.toJson());
        }
        return o;
    }

    public void write(PrintWriter w) throws IOException {
        if(true) {
            Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
            gson.toJson(toJson(), w);
        }
        else {
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
    }

    @Override public String toString() {
        return "ca::{w:"+_w+", h:"+_h+", c:"+_r.archetype().colors()+"}";
    }

    private Random branchRandom() {
        return new Random();
    }
}
