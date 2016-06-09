package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface Fitness {
    public double evaluate(Archetype a, Plane... p);
}
