package org.excelsi.nausicaa;


public interface Temporary {
    void associate(String key, String value);
    String resolve(String key);
    String resolve(String key, String def);
    void clear(String key);
}
