package com.github.pyckle.oref.ui;

import com.github.pyckle.oref.integration.config.OrefConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.Instant;
import java.util.Properties;

public class PekudeiOrefGui {
    void createAndShowGUI() {
        PekudeiOrefView orefView = new PekudeiOrefView(new OrefConfig(new Properties()));

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
                int height = frame.getHeight();
                int width = frame.getWidth();
                orefView.triggerResize(width, height, 0);
            }
        });

        frame.setSize(1366, 768);

        new Timer(1_000, e -> {
            orefView.update(Instant.now());
        }).start();
    }
}
