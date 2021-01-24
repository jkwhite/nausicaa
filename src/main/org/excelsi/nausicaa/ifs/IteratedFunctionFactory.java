package org.excelsi.nausicaa.ifs;


import org.excelsi.nausicaa.common.Genomes;
import org.excelsi.nausicaa.ca.Parameterized;
import org.excelsi.nausicaa.ca.Varmap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static org.excelsi.nausicaa.ifs.Codons.*;


public class IteratedFunctionFactory implements Parameterized {
    private static final Logger LOG = LoggerFactory.getLogger(IteratedFunctionFactory.class);

    private String _name;
    private String _genome;


    public IteratedFunctionFactory(String name, String genome) {
        _name = name;
        _genome = genome;
    }

    public Varmap getVarmap() {
        return Genomes.createVarmap(_genome);
    }

    @Override public Varmap vars() { return getVarmap(); }

    public String resolve(Varmap vars) {
        return Genomes.resolveParams(_genome, vars);
    }

    public IteratedFunction createIfs(Varmap vars) {
        String g = resolve(vars);
        LOG.info("resolved genome: '"+g+"'");
        return new IteratedFunction(
            new TreeTape.Op[]{new FillOval(0,0,2,2)},
            new TreeTape.Op[]{
                new Scale(-0.95,0.95).withFork(true),
                //new Scale(-1.11,1.11).withFork(true),
                new Translate(50,10),
                new Rotate(170,0.0)
            }, 17);
    }
}
