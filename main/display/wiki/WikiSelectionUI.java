package main.display.wiki;

import main.core.Wiki;
import main.display.*;
import main.exception.Exceptions;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import static java.awt.event.KeyEvent.*;

public class WikiSelectionUI {
    private static final Color BACKGROUND_COLOR = new Color(255, 192, 192);
    private static final Color INPUT_COLOR = new Color(255, 160, 160);
    private static final Color FONT_COLOR = new Color(32, 32, 32);
    private static final Color EVEN_ROW_COLOR = new Color(255, 224, 224);
    private static final Color REWARD_BACKGROUND_COLOR = new Color(224, 224, 224);
    private static final int ROW_HEIGHT = 20;
    private static final int CURSOR_RADIUS = 4;
    private static final int INPUT_PADDING = 4;
    private static final int INPUT_HEIGHT = 40;
    private static final int LINE_POSITION = 14;
    private static final int PAGE_SIZE = 10;
    private static final int REWARD_HEIGHT = ROW_HEIGHT * 2;
    private static final int REWARD_ICON_SIZE = 32;
    private static final Dimension PANEL_SIZE = new Dimension(650, PAGE_SIZE * ROW_HEIGHT + INPUT_HEIGHT);
    private static final Dimension DETAIL_SIZE = new Dimension(400, PANEL_SIZE.height - REWARD_HEIGHT);
    private static final Dimension INPUT_SIZE = new Dimension(PANEL_SIZE.width - DETAIL_SIZE.width, INPUT_HEIGHT);

    private final JDialog dialog;
    private final List<Wiki> wikis;
    private final AtomicReference<List<Wiki>> filteredWikis = new AtomicReference<>(List.of());
    private final JTextPane detail = detail();
    private final boolean asViewer;

    private int cursor = -1;

    private WikiSelectionUI(JFrame frame, List<Wiki> wikis, String search) {
        this.asViewer = Objects.nonNull(search) && !search.isBlank();
        this.dialog = new JDialog(frame, "任務選択" + (this.asViewer ? " (read-only)" : ""), true);
        this.wikis = wikis;

        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        JPanel panel = panel(filteredWikis);
        JTextField textField = input(searchResult -> {
            filteredWikis.set(searchResult);
            cursor = -1;
            detail.setText("");
            if (!searchResult.isEmpty()) {
                detail.setText(searchResult.get(0).description());
                cursor = 0;
            }
            dialog.repaint();
        });
        panel.add(textField);
        panel.add(detail);
        textField.setText(search);
        textField.setEditable(!this.asViewer);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
    }

    private Optional<Wiki> result() {
        dialog.setVisible(true);
        return Optional
                .of(filteredWikis.get())
                .filter(Predicate.not(List::isEmpty))
                .filter(wikis -> cursor != -1)
                .map(wikis -> wikis.get(cursor));
    }

    public static Optional<Wiki> show(JFrame frame, List<Wiki> wikis) {
        return WikiSelectionUI.show(frame, wikis, null);
    }

    public static Optional<Wiki> show(JFrame frame, List<Wiki> wikis, String init) {
        return new WikiSelectionUI(frame, wikis, init).result();
    }

    private static DocumentListener inputListener(List<Wiki> wikis, Consumer<List<Wiki>> callback) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { changed(event); }
            public void removeUpdate(DocumentEvent event) { changed(event); }
            public void changedUpdate(DocumentEvent event) { changed(event); }
            private void changed(DocumentEvent event) {
                Document document = event.getDocument();
                String text = Exceptions
                        .wrap(() -> document.getText(0, document.getLength()))
                        .toLowerCase();
                CompletableFuture
                        .supplyAsync(filterWiki(wikis, text))
                        .thenAccept(callback);
            }
        };
    }

    private static Supplier<List<Wiki>> filterWiki(List<Wiki> wikis, String text) {
        Predicate<Wiki> grepCode = wiki -> wiki.id().toLowerCase().contains(text);
        Predicate<Wiki> grepName = wiki -> wiki.name().contains(text);
        if (text.isEmpty()) return List::of;
        return () -> wikis.stream().filter(grepCode.or(grepName)).toList();
    }

    private JPanel panel(AtomicReference<List<Wiki>> wikiReference) {
        JPanel panel = new JPanel(null) {
            protected void paintComponent(Graphics graphics) {
                List<Wiki> wiki = wikiReference.get();
                render((Graphics2D) graphics, wiki);
            }
        };
        panel.setPreferredSize(PANEL_SIZE);
        return panel;
    }

    private JTextField input(Consumer<List<Wiki>> callback) {
        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createEmptyBorder(0, INPUT_PADDING, 0, INPUT_PADDING));
        textField
                .getDocument()
                .addDocumentListener(inputListener(wikis, callback));
        textField.setBackground(INPUT_COLOR);
        textField.setSize(INPUT_SIZE);
        Map<Integer, Consumer<Integer>> map = Map.of(
                VK_ENTER, keyCode -> {
                    dialog.setVisible(false);
                    cursor = this.asViewer ? -1 : cursor;
                },
                VK_ESCAPE, keyCode -> {
                    dialog.setVisible(false);
                    cursor = -1;
                },
                VK_UP, keyCode -> moveCursor(-1),
                VK_DOWN, keyCode -> moveCursor(1)
        );
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                textField.getParent().repaint();
                int keyCode = event.getKeyCode();
                Optional
                        .ofNullable(map.get(keyCode))
                        .ifPresent(consumer -> consumer.accept(keyCode));
            }
        });
        return textField;
    }

    private void moveCursor(int difference) {
        List<Wiki> wikis = filteredWikis.get();
        if (wikis.isEmpty()) return;
        cursor += difference;
        cursor = Math.floorMod(cursor, wikis.size());
        detail.setText(filteredWikis.get().get(cursor).description());
    }

    private void render(Graphics2D graphics, List<Wiki> wikis) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, PANEL_SIZE.width, PANEL_SIZE.height);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Translation.execute(graphics, () -> renderWikis(graphics, wikis));
        Translation.execute(graphics, () -> renderRewards(graphics));
    }

    private void renderWikis(Graphics2D graphics, List<Wiki> wikis) {
        int flooredCursor = wikis.isEmpty() ? 0 : Math.floorMod(cursor, wikis.size());
        AtomicInteger currentRow = new AtomicInteger();
        graphics.translate(0, INPUT_SIZE.height);
        if (cursor >= PAGE_SIZE) {
            graphics.translate(0, -ROW_HEIGHT * (cursor - PAGE_SIZE + 1));
        }
        wikis.forEach(wiki -> {
            if (currentRow.get() % 2 == 0) {
                graphics.setColor(EVEN_ROW_COLOR);
                graphics.fillRect(0, 0, INPUT_SIZE.width, ROW_HEIGHT);
            }
            graphics.setColor(FONT_COLOR);
            if (currentRow.getAndIncrement() == flooredCursor) {
                graphics.fillOval(4, ROW_HEIGHT / 2 - CURSOR_RADIUS, CURSOR_RADIUS, CURSOR_RADIUS);
            }
            graphics.drawString(wiki.id(), 18, LINE_POSITION);
            graphics.drawString(wiki.name(), 70, LINE_POSITION);
            graphics.translate(0, ROW_HEIGHT);
        });
    }

    private void renderRewards(Graphics2D graphics) {
        graphics.setColor(REWARD_BACKGROUND_COLOR);
        graphics.fillRect(INPUT_SIZE.width, PANEL_SIZE.height - REWARD_HEIGHT, PANEL_SIZE.width, REWARD_HEIGHT);
        graphics.translate(INPUT_SIZE.width + 10, PANEL_SIZE.height - (REWARD_HEIGHT + REWARD_ICON_SIZE) / 2);
        Optional<int[]> rewards = Optional
                .ofNullable(filteredWikis.get())
                .filter(wikis -> wikis.size() > cursor)
                .filter(wikis -> cursor != -1)
                .map(wikis -> wikis.get(cursor))
                .map(wiki -> new int[] { wiki.fuel(), wiki.bullet(), wiki.steel(), wiki.bauxite() });
        graphics.setColor(FONT_COLOR);
        for (int i = 0; i < 4; i++) {
            graphics.drawImage(
                    Images.RESOURCE.image,
                    0,
                    0,
                    REWARD_ICON_SIZE,
                    REWARD_ICON_SIZE,
                    REWARD_ICON_SIZE * i,
                    0,
                    REWARD_ICON_SIZE * (i + 1),
                    REWARD_ICON_SIZE,
                    null
            );
            final int index = i;
            rewards.ifPresent(reward -> graphics.drawString(String.valueOf(reward[index]), 40, 20));
            graphics.translate(90, 0);
        }
    }

    private static JTextPane detail() {
        JTextPane textPane = new JTextPane();
        textPane.setSize(DETAIL_SIZE);
        textPane.setLocation(PANEL_SIZE.width - DETAIL_SIZE.width, 0);
        textPane.setFocusable(false);
        textPane.setEditable(false);
        return textPane;
    }
}
