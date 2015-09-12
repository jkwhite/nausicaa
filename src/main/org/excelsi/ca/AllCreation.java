package org.excelsi.ca;


import java.util.*;


public class AllCreation /*implements Ruleset*/ {
    /*
    private Ruleset[] _rulesets;


    public AllCreation() {
        this(new Ruleset1D(new int[]{CA.randomColor(), CA.randomColor()}),
            new Ruleset2D(new int[]{CA.randomColor(), CA.randomColor()}));
    }

    public AllCreation(Ruleset... rulesets) {
        _rulesets = rulesets;
    }

    public Iterator<Rule> iterator() {
        return random();
    }

    public Rule fromString(String str) {
    }

    public Iterator<Rule> random() {
        final Iterator<Rule>[] its = new Iterator<Rule>[_rulesets.length];
        for(int i=0;i<its.length;i++) {
            its[i] = _rulesets[i].random();
        }
        return new Iterator<Rule>() {
            public boolean hasNext() { return true; }

            public Rule next() {
                Rule[] rules = new Rule[1+Rand.om.nextInt(its.length)];
                for(int i=0;i<rules.length;i++) {
                    rules[i] = its[Rand.om.nextInt(its.length)].next();
                }
                return new Multirule2D(rules);
            }

            public boolean remove() { return false; }
        };
    }

    public Rule create(Object... args) {
    }

    public Ruleset derive(int[] colors, int len) {
    }
    */
}
