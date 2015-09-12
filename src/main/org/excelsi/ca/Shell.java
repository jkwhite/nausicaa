package org.excelsi.ca;


import bsh.*;
import java.io.*;


public class Shell {
    public static void main(String[] args) throws Exception {
        int i=0;
        boolean graphical = false;
        while(i<args.length&&args[i].startsWith("-")) {
            if("-g".equals(args[i])) {
                graphical = true;
            }
            ++i;
        }
        //--i;

        if(graphical) {
            Viewer.main(args);
        }
        else {
            Interpreter in;
            in = new Interpreter(new InputStreamReader(System.in), System.out, System.err, true);
            in.eval("import org.excelsi.ca.*;");
            if(i<args.length) {
                in.eval(new BufferedReader(new FileReader(args[i])));
            }
            in.run();
        }
    }
}
