package org.excelsi.ca;


import java.io.*;


public class Streamer extends Thread {
    private InputStream _is;


    public Streamer(InputStream is) {
        _is = is;
    }

    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(_is));
            String line;
            while((line=br.readLine())!=null) {
                System.err.println(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if(br!=null) {
                try { br.close(); } catch(IOException e) {}
            }
        }
    }
}
