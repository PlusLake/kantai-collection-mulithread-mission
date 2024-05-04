package main.display;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Translation {
    public static void execute(Graphics2D graphics, int x, int y, Runnable runnable) {
        AffineTransform transform = graphics.getTransform();
        Shape shape = graphics.getClip();
        graphics.translate(x, y);
        runnable.run();
        graphics.setTransform(transform);
        graphics.setClip(shape);
    }

    public static void execute(Graphics2D graphics, Runnable runnable) {
        execute(graphics, 0, 0, runnable);
    }
}
