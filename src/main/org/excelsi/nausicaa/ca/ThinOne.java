package org.excelsi.nausicaa.ca;


public final class ThinOne extends AbstractMutator {
    public String name() { return "Thin one"; }
    public String description() { return "Adds some background to one color"; }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        byte c = (byte) _om.nextInt(r.getPattern().archetype().colors());
        return new InternalThinOne(c).mutateIndexedRule(r, f);
    }

    private final class InternalThinOne extends Thin {
        private final byte _c;


        public InternalThinOne(byte c) {
            _c = c;
        }

        public String name() { return "Unknown Kadath"; }
        public String description() { return "Dream-city of energy"; }

        @Override protected final byte mutate(Archetype a, MutationFactor f, byte t) {
            if(t==_c && test(f, t)) {
                t = 0;
            }
            return t;
        }
    }
}
