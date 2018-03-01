package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.io.*;
import java.util.*;


public class Cli {
    public static void main(String[] args) {
        try {
            Iterator<String> i = Arrays.asList(args).iterator();
            String cmd = i.next();
            switch(cmd) {
                case "generate":
                    generate(i);
                    break;
                default:
                    System.err.println("unknown cmd '"+cmd+"'");
                    System.exit(1);
                    break;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generate(Iterator<String> i) throws IOException {
        final CAGenerator g = new CAGenerator().progress(new CAGenerator.StderrProgress());
        while(i.hasNext()) {
            switch(i.next()) {
                case "-cores":
                    g.cores(Integer.parseInt(i.next()));
                    break;
                case "-animate":
                    g.animate(true);
                    break;
                case "-gif":
                    g.gif(true);
                    break;
                case "-frames":
                    g.frames(Integer.parseInt(i.next()));
                    break;
                case "-frameRate":
                    g.frameRate(Integer.parseInt(i.next()));
                    break;
                case "-scale":
                    g.scale(Float.parseFloat(i.next()));
                    break;
                case "-output":
                    g.output(i.next());
                    break;
                case "-width":
                    g.width(Integer.parseInt(i.next()));
                    break;
                case "-height":
                    g.height(Integer.parseInt(i.next()));
                    break;
                case "-weight":
                    g.weight(Integer.parseInt(i.next()));
                    break;
                case "-skipFrames":
                    g.skipFrames(Integer.parseInt(i.next()));
                    break;
                case "-ca":
                    g.ca(CA.fromFile(i.next(), "text"));
                    break;
                default:
                    throw new IllegalArgumentException("unknown argument");
            }
        }
        g.run();
    }
}
