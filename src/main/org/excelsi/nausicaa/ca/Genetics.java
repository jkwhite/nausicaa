package org.excelsi.nausicaa.ca;


import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class Genetics {
    public static Mutagen identity() {
        return (a,b)->{};
    }

    public static Mutagen flip() {
        return (a,b)->{
            for(int i=0;i<b.length;i++) {
                b[i] = (byte) (a.colors() - b[i] - 1);
            }
        };
    }

    public static Mutagen shift() {
        return (a,b)->{
            byte b0 = b[0];
            System.arraycopy(b, 1, b, 0, b.length-1);
            b[b.length-1] = b0;
        };
    }

    public static Mutagen flipOne() {
        return new Mutagen() {
            private int i = -1;

            public void mutate(Archetype a, byte[] b) {
                if(i>=0) {
                    b[i] = (byte) (a.colors() - b[i] - 1);
                }
                i = (i+1) % b.length;
                b[i] = (byte) (a.colors() - b[i] - 1);
            }
        };
    }

    public static Mutagen swap() {
        return new Mutagen() {
            private int i = 0;

            public void mutate(Archetype a, byte[] b) {
                int j = (i+1) % b.length;
                byte b0 = b[i];
                byte b1 = b[j];
                b[i] = b1;
                b[j] = b0;
                if(--i==-1) {
                    i = b.length-1;
                }
            }
        };
    }
}
