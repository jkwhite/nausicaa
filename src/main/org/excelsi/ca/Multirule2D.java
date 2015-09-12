package org.excelsi.ca;


import java.awt.image.*;
import java.util.*;


public class Multirule2D extends AbstractRule implements Multirule {
    private Rule[] _rules;
    private int[] _colors;
    private Random _rand;
    private Ruleset _origin;
    private Interceptor _interceptor;
    private Rule.Initialization _init;
    protected boolean _evaporate;
    protected boolean _whimsical = false;
    protected boolean _phaseTransition;


    public Multirule2D(Ruleset origin, Rule... rules) {
        super(1, 1);
        _origin = origin;
        //super(combineColors(rules), combinePatterns(rules), combineBackground(rules));
        _rules = rules;
        for(Rule r:_rules) {
            if(!(r instanceof Rule2D)) {
                throw new IllegalArgumentException("rule is not 2d");
            }
        }

        Set<Integer> colors = new HashSet<Integer>();
        for(Rule r:_rules) {
            for(int c:r.colors()) {
                colors.add(new Integer(c));
            }
        }
        int[] cs = new int[colors.size()];
        int j = 0;
        for(Integer i:colors) {
            cs[j++] = i.intValue();
        }
        _colors = cs;
        background();
    }

    public int dimensions() { return 2; }

    public void setInterceptor(Interceptor i) {
        _interceptor = i;
    }

    public void setFabric(Fabric f) {
    }

    public void setRandom(Random r) {
        _rand = r;
    }

    public void setEvaporate(boolean evap) {
        _evaporate = evap;
    }

    public void setWhimsical(boolean whimsical) {
        _whimsical = whimsical;
    }

    public void setPhaseTransition(boolean phase) {
        _phaseTransition = phase;
    }

    public void setMask(BitSet s) {
    }

    public BitSet getMask() {
        return null;
    }

    public Multirule2D copy() {
        return (Multirule2D) Rule1D.deepCopy(this);
    }

    public Ruleset origin() {
        return _origin;
    }

    public Rule[] rules() {
        return _rules;
    }

    public int length() {
        throw new UnsupportedOperationException();
    }

    public Rule mutate(Mutator m) throws MutationFailedException {
        return m.mutate(this);
    }

    public int[][] toPattern() {
        return _rules[0].toPattern();
    }

    public int[] colors() {
        return _colors;
    }

    public int background() {
        int bgr = _rules[0].background();
        for(Rule r:_rules) {
            if(r.background()!=bgr) {
                //throw new IllegalArgumentException("all rules must have same background color");
            }
        }
        return bgr;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for(Rule r:_rules) {
            b.append(r.toString());
            b.append("+");
        }
        b.setLength(b.length()-1);
        return b.toString();
    }

    public String toIncantation() {
        StringBuilder b = new StringBuilder();
        for(Rule r:_rules) {
            b.append(r.toIncantation());
            b.append(" ");
        }
        b.setLength(b.length()-1);
        return b.toString();
    }

    public long toSeed() {
        return toString().hashCode();
    }

    public void init(CA c, Initialization in) {
        _init = in;
        Random om = new Random(Rand.seed());
        int bgr = background();
        for(int i=0;i<c.getWidth();i++) {
            for(int j=0;j<c.getHeight();j++) {
                c.set(i, j, bgr);
            }
        }
        switch(in) {
            case single:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[0]);
                }
                for(Rule r:rules()) {
                    int ox = 1+om.nextInt(c.getWidth()-2);
                    int oy = 1+om.nextInt(c.getHeight()-2);
                    c.set(ox, oy, r.colors()[1]);
                }
                break;
            case random:
                for(int i=0;i<c.getWidth();i++) {
                    for(int j=0;j<c.getHeight();j++) {
                        c.set(i, j, _colors[om.nextInt(_colors.length)]);
                    }
                }
                break;
            case arabesque:
                for(Rule r:rules()) {
                    int ox = c.getWidth()/2;
                    int oy = c.getHeight()/2;
                    int[] col = r.colors();
                    c.set(ox, oy, col[1+om.nextInt(col.length-1)]);
                }
                break;
            case image:
                BufferedImage br = Viewer.getInstance().getInitialImage();
                for(int i=0;i<Math.min(br.getWidth(), c.getWidth());i++) {
                    for(int j=0;j<Math.min(br.getHeight(), c.getHeight());j++) {
                        int pack = br.getRGB(i, j);
                        if(i%10==0&&j%10==0) System.err.println("setting rgb for "+i+", "+j+": "+CA.toColorString(pack));
                        c.set(i, j, br.getRGB(i, j));
                        //c.set(i, j, _colors[om.nextInt(_colors.length)]);
                        //c.getImageBuffer().setRGB(i, j, br.getRGB(i, j));
                    }
                }
                break;
        }
    }

    public Iterator<CA> frames(final CA root) {
        return new Rule2D.FrameIterator<CA>(root) {
            private CA _src = root, _dest = new CA(root.getWidth(), root.getHeight());
            private int _frame = 0;

            public boolean hasNext() {
                switch(_init) {
                    case arabesque:
                        return _frame < Math.min(_src.getWidth(), _src.getHeight())/2-1;
                    default:
                        return super.hasNext();
                }
            }

            public CA next() {
                generate(_src, _dest, null);
                CA ret = _dest;
                _dest = _src;
                _src = ret;
                _frame++;
                return ret;
            }

            protected CA frame(CA c) {
                generate(c, 0, 1, false, false, null);
                return c;
            }
        };
    }

    public int getSuggestedInterval(CA c) {
        return _rules[0].getSuggestedInterval(c);
    }

    private float generate(CA b1, CA b2, Updater u) {
        float diff = 0;
        long last = System.currentTimeMillis();
        for(Rule r:_rules) {
            Rule2D r2 = (Rule2D) r;
            r2.setInterceptor(_interceptor);
            diff += r2.generate(b1, b2, null);
        }
        long now = System.currentTimeMillis();
        if(u!=null) {
            if(now>=last+u.interval()) {
                u.update(this, -1, -1, -1);
                last = now;
            }
        }
        if(_whimsical) {
            for(int i=0;i<_rules.length;i++) {
                Mutators m = null;
                switch(Rand.om.nextInt(250)) {
                    case 1:
                        m = Mutators.hue;
                        break;
                    case 2:
                        m = Mutators.noise;
                        break;
                    case 3:
                        m = Mutators.symmetry;
                        break;
                    case 4:
                        m = Mutators.thicken;
                        break;
                    case 5:
                        m = Mutators.thin;
                        break;
                }
                if(m!=null) {
                    Mutator mutator = MutatorFactory.instance().createMutator(m, Rand.om);
                    try {
                        System.err.println("mutating rule "+i+" with "+m);
                        Rule mr = mutator.mutate(_rules[i]);
                        if(mr!=null) {
                            _rules[i] = mr;
                            System.err.println("mutated rule "+i);
                        }
                    }
                    catch(MutationFailedException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
        //System.err.println((now-last)+" ms");
        return diff;
    }

    private CA _b;
    public float generate(CA c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
        if(_b==null||_b.getWidth()!=c.getWidth()||_b.getHeight()!=c.getHeight()) {
            _b = new CA(c.getWidth(), c.getHeight());
        }
        float diff = 0f;
        CA b1 = c, b2 = _b; // double buffer
        long last = System.currentTimeMillis();
        for(int i=start;i<end;i++) {
            generate(b1, b2, u);
            if(Thread.currentThread().isInterrupted()) {
                return diff;
            }

            // swap
            CA temp = b1;
            b1 = b2;
            b2 = temp;
        }
        if(c!=b1) {
            System.err.println("transferring image");
            long st = System.currentTimeMillis();
            c.setData(b1.getData());
            long ed = System.currentTimeMillis();
            System.err.println("transfer took "+(ed-st));
        }
        c.setGoodness(diff);
        return diff;
    }

    /*
    private static int[][] combinePatterns(Rule[] rules) {
        return null;
    }

    private static int[] combineColors(Rule[] rules) {
        Set<Integer> colors = new HashSet<Integer>();
        for(Rule r:rules) {
            colors.addAll(Arrays.asList(r.colors()));
        }
        int[] cs = new int[colors.size()];
        int j = 0;
        for(Integer i:colors) {
            cs[j++] = i.intValue();
        }
        return cs;
    }

    private static int combineBackground(Rule[] rules) {
        int bgr = rules[0].background();
        for(Rule r:rules) {
            if(r.background()!=bgr) {
                throw new IllegalArgumentException("all rules must have same background color");
            }
        }
        return bgr;
    }
    */
}

