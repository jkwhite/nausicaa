package org.excelsi.nausicaa.ca;


import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class ArchetypeTransform implements Transform {
    private static final Logger LOG = LoggerFactory.getLogger(ArchetypeTransform.class);

    private final Random _r;
    private boolean _dims = true;
    private boolean _size = true;
    private boolean _neighborhood = true;
    private boolean _values = true;


    public ArchetypeTransform(Random r) {
        _r = r;
    }

    @Override public String name() {
        return "Archetypes";
    }

    public ArchetypeTransform withDims(boolean d) {
        _dims = d;
        return this;
    }

    public ArchetypeTransform withSize(boolean s) {
        _size = s;
        return this;
    }

    public ArchetypeTransform withNeighborhoods(boolean n) {
        _neighborhood = n;
        return this;
    }

    public ArchetypeTransform withValues(boolean v) {
        _values = v;
        return this;
    }

    @Override public CA transform(CA c) {
        final Rule r = c.getRule();
        final Archetype a = r.archetype();

        if((!_dims&&!_size&&!_neighborhood&&!_values) || !(r instanceof Mutatable)) {
            return c;
        }

        Archetype an = null;
        do {
            switch(_r.nextInt(4)) {
                case 0:
                    if(_dims) {
                        do {
                            an = a.asDims(1+_r.nextInt(3));
                        } while(a.dims()==an.dims());
                    }
                    break;
                case 1:
                    if(_size) {
                        if(a.size()==1) {
                            an = a.asSize(2);
                        }
                        else {
                            an = a.asSize(a.size()+(_r.nextBoolean()?1:-1));
                        }
                    }
                    break;
                case 2:
                    if(_values) {
                        an = a.asValues(a.isDiscrete()?Values.continuous:Values.discrete);
                    }
                    break;
                default:
                case 3:
                    if(_neighborhood) {
                        do {
                            switch(_r.nextInt(3)) {
                                case 0:
                                    an = a.asNeighborhood(Archetype.Neighborhood.moore);
                                    break;
                                case 1:
                                    an = a.asNeighborhood(Archetype.Neighborhood.vonneumann);
                                    break;
                                default:
                                case 2:
                                    an = a.asNeighborhood(Archetype.Neighborhood.circular);
                                    break;
                            }
                        } while(an.neighborhood()==a.neighborhood());
                    }
                    break;
            }
        } while(an==null);

        final CA trans = c.mutate(
            (Rule)((Mutatable)r).mutate(
                new MutationFactor()
                    .withUpdateArchetype(true)
                    .withArchetype(an)
                    .withGenomeMutator((cs,im,gf,m)->{})
                    // .withLanguage(Languages.universal())
                    .withRandom(_r)
                    .withVars(c.getVars())
            ), _r
        );
        return trans;
    }
}
