package main.core;

import java.util.*;
import java.util.function.Predicate;
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
        List<Detail> details,
        List<String> aliases
) {
    public String toString() {
        String detail = details.stream().map(Detail::toString).collect(Collectors.joining("_"));
        return Stream
                .of(id, name, description, fuel, bullet, steel, bauxite, bonus, detail)
                .map(String::valueOf)
                .map(string -> string.replaceAll("\n", "\\\\n"))
                .collect(Collectors.joining("\t"));
    }

    public static Wiki parse(String string) {
        String[] array = string.split("\t", -1);
        int columnCount = 9;
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
                Detail.parseAll(array[8]),
                new ArrayList<>()
        );
    }

    public record Detail(
        int[] stageCode, // example: 1-1 = { 1, 1, 0 }, 7-2-1 = { 7, 2, 1 }
        boolean boss,
        int count,
        String victory
    ) {
        public String toString() {
            return "%d-%d-%d-%d-%d-%s".formatted(
                    stageCode[0],
                    stageCode[1],
                    stageCode[2],
                    boss ? 1 : 0,
                    count,
                    victory
            );
        }
        public Stage toStage() {
            return Stage.of(stageCode[0], stageCode[1], stageCode[2], 0, count);
        }
        public static List<Detail> parseAll(String string) {
            return Stream
                    .of(string.split("_"))
                    .filter(Predicate.not(String::isEmpty))
                    .map(Detail::parse)
                    .toList();
        }
        public static Detail parse(String string) {
            String[] array = string.split("-");
            int[] stageCode = Stream.of(array).limit(3).mapToInt(Integer::parseInt).toArray();
            return new Detail(
                    stageCode,
                    array[3].equals("1"),
                    Integer.parseInt(array[4]),
                    array[5]
            );
        }
    }
}
