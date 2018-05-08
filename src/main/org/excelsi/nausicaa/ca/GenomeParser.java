package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;


public class GenomeParser {
    private final Archetype _a;


    public GenomeParser(Archetype a) {
        _a = a;
    }

    public Rule parse(final String g) {
        StringBuilder vs = new StringBuilder();
        final int version;
        int i=1;
        if(g.charAt(0)=='@') {
            for(;Character.isDigit(g.charAt(i));i++) {
                vs.append(g.charAt(i));
            }
            version = Integer.parseInt(vs.toString());
        }
        else {
            version = 2;
        }
        if(version==3) {
            return parse3(g.substring(i));
        }
        else if(version<3) {
            return parse2(g);
        }
        else {
            throw new IllegalArgumentException("unsupported version "+version);
        }
    }

    public void write(Rule r, PrintWriter w) {
    }

    private Rule parse3(final String g) {
        final String pre = "--- !!org.excelsi.nausicaa.ca.GenomeParser$Data\n"+g;

        Data d = null;
        try {
            d = (Data) new Yaml().load(new StringReader(pre));
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
        throw new UnsupportedOperationException();
    }

    private Rule parse2(final String g) {
        class S {
            public final int c;
            public final String n;
            public final String g;

            public S(int c, String n, String g) {
                this.c = c;
                this.n = n;
                this.g = g;
            }
        };
        final String[] gs = g.replace('\n',',').split(",");
        //final S[] ps = new S[gs.length];
        final List<S> ps = new ArrayList<>();
        final Datamap dm = new Datamap();
        for(int i=0;i<gs.length;i++) {
            final String gr = gs[i].trim();
            //String g = gr.trim();
            int c = 100;
            String n = null;
            if(gr.indexOf(':')>=0) {
                String[] cg = gr.split(":");
                final String grs = cg[1];
                if(Character.isDigit(cg[0].trim().charAt(0))) {
                    c = Integer.parseInt(cg[0].trim());
                    ps.add(new S(c, null, grs));
                }
                else if(cg[0].trim().startsWith("da")) {
                    n = cg[0].trim().substring("da".length());
                    dm.index(n, new Index(grs));
                }
                //ps[i] = new S(c, n, grs);
            }
            //System.err.println("time: "+c+", rule: "+grs);
            //s.s(c, new ComputedPattern(_a,
                //new ComputedPattern.MachineElf(new Machine(_a, new Genome(grs, version)))));
        }

        SequencePattern.Sequence s = new SequencePattern.Sequence();
        for(S seq:ps) {
            s.s(seq.c, new ComputedPattern(_a,
                new ComputedPattern.MachineElf(new Machine(_a, dm, new Genome(seq.g, 2)))));
        }
        SequencePattern sp;
        //if(args.length>1 && args[1] instanceof MutationFactor) {
            //sp = new SequencePattern(s, ((MutationFactor)args[1]).transition());
        //}
        //else {
            sp = new SequencePattern(s);
        //}
        return new ComputedRule2d(sp);
    }

    public static final class Data {
        public List<String> c;
        public Map<String,String> d;
    }
}
