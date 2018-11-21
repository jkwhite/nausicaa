package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class CompositeRule implements Rule {
    private final Rule[] _rs;


    public CompositeRule(Rule[] rs) {
        _rs = rs;
    }

    @Override public Archetype archetype() {
        throw new UnsupportedOperationException();
    }

    @Override public Ruleset origin() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule getHyperrule() {
        throw new UnsupportedOperationException();
    }

    @Override public int[][] toPattern() {
        throw new UnsupportedOperationException();
    }

    @Override public int[] colors() {
        throw new UnsupportedOperationException();
    }

    @Override public int background() {
        throw new UnsupportedOperationException();
    }

    @Override public int length() {
        throw new UnsupportedOperationException();
    }

    @Override public int dimensions() {
        //throw new UnsupportedOperationException();
        return _rs[0].dimensions();
    }

    @Override public int colorCount() {
        throw new UnsupportedOperationException();
    }

    @Override public int width() {
        throw new UnsupportedOperationException();
    }

    @Override public int height() {
        throw new UnsupportedOperationException();
    }

    @Override public String id() {
        throw new UnsupportedOperationException();
    }

    @Override public void copy(Plane p) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
        throw new UnsupportedOperationException();
    }

    @Override public com.google.gson.JsonElement toJson() {
        throw new UnsupportedOperationException();
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public void write(PrintWriter w) {
        throw new UnsupportedOperationException();
    }

    @Override public String humanize() {
        return Arrays.toString(_rs);
    }

    @Override public Plane generate(Plane c, int start, int end, ExecutorService pool, boolean stopOnSame, boolean overwrite, Updater u, GOptions opt) {
        CompositeIntPlane p = (CompositeIntPlane) c;
        for(int i=_rs.length-1;i>=0;i--) {
            System.err.println("compositing at depth "+i);
            p.setReadDepth(i);
            p.setWriteDepth(i);
            _rs[i].generate(c, start, end, pool, stopOnSame, overwrite, u, opt);
        }
        return c;
    }

    @Override public Iterator<Plane> frameIterator(Plane initial, ExecutorService pool, GOptions opt) {
        throw new UnsupportedOperationException();
    }
}
