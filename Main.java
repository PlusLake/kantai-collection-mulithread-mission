import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        setup(controller);
        JFrame frame = frame(
                panel((graphics, size) -> render(graphics, size, controller)),
                event -> keyHandler(event, controller),
                controller::save
        );
        frame.setVisible(true);
    }

    static void setup(Controller controller) {
        final String workdir = Optional
                .ofNullable(System.getenv("PLUSLAKE_KANTAI_COLLECTION_WORKDIR"))
                .orElse(System.getProperty("user.home") + "/.pluslake/kankore/multithread/");
        final String missionDownloadPath = Optional
                .ofNullable(System.getenv("PLUSLAKE_KANTAI_COLLECTION_MISSION_DOWNLOAD_URL"))
                .orElse("https://raw.githubusercontent.com/PlusLake/kantai-collection-mulithread-mission/master/mission.tsv");
        final String missionLocalPath = workdir + "mission.tsv";
        controller.wiki = getWiki(workdir, missionDownloadPath, missionLocalPath);
        controller.savePath = workdir + "save.tsv";
        wrapException(() -> new File(controller.savePath).createNewFile());
        List<Mission> missions = wrapException(() -> new ArrayList<>(Files
                .lines(Path.of(controller.savePath), StandardCharsets.UTF_8)
                .map(Mission::parse)
                .toList()));
        if (missions.isEmpty()) missions.add(Mission.defaultMission());
        controller.missions = missions;
    }

    static List<MissionWikiRow> getWiki(String workdir, String missionDownloadPath, String missionLocalPath) {
        if (!Files.exists(Path.of(workdir + "mission.tsv"))) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(missionDownloadPath)).build();
            wrapException(() -> HttpClient
                    .newHttpClient()
                    .send(request, BodyHandlers.ofFile(Path.of(missionLocalPath), StandardOpenOption.CREATE, StandardOpenOption.WRITE)))
                    .body();
        }
        return wrapException(() -> Files
                .lines(Path.of(missionLocalPath))
                .map(MissionWikiRow::parse)
                .toList());
    }

    static JFrame frame(JPanel panel, Consumer<KeyEvent> keyHandler, Runnable save) {
        JFrame frame = new JFrame("艦隊これくしょん　マルチ任務");
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                keyHandler.accept(event);
                frame.repaint();
            }
            public void keyPressed(KeyEvent event) {
                keyHandler.accept(event);
                frame.repaint();
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                save.run();
                System.exit(0);
            }
        });
        return frame;
    }

    static JPanel panel(BiConsumer<Graphics2D, Dimension> renderCallback) {
        JPanel panel = new JPanel(null) {
            public void paintComponent(Graphics graphics) {
                renderCallback.accept((Graphics2D) graphics, getSize());
            }
        };
        panel.setPreferredSize(new Dimension(400, 600));
        return panel;
    }

    static void render(Graphics2D graphics, Dimension size, Controller controller) {
        final int PADDING = 8;
        final int MARGIN = 4;
        final int COLUMN_WIDTH = 250;
        final int MISSION_WIDTH = 150;
        final int STAGE_WIDTH = (COLUMN_WIDTH - MISSION_WIDTH - MARGIN * 2) / 2;
        final int STAGE_HEIGHT = 20;
        final int BOTTOM_HEIGHT = 200;
        final Color BACKGROUND_COLOR = new Color(255, 192, 192);
        final Color CARD_BACKGROUND_COLOR = new Color(255, 240, 240);
        final Color BOTTOM_BACKGROUND_COLOR = new Color(255, 160, 160);
        final Color FONT_COLOR = new Color(32, 32, 32);
        final Color HIGHLIGHT = new Color(255, 64, 64);

        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, size.width, size.height);
        graphics.setColor(BOTTOM_BACKGROUND_COLOR);
        graphics.fillRect(0, size.height - BOTTOM_HEIGHT, size.width, BOTTOM_HEIGHT);
        graphics.translate(PADDING, PADDING);
        AffineTransform ORIGIN = graphics.getTransform();
        controller.missions.forEach(mission -> {
            final int STAGES = mission.stages.size();
            final int TOTAL_HEIGHT = STAGES * STAGE_HEIGHT + (STAGES - 1) * MARGIN;
            final boolean isCurrentMission = !controller.isStageClearMode && controller.missions.indexOf(mission) == controller.cursor[0];
            graphics.setColor(CARD_BACKGROUND_COLOR);
            graphics.fillRect(0, 0, MISSION_WIDTH, TOTAL_HEIGHT);
            if (isCurrentMission && controller.cursor[1] == 0) {
                graphics.setColor(HIGHLIGHT);
                graphics.drawRect(-1, -1, MISSION_WIDTH + 2, TOTAL_HEIGHT + 2);
            }
            graphics.setColor(FONT_COLOR);
            graphics.drawString(mission.name, 8, 16);
            AffineTransform transform = graphics.getTransform();
            graphics.translate(MISSION_WIDTH + MARGIN, 0);
            mission.stages.forEach(stage -> {
                final boolean isCurrentStage = isCurrentMission && mission.stages.indexOf(stage) == controller.cursor[2];
                final String[] labels = {
                        stage.name(),
                        String.format("%d / %d", stage.count, stage.total)
                };
                for (int i = 0; i < 2; i++) {
                    graphics.setColor(CARD_BACKGROUND_COLOR);
                    graphics.fillRect(0, 0, STAGE_WIDTH, STAGE_HEIGHT);
                    if (isCurrentStage && controller.cursor[1] - 1 == i) {
                        graphics.setColor(HIGHLIGHT);
                        graphics.drawRect(-1, -1, STAGE_WIDTH + 2, STAGE_HEIGHT + 2);
                    }
                    graphics.setColor(FONT_COLOR);
                    graphics.drawString(labels[i], 13, 15);
                    graphics.translate(STAGE_WIDTH + MARGIN, 0);
                }
                graphics.translate(-(STAGE_WIDTH + MARGIN) * 2, STAGE_HEIGHT + MARGIN);
            });
            graphics.setTransform(transform);
            graphics.translate(0, TOTAL_HEIGHT + MARGIN);
        });
        graphics.setTransform(ORIGIN);
        graphics.translate(0, size.height - BOTTOM_HEIGHT);
        final AtomicInteger currentStage = new AtomicInteger();
        controller.stages().forEach(stage -> {
            graphics.setColor(CARD_BACKGROUND_COLOR);
            graphics.fillRect(0, 0, COLUMN_WIDTH, STAGE_HEIGHT);
            if (controller.isStageClearMode && currentStage.getAndIncrement() == controller.selectedStage) {
                graphics.setColor(HIGHLIGHT);
                graphics.drawRect(-1, -1, COLUMN_WIDTH + 2, STAGE_HEIGHT + 2);
            }
            graphics.setColor(FONT_COLOR);
            graphics.drawString(stage, 4, 15);
            graphics.translate(0, MARGIN + STAGE_HEIGHT);
        });
    }

    static void keyHandler(KeyEvent event, Controller controller) {
        final int KEY_CODE = event.getKeyCode();
        final int[] cursor = controller.cursor;
        final List<Mission> missions = controller.missions;
        if (event.getID() == KeyEvent.KEY_PRESSED && controller.isEditingStage) {
            if (KEY_CODE >= KeyEvent.VK_NUMPAD1 && KEY_CODE <= KeyEvent.VK_NUMPAD9) {
                controller.editStageSecond(KEY_CODE - 96);
            }
            return;
        }
        if (event.getID() == KeyEvent.KEY_PRESSED && event.isControlDown()) {
            if (KEY_CODE == KeyEvent.VK_DOWN || KEY_CODE == KeyEvent.VK_UP) {
                controller.isStageClearMode = KEY_CODE > 39;
                controller.selectedStage = 0;
            }
            if ((KEY_CODE == KeyEvent.VK_ADD || KEY_CODE == KeyEvent.VK_SUBTRACT) && cursor[1] == 2 && !controller.isStageClearMode) {
                controller.currentStage().count += 108 - KEY_CODE;
            }
        }
        if (event.getID() == KeyEvent.KEY_PRESSED && !event.isControlDown() && controller.isStageClearMode) {
            final int stagesSize = controller.stages().size();
            if (KEY_CODE == KeyEvent.VK_UP || KEY_CODE == KeyEvent.VK_DOWN) {
                controller.selectedStage += KEY_CODE - 39;
                controller.selectedStage = (controller.selectedStage + stagesSize) % stagesSize;
            }
            if (KEY_CODE == KeyEvent.VK_ADD || KEY_CODE == KeyEvent.VK_SUBTRACT) {
                controller.clearStage(108 - KEY_CODE);
            }
        }
        if (event.getID() == KeyEvent.KEY_PRESSED && !event.isControlDown() && !controller.isStageClearMode) {
            if (KEY_CODE == KeyEvent.VK_ADD) {
                if (cursor[1] == 0) missions.add(Mission.defaultMission());
                if (cursor[1] == 1) missions.get(cursor[0]).stages.add(new Stage(1, 1, 0, 1));
                if (cursor[1] == 2) missions.get(cursor[0]).stages.get(cursor[2]).total++;
            }
            if (KEY_CODE == KeyEvent.VK_SUBTRACT) {
                if (cursor[1] == 0) missions.remove(cursor[0]);
                if (cursor[1] == 1) {
                    missions.get(cursor[0]).removeStage(cursor[2]);
                    if (cursor[2] == missions.get(cursor[0]).stages.size()) cursor[2]--;
                }
                if (cursor[1] == 2) missions.get(cursor[0]).stages.get(cursor[2]).minusTotal();
                if (missions.isEmpty()) missions.add(Mission.defaultMission());
                if (cursor[0] >= missions.size()) cursor[0] = missions.size() - 1;
            }
            if (KEY_CODE == KeyEvent.VK_LEFT || KEY_CODE == KeyEvent.VK_RIGHT) {
                cursor[1] = Math.clamp(cursor[1] + KEY_CODE - 38, 0, 2);
                if (cursor[1] == 0) {
                    cursor[2] = 0;
                }
            }
            if ((KEY_CODE == KeyEvent.VK_DOWN || KEY_CODE == KeyEvent.VK_UP)) {
                if (cursor[1] == 0) {
                    cursor[0] = (cursor[0] - (39 - KEY_CODE) + missions.size()) % missions.size();
                } else {
                    cursor[2] += KEY_CODE - 39;
                    if (cursor[2] == missions.get(cursor[0]).stages.size()) {
                        cursor[2] = 0;
                        cursor[0] = ++cursor[0] % missions.size();
                    }
                    if (cursor[2] == -1) {
                        cursor[0] = (--cursor[0] + missions.size()) % missions.size();
                        cursor[2] = missions.get(cursor[0]).stages.size() - 1;
                    }
                }
            }
            if (KEY_CODE >= KeyEvent.VK_NUMPAD1 && KEY_CODE <= KeyEvent.VK_NUMPAD9 && cursor[1] == 1) {
                controller.isEditingStage = true;
                controller.editStageFirst(KEY_CODE - 96);
            }
            if (KEY_CODE == KeyEvent.VK_ENTER && cursor[1] == 0) {
                JFrame frame = (JFrame) event.getSource();
                selectMissionDialog(frame, controller.wiki).ifPresent(wiki -> {
                    List<Stage> stages = wiki.stages.stream().map(array -> new Stage(array[0], array[1])).toList();
                    Mission mission = controller.currentMission();
                    mission.name = wiki.name;
                    mission.stages = new ArrayList<>(stages);
                });
            }
        }
    }

    static Optional<MissionWikiRow> selectMissionDialog(JFrame frame, List<MissionWikiRow> wikis) {
        JDialog dialog = new JDialog(frame, "任務選択", true);
        JTextField textField = new JTextField();
        JTextPane textPane = new JTextPane();
        AtomicReference<List<MissionWikiRow>> filteredWikis = new AtomicReference<>(List.of());
        AtomicInteger cursor = new AtomicInteger();
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { changed(); }
            public void removeUpdate(DocumentEvent event) { changed(); }
            public void changedUpdate(DocumentEvent event) { changed(); }
            void changed() {
                CompletableFuture.supplyAsync(() -> {
                    String text = textField.getText();
                    return wikis
                            .stream()
                            .filter(wiki -> !text.isEmpty())
                            .filter(wiki -> wiki.name.contains(text))
                            .toList();
                }).thenAccept(wikis -> {
                    dialog.repaint();
                    filteredWikis.set(wikis);
                    cursor.set(-1);
                    if (!wikis.isEmpty()) {
                        cursor.set(0);
                        textPane.setText(wikis.getFirst().content);
                    }
                });
            }
        });
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                final int keyCode = event.getKeyCode();
                List<MissionWikiRow> filtered = filteredWikis.get();
                if (keyCode == KeyEvent.VK_ENTER) {
                    dialog.setVisible(false);
                    return;
                }
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    cursor.set(-1);
                    dialog.setVisible(false);
                    return;
                }
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                    if (filtered.isEmpty()) return;
                    cursor.set(Math.clamp(cursor.get() - 39 + keyCode, 0, filtered.size() - 1));
                    textPane.setText(filtered.get(cursor.get()).content);
                }
                dialog.repaint();
            }
        });
        BiConsumer<Graphics2D, Dimension> render = (graphics, size) -> {
            final int HEIGHT = 20;
            final int LINE_POSITION = 14;
            final int CURSOR_RADIUS = 4;
            final Color FONT_COLOR = new Color(16, 16, 16);
            final Color EVEN_ROW_COLOR = new Color(255, 224, 224);

            final AtomicInteger rowCount = new AtomicInteger();
            final int currentCursor = cursor.get();
            if (currentCursor > 7) {
                graphics.translate(0, -HEIGHT * (currentCursor - 7));
            }
            filteredWikis.get().forEach(wiki -> {
                if (rowCount.get() % 2 == 0) {
                    graphics.setColor(EVEN_ROW_COLOR);
                    graphics.fillRect(0, 0, size.width, HEIGHT);
                }
                graphics.setColor(FONT_COLOR);
                if (rowCount.getAndIncrement() == currentCursor) {
                    graphics.fillOval(4, HEIGHT / 2 - CURSOR_RADIUS, CURSOR_RADIUS * 2, CURSOR_RADIUS * 2);
                }
                graphics.drawString(wiki.code, 18, LINE_POSITION);
                graphics.drawString(wiki.name, 60, LINE_POSITION);
                graphics.translate(0, HEIGHT);
            });
        };
        dialog.setContentPane(selectMissionDialogPanel(textField, textPane, render));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.pack();
        dialog.setVisible(true);

        List<MissionWikiRow> result = filteredWikis.get();
        if (result.size() > cursor.get() && cursor.get() != -1) {
            return Optional.of(result.get(cursor.get()));
        }
        return Optional.empty();
    }

    static JPanel selectMissionDialogPanel(JTextField textField, JTextPane textPane, BiConsumer<Graphics2D, Dimension> render) {
        Dimension size = new Dimension(500, 200);
        final int DETAIL_WIDTH = 300;
        final int TEXT_FIELD_HEIGHT = 40;
        JPanel panel = new JPanel(null) {
            protected void paintComponent(Graphics graphics) {
                graphics.setColor(new Color(255, 192, 192));
                graphics.fillRect(0, 0, size.width, size.height);
                AffineTransform transform = ((Graphics2D) graphics).getTransform();
                graphics.translate(0, TEXT_FIELD_HEIGHT);
                render.accept((Graphics2D) graphics, size);
                ((Graphics2D) graphics).setTransform(transform);
            }
        };
        panel.setPreferredSize(size);
        textField.setBackground(new Color(255, 160, 160));
        textField.setSize(new Dimension(size.width - DETAIL_WIDTH, TEXT_FIELD_HEIGHT));
        textPane.setSize(new Dimension(DETAIL_WIDTH, size.height));
        textPane.setLocation(size.width - DETAIL_WIDTH, 0);
        textPane.setFocusable(false);
        textPane.setEditable(false);
        panel.add(textField);
        panel.add(textPane);
        return panel;
    }

    static class Controller {
        List<Mission> missions = new ArrayList<>();
        List<MissionWikiRow> wiki = new ArrayList<>();
        // MISSION / COLUMN / STAGE
        int[] cursor = {0, 0, 0};
        boolean isStageClearMode = false;
        boolean isEditingStage = false;
        int selectedStage = 0;
        String savePath;
        Mission currentMission() {
            return missions.get(cursor[0]);
        }
        Stage currentStage() {
            return currentMission().stages.get(cursor[2]);
        }
        List<String> stages() {
            return missions
                    .stream()
                    .flatMap(mission -> mission.stages.stream().map(Stage::name))
                    .distinct()
                    .sorted()
                    .toList();
        }
        void clearStage(int diff) {
            String targetStage = stages().get(selectedStage);
            missions
                    .stream()
                    .flatMap(mission -> mission.stages.stream())
                    .filter(stage -> stage.name().equals(targetStage))
                    .forEach(stage -> stage.count += diff);
        }
        void editStageFirst(int value) {
            Stage stage = missions.get(cursor[0]).stages.get(cursor[2]);
            stage.first = value;
            stage.second = -1;
        }
        void editStageSecond(int value) {
            missions.get(cursor[0]).stages.get(cursor[2]).second = value;
            isEditingStage = false;
        }
        void save() {
            wrapException(() -> Files.writeString(
                    Path.of(savePath),
                    missions
                            .stream()
                            .map(Mission::toString)
                            .collect(Collectors.joining("\n"))
            ));
        }
    }

    static class Mission {
        List<Stage> stages = new ArrayList<>();
        String name;
        Mission(String name) {
            this.name = name;
        }
        Mission withStages(Stage... stages) {
            this.stages.addAll(List.of(stages));
            return this;
        }
        void removeStage(int index) {
            if (stages.size() > 1) stages.remove(index);
        }
        static Mission parse(String string) {
            String[] splitted = string.split("\t");
            Mission mission = new Mission(splitted[0]);
            mission.stages = Stream
                    .of(splitted[1].split("_"))
                    .map(stage -> Stream.of(stage.split("-")).map(Integer::parseInt).toArray(Integer[]::new))
                    .map(array -> new Stage(array[0], array[1], array[2], array[3]))
                    .toList();
            return mission;
        }
        static Mission defaultMission() {
            return new Mission("Press enter to select").withStages(new Stage(1, 1, 0, 1));
        }
        public String toString() {
            String stagesString = stages.stream().map(Stage::toString).collect(Collectors.joining("_"));
            return "%s\t%s".formatted(name, stagesString);
        }
    }

    static class MissionWikiRow {
        String code;
        String name;
        String content;
        List<int[]> stages = new ArrayList<>();

        public String toString() {
            String stageString = stages.stream().map(stage -> "%d-%d".formatted(stage[0], stage[1])).collect(Collectors.joining(", "));
            return "[%s] %s (%s)".formatted(code, name, stageString);
        }

        static  MissionWikiRow empty() {
            MissionWikiRow result = new MissionWikiRow();
            result.name = Mission.defaultMission().name;
            result.stages = new ArrayList<>(List.of(new int[] {1, 1}));
            return result;
        }

        static MissionWikiRow parse(String string) {
            String[] array = string.split("\t");
            if (array.length != 3) {
                String message = "Invalid tsv file. Column count not 3 (%d)".formatted(array.length);
                throw new IllegalArgumentException(message);
            }
            MissionWikiRow mission = new MissionWikiRow();
            mission.code = array[0];
            mission.name = array[1];
            mission.content = array[2].replaceAll("\\\\n", "\n");
            mission.stages = findStagesFromText(array[2]);
            return mission;
        }

        static List<int[]> findStagesFromText(String string) {
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
    }

    static class Stage {
        int first;
        int second;
        int count;
        int total;

        Stage(int first, int second, int count, int total) {
            this.first = first;
            this.count = count;
            this.second = second;
            this.total = total;
        }
        Stage(int first, int second) {
            this(first, second, 0, 1);
        }
        String name() {
            if (second == -1) return String.format("%d-?", first);
            return String.format("%d-%d", first, second);
        }
        void minusTotal() {
            total = Math.max(--total, 1);
        }
        public String toString() {
            return "%d-%d-%d-%d".formatted(first, second, count, total);
        }
    }

    static <T> T wrapException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
