package main.core;

import java.util.*;
import java.util.stream.*;

public class Wiki {
    private String id;
    private String name;
    private String description;
    private int fuel;
    private int bullet;
    private int steel;
    private int bauxite;
    private String bonus;
    private List<int[]> oldStages = new ArrayList<>();
    private List<WikiStage> stages = new ArrayList<>();
    private List<String> aliases = new ArrayList<>();

    public String toString() {
        return Stream
                .of(id, name, description, fuel, bullet, steel, bauxite, bonus)
                .map(String::valueOf)
                .map(string -> string.replaceAll("\n", "\\\\n"))
                .collect(Collectors.joining("\t"));
    }

    public static Wiki parse(String string) {
        String[] array = string.split("\t");
        int columnCount = 8;
        if (array.length != columnCount) {
            String message = "Invalid tsv file. Column count not %d (%d) (%s)";
            throw new IllegalArgumentException(message.formatted(columnCount, array.length, string));
        }
        Wiki wiki = new Wiki();
        wiki.id = array[0];
        wiki.name = array[1];
        wiki.description = array[2].replaceAll("\\\\n", "\n");
        wiki.fuel = Integer.parseInt(array[3]);
        wiki.bullet = Integer.parseInt(array[4]);
        wiki.steel = Integer.parseInt(array[5]);
        wiki.bauxite = Integer.parseInt(array[6]);
        wiki.bonus = array[7].replaceAll("\\\\n", "\n");;
        wiki.oldStages = WikiStage
                .parse(wiki.description)
                .stream()
                .map(WikiStage::toOldWikiStage)
                .toList();
        return wiki;
    }

    public static Wiki parseFromWikiTableCells(List<String> cells) {
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
