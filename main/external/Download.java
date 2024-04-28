package main.external;

import main.exception.Exceptions;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.*;

public class Download {
    private static final String WIKI_DOWNLOAD_PATH = Optional
            .of("PLUSLAKE_KANTAI_COLLECTION_MISSION_DOWNLOAD_URL")
            .map(System::getenv)
            .orElse("https://raw.githubusercontent.com/PlusLake/kantai-collection-mulithread-mission/master/mission.tsv");

    public static void wiki(String localPath) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(WIKI_DOWNLOAD_PATH)).build();
        Exceptions.wrap(() -> HttpClient
                .newHttpClient()
                .send(request, BodyHandlers.ofFile(Path.of(WIKI_DOWNLOAD_PATH), CREATE, WRITE)))
                .body();
    }
}
