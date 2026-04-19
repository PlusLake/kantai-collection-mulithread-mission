package main.external;

import java.util.*;
import java.util.regex.*;

import main.core.*;
import main.core.Wiki.Detail;

public class WikiParser {
    private static final Pattern TR_PATTERN = Pattern.compile("<tr>(.*?)</tr>");
    private static final Pattern TD_PATTERN = Pattern.compile("<td[^>]*?>(.*?)</td>");

    private static final Pattern STAGE_PATTERN = Pattern.compile("\\((?<Region>\\d)-(?<Area>\\d)(?:-(?<Stage>\\d))?/?(?<Requirement>[^)]+?)?\\)");
    private static final Pattern COUNT_PATTERN = Pattern.compile("(?<Count>\\d+)回");
    private static final Pattern VICTORY_PATTERN = Pattern.compile("回(?<Victory>\\w)?勝利");

    public static List<Wiki> parsePage(String page) {
        List<Wiki> wikis = new ArrayList<>();
        Matcher trs = WikiParser.TR_PATTERN.matcher(page);
        while (trs.find()) {
            List<String> cells = new ArrayList<>(10);
            Matcher tds = WikiParser.TD_PATTERN.matcher(trs.group(1));
            while (tds.find()) cells.add(tds.group(1));
            if (cells.size() < 9 || !cells.get(0).contains("B")) continue;
            wikis.add(parseCellToWiki(cells.stream().map(WikiParser::detag).toList()));
        }
        return wikis;
    }

    public static Wiki parseCellToWiki(List<String> cells) {
        Deque<String> candidates = new LinkedList<>(List.of(cells.get(0).split(System.lineSeparator(), -1)));
        String id = candidates.poll();
        List<String> aliases = new ArrayList<>();
        List<Detail> details = parseDescriptionToDetails(cells.get(2));
        while (!candidates.isEmpty()) {
            String candidate = candidates.poll();
            if (candidate.matches("\\(.*\\)")) {
                aliases.add(candidate.substring(0, candidate.length() - 1));
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
                details,
                aliases
        );
    }

    public static List<Detail> parseDescriptionToDetails (String description) {
        boolean boss = description.contains("ボス戦");
        Matcher countMatcher = COUNT_PATTERN.matcher(description);
        int count = countMatcher.find() ? Integer.parseInt(countMatcher.group("Count")) : 1;
        Matcher victoryMatcher = VICTORY_PATTERN.matcher(description);
        String victory = victoryMatcher.find() && Objects.nonNull(victoryMatcher.group("Victory"))
                ? victoryMatcher.group("Victory")
                : "B";
        List<Detail> details = new ArrayList<>();
        Matcher matcher = STAGE_PATTERN.matcher(description);
        while (matcher.find()) {
            details.add(new Detail(
               new int[] {
                       Integer.parseInt(matcher.group("Region")),
                       Integer.parseInt(matcher.group("Area")),
                       Objects.nonNull(matcher.group("Stage")) ? Integer.parseInt(matcher.group("Stage")) : 0
               },
               boss,
               count,
               victory
            ));
        }
        return details;
    }

    private static String detag(String string) {
        return string.replaceAll("<br[^>]*?>", System.lineSeparator()).replaceAll("<[^>]*?>", "");
    }
}
