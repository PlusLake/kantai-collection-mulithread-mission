package main.core;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Mission {
    private final List<Stage> stages = new ArrayList<>();
    private String id;
    private String name;

    // version 1
    private Mission(String name) {
        this("", name);
    }

    // version 2
    private Mission(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        String stagesString = stages.stream().map(Stage::toString).collect(Collectors.joining("_"));
        return "%s\t%s\t%s".formatted(id, name, stagesString);
    }

    public void computeAllStages(Predicate<Stage> filter, Consumer<Stage> callback) {
        stages
                .stream()
                .filter(filter)
                .forEach(callback);
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void replaceStages(List<Stage> stages) {
        this.stages.clear();
        this.stages.addAll(stages);
        if (this.stages.isEmpty()) this.stages.add(Stage.of(1, 1));
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void removeStage(int index) {
        if (stages.size() > 1) stages.remove(index);
    }

    public static Mission parse(String line) {
        String[] splitted = line.split("\t");
        Mission mission;
        String stages;
        if (splitted.length == 2) {
            // version 1
            mission = new Mission(splitted[0]);
            stages = splitted[1];
        }
        else if (splitted.length == 3) {
            // version 2
            mission = new Mission(splitted[0], splitted[1]);
            stages = splitted[2];
        }
        else {
            throw new IllegalArgumentException("invalid line: %s".formatted(line));
        }
        Stream
                .of(stages.split("_"))
                .map(stage -> Stream.of(stage.split("-")).map(Integer::parseInt).toArray(Integer[]::new))
                .map(array -> Stage.of(array[0], array[1], array[2], array[3], array[4]))
                .forEach(mission.stages::add);
        return mission;
    }

    public static Mission defaultMission() {
        return Mission.parse("Press enter to select\t1-1-0-0-1");
    }

    public boolean isDefaultMission() {
        return id.equals(defaultMission().id)
                && stages.size() == 1
                && stages.get(0).name().equals("1-1");
    }
}
