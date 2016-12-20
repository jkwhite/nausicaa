package org.excelsi.nausicaa.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;


public interface Ruleset extends java.io.Serializable {
    Iterator<Rule> iterator();
    Iterator<Rule> random(Random r);
    Rule create(Object... args);
    Ruleset derive(int[] colors, int len);

    public static Ruleset create(final Archetype a) {
        switch(a.dims()) {
            case 1:
                return new IndexedRuleset1d(a);
            case 2:
                return new IndexedRuleset2d(a);
            default:
                throw new IllegalArgumentException("unsupported dimensionality "+a.dims());
        }
    }

    public static Ruleset create(final Archetype a, final Ruleset hyper) {
        switch(a.dims()) {
            case 1:
                return new IndexedRuleset1d(a, (IndexedRuleset)hyper);
            case 2:
                return new IndexedRuleset2d(a, (IndexedRuleset)hyper);
            default:
                throw new IllegalArgumentException("unsupported dimensionality "+a.dims());
        }
    }
}
