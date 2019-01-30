import org.excelsi.nausicaa.ca.*
import java.util.Random


class var {
    float val
    float max
    float min
    float vel
    float accel
    float orig
    float dest
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
      'name' : 'Random Walk 2',
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
        def var = new var()
        var.max = args["${a} max"] as Float 
        var.min = args["${a} min"] as Float
        var.val = var.min + rnd.nextFloat() * var.max
        var.vel = var.val
        var.orig = var.vel
        def wgt = rnd.nextFloat()
        var.dest = var.min*wgt + var.max*(1f-wgt)
        var.accel = 0.99
        //var.time = rnd.nextInt(100)+100
        var.time = 400f; //Math.abs(var.dest-var.orig)*400f
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
            def dst = v.dest
            def org = v.orig

            if(--v.time<=0) {
                v.time = rnd.nextInt(100)+100
                v.val = v.min + rnd.nextFloat() * v.max
                //v.orig = v.vel
                v.orig = v.dest
                def wgt = rnd.nextFloat()
                //v.dest = v.min + rnd.nextFloat() * v.max
                v.dest = v.min*wgt + v.max*(1f-wgt)
                v.time = 400f; //Math.abs(v.dest-v.orig)*400f
                System.err.println("reset time for ${a} to ${v.time}")
                v.maxtime = v.time
                //v.accel = 0.99
            }

            //def dlt = (dst+org - dst+org)/2f
            def midpoint = (dst+org)/2f
            def loc = midpoint/(dst+org);
            def locs = loc * loc
            def tm = (v.maxtime - v.time) - (v.maxtime/2)
            def tm2 = (v.maxtime - v.time) / (v.maxtime)
            //tm2 = tm2*tm2
            mt2 = Math.pow(Math.sin(3.1415*tm2),8)
            //cur = v.min + (tm * locs)
            cur = v.orig*(1f-mt2) + v.dest*(mt2)
            System.err.println("moved ${a} from ${v.val} to ${cur} in ${v.orig} to ${v.dest} with ${mt2} and time ${v.time}/${v.maxtime}")
            //def sqrt = Math.sqrt(midpoint)

            //def delta = v.vel * (v.maxtime - Math.abs(v.maxtime/2f-v.time))/v.maxtime

            //def move = (cur+dst)*delta

            //cur += delta
            //if(cur>v.max) { cur = v.max; v.vel = -v.vel; }
            //if(cur<v.min) { cur = v.min; v.vel = -v.vel; }
            v.val = cur
        }
    }
}
