package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import com.google.gson.*;


public abstract class AbstractIndexedRule extends AbstractRule implements IndexedRule {
    private final IndexedPattern _p;
    private final IndexedRule _hyper;


    public AbstractIndexedRule(IndexedPattern p) {
        this(p, null);
    }

    public AbstractIndexedRule(IndexedPattern p, IndexedRule hyper) {
        //super(p.archetype().patternLength(), 1);
        super(p.archetype().totalPatterns(), 1);
        _p = p;
        _hyper = hyper;
    }

    @Override public Archetype archetype() {
        return _p.archetype();
    }

    @Override public IndexedPattern getPattern() {
        return _p;
    }

    @Override public IndexedRule getHyperrule() {
        return _hyper;
    }

    @Override public int length() {
        return _p.archetype().sourceLength();
    }

    @Override public int colorCount() {
        return _p.archetype().colors();
    }

    @Override public int[] colors() {
        int[] cols = new int[_p.archetype().colors()];
        for(int i=0;i<cols.length;i++) {
            cols[i] = i;
        }
        return cols;
    }

    @Override public void copy(final Plane ip) {
        final IntPlane p = (IntPlane) ip;
        _p.inspect((a,t)->{
            for(int i=0;i<t.length;i++) {
                p.setCell(i, 0, t[i]);
            }
            //p.setRow(t, 0);
        });
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        _p.write(dos);
    }

    @Override public void write(PrintWriter w) {
        w.println("indexed");
        _p.write(w);
    }

    @Override public String humanize() {
        //return _p.summarize();
        StringBuilder b = new StringBuilder(Info.b10Id(this, 50));
        if(getHyperrule()!=null) {
            b.append(" : ").append(getHyperrule().humanize());
        }
        return b.toString();
    }

    @Override public String id() {
        return _p.formatTarget();
    }

    @Override public String toString() {
        return "IndexedRule1d::{pattern:"+_p+"}";
    }

    @Override public JsonElement toJson() {
        final String enc = "base64gz/bytes";
        JsonObject o = new JsonObject();
        o.addProperty("type", "indexed"+_p.archetype().dims()+"d");
        o.add("archetype", _p.archetype().toJson());
        o.addProperty("encoding", enc);
        o.addProperty("length", _p.length());
        o.addProperty("target", _p.serialize(enc));
        return o;
    }

    public static Rule fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        String type = Json.string(o, "type");
        String enc = Json.string(o, "encoding");
        String tgt = Json.string(o, "target");
        int length = Json.integer(o, "length", 3);
        Archetype a = Archetype.fromJson(o.get("archetype"));
        IndexedPattern p = IndexedPattern.deserialize(enc, tgt, length, a);
        return "indexed1d".equals(type)
            ? new IndexedRule1d(p) : new IndexedRule2d(p);
    }

    protected final void mutateRule(final IndexedPattern p) {
        if(mutagen()!=null) {
            p.mutate(mutagen());
        }
    }

    protected final IndexedPattern pattern() {
        return _p;
    }

    protected final Pattern createPattern(final ExecutorService pool) {
        //return new ComputedPattern(archetype());
        if(getHyperrule()==null) {
            return pattern().copy();
        }
        else {
            RulePattern rp = new RulePattern(pool, this, getHyperrule());
            return rp;
        }
    }
}
