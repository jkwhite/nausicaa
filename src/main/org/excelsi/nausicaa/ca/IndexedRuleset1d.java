package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.Random;


public class IndexedRuleset1d extends AbstractIndexedRuleset {
    public IndexedRuleset1d(Archetype a) {
        super(a);
    }

    @Override
    public Iterator<Rule> iterator() {
        return new Iterator<Rule>() {
            final Iterator<IndexedPattern> ps = Patterns.iterator(archetype()).iterator();

            @Override
            public boolean hasNext() {
                return ps.hasNext();
            }

            @Override
            public Rule next() {
                return new IndexedRule1d(ps.next(), IndexedRuleset1d.this);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterator<Rule> random(final Random r) {
        return new Iterator<Rule>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Rule next() {
                return new IndexedRule1d(Patterns.random(archetype(), r), IndexedRuleset1d.this);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Rule create(Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ruleset derive(int[] colors, int len) {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule custom(IndexedPattern.Transform transform) {
        return new IndexedRule1d(Patterns.custom(archetype(), transform));
    }

    @Override public IndexedRule custom(IndexedRule source, IndexedPattern.BinaryTransform transform) {
        return new IndexedRule1d(source.getPattern().transform(archetype(), transform), this);
    }
}
