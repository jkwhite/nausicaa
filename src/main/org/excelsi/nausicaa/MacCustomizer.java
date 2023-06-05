package org.excelsi.nausicaa;


import java.awt.Desktop;
import javax.swing.*;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;
import com.apple.mrj.MRJApplicationUtils;


public class MacCustomizer {
    public static void run(JMenuBar bar) {
        MacOSXController macController = new MacOSXController();
        boolean isMacOS = System.getProperty("os.name").contains("Mac OS");
        System.err.println("ismac "+isMacOS);
        if (isMacOS) {
            // MRJApplicationUtils.registerAboutHandler(macController);
            //MRJApplicationUtils.registerPrefsHandler(macController);
            // MRJApplicationUtils.registerQuitHandler(macController);

            Desktop desktop = Desktop.getDesktop();

            desktop.setAboutHandler(e -> macController.handleAbout());
            desktop.setPreferencesHandler(e -> macController.handlePrefs());
            desktop.setQuitHandler((e,r) -> macController.handleQuit());
        }
        else {
            throw new RuntimeException("not mac");
        }
    }

    public static class MacOSXController implements MRJAboutHandler, MRJQuitHandler, MRJPrefsHandler {
        public void handleAbout() {
            JOptionPane.showMessageDialog(null, 
                "<html>Copyright (C) 2007-2023 John K White, dhcmrlchtdj@gmail.com<br/>Licensed under the terms of the GNU General Public License Version 3</html>", 
                "NausiCAä 1.0", 
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(MacCustomizer.class.getResource("/na1_164.png")));
        }

        public void handlePrefs() throws IllegalStateException {
            JOptionPane.showMessageDialog(null, 
                "There are no preferences, only Zuul.", 
                "Preferences", 
                JOptionPane.INFORMATION_MESSAGE);
        }

        public void handleQuit() throws IllegalStateException {
            NViewer.instance().saveUIState();
            System.exit(0);
        }
    }
}
