package main.external;

import main.exception.Exceptions;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

public class Download {
    public static final String WIKI_URL = "https://wikiwiki.jp/kancolle/%E4%BB%BB%E5%8B%99";

    public static String wiki() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(WIKI_URL)).build();
        return Exceptions.wrap(() -> HttpClient
                .newHttpClient()
                .send(request, BodyHandlers.ofString()))
                .body();
    }
}
