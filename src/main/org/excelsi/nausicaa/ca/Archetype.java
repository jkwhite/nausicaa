package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.function.Predicate;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public final class Archetype {
    private final int _dims;
    private final int _size;
    private final int _colors;


    public Archetype(int dims, int size, int colors) {
        if(colors<2) {
            throw new IllegalArgumentException("colors must be at least 2: "+colors);
        }
        _dims = dims;
        _size = size;
        _colors = colors;
    }

    public int dims() {
        return _dims;
    }

    public int size() {
        return _size;
    }

    public int colors() {
        return _colors;
    }

    public Archetype asDims(int dims) {
        return new Archetype(dims, _size, _colors);
    }

    public Archetype asColors(int colors) {
        return new Archetype(_dims, _size, colors);
    }

    /**
     * Length of source pattern only.
     */
    public int sourceLength() {
        return (int) Math.pow(2*size()+1, dims());
    }

    /**
     * Length of pattern including target.
     */
    public int patternLength() {
        return 1 + sourceLength();
    }

    /**
     * Number of source patterns. Size of target array.
     */
    public int totalPatterns() {
        return (int) Math.pow(colors(), sourceLength());
    }

    /**
     * Number of rules that can be made from this archetype.
     */
    public long totalRules() {
        return (long) Math.pow(colors(), totalPatterns());
    }

    /**
     * Computes coefficients for index values in source pattern.
     */
    public int[] sourceCoefficients() {
        final int[] pow = new int[sourceLength()];
        for(int i=0;i<pow.length;i++) {
            pow[pow.length-1-i] = (int) Math.pow(colors(), i);
        }
        return pow;
    }

    /**
     * Enumerates all source patterns of this archetype in order.
     */
    public Iterable<byte[]> sources() {
        return new Iterable<byte[]>() {
            @Override public Iterator<byte[]> iterator() {
                return new Iterator() {
                    private int _current = 0;
                    private final int max = totalPatterns();

                    @Override public boolean hasNext() {
                        return _current < max;
                    }

                    @Override public byte[] next() {
                        String s = Integer.toString(_current++, colors());
                        byte[] p = new byte[sourceLength()];
                        int i = p.length-1;
                        int j = s.length()-1;
                        for(;i>=0&&j>=0;i--) {
                            p[i] = (byte) (s.charAt(j--)-'0');
                        }
                        while(i-->0) {
                            p[i] = 0;
                        }
                        return p;
                    }
                };
            }
        };
    }

    public Archetype validate(Predicate<Archetype> p) {
        if(!p.test(this)) {
            throw new LimitExceededException("exceeded VM limit");
        }
        return this;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(_colors);
        dos.writeInt(_size);
        dos.writeInt(_dims);
    }

    public static Archetype read(DataInputStream dis) throws IOException {
        int cols = dis.readInt();
        int size = dis.readInt();
        int dims = dis.readInt();
        return new Archetype(dims, size, cols);
    }

    public void write(PrintWriter w) {
        w.println(_dims);
        w.println(_size);
        w.println(_colors);
    }

    public static Archetype read(BufferedReader r) throws IOException {
        return new Archetype(
            Integer.parseInt(r.readLine()),
            Integer.parseInt(r.readLine()),
            Integer.parseInt(r.readLine())
        );
    }

    @Override public String toString() {
        return "{dims:"+_dims+", size:"+_size+", colors:"+_colors+", patternLength:"+patternLength()+", totalPatterns:"+totalPatterns()+", totalRules:"+totalRules()+"}";
    }
}
