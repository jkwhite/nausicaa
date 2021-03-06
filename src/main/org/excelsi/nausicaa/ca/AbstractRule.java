package org.excelsi.nausicaa.ca;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.io.*;


public abstract class AbstractRule /*extends WritableImagePlane*/ implements Rule {
    protected Flags _options = new Flags();
    private Mutagen _mutagen = null; //Genetics.swap();
    //private Mutagen _mutagen = Genetics.flip();
    private final int _x;
    private final int _y;


    public AbstractRule(int x, int y) {
        //super(null, x, y);
        _x = x;
        _y = y;
    }

    public int width() {
        return _x;
    }

    public int height() {
        return _y;
    }

    public Flags getFlags() {
        return _options;
    }

    public boolean getFlag(Options o) {
        return _options.get(o);
    }

    public void setFlag(Options o) {
        _options.set(o);
    }

    public void setFlag(Options o, boolean state) {
        _options.set(o, state);
    }

    @Override public int colorCount() {
        //return colors().length;
        return archetype().colors();
    }

    @Override public Archetype archetype() {
        throw new UnsupportedOperationException();
    }

    @Override public IndexedRule getHyperrule() {
        throw new UnsupportedOperationException();
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public void write(PrintWriter w) {
        throw new UnsupportedOperationException();
    }

    @Override public void tick() {
    }

    @Override public void copy(Plane p) {
    }

    @Override public String humanize() {
        return toString();
    }

    @Override public String id() {
        return toString();
    }

    @Override public Iterator<Plane> frameIterator(Plane initial, ExecutorService pool, GOptions opt) {
        throw new UnsupportedOperationException();
    }

    public final void setMutagen(Mutagen m) {
        _mutagen = m;
    }

    protected final Mutagen mutagen() {
        return _mutagen;
    }

    protected static class Flags implements java.io.Serializable {
        private BitSet _s = new BitSet();


        public boolean get(Options o) {
            return _s.get(o.ordinal());
        }

        public void set(Options o) {
            set(o, o.get());
        }

        public void set(Options o, boolean state) {
            _s.set(o.ordinal(), state);
        }

        public void write(DataOutputStream dos) throws IOException {
            dos.writeByte(_s.size());
            for(int i=0;i<_s.size();i++) {
                dos.writeBoolean(_s.get(i));
            }
        }

        public void read(DataInputStream dis) throws IOException {
            int size = dis.readByte();
            for(int i=0;i<size;i++) {
                _s.set(i, dis.readBoolean());
            }
        }
    }
}
