package main.display;

import main.exception.Exceptions;

import java.awt.*;

public enum Fonts {
    JAPANESE("font_noto_sans_jp_regular.ttf"),
    MONO("font_reddit_mono.ttf");

    public final Font font;

    Fonts(String filename) {
        String path = "/resource/" + filename;
        font = Exceptions
                .wrap(() -> Font.createFont(Font.TRUETYPE_FONT, Fonts.class.getResourceAsStream(path)))
                .deriveFont(12f);
    }
}
