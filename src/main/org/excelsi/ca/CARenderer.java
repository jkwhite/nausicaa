package org.excelsi.ca;


import javax.swing.*;
import org.excelsi.solace.Renderer;


public class CARenderer extends Renderer {
    public JComponent render(Object o) {
        return new JLabel(new ImageIcon(((CA)o).toScaledImage()));
    }
}
