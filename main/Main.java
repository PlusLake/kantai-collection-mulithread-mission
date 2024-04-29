package main;

import main.display.*;
import main.external.Persistent;

public class Main {
    public static void main(String[] args) {
        Composer composer = new Composer(
                Persistent.loadMissions(),
                Persistent.loadWikis()
        );
        new Window(composer::render)
                .key(composer::key)
                .whenClose(() -> Persistent.saveMissions(composer.getMissions()))
                .show();
    }
}
