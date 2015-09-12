
package org.excelsi.ca;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.*;


public class CAEditor extends JComponent implements ViewerListener {
    private Futures _f;
    private Rule _rule;
    private JFrame _root;


    public CAEditor(JFrame root, Viewer v) {
        _root = root;
        v.addViewerListener(this);
        if(v.currentFuture()!=null) {
            futureChanged(v.currentFuture());
        }
    }

    public void futureChanging(Futures f) {
    }

    public void futureChanged(Futures f) {
        removeAll();
        _f = f;
        setLayout(new BorderLayout());
        Display d = f.getMainDisplay();
        _rule = d.getRule();
        JPanel scr = new JPanel(new FlowLayout());
        scr.setLayout(new BoxLayout(scr, BoxLayout.Y_AXIS));
        if(_rule instanceof Multirule) {
            int i = 1;
            for(Rule r:((Multirule)_rule).rules()) {
                JPanel rp = new JPanel(new BorderLayout());
                rp.setLayout(new BoxLayout(rp, BoxLayout.X_AXIS));
                JPanel whole = new JPanel(new FlowLayout(FlowLayout.LEADING));
                JLabel l = new JLabel("Rule "+i++, SwingConstants.LEFT);
                scr.add(l);
                int[][] pats = r.toPattern();
                //Rule2D.testPattern(pats);
                //System.err.println("pattern count: "+pats.length);
                int[] colors = r.colors();
                int horiz = 4;
                whole.setLayout(new GridLayout(pats.length/horiz, horiz));
                for(int j=0;j<pats.length;j++) {
                    whole.add(new JPattern(d, r, i-2, pats, j, colors));
                }
                JPanel rp2 = new JPanel(new BorderLayout());
                rp2.add(whole, BorderLayout.NORTH);
                rp.add(rp2);
                rp.add(Box.createHorizontalGlue());
                scr.add(rp);
            }
        }
        scr.add(Box.createVerticalGlue());
        JScrollPane p = new JScrollPane(scr /*,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER*/);
        add(p, BorderLayout.CENTER);
        validate();
        if(getParent()!=null) {
            Dimension dim = getParent().getPreferredSize();
            _root.setSize(16+dim.width, 24+dim.height);
        }
    }

    private class JPattern extends JPanel {
        private int[][] _pats;
        private int[] _pat;
        private int[] _colors;
        private int _ridx;
        private int _pidx;
        private Rule _r;


        public JPattern(Display d, Rule r, int ridx, int[][] pats, int pidx, int[] colors) {
            JPanel c = new JPanel();
            _r = r;
            _ridx = ridx;
            _pidx = pidx;
            _pats = pats;
            _pat = pats[pidx];
            _colors = colors;
            c.setBackground(java.awt.Color.BLACK);
            if(r instanceof Rule2D) {
                c.setLayout(new GridLayout(4, 3, 1, 1));
            }
            else if(r instanceof Rule1D) {
                c.setLayout(new GridLayout(2, _pat.length-1, 1, 1));
            }
            Cell target = null;
            if(r instanceof Rule2D) {
                int pi = 0;
                //System.err.println("PAT: "+_pat.length);
                for(int i=0;i<9;i++) {
                    c.add(new Cell(_pat[i]));
                }
                c.add(new Cell(0));
                target = new Cell(_pat[9]);
                c.add(target);
                c.add(new Cell(0));
                /*
                    if(i==4) {
                        target = new Cell(_pat[_pat.length-1]);
                        c.add(target);
                    }
                    else {
                        //System.err.println("pi: "+pi);
                        ++pi;
                    }
                }
                */
            }
            else if(r instanceof Rule1D) {
                for(int i=0;i<_pat.length-1;i++) {
                    c.add(new Cell(_pat[i]));
                }
                for(int i=0;i<_pat.length-1;i++) {
                    if(i==_pat.length/2-1) {
                        target = new Cell(_pat[_pat.length-1]);
                        c.add(target);
                    }
                    else {
                        c.add(new Cell(0));
                    }
                }
            }
            final Cell res = target;
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Point p = res.getPopupLocation(e);
                    final JPopupMenu pm = new JPopupMenu();
                    for(final int col:_colors) {
                        JMenuItem it = pm.add(new ChangeColorAction(col, res, pm));
                    }
                    int[] others = Color.disjoint(_rule.colors(), _colors);
                    if(others.length>0) {
                        pm.addSeparator();
                        for(final int col:others) {
                            JMenuItem it = pm.add(new ChangeColorAction(col, res, pm));
                        }
                    }
                    res.setComponentPopupMenu(pm);
                    pm.setVisible(true);
                    Point loc = getLocationOnScreen();
                    pm.setLocation(loc.x+e.getX(), loc.y+e.getY());
                }
            });
            add(c);
        }

        public int[] getPattern() {
            return _pat;
        }

        public int[] getColors() {
            return _colors;
        }

        private class ChangeColorAction extends AbstractAction {
            private int _col;
            private JPopupMenu _pm;
            private Cell _res;


            public ChangeColorAction(int color, Cell res, JPopupMenu pm) {
                super("", new ColorIcon(color));
                _col = color;
                _pm = pm;
                _res = res;
            }

            public void actionPerformed(ActionEvent e) {
                _pm.setVisible(false);
                _pat[_pat.length-1] = _col;
                _res.setColor(_col);
                boolean found = false;
                for(int c:_colors) {
                    if(c==_col) {
                        found = true;
                        break;
                    }
                }
                int[] colors = _colors;
                if(!found) {
                    colors = new int[_colors.length+1];
                    System.arraycopy(_colors, 0, colors, 0, _colors.length);
                    colors[colors.length-1] = _col;
                }
                //Rule nr = _r.origin().create(_colors, _pats, _r.background());
                Rule nr = _r.origin().create(colors, _pats, _r.background());
                if(_rule instanceof Multirule) {
                    Rule[] orig = ((Multirule)_rule).rules();
                    Rule[] ar = new Rule[orig.length];
                    for(int i=0;i<ar.length;i++) {
                        if(i==_ridx) {
                            ar[i] = nr;
                        }
                        else {
                            ar[i] = orig[i].copy();
                        }
                    }
                    _f.tick(_rule.origin().create((Object[])ar));
                }
            }
        }
    }

    static class Cell extends JPanel {
        private int _color;


        public Cell(int color) {
            setSize(new java.awt.Dimension(8, 8));
            setPreferredSize(getSize());
            setColor(color);
        }

        public Cell() {
            setSize(new java.awt.Dimension(8, 8));
            setPreferredSize(getSize());
        }

        public void setColor(int color) {
            _color = color;
            setBackground(new java.awt.Color(color));
            setForeground(new java.awt.Color(color));
        }

        public int getColor() {
            return _color;
        }
    }

    static class ColorIcon implements Icon {
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }
        private java.awt.Color _color;


        public ColorIcon(int color) {
            setColor(color);
        }

        public void setColor(int color) {
            _color = new java.awt.Color(color);
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(_color);
            g.fillRoundRect(x, y, 16, 16, 4, 4);
        }
    }
}
