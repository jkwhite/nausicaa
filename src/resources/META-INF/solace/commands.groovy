import org.excelsi.nausicaa.ca.*;
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
    Ruleset.create(new Archetype(d,s,c), h)
}

init_single = { Initializers.single.create() }

init_random = { rnd=__random__, seed=19771026, zero=0 ->
    new RandomInitializer(rnd, seed, new RandomInitializer.Params(zero))
}

ca = { dims, rule, pal=null, init=null, prelude=0, weight=1f ->
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
    def c = new CA(rule, p, init, __random__, __random__.nextInt(), dims[0], dims[1], dims.size()>2?dims[2]:1, prelude, weight, 0, ComputeMode.channel);
    //rule.init(c, Rule.Initialization.single);
    //rule.generate(c, 1, hei, false, false, null);
    //c.rule = rule;
    return c;
}

load = { f ->
    CA.fromFile(f)
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

org.excelsi.nausicaa.ca.CA.metaClass.animate = {
    new Animated(delegate)
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
            Node img = renderers.render(init.toJfxImage(), p);
            Iterator fr = o.getCA().getRule().frameIterator(init, Pools.adhoc(), new GOptions())
            Runnable r = new Runnable() {
                public void run() {
                    Thread.sleep(250)
                    Plane nxt = fr.next();
                    Platform.runLater(new Runnable() {
                        public void run() {
                            //img.setImage(nxt.toJfxImage(img.getImage()))
                            //nxt.toJfxImage(img.getImage())
                            img.setImage(nxt.toJfxImage(img.getImage()))
                        }
                    })
                    if(img.isVisible()) {
                        Pools.adhoc().submit(this)
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

incant = { s ->
}
