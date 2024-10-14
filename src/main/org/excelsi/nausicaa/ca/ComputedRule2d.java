package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Random;
import com.google.gson.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class ComputedRule2d extends AbstractRule implements Mutatable, Genomic {
    private static final Logger LOG = LoggerFactory.getLogger(ComputedRule2d.class);
    private final Ruleset _origin;
    private final Pattern _p;
    private final Rule _meta;


    public ComputedRule2d(Pattern p) {
        this(p, null);
    }

    public ComputedRule2d(Pattern p, Ruleset origin) {
        this(p, origin, null, null);
    }

    public ComputedRule2d(Pattern p, Ruleset origin, Rule metarule, Rule hyper) {
        //super(p, hyper);
        super(1,1);
        _p = p;
        //System.err.println("################# ORIGIN: "+origin);
        _origin = origin!=null?origin:new ComputedRuleset(p.archetype());
        _meta = null;
    }

    //@Override public IndexedRule getMetarule() {
        //return _meta;
        //throw new UnsupportedOperationException();
    //}
//
    //@Override public IndexedRule withMetarule(IndexedRule meta) {
        //return new IndexedRule2d(pattern(), _origin, (IndexedRule2d) meta, getHyperrule());
        //throw new UnsupportedOperationException();
    //}
//
    //@Override public IndexedRule withHyperrule(IndexedRule hyper) {
        //return new IndexedRule2d(pattern(), _origin, _meta, hyper);
        //throw new UnsupportedOperationException();
    //}

    public ComputedRule2d derive(Pattern pattern) {
        //return new ComputedRule2d(pattern, _origin, _meta, getHyperrule());
        return new ComputedRule2d(pattern, _origin, _meta, null);
    }

    public ComputedRule2d derive(Language lang) {
        return new ComputedRule2d(_p, new ComputedRuleset(archetype(), lang), _meta, null);
    }

    //@Override public ComputedRule2d derive(Pattern.Transform transform) {
        //return new ComputedRule2d(pattern().transform(transform), _origin, _meta!=null?_meta.derive(transform):null, getHyperrule());
    //}

    @Override public Archetype archetype() {
        return _p.archetype();
    }

    @Override public int dimensions() {
        return archetype().dims();
    }

    @Override public int background() {
        return 0;
    }

    @Override public int length() {
        return _p.archetype().sourceLength();
    }

    @Override public int[] colors() {
        int[] cols = new int[_p.archetype().colors()];
        for(int i=0;i<cols.length;i++) {
            cols[i] = i;
        }
        return cols;
    }

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public Ruleset origin() {
        return _origin;
    }

    public Iterator<Plane> frameIterator(final Plane c, final ExecutorService pool, final GOptions opt) {
        if(archetype().dims()==1) {
            return new Iterator<Plane>() {
                @Override public boolean hasNext() { return false; }
                @Override public Plane next() { throw new IllegalStateException(); }
                @Override public void remove() {}
            };
        }
        if(c==null) {
            throw new IllegalArgumentException("null plane");
        }
        final Iterator<Plane> metarator = _meta!=null?_meta.frameIterator(c,pool, opt):null;
        final int block = c.getHeight() / opt.parallel();
        int nworkers = c.getHeight()/block + (c.getHeight()%block>0?1:0);
        final Worker[] workers = new Worker[nworkers];
        final Pattern[] patterns = new Pattern[nworkers];
        LOG.info("rule compute using "+workers.length+" workers on blocksize "+block+" with "+opt);
        final Variables vars = new Variables() {
            @Override public Double weight() { return c.creator().getWeight(); }
        };
        for(int i=0;i<workers.length;i++) {
            patterns[i] = createPattern(pool);
            Variables pvars;
            if(patterns[i] instanceof Variables) {
                pvars = Variables.cascade((Variables)patterns[i], vars);
            }
            else {
                pvars = vars;
            }
            if(opt.variables()!=null) {
                pvars = Variables.cascade(opt.variables(), pvars);
            }
            workers[i] = Workers.create(patterns[i], 0, i*block, c.getWidth(), Math.min(c.getHeight(), (i+1)*block),
                pvars,
                c.creator().getComputeMode(), c.creator().getUpdateMode(), c.creator().getExternalForce(), c.creator().getRandom());
        }
        final Future[] futures = new Future[workers.length];
        return new Iterator<Plane>() {
            Plane p1;
            Plane p2;
            Plane tmp;
            long count;
            int depthIdx;

            {
                if(opt.higherDim()>1) {
                    p1 = c.withDepth(opt.higherDim());
                    p2 = p1;
                    //System.err.println("switched to higher dim "+opt.higherDim());
                }
                else {
                    p1 = c;
                    p2 = c.copy();
                    //depthIdx = Math.min(0,c.getDepth());
                }
            }

            @Override public Plane next() {
                //if(metarator!=null) {
                    //Plane meta = metarator.next();
                    //w.frame(p1, p2, meta);
                //}
                //else {
                    //w.frame(p1, p2);
                //}
                final Plane frameP1 = p1;
                final Plane frameP2 = p2;
                //frameP2.lock(2);
                if(opt.higherDim()>1) {
                    ((Sliceable)frameP1).setReadDepth(depthIdx++);
                    ((Sliceable)frameP2).setWriteDepth(depthIdx);
                }
                try {
                    frameP2.lockWrite();
                    for(int i=0;i<workers.length;i++) {
                        final int w = i;
                        futures[i] = pool.submit(()->workers[w].frame(frameP1, frameP2));
                    }
                    try {
                        for(int i=0;i<futures.length;i++) {
                            try {
                                futures[i].get();
                                patterns[i].tick();
                            }
                            catch(ExecutionException e) {
                                LOG.info(humanize()+": "+e, e);
                            }
                        }
                    }
                    catch(InterruptedException e) {
                    }
                }
                finally {
                    frameP2.unlockWrite();
                }
                tmp = p1;
                p1 = p2;
                if(opt.doubleBuffer()) {
                    p2 = tmp;
                }
                else {
                    p2 = p1.copy();
                }
                if(++count%500==0) {
                    for(int i=0;i<workers.length;i++) {
                        LOG.info("worker "+i+" stat: "+workers[i].getStats());
                    }
                }
                return p1;
            }

            @Override public boolean hasNext() {
                return true;
            }

            @Override public void remove() {
            }
        };
    }

    @Override public Plane generate(final Plane c, final int start, final int end, final ExecutorService pool, final boolean stopOnSame, final boolean overwrite, final Updater u, final GOptions opt) {
        Plane ret = c;
        switch(_p.archetype().dims()) {
            case 1:
                Plane p1 = c;
                final Pattern pat = createPattern(pool);
                Variables vars = new Variables() {
                    @Override public Double weight() { return c.creator().getWeight(); }
                };
                if(pat instanceof Variables) {
                    vars = Variables.cascade((Variables)pat, vars);
                }

                Worker w = Workers.create(pat, 0, 1, c.getWidth(), c.getHeight(),
                    vars,
                    c.creator().getComputeMode(), c.creator().getUpdateMode(),
                    c.creator().getExternalForce(), c.creator().getRandom());
                w.frame(p1);
                ret = p1;
                break;
            case 2:
            case 3:
            default:
                LOG.info(this+": generating prelude from "+start+" to "+end+" with "+opt);
                final Iterator<Plane> ps = frameIterator(c, pool, opt);
                for(int i=start;i<end;i++) {
                    ret = ps.next();
                }
                break;
        }
        return ret;
    }

    @Override public Mutatable mutate(MutationFactor m) {
        return derive(
            (Pattern)
                ((Mutatable)_p).mutate(
                    m.withLanguage(((AbstractComputedRuleset)_origin).language())
                    ));
    }

    @Override public String humanize() {
        StringBuilder b = new StringBuilder(archetype().dims()+"d / "
            +(_p instanceof Humanizable ? ((Humanizable)_p).humanize() : _p.toString()));
        return b.toString();
    }

    @Override public void write(PrintWriter w) {
        w.println(genome());
    }

    @Override public String genome() {
        return _p instanceof Genomic ? ((Genomic)_p).genome() : _p.toString();
    }

    @Override public Varmap vars() {
        //System.err.println("is_gen: "+(_p instanceof Genomic));
        //System.err.println("** pat: "+_p);
        return _p instanceof Genomic ? ((Genomic)_p).vars() : new Varmap();
    }

    @Override public String prettyGenome() {
        return genome().replace(',','\n');
    }

    @Override public String toString() {
        return "ComputedRule2d::{pattern:"+_p+"}";
    }

    @Override public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type","computed");
        o.add("archetype", archetype().toJson());
        o.addProperty("genome",genome());
        if(_p instanceof SequencePattern) {
            o.addProperty("transition", ((SequencePattern)_p).transition());
        }
        o.add("language", ((AbstractComputedRuleset)_origin).language().toJson());
        return o;
    }

    public final Pattern createPattern() {
        return _p.copy();
    }

    protected final Pattern createPattern(final ExecutorService pool) {
        return _p.copy();
    }

    public static Rule fromJson(JsonElement e) {
        return fromJson(e, new Varmap());
    }

    public static Rule fromJson(JsonElement e, Varmap vars) {
        JsonObject o = (JsonObject) e;
        Archetype a = Archetype.fromJson(o.get("archetype"));
        String genome = Json.string(o, "genome");
        MutationFactor mf = null;
        if(o.has("transition")) {
            mf = new MutationFactor().withTransition(Json.flot(o, "transition", 0.1f));
        }
        else {
            mf = new MutationFactor();
        }
        mf.withVars(vars);
        Language lang;
        if(o.has("language")) {
            lang = Language.fromJson(o.get("language"));
        }
        else {
            lang = Languages.universal();
        }
        if(mf!=null) {
            return new ComputedRuleset(a, lang).create(genome, mf);
        }
        else {
            return new ComputedRuleset(a, lang).create(genome);
        }
    }
}
