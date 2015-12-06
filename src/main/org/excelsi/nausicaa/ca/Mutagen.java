package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface Mutagen {
    void mutate(Archetype a, byte[] bytes);
}
