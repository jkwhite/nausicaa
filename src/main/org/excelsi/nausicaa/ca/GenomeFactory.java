package org.excelsi.nausicaa.ca;


import java.util.Random;
import static org.excelsi.nausicaa.ca.WeightedFactory.weight;
import static org.excelsi.nausicaa.ca.Codons.*;


public class GenomeFactory {
    private static final WeightedFactory<Codon> G =
        new WeightedFactory<Codon>(
            //weight(1, new PushO()),
            weight(1, new Sum()),
            weight(1, new Sumn(1)),
            weight(1, new Subtract()),
            weight(1, new Multiply()),
            weight(1, new Divide()),
            weight(1, new Min()),
            weight(1, new Min(1)),
            weight(1, new Max()),
            weight(1, new Max(1)),
            weight(1, new Avg()),
            weight(1, new Avg(1)),
            weight(1, new Pow()),
            weight(1, new Xor()),
            weight(1, new And()),
            weight(1, new Or()),
            weight(1, new Rotleft()),
            weight(1, new Rotright()),
            weight(1, new Skip(-1)),
            //weight(1, new Histo(1)),
            //weight(1, new Sumn(-1)),
            weight(1, new Equals()),
            weight(1, new NotEquals()),
            weight(1, new Intersects()),
            weight(1, new If()),
            weight(1, new Time()),
            weight(1, new Nonzero(-1)),
            weight(1, new Push(4)),
            //weight(0, new Push(-1)),
            weight(1, new Constant(-1)),
            weight(1, new Duplicate()),
            weight(1, new Exclamatory()),
            weight(1, new Mod())
        );


    public Genome generate(final Random r) {
        final StringBuilder b = new StringBuilder(Codons.HISTO+"-");
        int len = 3+r.nextInt(12);
        for(int i=0;i<len;i++) {
            final Codon c = G.random(r);
            b.append(c.generate(r));
            b.append("-");
        }
        b.setLength(b.length()-1);
        return new Genome(b.toString());
    }

    public Codon randomCodon(final Archetype a, final Random r) {
        return Codons.codon(G.random(r).generate(r), a);
    }
}
