package org.excelsi.nausicaa.ca;


import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import com.google.gson.*;


public final class Archetype {
    public static final int COLORS_INFINITE = -1;

    public enum Neighborhood {
        moore("Moore"),
        vonneumann("von Neumann"),
        circular("Circular");

        private final String _name;


        Neighborhood(String n) {
            _name = n;
        }

        public String getName() {
            return _name;
        }

        public static Neighborhood from(String s) {
            switch(s) {
                case "vonneumann":
                    return vonneumann;
                case "moore":
                    return moore;
                case "circular":
                    return circular;
                default:
                    throw new IllegalArgumentException("no such neighborhood '"+s+"'");
            }
        }
    };
    private final int _dims;
    private final int _size;
    private final int _colors;
    private final Neighborhood _neighborhood;
    private final Values _values;


    public Archetype(int dims, int size, int colors) {
        this(dims, size, colors, Neighborhood.moore);
    }

    public Archetype(int dims, int size, int colors, Neighborhood neighborhood) {
        this(dims, size, colors, neighborhood, Values.discrete);
    }

    public Archetype(int dims, int size, int colors, Neighborhood neighborhood, Values values) {
        if(colors<2&&values!=Values.continuous) {
            throw new IllegalArgumentException("colors must be at least 2: "+colors);
        }
        if(dims==1 && neighborhood==Neighborhood.vonneumann) {
            throw new IllegalArgumentException("1-dimensional neighborhood cannot be vonneumann");
        }
        _dims = dims;
        _size = size;
        _colors = colors;
        _neighborhood = neighborhood;
        _values = values;
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

    public Neighborhood neighborhood() {
        return _neighborhood;
    }

    public Values values() {
        return _values;
    }

    public boolean isDiscrete() {
        return _values==Values.discrete;
    }

    public boolean isContinuous() {
        return _values==Values.continuous;
    }

    public Archetype asDims(int dims) {
        return new Archetype(dims, _size, _colors, _neighborhood, _values);
    }

    public Archetype asColors(int colors) {
        return new Archetype(_dims, _size, colors, _neighborhood, _values);
    }

    public Archetype asValues(Values vals) {
        return new Archetype(_dims, _size, _colors, _neighborhood, vals);
    }

    public Archetype asNeighborhood(Neighborhood n) {
        return new Archetype(_dims, _size, _colors, n, _values);
    }

    public Archetype asSize(int s) {
        return new Archetype(_dims, s, _colors, _neighborhood, _values);
    }

    /**
     * Length of source pattern only.
     */
    public int sourceLength() {
        switch(_neighborhood) {
            case vonneumann:
                return 1+dims()*(2*size());
            case circular:
                //return (int) (Math.PI*Math.pow(0.5+size(),2));
                return circularCoords(dims(), size()).length;
            default:
            case moore:
                return (int) Math.pow(2*size()+1, dims());
        }
        //2 1 -> 5
        //2 2 -> 9
        //3 1 -> 7
        //3 2 -> 13
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
        w.println(_neighborhood.toString());
    }

    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("dims",_dims);
        o.addProperty("size",_size);
        o.addProperty("colors",_colors);
        o.addProperty("neighborhood",_neighborhood.toString());
        o.addProperty("values",_values.toString());
        return o;
    }

    public static Archetype read(BufferedReader r, int version) throws IOException {
        if(version<5) {
            return new Archetype(
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine()),
                Integer.parseInt(r.readLine())
            );
        }
        else {
            int dims = Integer.parseInt(r.readLine());
            int size = Integer.parseInt(r.readLine());
            int cols = Integer.parseInt(r.readLine());
            Neighborhood n = Neighborhood.from(r.readLine());
            return new Archetype(dims, size, cols, n);
        }
    }

    public static Archetype fromJson(JsonElement e) {
        JsonObject o = (JsonObject) e;
        return new Archetype(
            Json.integer(o, "dims", 2),
            Json.integer(o, "size", 1),
            Json.integer(o, "colors", 2),
            Neighborhood.from(Json.string(o, "neighborhood", "moore")),
            Values.from(Json.string(o, "values", "discrete"))
        );
    }

    public static int[][] circularCoords(int d, int r) {
        List<int[]> cs = new ArrayList<>();
        for(int i=-r;i<=r;i++) {
            for(int j=-r;j<=r;j++) {
                double dist = Math.sqrt(i*i+j*j);
                if(dist<r+0.5) {
                    cs.add(new int[]{i,j});
                }
            }
        }
        int[][] ret = new int[cs.size()][];
        for(int i=0;i<ret.length;i++) {
            ret[i] = cs.get(i);
        }
        return ret;
    }

    @Override public String toString() {
        return "{dims:"+_dims+", size:"+_size+", colors:"+_colors+", patternLength:"+patternLength()+", totalPatterns:"+totalPatterns()+", totalRules:"+totalRules()+"}";
    }
}
