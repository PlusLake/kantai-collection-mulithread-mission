package main.display;

import main.exception.Exceptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public enum Images {
    RESOURCE("icon.png");

    public final BufferedImage image;

    Images(String filename) {
        String path = "/resource/" + filename;
        image = Exceptions.wrap(() -> ImageIO.read(Images.class.getResourceAsStream(path)));
    }
}
