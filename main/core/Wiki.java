package main.core;

import java.util.*;
import java.util.regex.*;

public class Wiki {

    private String id;
    private List<String> aliases = new ArrayList<>();
    private String name;
    private String description;
    private int fuel;
    private int bullet;
    private int steel;
    private int bauxite;
    private String bonus;
    private List<int[]> oldStages = new ArrayList<>();
    private List<WikiStage> stages = new ArrayList<>();

    public static Wiki parse(String string) {
        String[] array = string.split("\t");
        if (array.length != 3) {
            String message = "Invalid tsv file. Column count not 3 (%d)".formatted(array.length);
            throw new IllegalArgumentException(message);
        }
        Wiki mission = new Wiki();
        mission.id = array[0];
        mission.name = array[1];
        mission.description = array[2].replaceAll("\\\\n", "\n");
        mission.oldStages = findStagesFromText(array[2]);
        return mission;
    }

    public static Wiki parseAdvanced(List<String> cells) {
        Wiki wiki = new Wiki();
        Deque<String> candidates = new LinkedList<>(List.of(cells.get(0).split(System.lineSeparator(), -1)));
        wiki.id = candidates.poll();
        while (!candidates.isEmpty()) {
            String candidate = candidates.poll();
            if (candidate.matches("\\(.*\\)")) {
                wiki.aliases.add(candidate.substring(0,candidate.length() - 1));
            }
            else {
                wiki.id = wiki.id + "-" + candidate;
            }
        }
        wiki.name = cells.get(1);
        wiki.description = cells.get(2);
        wiki.fuel = Integer.parseInt(cells.get(3));
        wiki.bullet = Integer.parseInt(cells.get(4));
        wiki.steel = Integer.parseInt(cells.get(5));
        wiki.bauxite = Integer.parseInt(cells.get(6));
        wiki.bonus = cells.get(7);
        wiki.stages.addAll(WikiStage.parse(wiki.description));
        wiki.oldStages.addAll(wiki.stages.stream().map(WikiStage::toOldWikiStage).toList());
        return wiki;
    }

    private static List<int[]> findStagesFromText(String string) {
        Matcher matcher = Pattern.compile("(\\([1-9]-[1-9][^)]*\\))").matcher(string);
        List<int[]> result = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                result.add(new int[] {
                        matcher.group(i).charAt(1) - '0',
                        matcher.group(i).charAt(3) - '0'
                });
            }
        }
        return result;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getFuel() { return this.fuel; }
    public int getBullet() { return this.bullet; }
    public int getSteel() { return this.steel; }
    public int getBauxite() { return this.bauxite; }
    public String getBonus() { return this.bonus; }
    public List<WikiStage> getStages() { return this.stages; }
    public List<int[]> getOldStages() { return this.oldStages; }
}
