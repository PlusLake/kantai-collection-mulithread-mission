package main.display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import main.exception.Exceptions;

public enum Images {
    RESOURCE("icon.png");

    public final BufferedImage image;

    Images(String filename) {
        String path = "/assets/" + filename;
        image = Exceptions.wrap(() -> ImageIO.read(Images.class.getResourceAsStream(path)));
    }
}
