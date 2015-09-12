package org.excelsi.ca;


import org.jgraph.plaf.basic.*;
import org.jgraph.graph.*;
import org.jgraph.event.*;
import org.jgraph.JGraph;
import java.awt.Graphics;


public class CAGraphUI extends BasicGraphUI {
    public CellHandle createHandle(GraphContext context) {
        if(context!=null && !context.isEmpty() && graph.isEnabled()) {
            return new NullHandle(context);
        }
        return null;
    }

    //protected GraphSelectionListener createGraphSelectionListener() {
        //return new GraphSelectionHandler() {
            //public void valueChanged(GraphSelectionEvent e) {
            //}
        //};
    //}

    public class NullHandle extends RootHandle {
        public NullHandle(GraphContext ctx) {
            super(ctx);
        }

        public void paint(Graphics g) {
        }
    }
}
