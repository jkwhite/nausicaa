package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.function.Predicate;
import static org.excelsi.nausicaa.ca.WeightedFactory.Weight;
import static org.excelsi.nausicaa.ca.WeightedFactory.weight;
import static org.excelsi.nausicaa.ca.Codons.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class GenomeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GenomeFactory.class);

    private static final WeightedFactory<Codon> buildFactory(final Implicate im) {
        final Archetype a = im.archetype();
        List<Weight<Codon>> cs = new ArrayList<>(Arrays.asList(
            weight(2, new PushO()),
            weight(1, new PushC()),
            weight(1, new Sum()),
            weight(1, new Sumn(1)),
            weight(1, new SumnN()),
            weight(1, new Subtract()),
            weight(1, new Multiply()),
            weight(1, new Divide()),
            weight(1, new Min()),
            //weight(1, new Min(1)),
            weight(1, new MinN()),
            weight(1, new Max()),
            //weight(1, new Max(1)),
            weight(1, new MaxN()),
            weight(1, new Avg()),
            //weight(1, new Avg(1)),
            weight(1, new AvgN()),
            weight(1, new Pow()),
            weight(1, new Sqrt()),
            weight(1, new Cbrt()),
            weight(1, new Xor()),
            weight(1, new And()),
            weight(1, new Or()),
            weight(1, new Rotleft()),
            weight(1, new Rotright()),
            weight(1, new Skip(-1)),
            weight(1, new SkipN()),
            //weight(2, new Histo(1)),
            //weight(1, new Sumn(-1)),
            weight(1, new Equals()),
            weight(1, new EqualsA(-1)),
            weight(1, new NotEquals()),
            weight(1, new Intersects()),
            weight(1, new IntersectsSelf()),
            weight(1, new If()),
            weight(1, new Time()),
            weight(1, new Nonzero(-1)),
            weight(2, new Push((a.sourceLength()-1)/2, a.sourceLength())),
            weight(2, new PushN()),
            weight(2, new PushS()),
            weight(2, new PushA()),
            weight(2, new PushARot()),
            weight(2, new Push(-1, a.sourceLength())),
            weight(2, new Constant(-1, -1)),
            weight(1, new Duplicate()),
            weight(1, new Supersymmetry(a.colors()-1)),
            weight(1, new RotVecN(a.sourceLength())),
            weight(1, new GreaterThan()),
            weight(1, new LessThan()),
            weight(1, new Negate()),
            weight(1, new Lesser()),
            weight(1, new Greater()),
            weight(1, new Codons.Fork(-1, 10, a.colors()-1)),
            weight(1, new Stop()),
            weight(1, new Abort()),
            weight(1, new Pos()),
            weight(1, new Sigmoid()),
            weight(1, new Cos()),
            weight(1, new Sin()),
            weight(1, new Tanh()),
            weight(0, new Count()),
            weight(1, new CountFixed()),
            weight(1, new Jump()),
            weight(1, new Halt()),
            weight(1, new Convolve(a.sourceLength())),
            weight(1, new Codons.Rand()),
            weight(1, new Most(-1)),
            weight(1, new Least(-1)),
            weight(1, new Abs()),
            weight(1, new Mod())
        ));
        if(a.isContinuous()) {
            cs.add(weight(1,new HistoTroveFloat()));
            weight(1, new Exclamatory()); // currently unimplemented
        }
        else {
            cs.add(weight(1,new HistoTroveInt()));
            if(a.colors()<=10) {
                weight(1, new Exclamatory());
            }
        }
        if(true) {
            cs.add(weight(1,new Coord(-1)));
            cs.add(weight(1,new Coord(0)));
            cs.add(weight(1,new Coord(1)));
            cs.add(weight(1,new CoordRel(-1)));
            cs.add(weight(1,new CoordRel(0)));
            cs.add(weight(1,new CoordRel(1)));
            cs.add(weight(1,new Mandelbrot()));
            cs.add(weight(1,new Mandelbulb()));
        }
        for(Iterator<Weight<Codon>> it=cs.iterator();it.hasNext();) {
            final Weight<Codon> wc = it.next();
            if(!wc.e().supports(a.values()) ||
                ! im.language().accept(wc.e())) {
                //LOG.warn("LANG filtered out "+wc.e());
                it.remove();
            }
        }
        for(String ch:im.language().chains()) {
            LOG.info("adding chain '"+ch+"'");
            cs.add(weight(1,Codons.codon(ch, im)));
        }
        return new WeightedFactory<Codon>(cs.toArray(new Weight[0]));
    }

    /*
    private static final WeightedFactory<Codon> buildFactory(final Archetype a) {
        List<Weight<Codon>> cs = new ArrayList<>(Arrays.asList(
            weight(2, new PushO()),
            weight(1, new PushC()),
            weight(1, new Sum()),
            weight(1, new Sumn(1)),
            weight(1, new SumnN()),
            weight(1, new Subtract()),
            weight(1, new Multiply()),
            weight(1, new Divide()),
            weight(1, new Min()),
            //weight(1, new Min(1)),
            weight(1, new MinN()),
            weight(1, new Max()),
            //weight(1, new Max(1)),
            weight(1, new MaxN()),
            weight(1, new Avg()),
            //weight(1, new Avg(1)),
            weight(1, new AvgN()),
            weight(1, new Pow()),
            weight(1, new Sqrt()),
            weight(1, new Cbrt()),
            weight(1, new Xor()),
            weight(1, new And()),
            weight(1, new Or()),
            weight(1, new Rotleft()),
            weight(1, new Rotright()),
            weight(1, new Skip(-1)),
            weight(1, new SkipN()),
            //weight(2, new Histo(1)),
            //weight(1, new Sumn(-1)),
            weight(1, new Equals()),
            weight(1, new EqualsA(-1)),
            weight(1, new NotEquals()),
            weight(1, new Intersects()),
            weight(1, new IntersectsSelf()),
            weight(1, new If()),
            //weight(1, new Time()),
            weight(1, new Nonzero(-1)),
            weight(2, new Push((a.sourceLength()-1)/2, a.sourceLength())),
            weight(2, new PushN()),
            weight(2, new PushS()),
            weight(2, new PushA()),
            weight(2, new PushARot()),
            weight(2, new Push(-1, a.sourceLength())),
            weight(2, new Constant(-1, -1)),
            weight(1, new Duplicate()),
            weight(1, new Exclamatory()),
            weight(1, new Supersymmetry(a.colors()-1)),
            weight(1, new RotVecN(a.sourceLength())),
            weight(1, new GreaterThan()),
            weight(1, new LessThan()),
            weight(1, new Negate()),
            weight(1, new Lesser()),
            weight(1, new Greater()),
            weight(1, new Codons.Fork(-1, 10, a.colors()-1)),
            weight(1, new Stop()),
            weight(1, new Abort()),
            weight(1, new Pos()),
            weight(1, new Sigmoid()),
            weight(1, new Cos()),
            weight(1, new Sin()),
            weight(1, new Tanh()),
            weight(1, new Count()),
            weight(1, new Jump()),
            weight(1, new Halt()),
            weight(1, new Convolve(a.sourceLength())),
            weight(1, new Codons.Rand()),
            weight(1, new Most(-1)),
            weight(1, new Least(-1)),
            weight(1, new Abs()),
            weight(1, new Mod())
        ));
        if(a.isContinuous()) {
            cs.add(weight(1,new HistoTroveFloat()));
        }
        else {
            cs.add(weight(1,new HistoTroveInt()));
        }
        if(true) {
            cs.add(weight(1,new Coord(-1)));
            cs.add(weight(1,new Coord(0)));
            cs.add(weight(1,new Coord(1)));
            cs.add(weight(1,new CoordRel(-1)));
            cs.add(weight(1,new CoordRel(0)));
            cs.add(weight(1,new CoordRel(1)));
            cs.add(weight(1,new Mandelbrot()));
        }
        for(Iterator<Weight<Codon>> it=cs.iterator();it.hasNext();) {
            if(!it.next().e().supports(a.values())) {
                it.remove();
            }
        }
        return new WeightedFactory<Codon>(cs.toArray(new Weight[0]));
    }
    */

    /*
    private static final WeightedFactory<Codon> buildSymmetricFactory(final Archetype a) {
        List<Weight<Codon>> cs = new ArrayList<>(Arrays.asList(
            //weight(2, new PushO()),
            weight(1, new Sum()),
            weight(1, new Sumn(1)),
            weight(1, new SumnN()),
            weight(1, new Subtract()),
            weight(1, new Multiply()),
            weight(1, new Divide()),
            weight(1, new Min()),
            weight(1, new MinN()),
            weight(1, new Max()),
            weight(1, new MaxN()),
            weight(1, new Avg()),
            weight(1, new AvgN()),
            weight(1, new Pow()),
            weight(1, new Sqrt()),
            weight(1, new Cbrt()),
            weight(1, new Xor()),
            weight(1, new And()),
            weight(1, new Or()),
            weight(1, new Rotleft()),
            weight(1, new Rotright()),
            weight(1, new Skip(-1)),
            weight(1, new SkipN()),
            //weight(2, new Histo(1)),
            weight(1, new Equals()),
            weight(1, new NotEquals()),
            weight(1, new Intersects()),
            weight(1, new NotIntersects()),
            weight(1, new IntersectsSelf()),
            weight(1, new If()),
            weight(1, new Nonzero(-1)),
            //weight(2, new Push((a.sourceLength()-1)/2, a.sourceLength())),
            //weight(2, new PushN()),
            weight(2, new PushS()),
            //weight(2, new PushA()),
            //weight(2, new PushARot()),
            //weight(2, new Push(-1, a.sourceLength())),
            weight(2, new Constant(-1, -1)),
            weight(1, new Duplicate()),
            weight(1, new Exclamatory()),
            weight(1, new Supersymmetry(a.colors()-1)),
            weight(1, new RotVecN(a.sourceLength())),
            weight(1, new GreaterThan()),
            weight(1, new LessThan()),
            weight(1, new Negate()),
            weight(1, new Lesser()),
            weight(1, new Greater()),
            weight(1, new Codons.Fork(-1, 10, a.colors()-1)),
            weight(1, new Stop()),
            weight(1, new Abort()),
            weight(1, new Pos()),
            weight(1, new Not()),
            weight(1, new Sigmoid()),
            weight(1, new Tanh()),
            weight(1, new Count()),
            weight(1, new Jump()),
            weight(1, new Halt()),
            weight(1, new Convolve(a.sourceLength())),
            weight(1, new Mod())
        ));
        if(a.colors()<1000000) {
            cs.add(weight(1,new Histo(1)));
        }
        return new WeightedFactory<Codon>(cs.toArray(new Weight[0]));
    }
    */

        /*
    public List<Codon> allCodons(final Archetype a) {
        WeightedFactory<Codon> f = buildFactory(a);
        List<Codon> cs = new ArrayList<>();
        for(Weight<Codon> wc:f.all()) {
            cs.add(wc.e());
        }
        return cs;
    }
    */

        /*
    public List<Codon> codons(final Archetype a, Predicate<Codon> p) {
        WeightedFactory<Codon> f = buildFactory(a);
        List<Codon> cs = new ArrayList<>();
        for(Weight<Codon> wc:f.all()) {
            if(p.test(wc.e())) {
                cs.add(wc.e());
            }
        }
        return cs;
    }
    */

    //public Genome generate(final Archetype a, final Random r) {
    public Genome generate(final Implicate im, final Random r) {
        final Archetype a = im.archetype();
        //final WeightedFactory<Codon> f = buildFactory(a);
        final WeightedFactory<Codon> f = buildFactory(im);
        final StringBuilder b = new StringBuilder(Codons.PUSH_ALL+" ");
        //final StringBuilder b = new StringBuilder(Codons.HISTO+"-");
        int len = 2+r.nextInt(12);
        for(int i=0;i<len;i++) {
            final Codon c = f.random(r);
            b.append(c.generate(r));
            b.append(" ");
        }
        b.setLength(b.length()-1);
        return new Genome(b.toString());
    }

    public Codon randomCodon(final Implicate im, final Random r) {
        return Codons.codon(buildFactory(im).random(r).generate(r), im);
        /*
        if(im.language()!=null) {
            return Codons.codon(im.language().randomCodon(im.archetype(), r), im);
        }
        else {
            //return Codons.codon(buildFactory(im.archetype()).random(r).generate(r), im);
            return Codons.codon(buildFactory(im).random(r).generate(r), im);
        }
        */
    }
}
