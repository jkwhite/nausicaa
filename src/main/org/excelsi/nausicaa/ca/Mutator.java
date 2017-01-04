package org.excelsi.nausicaa.ca;


public interface Mutator {
    String name();
    String description();
    Rule mutate(Rule r) throws MutationFailedException;
    boolean supports(Rule r);
    IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException;
    //Multirule mutate(Multirule r) throws MutationFailedException;
    void setRandom(java.util.Random r);

    static Mutator chain(final Mutator... chain) {
        if(chain.length==1) {
            return chain[0];
        }
        else {
            return new Mutator() {
                @Override public String name() {
                    StringBuilder b = new StringBuilder();
                    for(int i=0;i<chain.length;i++) {
                        b.append(chain[i].name());
                        if(i<chain.length-1) {
                            b.append(" -> ");
                        }
                    }
                    return b.toString();
                }

                @Override public String description() {
                    StringBuilder b = new StringBuilder();
                    for(int i=0;i<chain.length;i++) {
                        b.append(chain[i].description());
                        if(i<chain.length-1) {
                            b.append(" -> ");
                        }
                    }
                    return b.toString();
                }

                @Override public boolean supports(Rule r) {
                    for(Mutator m:chain) {
                        if(!m.supports(r)) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override public Rule mutate(Rule r) throws MutationFailedException {
                    for(Mutator m:chain) {
                        r = m.mutate(r);
                    }
                    return r;
                }

                @Override public IndexedRule mutateIndexedRule(IndexedRule r, final MutationFactor f) throws MutationFailedException {
                    for(Mutator m:chain) {
                        r = m.mutateIndexedRule(r, f);
                    }
                    return r;
                }

                //@Override public Multirule mutate(Multirule r) throws MutationFailedException {
                    //for(Mutator m:chain) {
                        //r = m.mutate(r);
                    //}
                    //return r;
                //}

                @Override public void setRandom(java.util.Random r) {
                    for(Mutator m:chain) {
                        m.setRandom(r);
                    }
                }

                @Override public String toString() {
                    StringBuilder b = new StringBuilder("[");
                    for(int i=0;i<chain.length;i++) {
                        b.append(chain[i].toString());
                        if(i<chain.length-1) {
                            b.append(" -> ");
                        }
                    }
                    b.append("]");
                    return b.toString();
                }
            };
        }
    }
}
