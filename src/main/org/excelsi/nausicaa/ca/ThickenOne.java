package org.excelsi.nausicaa.ca;


public final class ThickenOne extends AbstractMutator {
    public String name() { return "Thicken one"; }
    public String description() { return "Subtracts some background from one color"; }

    private final Byte _c;


    public ThickenOne() {
        _c = null;
    }

    public ThickenOne(byte c) {
        _c = c;
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        byte c = _c!=null?_c.byteValue() : (byte) _om.nextInt(r.getPattern().archetype().colors());
        return new InternalThickenOne(c).mutateIndexedRule(r, f);
    }

    private final class InternalThickenOne extends Thicken {
        private final byte _c;


        public InternalThickenOne(byte c) {
            _c = c;
        }

        public String name() { return "Unknown Kadath"; }
        public String description() { return "Dream-city of energy"; }

        @Override protected final byte mutate(Archetype a, MutationFactor f, byte t) {
            if(test(f, t)) {
                t = _c;
            }
            return t;
        }
    }
}
