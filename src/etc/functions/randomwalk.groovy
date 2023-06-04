import org.excelsi.nausicaa.ca.*
import java.util.Random


class vr {
    float val
    float max
    float min
    float vel
    float accel
    int time
    int maxtime
    int dir
}

meta = { ca ->
    args = []
    if(ca!=null) {
        ca.vars.names.each { k ->
            args << k+' min'
            args << k+' max'
        }
    }
    args.addAll(['seed', 'frames', 'iter', 'file'])
    [
      'name' : 'Random Walk',
      'args' : args
    ]
}

run = { ca, args, api ->
    def frames = args['frames'] as Integer
    def iter = args['iter'] as Integer
    def file = args['file']
    def seed = args['seed']
    def rnd = new Random()
    if(!"".equals(seed.trim())) { rnd.setSeed(seed.trim() as Integer) }
    def vals = [:]
    def nargs = ca.vars.names()
    nargs.each { a ->
        def var = new vr()
        var.max = args["${a} max"] as Float 
        var.min = args["${a} min"] as Float
        var.val = var.min + rnd.nextFloat() * var.max
        var.vel = (2*rnd.nextFloat()-1) * (var.max - var.min) / 100
        var.accel = 0.99
        var.time = rnd.nextInt(50)+10
        var.maxtime = var.time
        var.dir = rnd.nextInt(3)
        vals[a] = var
    }
    int suf = 0
    def vars = new Varmap()
    api.progress.maximum = frames
    for(i=0;i<frames;i++) {
        api.progress.current = i
        nargs.each { a ->
            vars.put(a, vals[a].val as String)
        }
        def cur1 = ca.vars(vars.merge(ca.vars))
        def cur2 = cur1.mutate(cur1.rule.origin().create(cur1.rule.genome(), api.mutationFactor.withVars(cur1.vars)), ca.random)
        def it = cur2.compileRule().frameIterator(
            cur2.createPlane(api.pool, api.options),
            api.pool,
            api.options)
        System.err.println("iter ${i}: ${cur2.rule}, vars: ${cur2.vars}")
        def fr = it.next()
        for(j=1;j<iter;j++) {
            fr = it.next()
        }
        fr.save(file+'-'+suf+'.png', api.rendering)
        suf++
        if(api.cancelled) break;
        nargs.each { a ->
            def v = vals[a]
            def cur = v.val
            if(--v.time<=0) {
                v.time = rnd.nextInt(50)+10
                v.maxtime = v.time
                v.vel = (2*rnd.nextFloat()-1) * (v.max - v.min) / 100
                v.accel = 0.99
            }
            def delta = v.vel * (v.maxtime - Math.abs(v.maxtime/2f-v.time))/v.maxtime
            cur += delta
            if(cur>v.max) { cur = v.max; v.vel = -v.vel; }
            if(cur<v.min) { cur = v.min; v.vel = -v.vel; }
            v.val = cur
        }
    }
}
