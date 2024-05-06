package main.display;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Translation {
    public static void execute(Graphics2D graphics, int x, int y, Runnable runnable) {
        AffineTransform transform = graphics.getTransform();
        Shape shape = graphics.getClip();
        graphics.translate(x, y);
        Font font = graphics.getFont();
        runnable.run();
        graphics.setTransform(transform);
        graphics.setClip(shape);
        graphics.setFont(font);
    }

    public static void execute(Graphics2D graphics, Runnable runnable) {
        execute(graphics, 0, 0, runnable);
    }
}
