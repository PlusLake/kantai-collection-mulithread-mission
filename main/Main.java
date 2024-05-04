package main;

import main.display.mission.*;
import main.external.Persistent;

public class Main {
    public static void main(String[] args) {
        MissionUI missionUI = new MissionUI(
                Persistent.loadMissions(),
                Persistent.loadWikis()
        );
        new MissionWindow(missionUI::render)
                .key(missionUI::key)
                .whenClose(() -> Persistent.saveMissions(missionUI.getMissions()))
                .show();
    }
}
