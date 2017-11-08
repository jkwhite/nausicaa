package org.excelsi.nausicaa.ca;


import java.util.Random;
import static org.excelsi.nausicaa.ca.WeightedFactory.weight;
import static org.excelsi.nausicaa.ca.Codons.*;


public class GenomeFactory {
    private static final WeightedFactory<Codon> buildFactory(final Archetype a) {
        return new WeightedFactory<Codon>(
            weight(2, new PushO()),
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
            weight(2, new Histo(1)),
            //weight(1, new Sumn(-1)),
            weight(1, new Equals()),
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
            weight(2, new Constant(-1)),
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
            weight(1, new Pos()),
            weight(1, new Mod())
        );
    }


    public Genome generate(final Archetype a, final Random r) {
        final WeightedFactory<Codon> f = buildFactory(a);
        final StringBuilder b = new StringBuilder(Codons.PUSH_ALL+"-");
        int len = 1+r.nextInt(6);
        for(int i=0;i<len;i++) {
            final Codon c = f.random(r);
            b.append(c.generate(r));
            b.append("-");
        }
        b.setLength(b.length()-1);
        return new Genome(b.toString());
    }

    public Codon randomCodon(final Archetype a, final Random r) {
        return Codons.codon(buildFactory(a).random(r).generate(r), a);
    }
}
