package org.excelsi.nausicaa.ca;


import java.util.ArrayList;
import java.util.List;


public final class Genome {
    private final String _c;


    public Genome(String c) {
        _c = c;
    }

    public Op[] ops() {
        final List<Op> ops = new ArrayList<>();
        for(final String op:_c.split("-")) {
            ops.add(Ops.op(op));
        }
        return ops.toArray(new Op[0]);
    }

    public String c() {
        return _c;
    }

    @Override public String toString() {
        return c();
    }
}
