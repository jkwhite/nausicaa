package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface Fitness {
    public float evaluate(Archetype a, Plane p);
}
