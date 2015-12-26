package org.excelsi.nausicaa.ca;


import java.util.Random;


public interface MutationStrategy {
    CA mutate(CA ca, Random random);
}
