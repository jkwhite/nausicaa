package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import java.util.List;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;
import javafx.application.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;


public class JfxNausicaa extends Application {
    private Config _config;
    private Group _root;
    private BorderPane _border;
    private Actions2 _a = new Actions2();


    @Override
    public void start(final Stage stage) {
        _config = Config.load();

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        //JfxWorld w = new JfxWorld(100, 100, 100, true);
        //Scene scene = new Scene(root, 1280, 1024, true, SceneAntialiasing.BALANCED);
        //w.initScene();
        //Scene scene = w.getScene();
        BorderPane bp = new BorderPane();
        //bp.setStyle("-fx-background-color: transparent;");
        Group g = new Group();
        bp.setCenter(g);
        Scene scene = new Scene(bp, 800, 800, true, SceneAntialiasing.BALANCED);
        _root = g;
        _border = bp;

        //final PerspectiveCamera cam = new PerspectiveCamera(true);
        //cam.setFarClip(10000);
        //scene.setCamera(cam);

        Node menu = createMenu(stage);
        _border.setTop(menu);

        BorderPane mwin = new BorderPane();
        mwin.setPrefSize(screen.getWidth(), screen.getHeight());
        _root.getChildren().add(mwin);

        Node main = createMain(stage);
        mwin.setTop(main);
        //_root.getChildren().add(main);

        scene.getStylesheets().add("/nausicaa-jfx.css");
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

        stage.setTitle("Nausicaa");
        //stage.setFullScreen(true);
        //_w = w;
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        //final List<String> args = getParameters().getRaw();
        //if(args.size()>0) {
            //loadCA(new File(args.get(0)));
        //}
    }

    private Node createMain(Stage stage) {
        // Tab multi = new Tab("Multiverse", new Label("TODO"));
        NViewer v = new NViewer();
        v.init();
        v.invalidate();
        java.awt.Dimension d = v.getAppSize();
        v.setSize(800,800);
        SwingNode sn = new SwingNode();
        sn.setContent(v.getRootPane());
        BorderPane snt = new BorderPane();
        snt.setCenter(sn);
        Tab multi = new Tab("Multiverse",
            snt
        );
        JfxSequencer jseq = new JfxSequencer(stage, createSequencer(), _config);
        //BorderPane bp = new BorderPane();
        //bp.setCenter(jseq);
        Tab seq = new Tab("Sequencer", jseq);
        return new TabPane(multi, seq);
    }

    private Sequencer createSequencer() {
        try {
            return new Sequencer("/Users/jkw/work/ca/seq");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Menu createFileMenu(final Stage stage) {
        Menu file = new Menu("File");

        MenuItem newca = new MenuItem("New ...");
        newca.setOnAction((e)->{_a.newCA(_config);});

        MenuItem openc = new MenuItem("Open ...");
        openc.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        openc.setOnAction((e)->{ open(stage); });

        MenuItem screens = new MenuItem("Screenshot ...");
        screens.setAccelerator(KeyCombination.keyCombination("Shortcut+T"));
        screens.setOnAction((e)->{ screenshot(stage); });
        file.getItems().addAll(newca, openc, screens);
        return file;
    }

    private Menu createRenderMenu(final Stage stage) {
        Menu rend = new Menu("Render");
        MenuItem smesh = new MenuItem("Scatter Mesh");
        //smesh.setOnAction((e)->{ w.setRender(JfxCA.Render.mesh); });
        MenuItem bmesh = new MenuItem("Blob Mesh");
        //bmesh.setOnAction((e)->{ w.setRender(JfxCA.Render.blob_mesh); });
        MenuItem cells = new MenuItem("Cells");
        //cells.setOnAction((e)->{ w.setRender(JfxCA.Render.cells); });
        MenuItem best = new MenuItem("Auto");
        //best.setOnAction((e)->{ w.setRender(JfxCA.Render.best); });
        rend.getItems().addAll(smesh, bmesh, cells, best);
        return rend;
    }

    private Menu createAnimationMenu(final Stage stage) {
        Menu anim = new Menu("Animation");
        MenuItem astart = new MenuItem("Toggle update");
        astart.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        //astart.setOnAction((e)->{ w.toggleAnimate(); });
        MenuItem arot = new MenuItem("Toggle rotation");
        //arot.setOnAction((e)->{ w.toggleRotation(); });
        anim.getItems().addAll(astart, arot);

        anim.getItems().add(new SeparatorMenuItem());

        MenuItem disk = new MenuItem("Generate to disk ...");
        disk.setOnAction((e)->{ generateAnimation(stage); });
        anim.getItems().addAll(disk);

        return anim;
    }

    private Menu createViewMenu(final Stage stage) {
        Menu view = new Menu("View");
        MenuItem fullsc = new MenuItem("Full Screen");
        fullsc.setAccelerator(KeyCombination.keyCombination("Shortcut+ENTER"));
        fullsc.setOnAction((e)->{ stage.setFullScreen(!stage.isFullScreen()); });
        view.getItems().addAll(fullsc);

        view.getItems().add(new SeparatorMenuItem());

        MenuItem scaleup = new MenuItem("Scale up");
        scaleup.setAccelerator(KeyCombination.keyCombination("Shortcut+="));
        //scaleup.setOnAction((e)->{ w.scaleUp(); });
        MenuItem scaledown = new MenuItem("Scale down");
        scaledown.setAccelerator(KeyCombination.keyCombination("Shortcut+-"));
        //scaledown.setOnAction((e)->{ w.scaleDown(); });
        view.getItems().addAll(scaleup, scaledown);
        return view;
    }

    private Node createMenu(final Stage stage /*, final JfxWorld w*/) {
        Menu file = createFileMenu(stage);
        Menu anim = createAnimationMenu(stage);
        Menu rend = createRenderMenu(stage);
        Menu view = createViewMenu(stage);

        MenuBar mb = new MenuBar();
        mb.setUseSystemMenuBar(true);
        mb.getMenus().addAll(file, rend, anim, view);
        return mb;
    }

    public void open(Stage stage) {
        FileChooser f = new FileChooser();
        f.setTitle("Open");
        f.getExtensionFilters().addAll(
                new ExtensionFilter("CAs", "*.ca"),
                new ExtensionFilter("All Files", "*.*"));
        File sel = f.showOpenDialog(stage);
        if (sel != null) {
            loadCA(sel);
        }
    }

    public void screenshot(final Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save screnshot");
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            if(!selectedFile.toString().endsWith(".png")) {
                selectedFile = new File(selectedFile.toString()+".png");
            }
            snap(selectedFile);
        }
    }

    static class GeneratorConfig {
        public String file;
        public int frames;

        public GeneratorConfig() {
        }

        public GeneratorConfig file(String file) {
            this.file = file;
            return this;
        }

        public GeneratorConfig frames(int f) {
            frames = f;
            return this;
        }

        public int frames() { return frames; }
        public String file() { return file; }
    }

    public void generateAnimation(final Stage stage) {
        Dialog<GeneratorConfig> d = new Dialog<>();
        d.setTitle("Generate animation");
        d.setResizable(true);

        GridPane p = new GridPane();

        p.add(new Label("File"), 1, 1);
        TextField file = new TextField();
        p.add(file, 2, 1);

        p.add(new Label("Frames"), 1, 2);
        TextField frames = new TextField();
        p.add(frames, 2, 2);

        d.getDialogPane().setContent(p);

        ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().add(ok);

        d.setResultConverter(new Callback<ButtonType, GeneratorConfig>() {
            @Override
            public GeneratorConfig call(ButtonType b) {
                if (b == ok) {
                    return new GeneratorConfig().file(file.getText()).frames(Integer.parseInt(frames.getText()));
                }
                return null;
            }
        });
        Optional<GeneratorConfig> res = d.showAndWait();
        if(res.isPresent()) {
            generate(res.get());
        }
    }

    private void generate(final GeneratorConfig c) {
        /*
        final ExecutorService pool = Pools.named("compute", 3);
        final Iterator<Plane> frames = _w.getRule().frameIterator(_w.getPlane(), pool, new GOptions(true, 3, 1, 1f));
        final Thread t = new Thread("coordinator") {
            @Override public void run() {
                for(int i=0;i<c.frames();i++) {
                    final String file = c.file()+"-"+i+".png";
                    System.err.println("generating "+file);
                    final Plane p = frames.next();
                    _w.setPlane(p);
                    p.lockWrite();
                    System.err.println("writing");
                    snap(new File(file));
                    p.unlockWrite();
                    try {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    };
                }
            }
        };
        t.start();
        */
    }

    private void loadCA(final File selectedFile) {
        try {
            CA ca = CA.fromFile(selectedFile.toString(), "text");
            System.err.println("PRELUDE: "+ca.getPrelude());
            //_w.setCA(ca);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void snap(final File save) {
        /*
        if(Platform.isFxApplicationThread()) {
            synchronized(save) {
                try {
                    final WritableImage i = _w.getScene().snapshot(null);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(i, null), "png", save);
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                finally {
                    save.notify();
                }
            }
        }
        else {
            synchronized(save) {
                Platform.runLater(()->snap(save));
                try {
                    save.wait();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        */
    }
}
