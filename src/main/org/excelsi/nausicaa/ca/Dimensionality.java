package org.excelsi.nausicaa.ca;


public class Dimensionality extends AbstractMutator {
    public enum Direction { up, down };
    public String name() {
        switch(_d) {
            case up:
                return "Hyperspace";
            case down:
            default:
                return "Subspace";
        }
    }

    public String description() { return "Modulates dimensionality"; }

    private final Direction _d;


    public Dimensionality(Direction d) {
        _d = d;
    }

    @Override public IndexedRule mutateIndexedRule(IndexedRule r, MutationFactor f) throws MutationFailedException {
        Archetype a = r.getPattern().archetype();
        switch(_d) {
            case up:
                a = a.asDims(a.dims()+1);
                break;
            case down:
            default:
                a = a.asDims(a.dims()-1);
                break;
        }
        return (IndexedRule)r.origin().derive(a).random(_om).next();
    }
}
