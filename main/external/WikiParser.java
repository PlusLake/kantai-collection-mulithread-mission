package main.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.core.Wiki;
import main.core.Stage;
import main.exception.Exceptions;

public class WikiParser {

    public static final String WIKI_URL = "https://wikiwiki.jp/kancolle/%E4%BB%BB%E5%8B%99";
    private static final Pattern TR_PATTERN = Pattern.compile("<tr>(.*?)</tr>");
    private static final Pattern TD_PATTERN = Pattern.compile("<td[^>]*?>(.*?)</td>");

    public static List<Wiki> fetchWiki() {
        List<Wiki> wikis = new ArrayList<>();
        String page = WikiParser.getPage();
        Matcher trs = WikiParser.TR_PATTERN.matcher(page);
        while (trs.find()) {
            List<String> cells = new ArrayList<>(10);
            Matcher tds = WikiParser.TD_PATTERN.matcher(trs.group(1));
            while (tds.find()) {
                cells.add(tds.group(1));
            }
            if (cells.size() < 9 || !cells.get(0).contains("B")) {
                continue;
            }
            wikis.add(Wiki.parseAdvanced(cells.stream().map(WikiParser::detag).toList()));
        }
        return wikis;
    }

    private static String getPage() {
        return Exceptions.wrap(() -> HttpClient
                .newHttpClient()
                .send(HttpRequest.newBuilder(URI.create(WikiParser.WIKI_URL)).build(), BodyHandlers.ofString()))
                .body();
    }

    private static String detag(String string) {
        return string.replaceAll("<br[^>]*?>", System.lineSeparator()).replaceAll("<[^>]*?>", "");
    }

}
