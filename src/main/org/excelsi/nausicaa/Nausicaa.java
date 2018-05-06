package org.excelsi.nausicaa;


import java.util.*;
import javafx.application.Application;


public class Nausicaa {
    public static void main(String[] args) {
        int i = 0;
        boolean jfx = false;
        boolean cmd = false;
        boolean cli = false;
        final List<String> files = new ArrayList<>();
        while(i<args.length && args[i].startsWith("-")) {
            if("-jfx".equals(args[i])) {
                jfx = true;
            }
            else if("-cmd".equals(args[i])) {
                cmd = true;
            }
            else if("-cli".equals(args[i])) {
                cli = true;
            }
            else {
                System.err.println("ignoring unknown arg '"+args[i]+"'");
            }
            i++;
        }
        for(;i<args.length;i++) {
            files.add(args[i]);
        }
        final String[] dargs = files.toArray(new String[0]);
        try {
            if(jfx) {
                Application.launch(JfxUniverse.class, dargs);
            }
            else if(cmd) {
                Cli.main(dargs);
            }
            else if(cli) {
                org.excelsi.gimmal.AppFactory.main(dargs);
            }
            else {
                NViewer.main(dargs);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
