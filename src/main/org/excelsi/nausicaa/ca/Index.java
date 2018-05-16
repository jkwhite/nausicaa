package org.excelsi.nausicaa.ca;


import java.util.*;


public final class Index implements Genomic, Mutatable {
    private enum Expand {
        none,
        rot4
    };

    private final String _genome;
    private final String _name;
    private final int[] _target;
    private int _size;


    public Index(final String name, final String g) {
        _name = name;
        _genome = g;
        final int[] n = new int[2];
        int i = 0;
        while(Character.isDigit(g.charAt(i++)));
        final int dsize = Integer.parseInt(g.substring(0,i-1));

        final StringBuilder exp = new StringBuilder();
        while(!Character.isWhitespace(g.charAt(i))) { exp.append(g.charAt(i)); i++; }
        Expand ex;
        switch(exp.toString()) {
            case "rot4":
                ex = Expand.rot4;
                break;
            case "none":
            default:
                ex = Expand.none;
                break;
        }
        System.err.println(exp.toString()+" rot");
        final Map<Integer,Integer> tmap = new HashMap<>();
        while(i<g.length()) {
            i = next(i, dsize, ex, g, tmap);
            //tmap.put(n[0], n[1]);
            //if(n[0]>max) max = n[0];
        }
        int max = 0;
        for(Map.Entry<Integer,Integer> e:tmap.entrySet()) {
            if(e.getKey()>max) {
                max = e.getKey();
            }
        }
        _target = new int[1+max];
        for(int k=0;k<_target.length;k++) {
            _target[k] = -1;
        }
        for(Map.Entry<Integer,Integer> e:tmap.entrySet()) {
            _target[e.getKey()] = e.getValue();
        }
    }

    public Index copy() {
        return new Index(_name, _genome);
    }

    @Override public Mutatable mutate(MutationFactor m) {
        char[] g = _genome.toCharArray();
        int i=0;
        while(Character.isDigit(g[i++]));
        while(!Character.isWhitespace(g[i])) { i++; }
        for(;i<g.length;i++) {
            if(Character.isDigit(g[i])
                && m.random().nextInt(1000)<m.alpha()) {
                g[i] = (char)(m.random().nextInt(10)+'0');
                System.err.println("mutated index to '"+g[i]+"' #########");
            }
        }
        return new Index(_name, new String(g));
    }

    public int size() {
        return _size;
    }

    public int find(int src) {
        if(src<0) src=-src;
        if(src>=_target.length) src %= _target.length;
        //return src>=0&&src<_target.length?_target[src]:-1;
        return _target[src];
    }

    public int find(int[] src) {
        return find(src[0]+10*src[1]+100*src[2]+1000*src[3]+10000*src[4]);
    }

    @Override public String genome() {
        return _genome;
    }

    @Override public String prettyGenome() {
        return _genome;
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(_target.length).append(" entries:\n");
        for(int i=0;i<_target.length;i++) {
            b.append(i).append(" = ").append(_target[i]).append("\n");
        }
        return b.toString();
    }

    private final StringBuilder temp = new StringBuilder();
    private int next(int i, int dsize, Expand exp, String g, final Map<Integer,Integer> tmap) {
        if(i>=g.length()) return -1;
        while(Character.isWhitespace(g.charAt(i))) i++;
        final int start = i;
        while(i<g.length() && Character.isDigit(g.charAt(i))) i++;
        final int end = i;
        int cur = end;
        int tgt = Integer.parseInt(g.substring(cur-dsize,cur));
        //System.err.println("parsed target '"+g.substring(cur-dsize,cur)+"' for "+tgt);
        //cur -= dsize;
        //int scl = 1;
        //int src = 0;
        temp.setLength(0);
        temp.append(g.substring(start, cur-dsize));
        source(temp, dsize, tgt, exp, tmap);
        //for(int j=cur-dsize;j>=start;j--) {
            //src += scl*Integer.parseInt(g.substring(j,j+dsize));
            //scl *= (10*dsize);
        //}
        //n[0] = src;
        //n[1] = tgt;
        return i;
    }

    private void source(StringBuilder g, int dsize, int tgt, Expand exp, final Map<Integer,Integer> tmap) {
        //System.err.println("src="+g);
        int ent = g.length()/dsize;
        _size = ent;
        if(ent==5) {
            // CNESWC'
            int c = parseInt(g,dsize,0);
            int n = parseInt(g,dsize,1);
            int e = parseInt(g,dsize,2);
            int s = parseInt(g,dsize,3);
            int w = parseInt(g,dsize,4);
            //System.err.println("c="+c+", n="+n+", e="+e+", s="+s+", w="+w+" tgt="+tgt);
            if(exp==Expand.none) {
                tmap.put(n+10*w+100*c+1000*e+10000*s, tgt);
            }
            else if(exp==Expand.rot4) {
                tmap.put(c+10*n+100*e+1000*s+10000*w, tgt);
                tmap.put(c+10*e+100*s+1000*w+10000*n, tgt);
                tmap.put(c+10*s+100*w+1000*n+10000*e, tgt);
                tmap.put(c+10*w+100*n+1000*e+10000*s, tgt);
            }
        }
        else if(ent==9) {
            //C,N,NE,E,SE,S,SW,W,NW,C'
            int c = parseInt(g,dsize,0);
            int n = parseInt(g,dsize,1);
            int ne = parseInt(g,dsize,2);
            int e = parseInt(g,dsize,3);
            int se = parseInt(g,dsize,4);
            int s = parseInt(g,dsize,5);
            int sw = parseInt(g,dsize,6);
            int w = parseInt(g,dsize,7);
            int nw = parseInt(g,dsize,8);
            //System.err.println("c="+c+", n="+n+", e="+e+", s="+s+", w="+w+" tgt="+tgt);
            if(exp==Expand.none) {
                tmap.put(n+10*w+100*c+1000*e+10000*s, tgt);
            }
            else if(exp==Expand.rot4) {
                // CNESWC
                tmap.put(c+10*n+100*e+1000*s+10000*w, tgt);
                tmap.put(c+10*e+100*s+1000*w+10000*n, tgt);
                tmap.put(c+10*s+100*w+1000*n+10000*e, tgt);
                tmap.put(c+10*w+100*n+1000*e+10000*s, tgt);
            }
        }
        else {
            throw new UnsupportedOperationException("do not know how to handle "+ent+" sources");
        }
    }

    private static int parseInt(StringBuilder g, int dsize, int pos) {
        return Integer.parseInt(g.substring(pos*dsize, (pos+1)*dsize));
    }
}
