package main.display.mission;

import main.logging.Log;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public record StageDescription(String name, String operation) {
    private static final Map<String,StageDescription> map = Log.timer("load stage", StageDescription::load);
    private static Map<String, StageDescription> load() {
        InputStream stream = StageDescription.class.getResourceAsStream("/resource/stage.tsv");
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .map(line -> line.split("\t"))
                .collect(Collectors.toMap(array -> array[0], array -> new StageDescription(array[1], array[2])));
    }
    public static Optional<StageDescription> get(String key) {
        return Optional.ofNullable(map.get(key));
    }
}
