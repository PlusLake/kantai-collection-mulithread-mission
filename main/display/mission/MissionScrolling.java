package main.display.mission;

import main.core.Mission;

import java.util.List;

public class MissionScrolling {
    protected static int mainTranslation(int panelHeight, List<Mission> missions, int cursor) {
        int result = panelHeight
                - MissionUI.FOOTER_HEIGHT
                - currentMissionY(missions, cursor)
                - missionY(missions, cursor, 1)
                + MissionUI.STAGE_MARGIN
                - MissionUI.PANEL_PADDING * 2;
        return Math.min(0, result);
    }

    private static int currentMissionY(List<Mission> missions, int cursor) {
        return missionY(missions, 0, cursor);
    }

    private static int missionY(List<Mission> missions, int from, int to) {
        int stageCount = missions
                .stream()
                .skip(from)
                .limit(to)
                .map(Mission::getStages)
                .map(List::size)
                .mapToInt(Integer::intValue)
                .sum();
        return stageCount * (MissionUI.STAGE_HEIGHT + MissionUI.STAGE_MARGIN);
    }

    protected static int footerTranslation(int cursor) {
        int result = MissionUI.FOOTER_HEIGHT
                - MissionUI.PANEL_PADDING * 2
                + MissionUI.STAGE_MARGIN
                - ++cursor * (MissionUI.STAGE_HEIGHT + MissionUI.STAGE_MARGIN);
        return Math.min(0, result);
    }
}
