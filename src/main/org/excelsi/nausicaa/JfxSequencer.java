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


public class JfxSequencer extends Group {
    private final Sequencer _seq;


    public JfxSequencer(Sequencer seq) {
        _seq = seq;
        VBox v = new VBox();
        for(String n:seq.listSequences()) {
            v.getChildren().add(new Label(n));
        }
        v.getChildren().add(new Label("+ New Sequence"));
        getChildren().add(v);
    }
}
