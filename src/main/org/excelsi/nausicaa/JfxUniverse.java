package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.io.File;
import java.io.IOException;
import javafx.application.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.Pos;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCombination;


public class JfxUniverse extends Application {
    @Override
    public void start(final Stage stage) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        //JfxTabs tabs = new JfxTabs(_mc.getShellFactory());
        //_mc.setDelegate(tabs);
        //BorderPane root = new BorderPane();
        //root.setCenter(tabs);
        //root.setTop(createMenu(stage, tabs));

        JfxWorld w = new JfxWorld(100, 100, 100, true);
        //Scene scene = new Scene(root, 1280, 1024, true, SceneAntialiasing.BALANCED);
        w.initScene();
        Scene scene = w.getScene();

        Node menu = createMenu(stage, w);
        w.getBorder().setTop(menu);
        //scene.getStylesheets().add("/org/excelsi/solace/solace-default.css");
        //String usercss = _mc.getShellFactory().getMetaShell().getUserStylesheetUrl();
        //if(usercss!=null) {
            //scene.setUserAgentStylesheet(usercss);
            //scene.getStylesheets().add(usercss);
        //}
        scene.setFill(Color.BLACK);
        stage.setX(screen.getMinX());
        stage.setY(screen.getMinY());
        stage.setWidth(screen.getWidth());
        stage.setHeight(screen.getHeight());

        stage.setTitle("Universe");
        //stage.setFullScreen(true);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private Node createMenu(final Stage stage, final JfxWorld w) {
        Menu file = new Menu("File");
        MenuItem openc = new MenuItem("Open ...");
        openc.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        openc.setOnAction((e)->{ open(stage, w); });
        file.getItems().addAll(openc);

        //Menu edit = new Menu("Edit");
        //MenuItem cut = new MenuItem("Cut");
        //cut.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        //cut.setOnAction((e)->{ mc.cutSelection(); });
        //MenuItem copy = new MenuItem("Copy");
        //copy.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        //copy.setOnAction((e)->{ mc.copySelection(); });
        //MenuItem paste = new MenuItem("Paste");
        //paste.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        //paste.setOnAction((e)->{ mc.pasteBuffer(); });
        //edit.getItems().addAll(cut, copy, paste);

        Menu rend = new Menu("Render");
        MenuItem smesh = new MenuItem("Scatter Mesh");
        smesh.setOnAction((e)->{ w.setRender(JfxCA.Render.mesh); });
        MenuItem bmesh = new MenuItem("Blob Mesh");
        bmesh.setOnAction((e)->{ w.setRender(JfxCA.Render.blob_mesh); });
        MenuItem cells = new MenuItem("Cells");
        cells.setOnAction((e)->{ w.setRender(JfxCA.Render.cells); });
        rend.getItems().addAll(smesh, bmesh, cells);

        Menu view = new Menu("View");
        MenuItem fullsc = new MenuItem("Full Screen");
        fullsc.setAccelerator(KeyCombination.keyCombination("Shortcut+ENTER"));
        fullsc.setOnAction((e)->{ stage.setFullScreen(!stage.isFullScreen()); });
        view.getItems().addAll(fullsc);

        view.getItems().add(new SeparatorMenuItem());

        MenuItem scaleup = new MenuItem("Scale up");
        scaleup.setAccelerator(KeyCombination.keyCombination("Shortcut+="));
        scaleup.setOnAction((e)->{ w.scaleUp(); });
        MenuItem scaledown = new MenuItem("Scale down");
        scaledown.setAccelerator(KeyCombination.keyCombination("Shortcut+-"));
        scaledown.setOnAction((e)->{ w.scaleDown(); });
        view.getItems().addAll(scaleup, scaledown);

        //Menu window = new Menu("Window");
        //MenuItem shiftr = new MenuItem("Next Tab");
        //shiftr.setAccelerator(KeyCombination.keyCombination("Shortcut+RIGHT"));
        //shiftr.setOnAction((e)->{ mc.nextTerminal(); });
        //MenuItem shiftl = new MenuItem("Prev Tab");
        //shiftl.setAccelerator(KeyCombination.keyCombination("Shortcut+LEFT"));
        //shiftl.setOnAction((e)->{ mc.prevTerminal(); });
        //window.getItems().addAll(shiftr, shiftl);

        MenuBar mb = new MenuBar();
        mb.setUseSystemMenuBar(true);
        mb.getMenus().addAll(file, rend, view);
        return mb;
    }

    public void open(Stage stage, JfxWorld w) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("A New World");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("CAs", "*.ca"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                CA ca = CA.fromFile(selectedFile.toString(), "text");
                System.err.println("PRELUDE: "+ca.getPrelude());
                //ca = ca.prelude(10);
                w.setCA(ca);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
