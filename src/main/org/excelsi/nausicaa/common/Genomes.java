package org.excelsi.nausicaa.common;


import org.excelsi.nausicaa.ca.Varmap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Genomes {
    private static final Logger LOG = LoggerFactory.getLogger(Genomes.class);

    private Genomes() {}


    public static Varmap createVarmap(String g) {
        final String P_START = Varmap.P_START;
        final String P_END = Varmap.P_END;
        Varmap v = new Varmap();
        int idx = 0;
        while(idx>=0 && idx<g.length()) {
            int nidx = g.indexOf(P_START, idx);
            if(nidx>=idx) {
                int end = g.indexOf(P_END, nidx);
                if(end>nidx) {
                    String param = g.substring(nidx+1, end);
                    String val = null;
                    int def = param.indexOf(':');
                    if(def>0) {
                        val = param.substring(1+def);
                        param = param.substring(0,def);
                    }
                    v.put(param, val);
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
        return v;
    }

    public static String resolveParams(String g, Varmap vars) {
        final String P_START = Varmap.P_START;
        final String P_END = Varmap.P_END;
        LOG.debug("resolveParams vars: "+vars);
        //Thread.dumpStack();
        for(String p:vars.names()) {
            String v = vars.get(p);
            if(!"".equals(v)) {
                g = g.replaceAll("\\"+P_START+p+"(:.*?)?\\"+P_END, v);
            }
        }
        return g;
    }

}
