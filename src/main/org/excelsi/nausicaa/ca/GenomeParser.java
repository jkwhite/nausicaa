package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;


public class GenomeParser {
    private final Archetype _a;
    private final Language _lang;
    private MutationFactor _mf;


    public GenomeParser(Archetype a, Language lang) {
        _a = a;
        _lang = lang;
    }

    public GenomeParser mutationFactor(MutationFactor mf) {
        _mf = mf;
        return this;
    }

    public Rule parse(final String g, final Ruleset origin) {
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
            return parse3(g.substring(i), origin);
        }
        else if(version<3) {
            return parse2(g, origin);
        }
        else {
            throw new IllegalArgumentException("unsupported version "+version);
        }
    }

    public Info info(final Archetype a, final String g) {
        Pair<List<S>,Datamap> pa = parseS(g);
        List<String> info = new ArrayList<>();
        for(S s:pa.one) {
            if(s.n==null) {
                Codon[] cs = new Genome(s.g).codons(new Implicate(a, pa.two, _lang));
                StringBuilder b = new StringBuilder();
                codonInfo(b, cs, "");
                info.add(b.toString());
            }
        }
        return new Info(info);
    }

    private static final void codonInfo(StringBuilder b, Codon[] cs, String indent) {
        for(Codon c:cs) {
            b.append(indent).append(opName(c)).append('\n');
            if(c instanceof Codons.Chain) {
                codonInfo(b, ((Codons.Chain)c).childs(), indent+"\t");
            }
        }
    }

    private static final String opName(Codon c) {
        String n = c.getClass().getSimpleName();
        if(n.indexOf('$')>0) {
            n = n.substring(n.indexOf('$')+1);
        }
        return c.code()+"\t"+n;
    }

    public void write(Rule r, PrintWriter w) {
    }

    private Rule parse3(final String g, final Ruleset origin) {
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

    private Pair<List<S>,Datamap> parseS(final String g) {
        final String[] gs = g.replace('\n',',').split(",");
        //final S[] ps = new S[gs.length];
        final List<S> ps = new ArrayList<>();
        final Datamap dm = new Datamap();
        for(int i=0;i<gs.length;i++) {
            final String gr = gs[i].trim();
            //String g = gr.trim();
            int c = 100;
            Float w = null;
            String n = null;
            if(gr.indexOf(':')>=0) {
                String[] cg = gr.split(":");
                final String grs = cg[1];
                final String pref = cg[0].trim();
                if(Character.isDigit(pref.charAt(0))) {
                    int splk = pref.indexOf('/');
                    if(splk>0) {
                        c = Integer.parseInt(pref.substring(0, splk));
                        w = Float.parseFloat(pref.substring(splk+1));
                        System.err.println("C: "+c+", W: "+w);
                    }
                    else {
                        c = Integer.parseInt(pref);
                        w = null;
                    }
                    ps.add(new S(c, w, null, grs));
                }
                else if(cg[0].trim().startsWith("da")) {
                    n = cg[0].trim().substring("da".length());
                    ps.add(new S(0, null, n, grs));
                    dm.index(n, new Index(n, grs));
                }
            }
        }
        return new Pair(ps,dm);
    }
    
    private Rule parse2(final String g, final Ruleset origin) {
        Pair<List<S>,Datamap> pa = parseS(g);
        List<S> ps = pa.one;
        Datamap dm = pa.two;
        SequencePattern.Sequence s = new SequencePattern.Sequence();
        for(S seq:ps) {
            if(seq.n==null) {
                s.s(seq.c, seq.weight, new ComputedPattern(_a,
                    new ComputedPattern.MachineElf(new Machine(new Implicate(_a, dm, _lang), new Genome(seq.g, 2)))));
            }
            else {
                s.d(seq.n, dm.find(seq.n));
            }
        }
        SequencePattern sp;
        if(_mf!=null) {
            sp = new SequencePattern(s, _mf.transition());
        }
        else {
            sp = new SequencePattern(s);
        }
        return new ComputedRule2d(sp, origin);
    }

    public static final class Data {
        public List<String> c;
        public Map<String,String> d;
    }

    public static class Info {
        private final List<String> _infos;


        public Info(List<String> inf) {
            _infos = inf;
        }

        @Override public String toString() {
            StringBuilder b = new StringBuilder();
            for(String s:_infos) {
                b.append(s).append("-\n");
            }
            b.setLength(b.length()-2);
            return b.toString().trim();
        }
    }

    private static class S {
        public final int c;
        public final Float weight;
        public final String n;
        public final String g;

        public S(int c, Float w, String n, String g) {
            this.c = c;
            this.weight = w;
            this.n = n;
            this.g = g;
        }

        @Override public String toString() {
            return "{c:"+c+"weight:"+weight+", n:"+n+", g:"+g+"}";
        }
    };
}
