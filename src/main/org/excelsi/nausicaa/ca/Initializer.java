package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;


public interface Initializer {
    void init(Plane plane, Rule rule, Random random);
    void write(DataOutputStream dos) throws IOException;
}
