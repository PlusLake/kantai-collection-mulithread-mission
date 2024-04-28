package main;

import main.display.*;
import main.external.Persistent;
import main.external.WikiParser;

public class Main {
    public static void main(String[] args) {
        Composer composer = new Composer(
                Persistent.loadMissions(),
                WikiParser.fetchWiki()
        );
        new Window(composer::render)
                .key(composer::key)
                .whenClose(() -> Persistent.saveMissions(composer.getMissions()))
                .show();
    }
}
