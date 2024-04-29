package main.core;

import java.util.*;
import java.util.stream.*;

public record Wiki(
        String id,
        String name,
        String description,
        int fuel,
        int bullet,
        int steel,
        int bauxite,
        String bonus,
        List<int[]> oldStages,
        List<WikiStage> stages,
        List<String> aliases
) {
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
        return new Wiki(
                array[0],
                array[1],
                array[2].replaceAll("\\\\n", "\n"),
                Integer.parseInt(array[3]),
                Integer.parseInt(array[4]),
                Integer.parseInt(array[5]),
                Integer.parseInt(array[6]),
                array[7].replaceAll("\\\\n", "\n"),
                WikiStage
                        .parse(array[2].replaceAll("\\\\n", "\n"))
                        .stream()
                        .map(WikiStage::toOldWikiStage)
                        .toList(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static Wiki parseFromWikiTableCells(List<String> cells) {
        Deque<String> candidates = new LinkedList<>(List.of(cells.get(0).split(System.lineSeparator(), -1)));
        String id = candidates.poll();
        List<String> aliases = new ArrayList<>();
        List<WikiStage> stages = WikiStage.parse(cells.get(2));
        while (!candidates.isEmpty()) {
            String candidate = candidates.poll();
            if (candidate.matches("\\(.*\\)")) {
                aliases.add(candidate.substring(0,candidate.length() - 1));
            }
            else {
                id = id + "-" + candidate;
            }
        }
        return new Wiki(
                id,
                cells.get(1),
                cells.get(2),
                Integer.parseInt(cells.get(3)),
                Integer.parseInt(cells.get(4)),
                Integer.parseInt(cells.get(5)),
                Integer.parseInt(cells.get(6)),
                cells.get(7),
                stages.stream().map(WikiStage::toOldWikiStage).toList(),
                stages,
                aliases
        );
    }
}
