package main.display.mission;

import main.core.*;
import main.core.Wiki.Detail;
import main.display.*;
import main.display.wiki.WikiSelectionUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

import static java.awt.event.KeyEvent.*;

public class MissionUI {
    private static final Color BACKGROUND_COLOR = new Color(255, 192, 192);
    private static final Color CARD_BACKGROUND_COLOR = new Color(255, 240, 240);
    private static final Color FONT_COLOR = new Color(32, 32, 32);
    private static final Color CURSOR_COLOR = Color.RED;
    private static final Color FOOTER_BACKGROUND_COLOR = new Color(255, 160, 160);
    private static final Color TIP_BACKGROUND_COLOR = new Color(192, 96, 96);
    private static final Color TIP_FONT_COLOR = new Color(255, 224, 224);

    protected static final int PANEL_PADDING = 8;
    private static final int MISSION_WIDTH = 150;
    private static final int MISSION_MARGIN = 4;
    protected static final int STAGE_HEIGHT = 20;
    protected static final int STAGE_MARGIN = MISSION_MARGIN;
    private static final int STAGE_WIDTH = 50;
    private static final int FOOTER_COUNT = 4;
    protected static final int FOOTER_HEIGHT = (STAGE_HEIGHT + STAGE_MARGIN) * FOOTER_COUNT + PANEL_PADDING * 2 - STAGE_MARGIN;
    protected static final int TOTAL_WIDTH = MISSION_WIDTH + (STAGE_WIDTH + STAGE_MARGIN) * 2;
    protected static final int TIP_HEIGHT = 50;

    private final List<Mission> missions = new ArrayList<>();
    private final List<Wiki> wikis = new ArrayList<>();
    private final int[] cursor = {0, 0, 0, 0};
    private final int[] update = {-1, -1};
    private Mode currentMode = Mode.MAIN;
    private Rectangle editingArea = null;

    public MissionUI(List<Mission> missions, List<Wiki> wikis) {
        this.missions.addAll(missions);
        this.wikis.addAll(wikis);
        if (this.missions.isEmpty()) this.missions.add(Mission.defaultMission());
    }

    public List<Mission> getMissions() {
        return missions;
    }

    public void render(Graphics2D graphics, Dimension size) {
        clear(graphics, size);
        graphics.setFont(Fonts.JAPANESE.font);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Translation.execute(graphics, PANEL_PADDING, PANEL_PADDING, () -> renderMissions(graphics, size.height));
        Translation.execute(graphics, 0, size.height - FOOTER_HEIGHT - TIP_HEIGHT, () -> renderFooter(graphics, size));
        Translation.execute(graphics, 0, size.height - TIP_HEIGHT, () -> renderTip(graphics, size.width));
        if (currentMode == Mode.STAGE_EDIT) {
            graphics.setColor(new Color(0, 0, 0, 128));
            Area area = new Area(new Rectangle(0, 0, size.width, size.height - TIP_HEIGHT));
            area.subtract(new Area(editingArea));
            graphics.fill(area);
        }
    }

    private void clear(Graphics2D graphics, Dimension size) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, size.width, size.height);
    }

    private void renderMissions(Graphics2D graphics, int panelHeight) {
        graphics.translate(0, MissionScrolling.mainTranslation(panelHeight, missions, cursor[0]));
        missions.forEach(mission -> {
            int stages = mission.getStages().size();
            int missionHeight = stages * STAGE_HEIGHT + (stages - 1) * STAGE_MARGIN;
            boolean isCurrentMission = missions.indexOf(mission) == cursor[0];
            graphics.setColor(CARD_BACKGROUND_COLOR);
            graphics.fillRect(0, 0, MISSION_WIDTH, missionHeight);
            if (currentMode == Mode.MAIN && isCurrentMission && cursor[1] == 0) {
                graphics.setColor(CURSOR_COLOR);
                graphics.drawRect(-1, -1, MISSION_WIDTH + 1, missionHeight + 1);
            }
            graphics.setColor(FONT_COLOR);
            graphics.drawString(mission.getName(), 8, 16);
            Translation.execute(graphics, MISSION_WIDTH + STAGE_MARGIN, 0, () -> mission.getStages().forEach(stage -> {
                boolean isCurrentStage = isCurrentMission && mission.getStages().indexOf(stage) == cursor[2];
                renderStages(graphics, stage, isCurrentStage);
            }));
            graphics.translate(0, missionHeight + MISSION_MARGIN);
        });
    }

    private void renderStages(Graphics2D graphics, Stage stage, boolean isCurrentStage) {
        String[] labels = {
                stage.name(),
                "%d / %d".formatted(stage.getCount(), stage.getTotal())
        };
        for (int i = 0; i < 2; i++) {
            graphics.setColor(CARD_BACKGROUND_COLOR);
            graphics.fillRect(0, 0, STAGE_WIDTH, STAGE_HEIGHT);
            graphics.setColor(FONT_COLOR);
            // TODO: Calculate the center of the string
            if (currentMode == Mode.STAGE_EDIT && isCurrentStage && i == 0) {
                String string = "%s-?".formatted(update[0] == -1 ? "?" : update[0]);
                graphics.drawString(string, 13, 15);
                AffineTransform transform = graphics.getTransform();
                editingArea = new Rectangle(
                        (int) transform.getTranslateX(),
                        (int) transform.getTranslateY(),
                        STAGE_WIDTH,
                        STAGE_HEIGHT
                );
            } else {
                graphics.drawString(labels[i], 13, 15);
            }
            if (isCurrentStage && cursor[1] - 1 == i) {
                graphics.setColor(CURSOR_COLOR);
                graphics.drawRect(-1, -1, STAGE_WIDTH + 1, STAGE_HEIGHT + 1);
            }
            graphics.translate(STAGE_WIDTH + STAGE_MARGIN, 0);
        }
        graphics.translate((STAGE_WIDTH + STAGE_MARGIN) * -2, STAGE_HEIGHT + MISSION_MARGIN);
    }

    private void renderFooter(Graphics2D graphics, Dimension size) {
        graphics.setColor(FOOTER_BACKGROUND_COLOR);
        graphics.fillRect(0, 0, size.width, FOOTER_HEIGHT);
        graphics.setClip(new Rectangle(0, 0, size.width, FOOTER_HEIGHT));
        graphics.translate(PANEL_PADDING, PANEL_PADDING + MissionScrolling.footerTranslation(cursor[3]));
        List<String> stages = distinctedStages();
        stages.forEach(stage -> {
            graphics.setColor(CARD_BACKGROUND_COLOR);
            graphics.fillRect(0, 0, TOTAL_WIDTH, STAGE_HEIGHT);
            if (currentMode == Mode.STAGE_CLEAR && stages.indexOf(stage) == cursor[3]) {
                graphics.setColor(CURSOR_COLOR);
                graphics.drawRect(-1, -1, TOTAL_WIDTH + 1, STAGE_HEIGHT + 1);
            }
            graphics.setColor(FONT_COLOR);
            final int lineY = 15;
            graphics.drawString(stage, 6, lineY);
            StageDescription.get(stage).ifPresent(description -> {
                String string = "%s（%s）".formatted(description.name(), description.operation());
                graphics.drawString(string, 35, lineY);
            });
            graphics.translate(0, MISSION_MARGIN + STAGE_HEIGHT);
        });
    }

    private void renderTip(Graphics2D graphics, int width) {
        final int LINE_START_X = 4;
        final int LINE_START_Y = 15;
        final int INTERVAL_X = 10;
        final int INTERVAL_Y = 15;
        ArrayList<String> tips = new ArrayList<>(List.of(""));
        graphics.setColor(TIP_BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, TIP_HEIGHT);
        graphics.setColor(TIP_FONT_COLOR);
        graphics.setFont(Fonts.MONO.font);
        generateTips(tips);
        Function<String, Integer> textWidth = graphics.getFontMetrics()::stringWidth;
        int cursor = LINE_START_X;
        for (int i = 1, y = LINE_START_Y; i < tips.size(); i++) {
            cursor += textWidth.apply(tips.get(i - 1)) + (i == 1 ? 0 : INTERVAL_X);
            if (cursor + textWidth.apply(tips.get(i)) > (width - INTERVAL_X)) {
                cursor = LINE_START_X;
                y += INTERVAL_Y;
            }
            graphics.drawString(tips.get(i), cursor, y);
        }
    }

    private void generateTips(List<String> tips) {
        Map<Integer, List<String>> main = Map.of(
                0, List.of(
                        "[+] Add mission",
                        "[-] Remove selecting mission",
                        "[Enter] Open Wiki"
                ),
                1, List.of(
                        "[+] Add stage",
                        "[-] Remove stage",
                        "[1-9] Edit stage code"
                ),
                2, List.of(
                        "[+] Add total",
                        "[-] Minus total",
                        "[Ctrl +] Add count",
                        "[Ctrl -] Minus count"
                )
        );
        if (currentMode == Mode.MAIN) {
            tips.add("[↑↓←→] Navigate");
            tips.add("[Ctrl ↓] Clear mode");
            tips.addAll(main.get(cursor[1]));
        }
        if (currentMode == Mode.STAGE_CLEAR) {
            tips.add("[↑↓] Navigate");
            tips.add("[Ctrl ↑] Normal mode");
            tips.add("[+] Add count");
            tips.add("[-] Minus count");
        }
        if (currentMode == Mode.STAGE_EDIT) {
            tips.add("[Esc] Cancel");
            tips.add("[1-9] Enter stage code");
        }
    }

    public void key(KeyEvent event) {
        currentMode.keyHandler.accept(this, event);
    }

    private void keyMain(KeyEvent event) {
        JFrame frame = (JFrame) event.getSource();
        int keyCode = event.getKeyCode();
        if (!event.isControlDown()) keyMain(keyCode, frame);
        if (event.isControlDown()) keyDefaultWithControl(keyCode);
    }

    private void keyMain(int keyCode, JFrame frame) {
        // Add missions
        if (keyCode == VK_ADD || keyCode == VK_PAGE_UP) {
            if (cursor[1] == 0) {
                missions.add(Mission.defaultMission());
                cursor[0] = missions.size() - 1;
                cursor[1] = cursor[2] = 0;
            }
            if (cursor[1] == 1) {
                currentMission().getStages().add(Stage.of(1, 1, 0, 1));
                cursor[2] = currentMission().getStages().size() - 1;
            }
            if (cursor[1] == 2) currentStage().addTotal();
        }
        // Remove missions
        if (keyCode == VK_SUBTRACT || keyCode == VK_PAGE_DOWN) {
            if (cursor[1] == 0) missions.remove(cursor[0]);
            if (cursor[1] == 1) {
                missions.get(cursor[0]).removeStage(cursor[2]);
                if (cursor[2] == missions.get(cursor[0]).getStages().size()) cursor[2]--;
            }
            if (cursor[1] == 2) missions.get(cursor[0]).getStages().get(cursor[2]).minusTotal();
            if (missions.isEmpty()) missions.add(Mission.defaultMission());
            if (cursor[0] >= missions.size()) cursor[0] = missions.size() - 1;
        }
        // Select missions
        if (keyCode == VK_LEFT || keyCode == VK_RIGHT) {
            cursor[1] = Math.min(Math.max(cursor[1] + keyCode - 38, 0), 2);
            if (cursor[1] == 0) cursor[2] = 0;
        }
        if ((keyCode == VK_DOWN || keyCode == VK_UP)) {
            if (cursor[1] == 0) {
                cursor[0] = (cursor[0] - (39 - keyCode) + missions.size()) % missions.size();
                return;
            }
            cursor[2] += keyCode - 39;
            if (cursor[2] == missions.get(cursor[0]).getStages().size()) {
                cursor[2] = 0;
                cursor[0] = ++cursor[0] % missions.size();
            }
            if (cursor[2] == -1) {
                cursor[0] = (--cursor[0] + missions.size()) % missions.size();
                cursor[2] = missions.get(cursor[0]).getStages().size() - 1;
            }
        }
        // Update stage
        Consumer<Integer> edit = key -> {
            currentMode = Mode.STAGE_EDIT;
            update[0] = keyCode - key;
        };
        if (keyCode >= VK_NUMPAD1 && keyCode <= VK_NUMPAD9 && cursor[1] == 1) edit.accept(VK_NUMPAD0);
        if (keyCode >= VK_1 && keyCode <= VK_9 && cursor[1] == 1) edit.accept(VK_0);
        // Open wiki
        if (keyCode == VK_ENTER && cursor[1] == 0) {
            Mission current = this.missions.get(this.cursor[0]);
            String search = !current.isDefaultMission() ? current.getName() : "";
            WikiSelectionUI.show(frame, wikis, search).ifPresent(wiki -> {
                Mission mission = currentMission();
                mission.setName(wiki.name());
                mission.replaceStages(wiki.details().stream().map(Detail::toStage).toList());
            });
        }
    }

    private void keyDefaultWithControl(int keyCode) {
        if (keyCode == VK_ADD || keyCode == VK_PAGE_UP) currentStage().addCount();
        if (keyCode == VK_SUBTRACT || keyCode == VK_PAGE_DOWN) currentStage().minusCount();
        if (keyCode == VK_DOWN) {
            currentMode = Mode.STAGE_CLEAR;
            cursor[3] = 0;
        }
    }

    private void keyClearingStage(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.isControlDown() && keyCode == VK_UP) {
            currentMode = Mode.MAIN;
            cursor[3] = 0;
            return;
        }
        List<String> stages = distinctedStages();
        int stageSize = stages.size();
        String currentStage = stages.get(cursor[3]);
        if (keyCode == VK_UP || keyCode == VK_DOWN) {
            cursor[3] += keyCode - 39;
            cursor[3] = (cursor[3] + stageSize) % stageSize;
        }
        Map<Integer, Consumer<Stage>> map = Map.of(
                VK_ADD, Stage::addCount,
                VK_PAGE_UP, Stage::addCount,
                VK_SUBTRACT, Stage::minusCount,
                VK_PAGE_DOWN, Stage::minusCount
        );
        Predicate<Stage> filter = stage -> stage.name().equals(currentStage);
        Optional
                .ofNullable(map.get(keyCode))
                .ifPresent(callback -> missions.forEach(mission -> mission.computeAllStages(filter, callback)));
    }

    private void keyEditingStage(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Consumer<Integer> edit = key -> {
            currentStage().update(update[0], keyCode - key);
            currentMode = Mode.MAIN;
        };
        if (keyCode >= VK_NUMPAD1 && keyCode <= VK_NUMPAD9 && cursor[1] == 1) edit.accept(VK_NUMPAD0);
        if (keyCode >= VK_1 && keyCode <= VK_9 && cursor[1] == 1) edit.accept(VK_0);
        if (keyCode == VK_ESCAPE) currentMode = Mode.MAIN;
    }

    private Mission currentMission() {
        return missions.get(cursor[0]);
    }

    private Stage currentStage() {
        return currentMission().getStages().get(cursor[2]);
    }

    private List<String> distinctedStages() {
        return missions
                .stream()
                .flatMap(mission -> mission.getStages().stream().map(Stage::name))
                .distinct()
                .sorted()
                .toList();
    }

    public enum Mode {
        MAIN(MissionUI::keyMain),
        STAGE_CLEAR(MissionUI::keyClearingStage),
        STAGE_EDIT(MissionUI::keyEditingStage);
        private final BiConsumer<MissionUI, KeyEvent> keyHandler;
        Mode(BiConsumer<MissionUI, KeyEvent> keyHandler) {
            this.keyHandler = keyHandler;
        }
    }
}
