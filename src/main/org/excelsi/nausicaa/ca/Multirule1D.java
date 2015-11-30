package org.excelsi.nausicaa.ca;


import java.util.*;


public class Multirule1D extends AbstractRule implements Multirule {
    private Rule[] _rules;
    protected int[] _colors;
    protected int _bgr;
    private Random _om;
    private Ruleset _origin;
    //private Interceptor _interceptor;
    //private transient List<VacuumMetastabilityDisaster> _v = new Vector<VacuumMetastabilityDisaster>();
    //private transient List<Rule> _evaporated = new Vector<Rule>();
    //protected boolean _evaporate;
    //protected boolean _phaseTransition;


    public Multirule1D(Ruleset origin, Rule... rules) {
        super(1, 1);
        _origin = origin;
        //super(combineColors(rules), combinePatterns(rules), combineBackground(rules));
        _rules = rules;

        Set<Integer> colors = new HashSet<Integer>();
        for(Rule r:_rules) {
            initChild(r);
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
        _bgr = background();
        //for(int i=0;i<cs.length;i++) {
            //System.err.println(cs[i]);
        //}
        //System.err.println(new ArrayList(Arrays.asList(_colors)));
    }

    public int dimensions() { return 1; }

    /*
    public void setInterceptor(Interceptor i) {
        _interceptor = i;
    }
    */

    //public void setFabric(Fabric f) {
    //}

    public void setRandom(Random r) {
    }

    //public void setEvaporate(boolean evap) {
        //_evaporate = evap;
    //}
//
    //public void setPhaseTransition(boolean phase) {
        //_phaseTransition = phase;
    //}
//
    public void setMask(BitSet s) {
    }

    public BitSet getMask() {
        return null;
    }

    public int length() {
        throw new UnsupportedOperationException();
    }

    public Multirule1D copy() {
        return (Multirule1D) Rule1D.deepCopy(this);
    }

    /*
    public void init(CA c, Initialization in) {
        _om = new Random(Rand.seed());
        switch(in) {
            case single:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[0]);
                }
                c.set(c.getWidth()/2, 0, _colors[1]);
                break;
            case random:
                for(int i=0;i<c.getWidth();i++) {
                    c.set(i, 0, _colors[_om.nextInt(_colors.length)]);
                }
                break;
            default:
        }
    }
    */

    //public int getSuggestedInterval(CA c) {
        //return _rules[0].getSuggestedInterval(c);
    //}

    public Ruleset origin() {
        return _origin;
    }

    public Rule[] rules() {
        return _rules;
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
                throw new IllegalArgumentException("all rules must have same background color");
            }
        }
        return bgr;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for(Rule r:_rules) {
            b.append(r.toString());
            b.append("-");
        }
        b.setLength(b.length()-1);
        return b.toString();
    }

    /*
    public static Multirule fromString(String s) {
        String[] rules = s.split("-");
        Rule[] rs = new Rule[rules.length];
        for(int i=0;i<rules.length;i++) {
            rs[i] = Rule1D.fromString(rules[i]);
        }
        Multirule r = null;
        switch(rs[0].dimensions()) {
            case 1:
                r = new Multirule1D(null, rs);
                break;
            case 2:
                r = new Multirule2D(null, rs);
                break;
            default:
                throw new IllegalArgumentException("unsupported dimensionality: "+rs[0].dimensions());
        }
        for(Options o:EnumSet.allOf(Options.class)) {
            r.setFlag(o, rs[0].getFlag(o));
        }
        return r;
    }
    */

    /*
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
        long seed = 0;
        for(Rule r:_rules) {
            seed ^= r.toSeed();
            seed = Long.rotateLeft(seed, 1);
        }
        return seed;
    }
    */

    /*
    public static Rule fromIncantation(String s) {
        String[] rules = s.split("\\.");
        Rule[] rs = new Rule[rules.length];
        for(int i=0;i<rules.length;i++) {
            rs[i] = Rule1D.fromIncantation(rules[i].trim());
        }
        Rule r = null;
        switch(rs[0].dimensions()) {
            case 1:
                r = new Multirule1D(null, rs);
                break;
            case 2:
                r = new Multirule2D(null, rs);
                break;
            default:
                throw new IllegalArgumentException("unsupported dimensionality: "+rs[0].dimensions());
        }
        return r;
    }

    public void destabilized(VacuumMetastabilityDisaster v) {
        if(_v==null) {
            _v = new Vector<VacuumMetastabilityDisaster>();
        }
        _v.add(v);
    }

    public void evaporated(Rule r) {
        if(_evaporated==null) {
            _evaporated = new Vector<Rule>();
        }
        //System.err.println("evaporated: "+r.toSeed());
        _evaporated.add(r);
    }
    */

    private int[] _row;
    private int[] _next;
    private int[] _last;
    private int[] _existing;
    private boolean _haslast;
    public float generate2(Plane c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
        System.err.println("generating "+_rules.length+" rules");
        final int len = _rules[0].length()+1;
        final int offset = (len-2)/2;
        if(_next==null||_next.length!=c.getWidth()) {
            _row = new int[c.getWidth()+2*offset];
            _next = new int[c.getWidth()];
            _last = new int[c.getWidth()+2*offset];
            _existing = new int[c.getWidth()];
            //_totals = new int[_colors.length];
            //_lastTotals = new int[_colors.length];
            //_lastTotals2 = new int[_colors.length];
            _haslast = false;
        }

        int crow = start;
        int steps = end-start;
        int diff = 0;
        for(int gen=0;gen<steps;gen++) {
            //System.err.print("#");
            c.getRow(_row, crow-1, offset);
            c.getRow(_existing, crow, 0);
            for(int i=0;i<offset;i++) {
                _row[i] = _row[c.getWidth()+i];
                _row[c.getWidth()+offset+i] = _row[i+offset];
                //_next[i] = _existing[i];
                _next[i] = _bgr;
                _next[_next.length-i-1] = _bgr;
            }
            boolean same = false; // true;
            int failed = 0;
            boolean printed = false;
            boolean printed2 = false;
            long spaceCount = 0;
            for(Rule rule:_rules) {
                Rule1D r1d = (Rule1D) rule;
                r1d.generate2(_next, _row, _existing, over);
            }
            c.setRow(_next, crow);
            System.arraycopy(_row, 0, _last, 0, _row.length);
            _haslast = true;
            if(same&&stopOnSame) {
                return diff;
            }
            crow++;
            if(u!=null&&crow%u.interval()==0) {
                u.update(this, start, crow, end);
            }
            if(Thread.currentThread().isInterrupted()) {
                return diff;
            }
        }
        return 0;
    }

    public float generate(Plane c, int start, int end, boolean stopOnSame, boolean over, Updater u) {
        float diff = 0f;
        Rule[] rules = new Rule[_rules.length];
        System.arraycopy(_rules, 0, rules, 0, _rules.length);
        //Map<Rule,PhaseTransition> phaseTransitions = new HashMap<Rule,PhaseTransition>();
        //Map<Rule,Rule> reverseTransitions = new HashMap<Rule,Rule>();
        System.err.println(Thread.currentThread().getId()+" initial rule count: "+rules.length);
        for(int i=start;i<end;i++) {
            over = true;
            //if(_v!=null) _v.clear();
            //if(_evaporated!=null) _evaporated.clear();
            //System.err.println(Thread.currentThread().getId()+" "+i+" rule count: "+rules.length);
            for(Rule r:rules) {
                //if(r==rules[0]) System.err.println(Thread.currentThread().getId()+" mask for rule "+r.toSeed()+" is "+(r.getMask()==null?"null":"not null"));
                //if(r==rules[0]) { ((Rule1D)r).setDebug(true); }
                diff += r.generate(c, i, i+1, stopOnSame, over, null);
                over = false;
            }
            if(u!=null&&i%u.interval()==0) {
                u.update(this, start, i, end);
            }
            if(Thread.currentThread().isInterrupted()) {
                return diff;
            }
            //System.err.println("for row "+i+", v.size="+_v.size());
            /*
            if(getFlag(Options.evaporation)&&!_evaporated.isEmpty()) {
                List<Rule> tr = new ArrayList<Rule>();
                for(Rule r:rules) {
                    if(!_evaporated.contains(r)) {
                        tr.add(r);
                    }
                }
                rules = (Rule[]) tr.toArray(new Rule[tr.size()]);
            }
            */
            //for(Rule r:rules) { ((Rule1D)r).setDebug(-1); }
            /*
            if(getFlag(Options.phaseTransitions)&&!_v.isEmpty()) {
                //for(Rule r:rules) {
                    //((Rule1D)r).uncache();
                //}
                for(VacuumMetastabilityDisaster v:_v) {
                    PhaseTransition t = phaseTransitions.get(v.rule());
                    if(t==null) {
                        Random subr = new Random(v.rule().toSeed());
                        Rule from = v.rule();
                        if(Options.instantaneousMutationProp.get() && subr.nextBoolean()) {
                            try {
                                Mutator noise = MutatorFactory.instance().createMutator(Mutators.noise, subr);
                                Rule nr = from.mutate(noise);
                                //nr.setFabric(this);
                                initChild(nr);
                                for(int k=0;k<rules.length;k++) {
                                    if(rules[k] == from) {
                                        rules[k] = nr;
                                        break;
                                    }
                                }
                                t = new PhaseTransition(v.rule(), nr, nr.colors()[1], false);
                                phaseTransitions.put(v.rule(), t);
                            }
                            catch(MutationFailedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            int nc = CA.randomColor(subr);
                            Mutator hue = MutatorFactory.instance().createHue(subr, false, nc);
                            while(true) {
                                try {
                                    Mutators muta = null;
                                    switch(subr.nextInt(8)) {
                                        case 5:
                                            muta = Mutators.thicken;
                                            break;
                                        case 4:
                                            muta = Mutators.thin;
                                            break;
                                        case 3:
                                            muta = Mutators.color;
                                            break;
                                        case 2:
                                            muta = Mutators.expand;
                                            break;
                                        case 1:
                                            muta = Mutators.collapse;
                                            break;
                                        default:
                                            muta = Mutators.noise;
                                            break;
                                    }
                                    Mutator noise = MutatorFactory.instance().createMutator(muta, subr);
                                    Rule nr = from.mutate(hue);
                                    nr = nr.mutate(noise);
                                    if(nr==null) throw new MutationFailedException("failed for "+muta);
                                    //nr.setFabric(this);
                                    initChild(nr);
                                    Rule[] nrs = new Rule[1+rules.length];
                                    System.arraycopy(rules, 0, nrs, 0, rules.length);
                                    nrs[nrs.length-1] = nr;
                                    //System.err.println(Thread.currentThread().getId()+" nrs length "+nrs.length);
                                    rules = nrs;
                                    //System.err.println(Thread.currentThread().getId()+" rules length "+rules.length);
                                    t = new PhaseTransition(v.rule(), nr, nc, true);
                                    phaseTransitions.put(v.rule(), t);
                                    reverseTransitions.put(nr, v.rule());
                                    break;
                                }
                                catch(MutationFailedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(t.mask()) {
                        if(t.dest().getMask()==null) {
                            t.dest().setMask(new BitSet());
                        }
                        t.dest().getMask().set(v.x());
                    }
                    int newcol = t.color();
                    if(getFlag(Options.stabilityReset)) {
                        newcol |= CA.ALPHA_MASK;
                    }
                    else {
                        newcol = CA.setAlpha(newcol, CA.alpha(c.get(v.x(), v.y())), 1);
                    }
                    //newcol = CA.combineAlpha(c.get(v.x(), v.y(), t.color());
                    //if(true||CA.alpha(newcol)!=255) {
                        //System.err.println("setting newcol alpha for "+v.x()+", "+v.y()+": "+CA.alpha(newcol));
                    //}
                    c.set(v.x(), v.y(), newcol);
                    //for(Rule r:rules) { ((Rule1D)r).setDebug(-1); }
                }
            }
        */
        }
        System.err.println(Thread.currentThread().getId()+" final rule count: "+rules.length);
        //_v.clear();
        //c.setGoodness(diff);
        return diff;
    }

    private void initChild(Rule r) {
        /*
        r.setFabric(this);
        for(Options o:EnumSet.allOf(Options.class)) {
            r.setFlag(o, getFlag(o));
        }
        */
    }
}
