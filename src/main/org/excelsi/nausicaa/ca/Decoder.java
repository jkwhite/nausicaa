package org.excelsi.nausicaa.ca;


@FunctionalInterface
public interface Decoder<T> {
    T decode(Plane p);
}
