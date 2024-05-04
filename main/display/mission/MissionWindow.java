package main.display.mission;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.*;

public class MissionWindow {
    private final JFrame frame;
    private Runnable whenClose = () -> {};

    public MissionWindow(BiConsumer<Graphics2D, Dimension> render) {
        frame = frame(panel(render));
    }

    private JFrame frame(JPanel panel) {
        JFrame frame = new JFrame("艦隊これくしょん　マルチ任務");
        frame.setContentPane(panel);
        frame.addWindowListener(windowListener());
        frame.pack();
        frame.setSize(new Dimension(400, 600)); // TODO: calculate size instead of this
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private JPanel panel(BiConsumer<Graphics2D, Dimension> renderer) {
        return new JPanel(null) {
            protected void paintComponent(Graphics graphics) {
                renderer.accept((Graphics2D) graphics, getSize());
            }
        };
    }

    private WindowAdapter windowListener() {
        return new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                whenClose.run();
                System.exit(0);
            }
        };
    }

    public MissionWindow key(Consumer<KeyEvent> handler) {
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                handler.accept(event);
                frame.repaint();
            }
        });
        return this;
    }

    public MissionWindow whenClose(Runnable whenClose) {
        this.whenClose = whenClose;
        return this;
    }

    public MissionWindow show() {
        frame.setVisible(true);
        return this;
    }
}
