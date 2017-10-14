package org.excelsi.nausicaa.ca;


import java.util.concurrent.ExecutorService;
import java.util.Iterator;


public class RulePattern implements Pattern {
    private final ExecutorService _pool;
    private final Rule _r;
    private final Rule _hr;
    private final Iterator<Plane> _ps;
    private Plane _cur;


    public RulePattern(final ExecutorService pool, final Rule r, final Rule hr) {
        _pool = pool;
        _r = r;
        _hr = hr;
        Plane p = new BufferedImagePlane(null, r.width(), heightForArchetype(hr.archetype()));
        r.copy(p);
        _ps = hr.frameIterator(p, _pool, true, 1);
        _cur = p;
    }

    @Override public Archetype archetype() {
        return _r.archetype();
    }

    @Override public byte next(final int pattern, final byte[] p2) {
        return _cur.next(pattern);
    }

    @Override public void tick() {
        _cur = _ps.next();
    }

    public Plane generate(final int height, final Palette palette) {
        Plane p = new BufferedImagePlane(null, _r.width(), height, palette);
        _r.copy(p);
        _hr.generate(p, 1, height, _pool, false, false, null);
        return p;
    }

    private static int heightForArchetype(final Archetype a) {
        switch(a.dims()) {
            case 1:
                return 10;
            default:
                return 1;
        }
    }
}
