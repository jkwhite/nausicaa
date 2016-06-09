package org.excelsi.nausicaa.ca;


import java.util.Random;


@FunctionalInterface
public interface MutationStrategy {
    CA mutate(CA ca, Random random, MutationFactor f);
}
