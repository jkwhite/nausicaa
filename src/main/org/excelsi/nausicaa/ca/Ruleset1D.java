package org.excelsi.nausicaa.ca;


import java.util.*;


public class Ruleset1D implements Ruleset {
    private int[] _colors;
    private int _length;


    public Ruleset1D(int[] colors) {
        this(colors, 3);
    }

    public Ruleset1D(int[] colors, int length) {
        _colors = colors;
        _length = length;
    }

    public Rule create(Object... args) {
        return new Rule1D(this, (int[]) args[0], (int[][]) args[1], (int) ((Integer)args[2]).intValue());
    }

    public Ruleset derive(int[] colors, int len) {
        return new Ruleset1D(colors, len);
    }

    public Iterator<Rule> iterator() {
        final int[][] pats = Rule1D.archetype(_colors, _length);

        final int[] ns = new int[pats.length];
        for(int i=0;i<ns.length;i++) {
            ns[i] = 0;
        }
        final long numRules = (int)Math.pow(_colors.length, pats.length);
        return new Iterator<Rule>() {
            long i=0;

            public boolean hasNext() {
                return i<numRules;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public Rule next() {
                for(int j=0;j<pats.length;j++) {
                    pats[j][_length] = _colors[ns[j]];
                }
                Rule r = new Rule1D(Ruleset1D.this, _colors, pats, _colors[0]);
                Rule1D.incArray(ns, _colors.length);
                i++;
                return r;
            }
        };
    }

    //public Rule fromString(String str) {
        //Rule r = Rule1D.fromString(_colors, _colors[0], _length, str);
        //Rule r = Rule1D.fromString(str);
        //return r;
    //}

    public Iterator<Rule> random() {
        return random(new Random());
    }

    public Iterator<Rule> random(final Random rnd) {
        final int[][] arch = Rule1D.archetype(_colors, _length);

        return new Iterator<Rule>() {
            final int[] ns = new int[arch.length];

            public boolean hasNext() {
                return true;
            }

            public void remove() {
            }

            public Rule next() {
                int start = rnd.nextInt(ns.length-2);
                int end = rnd.nextInt(ns.length-start)+start;
                boolean interior = rnd.nextBoolean();
                for(int i=0;i<ns.length;i++) {
                    if((interior&&i>=start&&i<=end)||
                            (!interior&&(i<start||i>end))) {
                        ns[i] = rnd.nextInt(_colors.length);
                        //if(rnd.nextBoolean()) {
                            //ns[i] = 0; // weigh 0 more heavily
                        //}
                    }
                    else {
                        ns[i] = 0;
                    }
                }
                for(int j=0;j<arch.length;j++) {
                    arch[j][_length] = _colors[ns[j]];
                }
                int[][] pats = arch;
                Rule r = new Rule1D(Ruleset1D.this, _colors, pats, _colors[0]);
                return r;
            }
        };
    }
}
