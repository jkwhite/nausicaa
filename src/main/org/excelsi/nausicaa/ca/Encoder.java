package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface Encoder<T> {
    byte[] encode(T o);
}
