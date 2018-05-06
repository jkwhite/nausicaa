package org.excelsi.nausicaa.ca;


import java.util.Random;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import groovy.lang.GroovyShell;
import groovy.lang.Binding;


public class CustomInitializer implements Initializer {
    private final String _text;


    public CustomInitializer() {
        this(null);
    }

    public CustomInitializer(String text) {
        _text = text;
    }

    public void init(Plane plane, Rule rule, Random random) {
        Binding b = new Binding();
        GroovyShell interpreter = new GroovyShell(b);
        b.setVariable("a", rule.archetype());
        b.setVariable("i", new Painter(plane));
        b.setVariable("r", random);
        try {
            interpreter.evaluate(_text);
        }
        catch(Exception e) {
            throw new IllegalStateException("failed executing initializer: "+e, e);
        }
    }

    @Override public void write(DataOutputStream dos) throws IOException {
        dos.writeByte(Initializers.custom.getId());
    }

    @Override public void write(PrintWriter w) {
        w.println(Initializers.custom.name());
        w.println("====");
        w.println(_text);       
        w.println("====");
    }

    public static CustomInitializer read(BufferedReader r, int version) throws IOException {
        StringBuilder t = new StringBuilder();
        String delim = r.readLine();
        String line;
        while(true) {
            line = r.readLine();
            if(delim.equals(line)) {
                break;
            }
            t.append(line).append("\n");
        }
        return new CustomInitializer(t.toString());
    }
}
