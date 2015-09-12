package org.excelsi.ca;


import java.util.*;


public class RulesetSquare implements Ruleset {
    private int[] _colors;
    private int _length;
    private boolean _wrap;


    public RulesetSquare(int[] colors) {
        this(colors, 3);
    }

    public RulesetSquare(int[] colors, boolean wrap) {
        this(colors, 3, wrap);
    }

    public RulesetSquare(int[] colors, int length) {
        this(colors, length, true);
    }

    public RulesetSquare(int[] colors, int length, boolean wrap) {
        _colors = colors;
        _length = length*length;
        _wrap = wrap;
    }

    public Rule create(Object... args) {
        return new RuleSquare(this, (int[]) args[0], (int[][]) args[1], (int) ((Integer)args[2]).intValue());
    }

    public boolean getWrap() {
        return _wrap;
    }

    public void setWrap(boolean wrap) {
        _wrap = wrap;
    }

    public Ruleset derive(int[] colors, int len) {
        return new RulesetSquare(colors, len, _wrap);
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
                Rule r = new RuleSquare(RulesetSquare.this, _colors, pats, _colors[0]);
                Rule1D.incArray(ns, _colors.length);
                i++;
                return r;
            }
        };
    }

    public Rule fromString(String str) {
        //Rule r = Rule1D.fromString(_colors, _colors[0], _length, str);
        Rule r = Rule1D.fromString(str);
        return r;
    }

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
                        if(rnd.nextBoolean()) {
                            ns[i] = 0; // weigh 0 more heavily
                        }
                    }
                    else {
                        ns[i] = 0;
                    }
                }
                for(int j=0;j<arch.length;j++) {
                    arch[j][_length] = _colors[ns[j]];
                }
                int[][] pats = arch;
                Rule r = new RuleSquare(RulesetSquare.this, _colors, pats, _colors[0]);
                return r;
            }
        };
    }
}
