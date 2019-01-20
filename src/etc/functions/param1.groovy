import org.excelsi.nausicaa.ca.*


meta = { ca ->
    [
      'name' : 'Param Generator',
      'args' : ['variable', 'start', 'end', 'step', 'iter', 'file']
    ]
}

run = { ca, args, api ->
    def variable = args['variable']
    def start = args['start'] as Float
    def end = args['end'] as Float
    def inc = args['step'] as Float
    def iter = args['iter'] as Integer
    def file = args['file']
    int suf = 0
    for(i=start;i<=end;i+=inc) {
        def vars = new Varmap().put(variable, i as String)
        def cur1 = ca.vars(ca.vars.merge(vars))
        def cur2 = cur1.mutate(cur1.rule.origin().create(cur1.rule.genome(), api.mutationFactor.withVars(vars)), ca.random)
        def it = cur2.compileRule().frameIterator(
            cur2.createPlane(api.pool, api.options),
            api.pool,
            api.options)
        System.err.println("iter ${i}: ${cur2.rule}, vars: ${vars}")
        def fr = it.next()
        for(j=1;j<iter;j++) {
            fr = it.next()
        }
        fr.save(file+'-'+suf+'.png', api.rendering)
        suf++
    }
}
