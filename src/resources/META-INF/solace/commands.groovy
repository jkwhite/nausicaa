import org.excelsi.nausicaa.ca.*;
import org.excelsi.solace.Jfx;
import org.excelsi.solace.JfxRenderer;
import org.excelsi.solace.JfxRendererRegistry;
import org.excelsi.solace.Painter;
import org.excelsi.solace.Predicates;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

ExpandoMetaClass.enableGlobally()

__random__ = new java.util.Random();

//rules = new Rulespace1D( [ new Ruleset1D( [CA.randomColor(), CA.randomColor()].toArray(new Integer[0]) ) ].toArray(new Ruleset1D[0]) )

rules = { d, s, c, h=null ->
    if(h!=null) {
        Ruleset.create(new Archetype(d,s,c), h)
    }
    else {
        Ruleset.create(new Archetype(d,s,c))
    }
}

test_incant = { d,s,c ->
    new ComputedRuleset(new Archetype(d,s,c))
}

incant = { d, s, c, i ->
    new ComputedRuleset(new Archetype(d,s,c)).create(i)
}

rule = { rs, r ->
    GenomeParser.parse(r, rs)
}

init_single = { c=-1, x=-1, y=-1, z=-1 ->
    //Initializers.single.create()
    new SingleInitializer(c, x, y, z, 1)
}

init_random = { rnd=__random__, seed=19771026, zero=0 ->
    new RandomInitializer(rnd, seed, new RandomInitializer.Params(zero))
}

init_custom = { txt, w=0, h=0, x=0, y=0 ->
    System.err.println("custom init with text '${txt}'")
    if(x==-1) {
        tmp = txt.trim()
        idx = tmp.indexOf('\n')
        if(idx==-1) idx = txt.length()
        x = (int)(w/2-idx/2)
    }
    if(y==-1) {
        cnt=0
        for(int i=0;i<txt.length();i++) { if(txt.charAt(i)=='\n') cnt++ }
        y = (int)(h/2 - cnt/2)
    }
    new CustomInitializer("i.draw(${x},${y},'''${txt}''')")
}

ca = { dims, rule, pal=null, init=null, prelude=0, weight=1d ->
    if(pal==null) {
        p = Palette.random(rule.archetype().colors(), __random__);
    }
    else if(pal instanceof Palette) {
        p = pal
    }
    else {
        p = new IndexedPalette(pal.collect { Colors.fromString(it) })
    }
    if(init==null) {
        init = Initializers.random.create()
    }
    def c = new CA(rule, p, init, __random__, __random__.nextLong(), dims[0], dims[1], dims.size()>2?dims[2]:1, prelude, weight, 0, ComputeMode.channel, MetaMode.none, new UpdateMode.SimpleSynchronous(), EdgeMode.defaultMode(), ExternalForce.nop(), new Varmap(), null, 'Nameless', 'Nameless');
    //rule.init(c, Rule.Initialization.single);
    //rule.generate(c, 1, hei, false, false, null);
    //c.rule = rule;
    return c;
}

load = { f ->
    CA.fromFile(f, 'text')
}

org.excelsi.nausicaa.ca.Ruleset.metaClass.random = {
    random(__random__)
}

org.excelsi.nausicaa.ca.Ruleset.metaClass.rule = { id ->
    create(id)
}

org.excelsi.nausicaa.ca.Rule.metaClass.ca = { dims, pal=null, init=null ->
    ca(dims, delegate, pal, init)
}

org.excelsi.nausicaa.ca.CA.metaClass.scaled = { scale ->
    delegate.createPlane().scale(scale, scale<1?true:false).toJfxImage()
}

org.excelsi.nausicaa.ca.CA.metaClass.animate = { scale=1f, frames=-1, rate=250 ->
    new Animated(delegate, scale, frames, rate)
}

org.excelsi.nausicaa.ca.CA.metaClass.evolve = { epicycles, cycles, subcycles, population, idealPm, idealNrsdev, idealNrmean ->
    def Evolver evolver = new EvolverBuilder()
        .withEncoder(null)
        .withTraining(Training.of(new RandomInitializer(1)))
        .withFitness(FitnessCriteria.interesting3(idealPm, idealNrsdev, idealNrmean))
        .withMutationStrategy(new RetryingMutationStrategy(new RandomMutationStrategy(MutatorFactory.defaultMutators(), true), MutationStrategies.noise(), 4))
        .withPopulation(population)
        .withBirthRate(0.2)
        .withDeathRate(0.2)
        .build()
    def evolved = evolver.run(delegate,
            __random__,
            epicycles,
            cycles,
            subcycles,
            9,
            Pools.core())
    evolved
}

org.excelsi.nausicaa.ca.CA.metaClass.stats = { depth=1 ->
    def c = delegate.size(delegate.width, delegate.height, depth)
    def p = c.createPlane()
    def np = c.rule.frameIterator(p, Pools.adhoc(), false).next()
    def ms = Stats.forPlane(p).compareWith(Stats.forPlane(np))
}

org.excelsi.nausicaa.ca.Plane.metaClass.save = { filename ->
    def p = delegate.toBufferedImage();
    Pipeline.write(p, filename);
}

org.excelsi.nausicaa.ca.CA.metaClass.mutateGenome = { genome ->
    def f = new MutationFactor()
    delegate.mutate(delegate.getRule().origin().create(genome, f), delegate.getRandom())
}

org.excelsi.nausicaa.ca.CA.metaClass.frame = { int nFrames ->
    def pool = Executors.newFixedThreadPool(6);
    def it = delegate.getRule().frameIterator(delegate.createPlane(), pool, new GOptions(true, 7, 0, 1d));

    def fr = it.next();
    for(int i=1;i<nFrames;i++) {
        fr = it.next();
    }
    pool.shutdown();
    fr
}

org.excelsi.nausicaa.ca.CA.metaClass.frames = { int nFrames ->
    def pool = Executors.newFixedThreadPool(6);
    def it = delegate.getRule().frameIterator(delegate.createPlane(), pool, new GOptions(false, 7, 0, 1d));

    def fr = []
    for(int i=1;i<nFrames;i++) {
        fr << it.next();
    }
    pool.shutdown();
    fr
}

org.excelsi.nausicaa.ca.CA.metaClass.generate = { fileTemplate, nFrames, scale ->
    def pool = Executors.newFixedThreadPool(4);
    if(scale==1f) {
        delegate.getRule()
            .stream(delegate.createPlane(), pool, true)
            .limit(nFrames)
            .map(Pipeline.context("p", "i", Pipeline.identifier()))
            .map(Pipeline.toBufferedImage("p", "b"))
            .forEach(Pipeline
                .writeFormat("b", "i", fileTemplate)
            );
    }
    else {
        delegate.getRule()
            .stream(delegate.createPlane(), pool, true)
            .limit(nFrames)
            .map(Pipeline.context("p", "i", Pipeline.identifier()))
            .map(Pipeline.toBufferedImage("p", "b"))
            .map(Pipeline.indexed2Rgb("b", "b"))
            .map(Pipeline.scale("b", "b", scale))
            .forEach(Pipeline
                .writeFormat("b", "i", fileTemplate)
            );
    }
    pool.shutdown()
    delegate
}

$r.register(
    Predicates.instof(CA),
    new JfxRenderer() {
        Node render(Object o, Painter p, JfxRendererRegistry renderers) {
            return renderers.render(o.createPlane().toJfxImage(), p);
        }
    })

$r.register(
    Predicates.instof(Animated),
    new JfxRenderer() {
        Node render(Object o, Painter p, JfxRendererRegistry renderers) {
            Plane init = o.getCA().createPlane();
            Node img
            if(o.getScale()!=1f) {
                img = renderers.render(init.scale((float)o.getScale(), o.getScale()<1?true:false).toJfxImage(), p)
            }
            else {
                img = renderers.render(init.toJfxImage(), p);
            }
            /*if(o.getScale()!=1f) {
                img.setFitWidth(o.getCA().getWidth()*o.getScale());
                img.setFitHeight(o.getCA().getHeight()*o.getScale());
                if(o.getScale()>1) {
                    System.err.println("disabling smoothing")
                    img.setSmooth(false)
                }
            }*/
            ExecutorService execPool = Pools.named("exec", 1)
            Iterator fr = o.getCA().getRule().frameIterator(init, execPool, new GOptions())
            int nframes = 0
            Runnable r = new Runnable() {
                public void run() {
                    Thread.sleep(o.getRate())
                    Plane nxt = fr.next();
                    ++nframes;
                    Platform.runLater(new Runnable() {
                        public void run() {
                            if(o.getScale()==1) {
                                img.setImage(nxt.toJfxImage(img.getImage()))
                            }
                            else {
                                img.setImage(nxt.scale((float)o.getScale(), o.getScale()<1?true:false).toJfxImage(img.getImage()))
                            }
                        }
                    })
                    //try {
                        //System.err.println("parent: "+img.getParent()+" root: "+Jfx.rootFor((Node)img.getParent()))
                        //System.err.println("vis: "+img.isVisible()+", par: "+img.getParent().isVisible())
                    //}
                    //catch(Exception e) {
                        //e.printStackTrace();
                    //}
                    if(img.getParent()==null||Jfx.rootFor(img)==null||!img.isVisible()||(nframes>=o.getFrames()&&o.getFrames()!=-1)) {
                        System.err.println("shutting down animator")
                        execPool.shutdownNow()
                    }
                    else {
                        Pools.adhoc().submit(this)
                        //System.err.print(".")
                    }
                }
            };
            Pools.adhoc().submit(r)
            return img;
        }
    })

/*
org.excelsi.nausicaa.ca.Rule.metaClass.ca = { wid, hei ->
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

org.excelsi.nausicaa.ca.Rule.metaClass.mog = { m=null ->
    mutate(delegate, m)
}

org.excelsi.nausicaa.ca.Rule.metaClass.var = { cnt=8 ->
    (0..cnt).collect { mutate(delegate) }
}
*/

// table(8, (0..255).collect { ca(200, 100, wolfram(it)).label("rule ${it}") }, [padding:10] )
// chaos.space1d().random().next().var().collect { it.ca(200,100) }.table(3)

pal_grey = { d ->
    pal = []
    for(i=0;i<=255;i+=255f / (d-1)) {
        hex = Integer.toString(i.intValue(), 16); //.substring(2)
        //pal << Colors.fromString("0x${hex}${hex}${hex}")
        pal << "0x${hex}${hex}${hex}"
    }
    /*new Palette(pal)*/
    pal
}

new_ca = { d=1, c=2, s=1, dims=[200,200,1], pal=null, init=null ->
    ca(dims, rules(d,s,c).random(__random__).next(), pal, init)
}

load_ca = { f ->
    CA.fromTextFile(f)
}
