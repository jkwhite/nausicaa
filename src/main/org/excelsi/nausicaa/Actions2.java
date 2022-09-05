package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;

import java.math.BigInteger;
// import java.awt.*;
import java.awt.image.BufferedImage;
// import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.imageio.ImageIO;
// import javax.swing.*;
import java.util.EnumSet;
import java.util.Random;
import java.util.Optional;
// import java.awt.event.ActionEvent;
// import javax.swing.AbstractAction;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;


public class Actions2 {
    public void newCA(Config config) {
        // ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
        Dialog<Void> d = new Dialog<>();

        GridPane g = new GridPane(); // 8,2

        g.add(new Label("Dimensions"), 0, 0);
        TextField dims = new TextField();
        dims.setText(config.getVariable("default_dimensions", "2"));
        dims.setPrefColumnCount(3);
        g.add(dims, 1, 0);

        g.add(new Label("Size"), 0, 1);
        TextField siz = new TextField();
        siz.setText(config.getVariable("default_size", "1"));
        siz.setPrefColumnCount(3);
        g.add(siz, 1, 1);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().add(ButtonType.OK);
        d.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<Void> r = d.showAndWait();
    }
}
