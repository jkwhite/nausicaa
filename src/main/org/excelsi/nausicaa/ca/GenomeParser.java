package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
//import org.yaml.snakeyaml.Yaml;


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

    public static GenomeParser forRule(Rule r) {
        return new GenomeParser(r.archetype(),
            ((AbstractComputedRuleset)r.origin()).language());
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
        List<SeqInfo> info = new ArrayList<>();
        for(S s:pa.one) {
            if(s.n==null) {
                Codon[] cs = new Genome(s.g).codons(new Implicate(a, pa.two, _lang));
                StringBuilder b = new StringBuilder();
                codonInfo(b, cs, "");
                info.add(new SeqInfo(cs, b.toString()));
            }
        }
        return new Info(info);
    }

    public Rule toUniversal(final Rule r) {
        String g = ((Genomic)r).genome();
        Pair<List<S>,Datamap> pa = parseS(g);
        List<S> ps = pa.one;
        Datamap dm = pa.two;
        SequencePattern.Sequence s = new SequencePattern.Sequence();
        Language universal = Languages.universal();
        for(S seq:ps) {
            if(seq.n==null) {
                Codon[] cs = new Genome(seq.g).codons(new Implicate(_a, pa.two, _lang));
                StringBuilder tr = new StringBuilder();
                for(Codon c:cs) {
                    tr.append(c.code()).append(" ");
                }
                s.s(seq.c, seq.weight[0], seq.weight[1], new ComputedPattern(_a,
                    new ComputedPattern.MachineElf(new Machine(new Implicate(_a, dm, universal), new Genome(tr.toString().trim().replace('+',' '), 2), _mf.trace()))));
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
        ComputedRuleset rs = new ComputedRuleset(_a, universal);
        return new ComputedRule2d(sp, rs);
    }

    public static Varmap createVarmap(final String g) {
        Varmap m = new Varmap(parseParams(g));
        //System.err.println("created varmap: "+m);
        return m;
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
        /*
        final String pre = "--- !!org.excelsi.nausicaa.ca.GenomeParser$Data\n"+g;

        Data d = null;
        try {
            d = (Data) new Yaml().load(new StringReader(pre));
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
        */
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
            Double[] w = null;
            String n = null;
            if(gr.indexOf(':')>=0) {
                String[] cg = gr.split(":");
                final String grs = cg[1];
                final String pref = cg[0].trim();
                if(Character.isDigit(pref.charAt(0))) {
                    int splk = pref.indexOf('/');
                    if(splk>0) {
                        c = Integer.parseInt(pref.substring(0, splk));
                        //w = Double.parseDouble(pref.substring(splk+1));
                        w = parseWeight(pref.substring(splk+1));
                        //System.err.println("C: "+c+", W: "+w);
                    }
                    else {
                        c = Integer.parseInt(pref);
                        w = new Double[2];
                    }
                    String[] params = parseParams(grs);
                    ps.add(new S(c, w, null, grs, params));
                }
                else if(cg[0].trim().startsWith("da")) {
                    n = cg[0].trim().substring("da".length());
                    ps.add(new S(0, null, n, grs, null));
                    dm.index(n, new Index(n, grs));
                }
            }
        }
        return new Pair(ps,dm);
    }

    private static Double[] parseWeight(String w) {
        Double[] ws = new Double[2];
        if(w.indexOf(';')>=0) {
            String[] wd = w.split(";");
            ws[0] = Double.parseDouble(wd[0]);
            ws[1] = Double.parseDouble(wd[1]);
        }
        else {
            ws[0] = Double.parseDouble(w);
        }
        return ws;
    }

    private static String[] parseParams(String g) {
        final String P_START = Varmap.P_START;
        final String P_END = Varmap.P_END;
        Set<String> ps = new HashSet<>();
        int idx = 0;
        while(idx>=0 && idx<g.length()) {
            int nidx = g.indexOf(P_START, idx);
            if(nidx>=idx) {
                int end = g.indexOf(P_END, nidx);
                if(end>nidx) {
                    String param = g.substring(nidx+1, end);
                    ps.add(param);
                    idx = end+1;
                }
                else {
                    idx = -1;
                }
            }
            else {
                idx = -1;
            }
        }
        //System.err.println("! params for '"+g+"': "+ps);
        return ps.toArray(new String[0]);
    }
    
    private Rule parse2(final String g, final Ruleset origin) {
        Pair<List<S>,Datamap> pa = parseS(g);
        List<S> ps = pa.one;
        Datamap dm = pa.two;
        SequencePattern.Sequence s = new SequencePattern.Sequence();
        for(S seq:ps) {
            if(seq.n==null) {
                s.s(seq.c, seq.weight[0], seq.weight[1], new ComputedPattern(_a,
                    new ComputedPattern.MachineElf(
                        new Machine(
                            new Implicate(_a, dm, _lang, _mf!=null?_mf.vars():new Varmap()),
                            new Genome(seq.g, 2), _mf!=null?_mf.trace():false))));
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
        private final List<SeqInfo> _infos;


        public Info(List<SeqInfo> inf) {
            _infos = inf;
        }

        @Override public String toString() {
            StringBuilder b = new StringBuilder();
            for(SeqInfo s:_infos) {
                b.append(s.info).append("-\n");
            }
            b.setLength(b.length()-2);
            return b.toString().trim();
        }
    }

    public static class SeqInfo {
        public final Codon[] codons;
        public final String info;

        public SeqInfo(Codon[] cs, String info) {
            this.codons = cs;
            this.info = info;
        }

        @Override public String toString() {
            return info;
        }
    }

    private static class S {
        public final int c;
        public final Double[] weight;
        public final String n;
        public final String g;
        public final String[] params;

        public S(int c, Double[] w, String n, String g, String[] params) {
            this.c = c;
            this.weight = w;
            this.n = n;
            this.g = g;
            this.params = params;
        }

        @Override public String toString() {
            return "{c:"+c+"weight:"+Arrays.toString(weight)+", n:"+n+", g:"+g+"}";
        }
    };
}
