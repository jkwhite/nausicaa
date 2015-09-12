package org.excelsi.ca;


import org.jgraph.graph.GraphConstants;
import org.redsails.graph.*;
import java.util.*;
import java.io.*;
import java.awt.geom.Rectangle2D;
import java.awt.Color;


public class Branch<T> extends DefaultVertex implements Serializable {
    private T _data;
    //private List<Branch<T>> _children;
    //private Branch<T> _parent;


    public Branch(T data) {
        DefaultPort p = new DefaultPort();
        p.setInput(true);
        p.setOutput(true);
        addPort(p);
        _data = data;
    }

    //private Branch(Branch<T> parent, T data) {
        //_parent = parent;
        //_data = data;
    //}

    //public Branch<T> leaf(int i) {
        //return _children.get(i);
    //}

    public int size() {
        //return _children!=null?_children.size():0;
        return 0;
    }

    public Branch<T> grow(T data) {
        return grow(data, null);
    }

    public Branch<T> grow(T data, String reason) {
        DefaultEdge e = new DefaultEdge() {
            public void decorate(Map m) {
                super.decorate(m);
                GraphConstants.setLineColor(m, Color.WHITE);
            }
        };
        if(reason!=null) {
            //e.setUserObject(reason);
        }
        Branch<T> child = (Branch<T>) fuse(new Branch<T>(data), e);
        Rectangle2D nb = new Rectangle2D.Float();
        nb.setRect(GraphConstants.getBounds(getAttributes()));
        GraphConstants.setBounds(child.getAttributes(), nb);
        return child;
        //Branch<T> leaf = new Branch<T>(this, data);
        //if(_children==null) {
            //_children = new ArrayList<Branch<T>>(1);
        //}
        //_children.add(leaf);
        //return leaf;
    }

    public T data() {
        return _data;
    }

    public String toString() {
        return _data.toString();
    }

    public void decorate(Map attributes) {
        Rectangle2D bounds = GraphConstants.getBounds(attributes);
        if(bounds==null) {
            bounds = new Rectangle2D.Float(0, 0, World.getSize(), World.getSize());
        }
        else {
            bounds.setRect(bounds.getX(), bounds.getY(), World.getSize(), World.getSize());
        }
        GraphConstants.setBounds(attributes, bounds);
    }
}
