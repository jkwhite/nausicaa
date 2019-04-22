package org.excelsi.nausicaa.ca;



import java.util.Collections;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class GenomeMutators extends Enloggened {
    private static final Logger LOG = LoggerFactory.getLogger(GenomeMutators.class);


    public static GenomeMutator makeDeterministic() {
        return
            (cs,im,gf,m)->{
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
                    for(int i=st;i!=en;) {
                        if(!cs.get(i).deterministic()) {
                            cs.remove(i);
                            break;
                        }
                        if(++i==cs.size()) {
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
                Collections.shuffle(cs,m.r());
            };
    }

    public static GenomeMutator replace() {
        return
            (cs,im,gf,m)->{
                int idx = m.r().nextInt(cs.size());
                cs.set(idx, gf.randomCodon(im, m.r()));
            };
    }

    public static GenomeMutator swap() {
        return
            (cs,im,gf,m)->{
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
                int idx = m.r().nextInt(cs.size());
                cs.add(idx, gf.randomCodon(im, m.r()));
            };
    }

    public static GenomeMutator duplicate() {
        return
            (cs,im,gf,m)->{
                int idx = m.r().nextInt(cs.size());
                cs.add(idx, cs.get(idx).copy());
            };
    }

    public static GenomeMutator remove() {
        return
            (cs,im,gf,m)->{
                if(cs.size()>2) {
                    int idx = m.r().nextInt(cs.size());
                    cs.remove(idx);
                }
            };
    }

    public static GenomeMutator decimate() {
        return
            (cs,im,gf,m)->{
                boolean first = true;
                while(cs.size()>1 && (first || m.r().nextInt(3)==0)) {
                    int idx = m.r().nextInt(cs.size());
                    cs.remove(idx);
                    first = false;
                }
            };
    }

    public static GenomeMutator repeat() {
        return
            (cs,im,gf,m)->{
                int st = m.r().nextInt(cs.size());
                int en = st+m.r().nextInt(cs.size()-st);
                int o = en;
                for(int i=st;i<en;i++) {
                    cs.add(o++, cs.get(i).copy());
                }
            };
    }

    public static GenomeMutator symmetry() {
        return null;
    }

    public static GenomeMutator add() {
        return
            (cs,im,gf,m)->{
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
                        if(c instanceof Unstable) {
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
                        if(++i==cs.size()) {
                            i=0;
                        }
                    }
                }
                LOG.debug("done adjust mutate");
            };
    }


    private GenomeMutators() {
    }
}
