package org.excelsi.nausicaa.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;


public interface Ruleset extends java.io.Serializable {
    Iterator<Rule> iterator();
    //Rule fromString(String str);
    Iterator<Rule> random(Random r);
    Rule create(Object... args);
    Ruleset derive(int[] colors, int len);
}
