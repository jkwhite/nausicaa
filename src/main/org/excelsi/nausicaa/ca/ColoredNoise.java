package org.excelsi.nausicaa.ca;


public class ColoredNoise extends AbstractMutator {
    public String name() { return "Colored Noise"; }
    public String description() { return "Introduces random fluctuations in patterns"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        //IndexedPattern p = r.getPattern();
        return r.derive((a, target)->{
            for(int i=0;i<target.length;i++) {
                if(target[i]>0 && chance(f)) {
                    target[i] = (byte) (1+_om.nextInt(a.colors()-1));
                }
            }
        });
    }
}
