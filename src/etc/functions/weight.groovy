import org.excelsi.nausicaa.ca.*


meta = { ca ->
    [
      'name' : 'Weight Walk',
      'args' : ['start', 'end', 'step', 'iter', 'seed', 'file']
    ]
}

run = { ca, args, api ->
    def start = args['start'] as Double
    def end = args['end'] as Double
    def inc = args['step'] as Double
    def iter = args['iter'] as Integer
    def file = args['file']
    def seed = args['seed'] as Integer
    int suf = 0
    api.progress.maximum = ((end-start)/inc) as Integer
    for(i=start;i<=end;i+=inc) {
        api.progress.current = ((i-start)/inc) as Integer
        def genome = ca.rule.genome()
        def ngenome = genome.replaceFirst(/\/\d+(\.\d+)?/, "/${i}:")
        // def ngenome = genome.replaceFirst(/\/\d+(\.\d+)?(;.*)?:/, "/${i}:")
        api.log.info("iter ${i}: orig: ${genome}, new: ${ngenome}")
        def rnd = new Random()
        rnd.setSeed(seed)
        def cur2 = ca.mutate(ca.rule.origin().create(ngenome, api.mutationFactor), rnd).seed(seed)
        def it = cur2.compileRule().frameIterator(
            cur2.createPlane(api.pool, api.options),
            api.pool,
            api.options)
        def fr = it.next()
        for(j=1;j<iter;j++) {
            fr = it.next()
        }
        fr.save(file+'-'+suf+'.png', api.rendering)
        suf++
        if(api.cancelled) break;
    }
}
