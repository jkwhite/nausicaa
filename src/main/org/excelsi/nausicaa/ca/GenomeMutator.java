package org.excelsi.nausicaa.ca;


import java.util.LinkedList;


@FunctionalInterface
public interface GenomeMutator {
    void mutate(LinkedList<Codon> cs, Implicate im, GenomeFactory gf, MutationFactor m);
}
