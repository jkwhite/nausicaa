package org.excelsi.nausicaa.ca;


public interface Encoder {
    byte[] encode(Object o);
    Object decode(byte[] encoded);
}
