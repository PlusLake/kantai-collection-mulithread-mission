package main.core;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Mission {
    private final List<Stage> stages = new ArrayList<>();
    private String name;

    private Mission(String name) {
        this.name = name;
    }

    public String toString() {
        String stagesString = stages.stream().map(Stage::toString).collect(Collectors.joining("_"));
        return "%s\t%s".formatted(name, stagesString);
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
        Mission mission = new Mission(splitted[0]);
        Stream
                .of(splitted[1].split("_"))
                .map(stage -> Stream.of(stage.split("-")).map(Integer::parseInt).toArray(Integer[]::new))
                .map(array -> Stage.of(array[0], array[1], array[2], array[3], array[4]))
                .forEach(mission.stages::add);
        return mission;
    }

    public static Mission defaultMission() {
        return Mission.parse("Press enter to select\t1-1-0-0-1");
    }

    public boolean isDefaultMission() {
        return name.equals(defaultMission().name)
                && stages.size() == 1
                && stages.get(0).name().equals("1-1");
    }
}
