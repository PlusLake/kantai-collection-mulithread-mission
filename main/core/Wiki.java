package main.core;

import java.util.*;
import java.util.regex.*;

public class Wiki {
    private String code;
    private String name;
    private String content;
    private List<int[]> stages = new ArrayList<>();

    public static Wiki parse(String string) {
        String[] array = string.split("\t");
        if (array.length != 3) {
            String message = "Invalid tsv file. Column count not 3 (%d)".formatted(array.length);
            throw new IllegalArgumentException(message);
        }
        Wiki mission = new Wiki();
        mission.code = array[0];
        mission.name = array[1];
        mission.content = array[2].replaceAll("\\\\n", "\n");
        mission.stages = findStagesFromText(array[2]);
        return mission;
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

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getContent() { return content; }
    public List<int[]> getStages() { return stages; }
}
