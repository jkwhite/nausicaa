package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public enum Initializers {
    random((byte)0),
    single((byte)1);

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
