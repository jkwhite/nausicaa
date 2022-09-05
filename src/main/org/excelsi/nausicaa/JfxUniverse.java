package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ifs.IteratedFunctionFactory;
import org.excelsi.nausicaa.ifs.IteratedFunction;
import org.excelsi.nausicaa.ca.*;
import java.util.HashMap;
import java.util.Map;
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
import javafx.scene.effect.*;
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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class JfxUniverse extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(JfxUniverse.class);
    private JfxWorld _w;


    @Override
    public void start(final Stage stage) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        //JfxWorld w = new JfxCaWorld(100, 100, 100, true);
        //JfxWorld w = new JfxIteratedFunction(800, 800, 800, true);
        //System.err.println("bounds: "+screen);
        JfxWorld w = new JfxIteratedFunction(1*(int)screen.getWidth(), 1*(int)screen.getHeight(), 400, true, true);
        w.initScene();
        Scene scene = w.getScene();

        Node menu = createMenu(stage, w);
        w.getGui().setTop(menu);
        // w.getGui().getStylesheets().add("/org/excelsi/nausicaa/nausicaa-jfx.css");
        w.getGui().getStylesheets().add("/nausicaa-jfx.css");
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
        _w = w;
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        final List<String> args = getParameters().getRaw();
        if(args.size()>0) {
            load(new File(args.get(0)));
        }
    }

    private Node createMenu(final Stage stage, final JfxWorld w) {
        Menu file = new Menu("File");
        MenuItem openc = new MenuItem("Open ...");
        openc.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        openc.setOnAction((e)->{ open(stage); });
        MenuItem savec = new MenuItem("Save");
        savec.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        savec.setOnAction((e)->{ save(stage); });
        MenuItem screens = new MenuItem("Screenshot ...");
        screens.setAccelerator(KeyCombination.keyCombination("Shortcut+T"));
        screens.setOnAction((e)->{ screenshot(stage); });
        file.getItems().addAll(openc, screens);

        Menu rend = new Menu("Render");
        MenuItem smesh = new MenuItem("Scatter Mesh");
        smesh.setOnAction((e)->{ w.setRender(JfxWorld.Render.mesh); });
        MenuItem bmesh = new MenuItem("Blob Mesh");
        bmesh.setOnAction((e)->{ w.setRender(JfxWorld.Render.blob_mesh); });
        MenuItem cells = new MenuItem("Cells");
        cells.setOnAction((e)->{ w.setRender(JfxWorld.Render.cells); });
        MenuItem best = new MenuItem("Auto");
        best.setOnAction((e)->{ w.setRender(JfxWorld.Render.best); });
        rend.getItems().addAll(smesh, bmesh, cells, best);

        Menu anim = new Menu("Animation");
        MenuItem astart = new MenuItem("Toggle update");
        astart.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        astart.setOnAction((e)->{ w.toggleAnimate(); });
        MenuItem arot = new MenuItem("Toggle rotation");
        //astart.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        arot.setOnAction((e)->{ w.toggleRotation(); });
        anim.getItems().addAll(astart, arot);

        anim.getItems().add(new SeparatorMenuItem());

        MenuItem disk = new MenuItem("Generate to disk ...");
        disk.setOnAction((e)->{ generateAnimation(stage); });
        anim.getItems().addAll(disk);

        Menu func = new Menu("Functions");
        MenuItem iter = new MenuItem("Iterated Function Demo 1");
        iter.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        iter.setOnAction((e)->{ loadDemoIfs(); });

        MenuItem params = new MenuItem("Iterated Function Demo 2");
        params.setAccelerator(KeyCombination.keyCombination("Shortcut+I"));
        params.setOnAction((e)->{ runParamDemo(); });

        func.getItems().addAll(iter, params);

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

        MenuBar mb = new MenuBar();
        mb.setUseSystemMenuBar(true);
        mb.getMenus().addAll(file, rend, anim, func, view);
        return mb;
    }

    public void open(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("A New World");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("CAs", "*.ca"),
                new ExtensionFilter("Functions", "*.ifs"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            load(selectedFile);
        }
    }

    public void save(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        //fileChooser.getExtensionFilters().addAll(
                //new ExtensionFilter("CAs", "*.ca"),
                //new ExtensionFilter("Functions", "*.ifs"),
                //new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            save(selectedFile);
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
        final ExecutorService pool = Pools.named("compute", 3);
        final JfxCaWorld w = (JfxCaWorld) _w;
        final Iterator<Plane> frames = w.getRule().frameIterator(w.getPlane(), pool, new GOptions(true, 3, 1, 1f));
        final Thread t = new Thread("coordinator") {
            @Override public void run() {
                for(int i=0;i<c.frames();i++) {
                    final String file = c.file()+"-"+i+".png";
                    System.err.println("generating "+file);
                    final Plane p = frames.next();
                    w.setPlane(p);
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
    }

    private final IteratedFunctionFactory IFS_DEMO = new IteratedFunctionFactory(
        "Demo 1",
        "{iterations:16}:circ(0,0,{size:1},{size:1}):scl*({scale_x:-0.95},{scale_y:0.95}) tran({trans_x:50},{trans_y:0}) rot({rotation:170})");
    private void loadDemoIfs() {
        showVariablePane(IFS_DEMO);
    }

    private void runParamDemo() {
        IteratedFunctionFactory f = new IteratedFunctionFactory(
            "Demo 1",
            "{iterations:16}:circ(0,0,{size:1},{size:1}):scl({scale_x:-0.5},{scale_y:0.5}) tran({trans_x:25},{trans_y:0}) rot({rotation:170})");
            //"{iterations:16}:circ(0,0,2,2):scl({scale:-0.95}) tran(50,10) rot({rotation:170})");
        final Varmap vars = f.getVarmap();
        vars.put("size","0.5");
        JfxIteratedFunction w = (JfxIteratedFunction) _w;
        Thread p = new Thread("runParamDemo") {
            @Override public void run() {
                int i=0;
                for(double rot=0;rot<360;rot+=0.25) {
                    vars.put("rotation", Double.toString(rot));
                    IteratedFunction it = f.createIfs(vars);
                    w.setFunction(it);
                    while(w.isGenerating()) {
                        try {
                            Thread.sleep(100);
                        }
                        catch(InterruptedException e) {
                            break;
                        }
                    }
                    File sav = new File("/Users/jkw/work/ifs/reorderdemo-"+i+".png");
                    snap(sav);
                    i++;
                }
            }
        };
        p.setDaemon(true);
        p.start();
    }

    private void showVariablePane(IteratedFunctionFactory f) {
        final Varmap vars = f.getVarmap();
        LOG.info("showing var editor for "+vars);
        BorderPane bp = new BorderPane();
        VBox v = new VBox();
        //bp.setTop(v);
        v.getChildren().add(new Label("Variables"));
        final Map<String,TextField> nvars = new HashMap<>();
        for(String nm:vars.getNames()) {
            Label lab = new Label(nm);
            v.getChildren().add(lab);
            TextField value = new TextField(vars.get(nm));
            v.getChildren().add(value);
            nvars.put(nm, value);
        }
        Button ok = new Button("Ok");
        ok.setOnAction((ev)->{
            for(Map.Entry<String,TextField> e:nvars.entrySet()) {
                vars.put(e.getKey(), e.getValue().getText());
            }
            IteratedFunction ifs = f.createIfs(vars);
            ((JfxIteratedFunction)_w).setFunction(ifs);
            _w.getGui().setRight(null);
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction((e)->{
            _w.getGui().setRight(null);
        });
        HBox h = new HBox(ok, cancel);
        v.getChildren().add(h);
        v.setPrefWidth(400);
        v.setMinWidth(400);
        //_w.getBorder().setCenter(null);
        _w.getGui().setRight(v);
    }

    private void load(final File selectedFile) {
        _w.load(selectedFile);
        /*
        if(selectedFile.getName().endsWith(".ca")) {
            try {
                CA ca = CA.fromFile(selectedFile.toString(), "text");
                System.err.println("PRELUDE: "+ca.getPrelude());
                //ca = ca.prelude(10);
                ((JfxCaWorld)_w).setCA(ca);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        */
    }

    private void save(final File selectedFile) {
        _w.save(selectedFile);
    }

    private void snap(final File save) {
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
    }
}
