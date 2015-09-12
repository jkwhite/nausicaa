package org.excelsi.ca;
import java.util.*;


public class Rulespace2D implements Ruleset {
    private Ruleset2D[] _rules;


    public Rulespace2D(Ruleset2D... rules) {
        _rules = rules;
    }

    public Rule create(Object... args) {
        return new Multirule2D(this, (Rule[]) args);
    }

    public Ruleset derive(int[] colors, int len) {
        throw new IllegalStateException();
    }

    public Iterator<Rule> iterator() {
        final Iterator<Rule>[] its = new Iterator[_rules.length];
        for(int i=0;i<its.length;i++) {
            its[i] = _rules[i].iterator();
        }
        return new Iterator<Rule>() {
            public boolean hasNext() {
                boolean has = true;
                for(Iterator i:its) {
                    has = has && i.hasNext();
                }
                return has;
            }

            public Rule next() {
                Rule[] next = new Rule[its.length];
                for(int i=0;i<next.length;i++) {
                    next[i] = its[i].next();
                }
                return new Multirule2D(Rulespace2D.this, next);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Rule fromString(String str) {
        return null;
    }

    public Iterator<Rule> random() {
        return random(new Random());
    }

    public Iterator<Rule> random(final Random rnd) {
        final Iterator<Rule>[] its = new Iterator[_rules.length];
        for(int i=0;i<its.length;i++) {
            its[i] = _rules[i].random(rnd);
        }
        return new Iterator<Rule>() {
            public boolean hasNext() {
                boolean has = true;
                for(Iterator i:its) {
                    has = has && i.hasNext();
                }
                return has;
            }

            public Rule next() {
                Rule[] next = new Rule[its.length];
                for(int i=0;i<next.length;i++) {
                    next[i] = its[i].next();
                    //System.err.println("got next: "+next[i]);
                }
                return new Multirule2D(Rulespace2D.this, next);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
