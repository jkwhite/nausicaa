import org.excelsi.ca.*;

ExpandoMetaClass.enableGlobally()

//rules = new Rulespace1D( [ new Ruleset1D( [CA.randomColor(), CA.randomColor()].toArray(new Integer[0]) ) ].toArray(new Ruleset1D[0]) )

ca = { wid, hei, rule ->
    def c = new CA(wid, hei);
    rule.init(c, Rule.Initialization.single);
    rule.generate(c, 1, hei, false, false, null);
    c.rule = rule;
    return c;
}

org.excelsi.ca.Rule.metaClass.ca = { wid, hei ->
    ca(wid, hei, delegate)
}

wolfram = {
    def g = Gray.rule(it)
    return g
}

mutate = { rule, m=null ->
    if(m==null) {
        while(true) {
            try {
                return rule.mutate(MutatorFactory.instance().createRandomMutator(Rand.om))
            }
            catch(MutationFailedException e) {
            }
        }
    }
    else {
        return rule.mutate(MutatorFactory.instance().createMutator(Enum.valueOf(Mutators.class, m)));
    }
}

chaos = new Chaos();

org.excelsi.ca.Rule.metaClass.mog = { m=null ->
    mutate(delegate, m)
}

org.excelsi.ca.Rule.metaClass.var = { cnt=8 ->
    (0..cnt).collect { mutate(delegate) }
}

// table(8, (0..255).collect { ca(200, 100, wolfram(it)).label("rule ${it}") }, [padding:10] )
// chaos.space1d().random().next().var().collect { it.ca(200,100) }.table(3)
