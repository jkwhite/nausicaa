package org.excelsi.ca;


import java.awt.*;
import javax.swing.*;


public class Things {
    public static void centerWindow( Window toCenter ) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = toCenter.getBounds();
        toCenter.setLocation( (dim.width-frame.width)/2,
            (dim.height-frame.height)/2 );
    }
}
