package org.excelsi.ca;


import java.io.*;


public class Runner {
    private String[] _cmd;
    private ProcessBuilder _pb;


    public Runner(String... cmd) {
        _cmd = cmd;
        _pb = new ProcessBuilder(cmd);
        _pb.redirectErrorStream(true);
    }

    public int go() throws IOException {
        final Process p = _pb.start();
        new Thread() {
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while((line=br.readLine())!=null) {
                        System.out.println(line);
                    }
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line;
                    while((line=br.readLine())!=null) {
                        System.err.println(line);
                    }
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        int exit = 0;
        try {
            exit = p.waitFor();
        }
        catch(InterruptedException e) {
            p.destroy();
        }
        return exit;
    }
}
