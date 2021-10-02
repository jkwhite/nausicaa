package org.excelsi.nausicaa.ifs;


import org.excelsi.nausicaa.common.Genomes;
import org.excelsi.nausicaa.ca.Parameterized;
import org.excelsi.nausicaa.ca.Varmap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static org.excelsi.nausicaa.ifs.Codons.*;
import java.util.regex.*;
import java.util.Arrays;


public class IteratedFunctionFactory implements Parameterized {
    private static final Logger LOG = LoggerFactory.getLogger(IteratedFunctionFactory.class);

    private String _name;
    private String _genome;
    private Varmap _vars;


    public IteratedFunctionFactory(String name, String genome) {
        _name = name;
        _genome = genome;
        _vars = Genomes.createVarmap(_genome);
    }

    public Varmap getVarmap() {
        return _vars;
    }

    @Override public Varmap vars() { return getVarmap(); }

    public String resolve(Varmap vars) {
        return Genomes.resolveParams(_genome, vars);
    }

    public IteratedFunction createIfs() {
        return createIfs(_vars);
    }

    public IteratedFunction createIfs(Varmap vars) {
        String g = resolve(vars);
        LOG.info("resolved genome: '"+g+"'");
        S s = parseGenome(g);
        System.err.println("parsed genome: '"+s+"'");
        TreeTape.Op[] init = new TreeTape.Op[s.init.length];
        TreeTape.Op[] iter = new TreeTape.Op[s.iter.length];
        for(int i=0;i<s.init.length;i++) {
            init[i] = Codons.createOp(s.init[i].nm, s.init[i].args, s.init[i].fork);
        }
        for(int i=0;i<s.iter.length;i++) {
            iter[i] = Codons.createOp(s.iter[i].nm, s.iter[i].args, s.iter[i].fork);
        }
        return new IteratedFunction(init, iter, s.iterations);
    }

    public IteratedFunction oldCreateIfs(Varmap vars) {
        String g = resolve(vars);
        LOG.info("resolved genome: '"+g+"'");
        S s = parseGenome(g);
        System.err.println("parsed genome: '"+s+"'");
        Double sclx = Double.parseDouble(vars.get("scale_x","-0.95"));
        Double scly = Double.parseDouble(vars.get("scale_y","0.95"));
        Double trnx = Double.parseDouble(vars.get("trans_x","50"));
        Double trny = Double.parseDouble(vars.get("trans_y","0"));
        Double size = Double.parseDouble(vars.get("size","1"));
        Double rot = Double.parseDouble(vars.get("rotation","170"));
        Integer it = Integer.parseInt(vars.get("iterations","16"));
        return new IteratedFunction(
            //new TreeTape.Op[]{new FillOval(0,0,size,size)},
            new TreeTape.Op[]{new FillRect(0,0,size,size)},
            //new TreeTape.Op[]{new StrokeRect(0,0,size,size)},
            new TreeTape.Op[]{
                //new Scale(1.25,1.25),
                new Rotate(rot,0.0).withFork(true),
                new Scale(sclx,scly), //.withFork(true),
                new Translate(trnx,trny)
                //new Scale(-1.11,1.11).withFork(true),
            }, it);
    }

    private static class S {
        public int iterations;
        F[] init;
        F[] iter;
        @Override public String toString() { return "S::{iterations:"+iterations+",init:"
            +Arrays.asList(init)+",iter:"+Arrays.asList(iter)+"}";
        }
    }

    private static class F {
        String nm;
        double[] args;
        boolean fork;
        @Override public String toString() { return "F::{nm:'"+nm+"',fork:"+fork+",args:"+Arrays.toString(args)+"}"; };
    }

    private static S parseGenome(String g) {
        String[] parts = g.split(":");
        S s = new S();
        s.iterations = Integer.parseInt(parts[0]);
        s.init = parseFs(parts[1]);
        s.iter = parseFs(parts[2]);
        return s;
    }

    private static F[] parseFs(String fs) {
        String[] cs = fs.split(" ");
        F[] seq = new F[cs.length];
        for(int i=0;i<cs.length;i++) {
            seq[i] = parseF(cs[i]);
        }
        return seq;
    }

    private static F parseF(String f) {
        F fn = new F();
        System.err.println("parseF: '"+f+"'");
        Pattern p = Pattern.compile("(\\w+\\*?)[(](.*?)[)]");
        Matcher m = p.matcher(f);
        if(m.find()) {
            fn.nm = m.group(1);
            if(fn.nm.endsWith("*")) {
                fn.fork = true;
                fn.nm = fn.nm.substring(0, fn.nm.length()-1);
            }
            if(m.groupCount()>1 && m.group(2).length()>0) {
                String[] args = m.group(2).split(",");
                fn.args = new double[args.length];
                for(int i=0;i<args.length;i++) {
                    fn.args[i] = Double.parseDouble(args[i]);
                }
            }
            else {
                fn.args = new double[0];
            }
        }
        else {
            throw new RuntimeException("malformed f: '"+f+"'");
        }
        //String in = "";
        //while(m.find()) {
            //System.err.println(in+m.groupCount()+" FOUND: '"+m.group()+"': "+m.start()+"-"+m.end());
            //for(int i=1;i<=m.groupCount();i++) {
                //System.err.println(in+i+": '"+m.group(i)+"'");
            //}
            //in = in+" ";
        //}
        return fn;
    }

    // 16:a0 a0 a50 a50 ci:a-0.75 a0.75 sc a100 a100 tr 90 ro
    // 16:ci(0,0,50,50):sc(-0.75,0.75) tr(100,100) ro(90)

    //"{iterations:16}:circ(0,0,{size:50},{size:50}):scl({scale_x:-0.75},{scale_y:0.75}) tran({trans_x:100},{trans_y:100}) rot({rotation:90})");
    public IteratedFunction rustDemoCreateIfs(Varmap vars) {
        String g = resolve(vars);
        LOG.info("resolved genome: '"+g+"'");
        Double sclx = Double.parseDouble(vars.get("scale_x","-0.95"));
        Double scly = Double.parseDouble(vars.get("scale_y","0.95"));
        Double trnx = Double.parseDouble(vars.get("trans_x","50"));
        Double trny = Double.parseDouble(vars.get("trans_y","0"));
        Double size = Double.parseDouble(vars.get("size","1"));
        Double rot = Double.parseDouble(vars.get("rotation","170"));
        Integer it = Integer.parseInt(vars.get("iterations","16"));
        return new IteratedFunction(
            //new TreeTape.Op[]{new FillOval(0,0,size,size)},
            new TreeTape.Op[]{new FillRect(0,0,size,size)},
            //new TreeTape.Op[]{new StrokeRect(0,0,size,size)},
            new TreeTape.Op[]{
                //new Scale(1.25,1.25),
                new Rotate(rot,0.0).withFork(true),
                new Scale(sclx,scly), //.withFork(true),
                new Translate(trnx,trny)
                //new Scale(-1.11,1.11).withFork(true),
            }, it);
    }
}
