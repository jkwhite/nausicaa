package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import groovy.lang.GroovyShell;
import groovy.lang.Binding;
//import org.excelsi.solace.GShell;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Functions {
    private static final Logger LOG = LoggerFactory.getLogger(Functions.class);
    private final File _dir;
    private final Map<String,CAFunction> _fs;


    public Functions(File dir) {
        _dir = dir;
        _fs = new HashMap<>();
        load();
    }

    public CAFunction[] catalog() {
        return _fs.values().toArray(new CAFunction[0]);
    }

    public CAFunction function(String name) {
        return _fs.get(name);
    }

    public static void abandon(String msg) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("/Users/jkw/nausicaa-debug"));
            for(Map.Entry e:System.getProperties().entrySet()) {
                pw.println("'"+e.getKey()+"' => '"+e.getValue()+"'");
            }
            pw.println(System.getenv().toString());
            pw.println(msg);
            pw.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public static void initAppRoot() {
        if(System.getProperty("app.root")==null) {
            String jcp = System.getProperty("java.class.path");
            if(jcp.indexOf("Contents/")>0) {
                jcp = jcp.substring(0, jcp.indexOf("Contents/"));
                File root = new File(jcp+"/Contents/");
                if(root.isDirectory()) {
                    System.setProperty("app.root", root.toString());
                }
                else {
                    abandon("not a directory or no such file '"+root+"'");
                }
            }
            else {
                abandon("Could not find 'Contents/' in '"+jcp+"'");
            }
        }
        if(System.getProperty("app.root")==null) {
            abandon("somehow app.root is still null");
        }
        System.err.println("app.root='"+System.getProperty("app.root")+"'");
    }

    private void load() {
        initAppRoot();
        File funcs = new File(System.getProperty("app.root")+"/etc/functions");
        if(!funcs.exists() || !funcs.isDirectory()) {
            abandon("bad function path '"+funcs+"'");
        }
        for(File f:funcs.listFiles()) {
            if(!f.getName().startsWith(".")) {
                try {
                    CAFunction fn = new GroovyCAFunction(f);
                    _fs.put(fn.getName(), fn);
                }
                catch(Exception e) {
                    LOG.error("failed processing '"+f+"': "+e, e);
                    //e.printStackTrace();
                }
            }
        }
    }

    public static interface CAFunction {
        String getName();
        String[] buildArgs(CA ca);
        void run(CA ca, Varmap params, API api) throws Exception;
    }

    public static interface API {
        ExecutorService getPool();
        GOptions getOptions();
        MutationFactor getMutationFactor();
        Rendering getRendering();
        boolean getCancelled();
        Progress getProgress();
    }

    public static interface Progress {
        void setMaximum(int max);
        void setCurrent(int cur);
        void setStatus(String status);
    }

    private static class GroovyCAFunction implements CAFunction {
        private final String _name;
        //private final String[] _args;

        private final GroovyShell _s;
        private final Binding _b;
        //private final GShell _gs;

        public GroovyCAFunction(File f) throws Exception {
            _b = new Binding();
            _s = new GroovyShell(_b);
            //_gs = new GShell();
            //_gs.init();
            Object res;
            try(BufferedInputStream i = new BufferedInputStream(new FileInputStream(f))) {
                _s.evaluate(i);
                //_gs.evaluate(GShell.readFully(i));
                res = _s.evaluate("meta()");
            }
            //_gs.evalScript(f);
            //res = _gs.evaluate("meta()");
            if(res instanceof Map) {
                Map mr = (Map) res;
                if(mr.containsKey("name")) {
                    _name = mr.get("name").toString();
                }
                else {
                    _name = "<"+f+">";
                }
                //if(mr.containsKey("args") && mr.get("args") instanceof List) {
                    //_args = ((List<String>)mr.get("args")).toArray(new String[0]);
                //}
                //else {
                    //_args = new String[0];
                //}
            }
            else {
                _name = "<"+f+">";
                //_args = new String[0];
            }
        }

        @Override public String getName() { return _name; }

        @Override public String[] buildArgs(CA ca) {
            try {
                _b.setVariable("_ca", ca);
                Object res = _s.evaluate("meta(_ca)");
                String[] args;
                if(res instanceof Map) {
                    Map mr = (Map) res;
                    if(mr.containsKey("args") && mr.get("args") instanceof List) {
                        List as = (List) mr.get("args");
                        //for(Object aso:as) {
                            //System.err.println(aso.toString()+":"+aso.getClass().toString());
                        //}
                        args = ((List<String>)mr.get("args")).toArray(new String[0]);
                    }
                    else {
                        args = new String[0];
                    }
                }
                else {
                    args = new String[0];
                }
                return args;
            }
            catch(Exception e) {
                throw new IllegalStateException("failed building args: "+e, e);
            }
        }

        @Override public void run(CA ca, Varmap args, API api) throws Exception {
            LOG.info("start running param with args: "+args);
            _b.setVariable("_ca", ca);
            _b.setVariable("_api", api);
            _b.setVariable("_args", args);
            _s.evaluate("run(_ca, _args, _api)");
            //_gs.setVariable("_ca", ca);
            //_gs.setVariable("_api", api);
            //_gs.setVariable("_args", args);
            //_gs.evaluate("run(_ca, _args, _api)");
            LOG.info("done running param with args: "+args);
        }
    }
}
