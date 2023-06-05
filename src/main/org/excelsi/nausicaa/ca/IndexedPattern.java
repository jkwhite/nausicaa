package org.excelsi.nausicaa.ca;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public final class IndexedPattern implements Pattern {
    private final Archetype _a;
    private final long _id;
    private final int _length;
    private final byte[] _target;
    private final int[] _pow;


    /**
     * Constructs a new Pattern.
     *
     * @param id number of bytes in pattern, e.g. 3 or 9
     * @param length number of colors in pattern
     */
    public IndexedPattern(Archetype a, long id, int length, byte[] target) {
        _a = a;
        _id = id;
        _length = length;
        _target = target;
        _pow = new int[a.sourceLength()];
        for(int i=0;i<_pow.length;i++) {
            _pow[_pow.length-1-i] = (int) Math.pow(a.colors(), i);
        }
    }

    public long id() {
        return _id;
    }

    @Override public byte next(final int pattern, final byte[] p2) {
        if(pattern>=_target.length) {
            return 0;
        }
        return _target[pattern];
    }

    @Override public int next(int pattern, int[] p2, Ctx ctx) {
        int idx = 0;
        for(int k=0;k<p2.length;k++) {
            idx += p2[k] * _pow[k];
        }
        return next(idx, 0);
    }

    @Override public void tick() {
    }

    public byte next(final int pattern, final int offset) {
        int p = pattern + offset;
        while(p>=_target.length) {
            p -= _target.length;
        }
        return _target[p];
    }

    public int length() {
        return _length;
    }

    @Override public Archetype archetype() {
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

    public String serialize(String fmt) {
        if("base64gz/bytes".equals(fmt)) {
            return Base64.encodeObject(_target, Base64.GZIP | Base64.DONT_BREAK_LINES);
        }
        else {
            throw new IllegalArgumentException("unknown format '"+fmt+"'");
        }
    }

    public static IndexedPattern deserialize(String fmt, String tgt, int length, Archetype a) {
        if("base64gz/bytes".equals(fmt)) {
            byte[] target = (byte[]) Base64.decodeToObject(tgt);
            return new IndexedPattern(a, 0, length, target);
        }
        else {
            throw new IllegalArgumentException("unknown format '"+fmt+"'");
        }
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

    public void write(PrintWriter w) {
        final long max = _a.totalPatterns();
        final byte[] base = new byte[_a.sourceLength()];
        final StringBuilder b = new StringBuilder();
        for(int i=0;i<_a.sourceLength();i++) {
            w.append("p"+i+"\t");
        }
        w.append("t\n");
        for(int i=0;i<max;i++) {
            Patterns.expandSourceIndex(_a, i, base);
            for(int j=0;j<base.length;j++) {
                b.append('c').append((int)base[j]).append('\t');
            }
            b.append('c').append((int)_target[i]).append('\n');
            w.append(b);
            b.setLength(0);
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

    public String histogram() {
        int[] counts = new int[_a.colors()];
        for(int i=0;i<_target.length;i++) {
            if(_target[i] < counts.length) {
                counts[_target[i]]++;
            }
            else {
                throw new IllegalStateException(_target[i]+" greater than "+counts.length);
            }
        }
        return Arrays.toString(counts);
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
