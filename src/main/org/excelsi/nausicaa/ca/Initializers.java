package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public enum Initializers {
    random((byte)0),
    single((byte)1),
    neapolitan((byte)2),
    word((byte)3),
    image((byte)4),
    gaussian((byte)5);

    private final byte _id;

    private Initializers(byte id) {
        _id = id;
    }

    public byte getId() {
        return _id;
    }

    public Initializer create() {
        switch(this) {
            case single:
                return new SingleInitializer();
            case neapolitan:
                return new NeapolitanInitializer();
            case word:
                return new WordInitializer();
            case image:
                return new ImageInitializer();
            case gaussian:
                return new GaussianInitializer();
            case random:
            default:
                return new RandomInitializer();
        }
    }

    public static Initializer read(DataInputStream dos) throws IOException {
        byte id = dos.readByte();
        for(Initializers i:values()) {
            if(id==i.getId()) {
                return i.create();
            }
        }
        throw new IllegalStateException("unknown initializer id "+id);
    }
}
