package org.excelsi.nausicaa;


import org.excelsi.nausicaa.ca.*;
import static org.excelsi.nausicaa.ca.Sequence.Segment;
import static org.excelsi.nausicaa.ca.Sequencer.SegmentInfo;
import java.util.List;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javafx.application.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Window;
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
import javafx.collections.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class JfxSequencer extends Group {
    private static final Logger LOG = LoggerFactory.getLogger(JfxSequencer.class);
    private final Window _window;
    private final Config _config;
    private final Sequencer _seq;
    private final VBox _seqlist;
    private final ListView<Sequence> _sequences;


    public JfxSequencer(Window window, Sequencer seq, Config cfg) {
        _window = window;
        _seq = seq;
        _config = cfg;
        BorderPane bp = new BorderPane();
        getChildren().add(bp);

        VBox v = new VBox();
        v.getChildren().add(new Label("Sequences"));
        //for(String n:seq.listSequences()) {
            //v.getChildren().add(new Label(n));
        //}
        ObservableList<Sequence> seqlist = FXCollections.observableArrayList(
            seq.listSequences());
        ListView<Sequence> seqs = new ListView(seqlist);
        seqs.setEditable(true);
        seqs.setCellFactory((list)->{ return new SequenceCell(); });
        seqs.setPlaceholder(new Label("(No sequences)"));
        seqs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sequence>() {
                @Override public void changed(ObservableValue<? extends Sequence> o, Sequence old, Sequence nv) {
                    //System.err.println("new seq: "+nv);
                    if(nv!=null) {
                        _seq.setActive(nv);
                        bp.setCenter(new SequenceView(nv));
                    }
                }
            });

        v.getChildren().add(seqs);
        _sequences = seqs;

        Button ns = new Button("New Sequence");
        v.getChildren().add(ns);
        bp.setLeft(v);
        _seqlist = v;

        ns.setOnAction((e)->newSequence());
    }

    private void newSequence() {
        String uname = _seq.findUniqueName("Nameless");
        Sequence s = new Sequence(uname);
        _sequences.getItems().add(s);
        Platform.runLater( ()->{
            _sequences.edit(_sequences.getItems().size()-1);
        });
    }

    private void newSequenceO() {
        TextInputDialog d = new TextInputDialog("New Sequence");
        d.setTitle("New Sequence");
        d.setContentText("Name");
        Optional<String> n = d.showAndWait();
        if(n.isPresent()) {
        }
    }

    private class SequenceCell extends ListCell<Sequence> {
        @Override protected void updateItem(Sequence s, boolean empty) {
            super.updateItem(s, empty);
            if(isEditing()) {
                setText(null);
                TextField f = new TextField(s!=null?s.name():"");
                setGraphic(f);
                f.setOnAction((e)->{ getItem().name(f.getText()); commitEdit(getItem()); });
                f.requestFocus();
                f.selectAll();
            }
            else {
                setText(s==null?"":s.name());
                setGraphic(null);
            }
        }

        @Override public void startEdit() {
            super.startEdit();
            TextField f = new TextField(getText());
            setGraphic(f);
            f.setOnAction((e)->{ getItem().name(f.getText()); commitEdit(getItem()); });
            setText(null);
            f.requestFocus();
            f.selectAll();
        }

        @Override public void cancelEdit() {
            super.cancelEdit();
            setGraphic(null);
            setText(getItem().name());
        }

        @Override public void commitEdit(Sequence s) {
            super.commitEdit(s);
            Sequence m = getItem();
            setGraphic(null);
            setText(getItem().name());
            _seq.saveActive();
        }
    }

    private class SequenceView extends Group {
        public SequenceView(Sequence s) {
            VBox segs = new VBox();
            Segment[] segments = s.segments();
            for(int i=0;i<segments.length;i++) {
                SegmentView segv = new SegmentView(segments[i], i);
                segs.getChildren().add(segv);
            }
            Button addSeg = new Button("Add Segment");
            addSeg.setOnAction((e)-> { addSegment(); });
            segs.getChildren().add(addSeg);
            getChildren().add(segs);
        }
    }

    public void addSegment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("A New World");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("CAs", "*.ca"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(_window);
        if (selectedFile != null) {
            try {
                CA ca = CA.fromFile(selectedFile.toString(), "text");
                _seq.getActive().add(ca, 100);
                _seq.saveActive();
            }
            catch(IOException e) {
                LOG.error("failed parsing '"+selectedFile.toString()+"': "+e.getMessage(), e);
            }
        }
    }

    private class SegmentView extends Group {
        public SegmentView(Segment s, int idx) {
            SegmentInfo inf = _seq.readSegmentInfo(s, idx);

            VBox v = new VBox();

            HBox title = new HBox();
            String name = s.ca().getName();
            title.getChildren().add(new Label(name!=null?name:"Nameless"));
            title.getChildren().add(new Label("Generations: "+s.gens()));
            v.getChildren().add(title);

            HBox fr = new HBox();
            String[] frames = inf.frames();
            for(int i=0;i<frames.length;i+=10) {
                try(InputStream is=new BufferedInputStream(new FileInputStream(new File(inf.dir(), frames[i])))) {
                    Image frame = new Image(is, 100, 100, true, true);
                    fr.getChildren().add(new ImageView(frame));
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
            v.getChildren().add(fr);
            getChildren().add(v);
        }
    }
}
