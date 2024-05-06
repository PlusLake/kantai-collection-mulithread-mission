package main.display.mission;

import main.logging.Log;

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
        frame.setLocationRelativeTo(null);
        frame.addComponentListener(new FrameSizeTuner());
        return frame;
    }

    private JPanel panel(BiConsumer<Graphics2D, Dimension> renderer) {
        JPanel panel = new JPanel(null) {
            protected void paintComponent(Graphics graphics) {
                Log.timer("MissionUI rendering", () -> renderer.accept((Graphics2D) graphics, getSize()));
            }
        };
        panel.setPreferredSize(new Dimension(MissionUI.TOTAL_WIDTH + MissionUI.PANEL_PADDING * 2, 500));
        return panel;
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

    private static class FrameSizeTuner extends ComponentAdapter {
        public void componentResized(ComponentEvent event) {
            // At most of the time, simply using setMinimumSize(new Dimension(x, y)) is just "OK"
            // However the result size depends on different OSes. For example,
            // Windows 7/Vista's window has thickness so that the panel can be smaller than we expected.
            // So we just wait JFrame#pack takes effect, and fire this callback to set the correct min size,
            // and finally remove the callback from the JFrame.
            JFrame frame = (JFrame) event.getSource();
            frame.setMinimumSize(frame.getSize());
            frame.removeComponentListener(this);
        }
    }
}
