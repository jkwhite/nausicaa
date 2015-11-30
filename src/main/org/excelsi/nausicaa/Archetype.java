package org.excelsi.nausicaa;


import java.util.Iterator;


public class Archetype {
    private final int _colors;
    private final int _size;
    private final int _dims;


    public Archetype(int dims, int size, int colors) {
        if(colors<2||colors>127) {
            throw new IllegalArgumentException("colors must be between 2 and 127: "+colors);
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
     * Number of source patterns, not including target.
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

    @Override public String toString() {
        return "{dims:"+_dims+", size:"+_size+", colors:"+_colors+", patternLength:"+patternLength()+", totalPatterns:"+totalPatterns()+", totalRules:"+totalRules()+"}";
    }
}
