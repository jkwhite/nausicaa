package org.excelsi.nausicaa;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.excelsi.solace.*;


public class Console extends JComponent {
    private static final Logger LOG = LoggerFactory.getLogger(Console.class);
    private JFXPanel _c;
    private JFrame _root;


    public Console(JFrame root) {
        _root = root;
        setLayout(new BorderLayout());
        _c = new JFXPanel();
        // _c.setPreferredSize(new Dimension(800, 600));
        // _c.setPreferredSize(root.getSize());
        setPreferredSize(root.getSize());
        add(_c, BorderLayout.CENTER);
        Platform.runLater(()->{ initConsole(); });
        Platform.setImplicitExit(false);
    }

    private void initConsole() {
        Group root = new Group();
        Scene scene = new Scene(root, 1280, 1024, true, SceneAntialiasing.BALANCED);
        scene.getStylesheets().add("/META-INF/solace/solace-default.css");
        scene.setFill(javafx.scene.paint.Color.BLACK);

        GShellFactory sf = new GShellFactory();
        MetaShell ms = new MetaShell();
        ms.setRoot("${user.home}/.solace");
        sf.setMetaShell(ms);

        JfxTabs tabs = new JfxTabs(sf);
        root.getChildren().add(tabs);

        JfxMetaConsole mc = new JfxMetaConsole();
        mc.setDelegate(tabs);
        mc.setShellFactory(sf);

        String usercss = mc.getShellFactory().getMetaShell().getUserStylesheetUrl();
        if(usercss!=null) {
            scene.getStylesheets().add(usercss);
        }

        LOG.info("console psize: "+getPreferredSize());
        tabs.setPrefWidth(getPreferredSize().width);
        tabs.setPrefHeight(getPreferredSize().height);
        _c.setScene(scene);

        mc.newTerminal();
    }
}
