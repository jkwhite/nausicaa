package org.excelsi.nausicaa.ca;



import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class GenomeMutators extends Enloggened {
    private static final Logger LOG = LoggerFactory.getLogger(GenomeMutators.class);


    public static GenomeMutator makeDeterministic() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running makeDeterministic mutate");
                boolean hasUnd = false;
                for(Codon c:cs) {
                    if(!c.deterministic()) {
                        hasUnd = true;
                        break;
                    }
                }
                if(hasUnd) {
                    int st = m.r().nextInt(cs.size());
                    int en = st==0?cs.size():st-1;
                    for(int i=st;i!=en&&cs.size()>1;) {
                        if(!cs.get(i).deterministic()) {
                            cs.remove(i);
                            break;
                        }
                        if(++i==cs.size()&&i!=en) {
                            i=0;
                        }
                    }
                }
                else {
                    replace().mutate(cs,im,gf,m);
                }
            };
    }

    public static GenomeMutator jumble() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running jumble mutate");
                Collections.shuffle(cs,m.r());
            };
    }

    public static GenomeMutator replace() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running replace mutate");
                int idx = m.r().nextInt(cs.size());
                cs.set(idx, gf.randomCodon(im, m.r()));
            };
    }

    public static GenomeMutator swap() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running swap mutate");
                if(cs.size()>1) {
                    int idx1 = m.r().nextInt(cs.size());
                    boolean d = m.r().nextBoolean();
                    int idx2 = d&&idx1<cs.size()-1?idx1+1:idx1>1?idx1-1:m.r().nextInt(cs.size());
                    Codon c1 = cs.get(idx1);
                    Codon c2 = cs.get(idx2);
                    cs.set(idx1, c2);
                    cs.set(idx2, c1);
                }
            };
    }

    public static GenomeMutator insert() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running insert mutate");
                int idx = m.r().nextInt(cs.size());
                cs.add(idx, gf.randomCodon(im, m.r()));
            };
    }

    public static GenomeMutator duplicate() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running duplicate mutate");
                int idx = m.r().nextInt(cs.size());
                cs.add(idx, cs.get(idx).copy());
            };
    }

    public static GenomeMutator remove() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running remove mutate");
                if(cs.size()>2) {
                    int idx = m.r().nextInt(cs.size());
                    cs.remove(idx);
                }
            };
    }

    public static GenomeMutator decimate() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running decimate mutate");
                boolean first = true;
                while(cs.size()>2 && (first || m.r().nextInt(3)==0)) {
                    int idx = m.r().nextInt(cs.size());
                    cs.remove(idx);
                    first = false;
                }
            };
    }

    public static GenomeMutator repeat() {
        final int SAFETY_LIMIT = 10000; // prevent genomes from growing infinitely
        return
            (cs,im,gf,m)->{
                if(cs.size()>SAFETY_LIMIT) {
                    LOG.info("redirecting repeat mutate to decimate because genome size "
                        +cs.size()+" is greater than "+SAFETY_LIMIT);
                    decimate().mutate(cs, im, gf, m);
                }
                else {
                    int st = m.r().nextInt(cs.size());
                    int en = st+m.r().nextInt(cs.size()-st);
                    LOG.debug("running repeat mutate from "+st+" to "+en);
                    int o = en;
                    for(int i=st;i<en;i++) {
                        cs.add(o++, cs.get(i).copy());
                    }
                    LOG.debug("done repeat mutate");
                }
            };
    }

    public static GenomeMutator symmetry() {
        return null;
    }

    public static GenomeMutator add() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running add mutate");
                cs.add(gf.randomCodon(im, m.r()));
            };
    }

    public static GenomeMutator adjust() {
        return
            (cs,im,gf,m)->{
                boolean any = true;
                boolean de = false;
                int tries = 0;
                LOG.debug("running adjust mutate");
                while(++tries<100 && any && !de) {
                    //System.err.print("#");
                    //if(tries%10==0) System.err.println();
                    any = false;
                    int st = m.r().nextInt(cs.size());
                    int en = st==0?cs.size():st-1;
                    for(int i=st;i!=en;) {
                        final Codon c = cs.get(i);
                        if(c instanceof Unstable
                            && (! (c instanceof Codons.Chain) || m.intrabondMutations())) {
                            any = true;
                            Codon after = ((Unstable)c).destabilize(m.r());
                            LOG.debug("unstable before: "+c.code()+", after: "+after.code());
                            cs.set(i, after);
                            if(!c.code().equals(after.code())) {
                                de = true;
                                LOG.debug("found destabilize");
                                break;
                            }
                        }
                        if(++i==cs.size()&&i!=en) {
                            i=0;
                        }
                    }
                }
                LOG.debug("done adjust mutate");
            };
    }

    public static GenomeMutator transmute() {
        return
            (cs,im,gf,m)->{
                boolean any = true;
                boolean de = false;
                int tries = 0;
                LOG.debug("running transmute mutate");
                while(++tries<100 && any && !de) {
                    //System.err.print("#");
                    //if(tries%10==0) System.err.println();
                    any = false;
                    int st = m.r().nextInt(cs.size());
                    int en = st==0?cs.size():st-1;
                    for(int i=st;i!=en;) {
                        final Codon c = cs.get(i);
                        if(c instanceof Transmutable
                            && (! (c instanceof Codons.Chain) || m.intrabondMutations())) {
                            any = true;
                            Codon after = ((Transmutable)c).transmute(im,m.r());
                            LOG.debug("transmutable before: "+c.code()+", after: "+after.code());
                            cs.set(i, after);
                            if(!c.code().equals(after.code())) {
                                de = true;
                                LOG.debug("found transmute");
                                break;
                            }
                        }
                        if(++i==cs.size()&&i!=en) {
                            i=0;
                        }
                    }
                }
                LOG.debug("done transmute mutate");
            };
    }

    public static GenomeMutator bond() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running bond mutate");
                LinkedList<Codon> ncs = new LinkedList<>();
                int s = m.r().nextInt(cs.size());
                int e = s+1+m.r().nextInt(cs.size()-s-1);
                if(s>0) ncs.addAll(cs.subList(0,s));
                LinkedList<Codon> ch = new LinkedList<>();
                for(int i=s;i<=e;i++) {
                    Codon ic = cs.get(i);
                    if(ic instanceof Codons.Chain) {
                        for(Codon child:((Codons.Chain)ic).childs()) {
                            ch.add(child);
                        }
                    }
                    else {
                        ch.add(ic);
                    }
                }
                ncs.add(new Codons.Chain(ch.toArray(new Codon[0])));
                if(e<cs.size()-1) ncs.addAll(cs.subList(e+1,cs.size()));
                LOG.debug("bond orig: "+cs);
                LOG.debug("bond res:  "+ncs);
                cs.clear();
                cs.addAll(ncs);
            };
    }

    public static GenomeMutator unbond() {
        return
            (cs,im,gf,m)->{
                LOG.debug("running unbond mutate");
                LinkedList<Codon> ncs = new LinkedList<>();
                for(Codon c:cs) {
                    if(c instanceof Codons.Chain) {
                        if(m.r().nextInt(100)<20) {
                            for(Codon ch:((Codons.Chain)c).childs()) {
                                ncs.add(ch);
                            }
                        }
                    }
                    else {
                        ncs.add(c);
                    }
                }
                LOG.debug("unbond orig: "+cs);
                LOG.debug("unbond res:  "+ncs);
                cs.clear();
                cs.addAll(ncs);
            };
    }

    private GenomeMutators() {
    }
}
