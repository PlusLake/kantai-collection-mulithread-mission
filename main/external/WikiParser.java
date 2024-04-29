package main.external;

import java.util.*;
import java.util.regex.*;

import main.core.Wiki;

public class WikiParser {
    private static final Pattern TR_PATTERN = Pattern.compile("<tr>(.*?)</tr>");
    private static final Pattern TD_PATTERN = Pattern.compile("<td[^>]*?>(.*?)</td>");

    public static List<Wiki> parsePage(String page) {
        List<Wiki> wikis = new ArrayList<>();
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
            wikis.add(Wiki.parseFromWikiTableCells(cells.stream().map(WikiParser::detag).toList()));
        }
        return wikis;
    }

    private static String detag(String string) {
        return string.replaceAll("<br[^>]*?>", System.lineSeparator()).replaceAll("<[^>]*?>", "");
    }
}
