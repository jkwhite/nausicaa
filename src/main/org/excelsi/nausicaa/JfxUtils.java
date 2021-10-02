package org.excelsi.nausicaa;


import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.*;
import javafx.application.Platform;

import java.io.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class JfxUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JfxUtils.class);


    public static void snap(final File save, final Scene scene) {
        if(Platform.isFxApplicationThread()) {
            synchronized(save) {
                try {
                    final WritableImage i = scene.snapshot(null);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(i, null), "png", save);
                    }
                    catch(IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                finally {
                    save.notify();
                }
            }
        }
        else {
            synchronized(save) {
                Platform.runLater(()->snap(save, scene));
                try {
                    save.wait();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
