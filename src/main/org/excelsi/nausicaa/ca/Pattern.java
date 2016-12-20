package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;


public interface Pattern {
    Archetype archetype();
    byte next(int pattern, byte[] p2);
    void tick();
    //void write(DataOutputStream dos) throws IOException;
    //void write(Writer w) throws IOException;
}
