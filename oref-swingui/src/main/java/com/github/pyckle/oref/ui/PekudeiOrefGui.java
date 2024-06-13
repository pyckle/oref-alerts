package com.github.pyckle.oref.ui;

import com.github.pyckle.oref.integration.config.OrefConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

public class PekudeiOrefGui {
    private final Properties properties;

    public PekudeiOrefGui(Properties properties) {
        this.properties = properties;
    }

    void createAndShowGUI() {
        PekudeiOrefView orefView = new PekudeiOrefView(new OrefConfig(properties));

        JFrame frame = new JFrame("Oref Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(orefView.getPanel());
        frame.getContentPane().setBackground(Color.BLACK);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // note - use the inner panel for these sizes because outer frame includes the os frame.
                orefView.triggerResize(orefView.getPanel().getWidth(), orefView.getPanel().getHeight());
            }
        });

        frame.setSize(1366, 768);

        new Timer(100, e -> {
            orefView.update();
        }).start();
    }
}
