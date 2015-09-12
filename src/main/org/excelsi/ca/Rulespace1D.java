package org.excelsi.ca;


import java.util.*;


public class Rulespace1D implements Ruleset {
    private Ruleset[] _rules;
    private boolean _square;


    public Rulespace1D(Ruleset1D... rules) {
        _rules = rules;
    }

    public Rulespace1D(RulesetSquare... rules) {
        _rules = rules;
        _square = true;
    }

    public Ruleset derive(int[] colors, int len) {
        throw new IllegalStateException();
    }

    public Rule create(Object... args) {
        if(_square) {
            return new MultiruleSquare(this, (Rule[]) args);
        }
        else {
            return new Multirule1D(this, (Rule[]) args);
        }
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
                if(_square) {
                    return new MultiruleSquare(Rulespace1D.this, next);
                }
                else {
                    return new Multirule1D(Rulespace1D.this, next);
                }
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
                if(_square) {
                    return new MultiruleSquare(Rulespace1D.this, next);
                }
                else {
                    return new Multirule1D(Rulespace1D.this, next);
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
