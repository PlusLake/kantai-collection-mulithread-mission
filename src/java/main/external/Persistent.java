package main.external;

import main.core.*;
import main.exception.Exceptions;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class Persistent {
    private static final String WORK_DIRECTORY = Optional
            .of("PLUSLAKE_KANTAI_COLLECTION_WORKDIR")
            .map(System::getenv)
            .orElse(System.getProperty("user.home") + "/.pluslake/kankore/multithread/");
    private static final String WIKI_LOCAL_PATH = WORK_DIRECTORY + "wiki.tsv";
    public static final String SAVE_PATH = WORK_DIRECTORY + "save.tsv";

    static {
        Exceptions.wrap(() -> Files.createDirectories(Paths.get(WORK_DIRECTORY)));
    }

    public static void saveMissions(List<Mission> missions) {
        Stream<String> stream = missions.stream().map(Mission::toString);
        Iterable<String> iterable = stream::iterator;
        Exceptions.wrap(() -> Files.write(Path.of(SAVE_PATH), iterable));
    }

    public static List<Mission> loadMissions() {
        Exceptions.wrap(() -> new File(SAVE_PATH).createNewFile());
        return Exceptions
                .wrap(() -> Files.lines(Path.of(SAVE_PATH)))
                .map(Mission::parse)
                .toList();
    }

    public static List<Wiki> loadWikis() {
        if (!Files.exists(Path.of(WIKI_LOCAL_PATH))) saveWikis();
        return Exceptions.wrap(() -> Files
                .lines(Path.of(WIKI_LOCAL_PATH))
                .map(Wiki::parse)
                .toList());
    }

    private static void saveWikis() {
        Stream<String> stream = WikiParser
                .parsePage(Download.wiki())
                .stream()
                .map(Wiki::toString);
        Iterable<String> iterable = stream::iterator;
        Exceptions.wrap(() -> Files.write(Path.of(WIKI_LOCAL_PATH), iterable));
    }
}
