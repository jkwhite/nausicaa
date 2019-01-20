package org.excelsi.nausicaa;


import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.excelsi.nausicaa.ca.*;


public class FunctionRunner extends JDialog {
    public FunctionRunner(final JFrame root, final CA ca, final Functions.API api, final Varmap vars, final Functions.CAFunction fn) {
        super(root, "Executing "+fn.getName());

        JPanel main = new JPanel(new BorderLayout());
        final JLabel task = new JLabel("Running...");
        Font font = task.getFont();
        task.setFont(font.deriveFont(font.getSize()-2f));

        final JProgressBar prog = new JProgressBar(1, 100);
        prog.setValue(0);
        prog.setIndeterminate(true);
        main.add(prog, BorderLayout.NORTH);
        main.add(task, BorderLayout.WEST);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(main, BorderLayout.CENTER);

        final Thread runner = new Thread() {
            @Override public void run() {
                try {
                    fn.run(ca, vars, api);
                    dispose();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };

        final JButton[] hack = new JButton[1];
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                task.setText("Canceling");
                cancel.setEnabled(false);
                runner.interrupt();
            }
        });
        hack[0] = cancel;
        JPanel south = new JPanel(new BorderLayout());
        south.add(cancel, BorderLayout.EAST);
        main.add(south, BorderLayout.SOUTH);
        Dimension di = main.getPreferredSize();
        setSize(100+di.width, 50+di.height);
        Things.centerWindow(this);
        setVisible(true);

        runner.start();
    }
}
