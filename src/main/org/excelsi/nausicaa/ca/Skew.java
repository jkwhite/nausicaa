package org.excelsi.nausicaa.ca;


import java.util.Arrays;


public class Skew extends AbstractMutator {
    private final Probability _p;
    private final String _name;
    private final boolean _show = false;


    public Skew(String name, Probability p) {
        _name = name;
        _p = p;
    }

    public String name() { return _name; }
    public String description() { return "Skews color histogram"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        return r.derive((a, target)->{
            if(_show) {
                System.err.println(_name+" before: "+showHisto(a, target));
            }
            for(int i=0;i<target.length;i++) {
                if(chance(f)) {
                    target[i] = (byte) (_p.p(_om, a.colors()));
                }
            }
            if(_show) {
                System.err.println(_name+" after: "+showHisto(a, target));
            }
        });
    }

    private String showHisto(Archetype a, byte[] target) {
        int[] counts = new int[a.colors()];
        for(int i=0;i<target.length;i++) {
            if(target[i] < counts.length) {
                counts[target[i]]++;
            }
            else {
                throw new MutationFailedException(target[i]+" greater than "+counts.length+" in "+_name);
            }
        }
        return Arrays.toString(counts);
    }
}
