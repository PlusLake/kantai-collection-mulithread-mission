package main;

import java.util.ArrayList;
import java.util.List;

import main.core.Mission;
import main.core.Wiki;
import main.display.mission.MissionUI;
import main.display.mission.MissionWindow;
import main.external.Persistent;

public class Main {
    public static void main(String[] args) {
        List<Mission> missions = new ArrayList<>(Persistent.loadMissions());
        List<Wiki> wikis = Persistent.loadWikis();
        // handle mission format 1 to 2 migration
        for (Mission mission : missions) {
            if (mission.getId().isBlank()) {
                mission.setId(
                    wikis.stream()
                        .filter(wiki -> wiki.name().equals(mission.getName()))
                        .findFirst()
                        .map(Wiki::id)
                        .orElse("")
                );
            }
        }
        MissionUI missionUI = new MissionUI(List.copyOf(missions), wikis);
        new MissionWindow(missionUI::render)
                .key(missionUI::key)
                .whenClose(() -> Persistent.saveMissions(missionUI.getMissions()))
                .show();
    }
}
