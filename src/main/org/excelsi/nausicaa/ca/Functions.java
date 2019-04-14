package org.excelsi.nausicaa.ca;


import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import groovy.lang.GroovyShell;
import groovy.lang.Binding;
//import org.excelsi.solace.GShell;


public class Functions {
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

    private void load() {
        File funcs = new File(System.getProperty("app.root")+"/etc/functions");
        if(System.getProperty("app.root")==null) {
            throw new IllegalStateException("Quite a pickle: Nausicaa missing app.root sysproperty; cwd="+System.getProperty("user.dir"));
        }
        if(!funcs.exists() || !funcs.isDirectory()) {
            funcs = new File(System.getProperty("app.root")+"/resources/main/functions");
            if(!funcs.exists() || !funcs.isDirectory()) {
                throw new IllegalStateException("Quite a pickle: "+funcs+" does not exist or is not a dir, app.root="+System.getProperty("app.root"));
            }
        }
        for(File f:funcs.listFiles()) {
            if(!f.getName().startsWith(".")) {
                try {
                    CAFunction fn = new GroovyCAFunction(f);
                    _fs.put(fn.getName(), fn);
                }
                catch(Exception e) {
                    System.err.println("failed processing '"+f+"': "+e);
                    e.printStackTrace();
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
            System.err.println("start running param with args: "+args);
            _b.setVariable("_ca", ca);
            _b.setVariable("_api", api);
            _b.setVariable("_args", args);
            _s.evaluate("run(_ca, _args, _api)");
            //_gs.setVariable("_ca", ca);
            //_gs.setVariable("_api", api);
            //_gs.setVariable("_args", args);
            //_gs.evaluate("run(_ca, _args, _api)");
            System.err.println("done running param with args: "+args);
        }
    }
}
