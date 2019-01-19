import org.excelsi.nausicaa.ca.*


meta = { ca ->
    [
      'name' : 'Param Generator',
      'args' : ['param', 'start', 'end', 'step', 'iter', 'file']
    ]
}

run = { ca, mf, params ->
    def param = params['param']
    def start = params['start'] as Float
    def end = params['end'] as Float
    def inc = params['step'] as Float
    def iter = params['iter'] as Integer
    def file = params['file']
    for(i=start;i<=end;i+=inc) {
        def cur = ca.vars(new Varmap().put(param, i))
            .mutate(ca.rule.origin().create(ca.rule.genome(), mf), ca.random)
        cur.frame(iter).save(file+'-'+i+'.png')
        System.err.println(i)
    }
}
