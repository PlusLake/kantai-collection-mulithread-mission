package main.core;

import java.util.*;
import java.util.regex.*;

public class WikiStage {

    private static final Pattern STAGE_PATTERN = Pattern.compile("\\((?<Region>\\d)-(?<Area>\\d)(?:-(?<Stage>\\d))?/?(?<Requirement>[^)]+?)?\\)");
    private static final Pattern COUNT_PATTERN = Pattern.compile("(?<Count>//d+)回");
    private static final Pattern VICTORY_PATTERN = Pattern.compile("回(?<Victory>\\w)?勝利");

    private int region;
    private int area;
    private int stage;
    private boolean boss;
    private int count;
    private String victory;

    public int[] toOldWikiStage() {
        return new int[] {this.region, this.area, this.stage};
    }

    public static List<WikiStage> parse(String description) {
        boolean boss = description.contains("ボス戦");
        Matcher countMatcher = WikiStage.COUNT_PATTERN.matcher(description);
        int count = countMatcher.find() ? Integer.parseInt(countMatcher.group("Count")) : 1;
        Matcher victoryMatcher = WikiStage.VICTORY_PATTERN.matcher(description);
        String victory = victoryMatcher.find() && Objects.nonNull(victoryMatcher.group("Victory")) ? victoryMatcher.group("Victory") : "B";
        List<WikiStage> stages = new ArrayList<>();
        Matcher matcher = WikiStage.STAGE_PATTERN.matcher(description);
        while (matcher.find()) {
            WikiStage stage = new WikiStage();
            stage.region = Integer.parseInt(matcher.group("Region"));
            stage.area = Integer.parseInt(matcher.group("Area"));
            stage.stage = Objects.nonNull(matcher.group("Stage")) ? Integer.parseInt(matcher.group("Stage")) : 0;
            stage.boss = boss;
            stage.count = count;
            stage.victory = victory;
            stages.add(stage);
        }
        return stages;
    }

    public int getRegion() { return this.region; }
    public int getArea() { return this.area; }
    public int getStage() { return this.stage; }
    public boolean getBoss() { return this.boss; }
    public int getCount() { return this.count; }
    public String getVictory() { return this.victory; }
}
