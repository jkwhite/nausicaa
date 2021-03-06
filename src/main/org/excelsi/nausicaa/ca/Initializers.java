package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import com.google.gson.*;


public enum Initializers {
    random((byte)0),
    single((byte)1),
    neapolitan((byte)2),
    word((byte)3),
    image((byte)4),
    gaussian((byte)5),
    clusteredgaussian((byte)6),
    ca((byte)7),
    custom((byte)8),
    coordinate((byte)9);

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
            case clusteredgaussian:
                return new ClusteredGaussianInitializer();
            case custom:
                return new CustomInitializer();
            case ca:
                return new CAInitializer();
            case coordinate:
                return new CoordinateInitializer();
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

    public static Initializer read(BufferedReader r, int v) throws IOException {
        String type = r.readLine();
        switch(type) {
            case "single":
                return SingleInitializer.read(r, v);
            case "neapolitan":
                return NeapolitanInitializer.read(r, v);
            case "word":
                return WordInitializer.read(r, v);
            case "image":
                return ImageInitializer.read(r, v);
            case "gaussian":
                return GaussianInitializer.read(r, v);
            case "clusteredgaussian":
                return ClusteredGaussianInitializer.read(r, v);
            case "ca":
                return CAInitializer.read(r, v);
            case "custom":
                return CustomInitializer.read(r, v);
            case "random":
                return RandomInitializer.read(r, v);
            default:
                throw new IOException("unknown initializer '"+type+"'");
        }
    }

    public static Initializer fromJson(JsonElement e) throws IOException {
        JsonObject o = (JsonObject) e;
        String type = Json.string(o, "type");
        switch(type) {
            case "single":
                return SingleInitializer.fromJson(o);
            case "neapolitan":
                return NeapolitanInitializer.fromJson(o);
            case "word":
                return WordInitializer.fromJson(o);
            case "image":
                return ImageInitializer.fromJson(o);
            case "gaussian":
                return GaussianInitializer.fromJson(o);
            case "clusteredgaussian":
                return ClusteredGaussianInitializer.fromJson(o);
            case "ca":
                return CAInitializer.fromJson(o);
            case "custom":
                return CustomInitializer.fromJson(o);
            case "random":
                return RandomInitializer.fromJson(o);
            case "coordinate":
                return CoordinateInitializer.fromJson(o);
            default:
                throw new IllegalArgumentException("unknown initializer '"+type+"'");
        }
    }
}
