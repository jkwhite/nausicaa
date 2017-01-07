package org.excelsi.nausicaa.ca;


import java.util.Random;
import static org.excelsi.nausicaa.ca.WeightedFactory.weight;
import static org.excelsi.nausicaa.ca.Codons.*;


public class GenomeFactory {
    private static final WeightedFactory<Codon> G =
        new WeightedFactory<Codon>(
            weight(1, new PushO()),
            weight(1, new Sum()),
            weight(1, new Subtract()),
            weight(1, new Multiply()),
            weight(1, new Divide()),
            weight(1, new Min()),
            weight(1, new Max()),
            weight(1, new Pow()),
            weight(1, new Avg()),
            weight(2, new Sumn(-1)),
            weight(1, new Equals()),
            weight(3, new Intersects()),
            weight(2, new If()),
            weight(2, new Push(4)),
            weight(2, new Push(-1)),
            weight(2, new Constant(-1)),
            weight(1, new Mod())
        );


    public Genome generate(final Random r) {
        final StringBuilder b = new StringBuilder();
        int len = 3+r.nextInt(8);
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
