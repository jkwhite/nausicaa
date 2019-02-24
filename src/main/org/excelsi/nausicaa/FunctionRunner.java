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
    public FunctionRunner(final JFrame root, final CA ca, final MutationFactor mf, final ExecutorService pool, final GOptions opt, final Rendering rend, final Varmap vars, final Functions.CAFunction fn) {
        super(root, "Executing "+fn.getName());

        final JProgressBar prog = new JProgressBar(1, 100);
        prog.setValue(0);
        final JLabel task = new JLabel("Running...");

        final Functions.Progress progress = new Functions.Progress() {
            @Override public void setMaximum(int max) { SwingUtilities.invokeLater(()->{prog.setMaximum(max);}); }
            @Override public void setCurrent(int cur) { SwingUtilities.invokeLater(()->{prog.setValue(cur);}); }
            @Override public void setStatus(String status) { SwingUtilities.invokeLater(()->{task.setText(status);}); }
        };
        final Functions.API api = 
            new Functions.API() {
                @Override public MutationFactor getMutationFactor() { return mf; }
                @Override public ExecutorService getPool() { return pool; }
                @Override public GOptions getOptions() { return opt; }
                @Override public Rendering getRendering() { return rend; }
                @Override public boolean getCancelled() { return Thread.currentThread().isInterrupted(); }
                @Override public Functions.Progress getProgress() { return progress; }
            };

        JPanel main = new JPanel(new BorderLayout());
        Font font = task.getFont();
        task.setFont(font.deriveFont(font.getSize()-2f));

        //prog.setIndeterminate(true);
        main.add(prog, BorderLayout.NORTH);
        main.add(task, BorderLayout.WEST);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(main, BorderLayout.CENTER);
        final JDialog d = this;

        final Thread runner = new Thread() {
            @Override public void run() {
                try {
                    fn.run(ca, vars, api);
                    d.dispose();
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
                int tries = 0;
                while(++tries<10 && !runner.isInterrupted()) {
                    runner.interrupt();
                    try {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException ex) {
                    }
                }
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
