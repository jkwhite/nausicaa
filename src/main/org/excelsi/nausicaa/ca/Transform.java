package org.excelsi.nausicaa.ca;


public interface Transform {
    String name();
    CA transform(CA c);
}
