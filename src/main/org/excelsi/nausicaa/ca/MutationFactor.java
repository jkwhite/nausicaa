package org.excelsi.nausicaa.ca;


import java.util.function.Predicate;
import java.util.Random;


public final class MutationFactor {
    private int _alpha;
    private Random _r;
    private Predicate<Archetype> _validator;
    private String _mode = "normal";
    private int _stage;
    private float _transition;
    private boolean _symmetry;
    private boolean _weight;
    private boolean _rule;
    private boolean _inits;
    private boolean _meta;
    private Datamap _data;
    private Varmap _vars;
    private GenomeMutator _gm;
    private Language _lang;
    private Archetype _archetype;
    private boolean _trace;
    private boolean _bonding;
    private boolean _intrabond;


    public MutationFactor() {
        this(20, (a)->{return true;});
    }

    public MutationFactor(final int alpha, final Predicate<Archetype> validator) {
        _alpha = alpha;
        _validator = validator;
    }

    public int alpha() {
        return _alpha;
    }

    public MutationFactor withAlpha(int alpha) {
        _alpha = alpha;
        return this;
    }

    public Predicate<Archetype> validator() {
        return _validator;
    }

    public MutationFactor withValidator(Predicate<Archetype> validator) {
        _validator = validator;
        return this;
    }

    public MutationFactor withRandom(Random r) {
        _r = r;
        return this;
    }

    public Random random() {
        return _r;
    }

    public Random r() {
        return random();
    }

    public MutationFactor withMode(String m) {
        _mode = m;
        return this;
    }

    public String mode() {
        return _mode;
    }

    public MutationFactor withStage(int stage) {
        _stage = stage;
        return this;
    }

    public int stage() {
        //if(_stage==0) {
            //System.err.println("************* ZERO STAGE ***********");
            //Thread.dumpStack();
        //}
        return _stage;
    }

    public float transition() {
        return _transition;
    }

    public MutationFactor withTransition(float t) {
        _transition = t;
        return this;
    }

    public MutationFactor withSymmetry(boolean s) {
        _symmetry = s;
        return this;
    }

    public MutationFactor withUpdateWeight(boolean w) {
        _weight = w;
        return this;
    }

    public MutationFactor withRule(boolean r) {
        _rule = r;
        return this;
    }

    public MutationFactor withInitializers(boolean i) {
        _inits = i;
        return this;
    }

    public MutationFactor withMeta(boolean m) {
        _meta = m;
        return this;
    }

    public MutationFactor withLanguage(Language l) {
        _lang = l;
        return this;
    }

    public MutationFactor withTrace(boolean t) {
        _trace = t;
        return this;
    }

    public MutationFactor withBondMutations(boolean b) {
        _bonding = b;
        return this;
    }

    public MutationFactor withIntrabondMutations(boolean b) {
        _intrabond = b;
        return this;
    }

    public boolean symmetry() {
        return _symmetry;
    }

    public boolean updateWeight() {
        return _weight;
    }

    public boolean rule() {
        return _rule;
    }

    public boolean initializers() {
        return _inits;
    }

    public boolean meta() {
        return _meta;
    }

    public Language language() {
        return _lang;
    }

    public boolean trace() {
        return _trace;
    }

    public boolean bondMutations() {
        return _bonding;
    }

    public boolean intrabondMutations() {
        return _intrabond;
    }

    public MutationFactor withDatamap(Datamap dm) {
        _data = dm;
        return this;
    }

    public Datamap datamap() {
        return _data;
    }

    public MutationFactor withVars(Varmap m) {
        _vars = m;
        return this;
    }

    public Varmap vars() {
        return _vars;
    }

    public MutationFactor withArchetype(Archetype a) {
        _archetype = a;
        return this;
    }

    public Archetype archetype() {
        return _archetype;
    }

    public MutationFactor withGenomeMutator(GenomeMutator gm) {
        _gm = gm;
        return this;
    }

    public GenomeMutator genomeMutator() {
        return _gm;
    }

    public String toString() {
        return "{alpha:"+_alpha+"}";
    }

    public static MutationFactor defaultFactor() {
        return new MutationFactor(
            20,
            (a)->{return a.totalPatterns()<400000000;}
        );
    }
}
