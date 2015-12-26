package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public final class IndexedPattern {
    private final Archetype _a;
    private final long _id;
    private final int _length;
    private final byte[] _target;


    /**
     * Constructs a new Pattern.
     *
     * @param length number of bytes in pattern, e.g. 3 or 9
     * @param colors number of colors in pattern
     * @param next end results
     */
    public IndexedPattern(Archetype a, long id, int length, byte[] target) {
        _a = a;
        _id = id;
        _length = length;
        _target = target;
    }

    public long id() {
        return _id;
    }

    public byte next(final int pattern) {
        if(pattern>=_target.length) {
            return 0;
        }
        return _target[pattern];
    }

    public byte next(final int pattern, final int offset) {
        int p = pattern + offset;
        while(p>=_target.length) {
            p -= _target.length;
        }
        return _target[p];
        //int b = (_target[pattern]+offset) % _a.colors();
        //int b = (_target[pattern]+offset);
        //if(b>=_a.colors()) {
            //b = (_a.colors()-1);
        //}
        //return (byte) b;
    }

    public int length() {
        return _length;
    }

    public Archetype archetype() {
        return _a;
    }

    public IndexedPattern transform(Transform t) {
        final byte[] newTarget = new byte[_target.length];
        System.arraycopy(_target, 0, newTarget, 0, newTarget.length);
        t.modulate(_a, newTarget);
        return new IndexedPattern(_a, 0, _length, newTarget);
    }

    public IndexedPattern copy() {
        return transform((a, t)->{});
    }

    public void inspect(Transform t) {
        t.modulate(_a, _target);
    }

    public void mutate(Mutagen m) {
        m.mutate(_a, _target);
    }

    public IndexedPattern transform(Archetype a, BinaryTransform t) {
        return Patterns.custom(_a, _target, a, t);
    }

    @Override public String toString() {
        return Patterns.formatPattern(_target);
    }

    public String formatTarget() {
        return Patterns.formatPattern(_target);
    }

    public String summarize() {
        return _a.toString();
    }

    public void write(DataOutputStream dos) throws IOException {
        _a.write(dos);
        //dos.writeLong(_id);
        dos.writeInt(_length);
        dos.writeInt(_target.length);
        for(byte b:_target) {
            dos.writeByte(b);
        }
    }

    public static IndexedPattern read(DataInputStream dis) throws IOException {
        Archetype a = Archetype.read(dis);
        int len = dis.readInt();
        int tlen = dis.readInt();
        byte[] target = new byte[tlen];
        for(int i=0;i<target.length;i++) {
            target[i] = dis.readByte();
        }
        return new IndexedPattern(a, 0, len, target);
    }

    public String toDetail() {
        StringBuilder b = new StringBuilder();
        int i=0;
        for(byte[] p:_a.sources()) {
            b.append(Patterns.formatPattern(p));
            b.append(" => ");
            b.append(Patterns.formatPattern(_target[i++]));
            b.append("\n");
        }
        return b.toString();
    }

    @FunctionalInterface
    interface Transform {
        void modulate(Archetype a, byte[] target);
    }

    @FunctionalInterface
    interface BinaryTransform {
        void modulate(Archetype sa, byte[] source, Archetype ta, byte[] target);
    }
}
