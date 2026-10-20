/*
 * Decompiled with CFR 0.152.
 */
package app.soopcollector;

import app.soopcollector.GoogleSheetsExporter;
import app.soopcollector.RouletteOverlay;
import app.soopcollector.RuleEngine;
import app.soopcollector.UpdateService;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import zzik2.soop4j.SoopClient;
import zzik2.soop4j.api.SoopLive;
import zzik2.soop4j.chat.SoopChat;
import zzik2.soop4j.chat.SoopChatAdapter;
import zzik2.soop4j.chat.event.ChatEvent;
import zzik2.soop4j.chat.event.DisconnectEvent;
import zzik2.soop4j.chat.event.DonationEvent;
import zzik2.soop4j.chat.event.DonationType;
import zzik2.soop4j.chat.event.EnterChatRoomEvent;
import zzik2.soop4j.chat.event.RawEvent;
import zzik2.soop4j.chat.event.UnknownEvent;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.exception.AuthenticationRequiredException;
import zzik2.soop4j.exception.StreamerOfflineException;
import zzik2.soop4j.http.SoopHttpClient;

public final class Main
extends JFrame {
    private static final String FIXED_STREAMER_ID = "kjhh0029";
    private static final boolean TEST_MODE = Boolean.getBoolean("kanashi.testMode");
    private static final String GOOGLE_SHEETS_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbw5aF5IHW4hWYwmXlPCWOt_28kS-Ku5R-x9QucTTJ2ObK4-E6rMd2oK1mL8DkP3rUxY/exec";
    private static final String GOOGLE_SHEETS_ACCESS_KEY = "kanashi-kk1231567-2026";
    private static final Color BG = new Color(255, 248, 242);
    private static final Color PANEL = Color.WHITE;
    private static final Color PANEL_2 = new Color(255, 239, 225);
    private static final Color ACCENT = new Color(255, 122, 61);
    private static final Color ACCENT_DARK = new Color(218, 78, 25);
    private static final Color TEXT = new Color(52, 42, 37);
    private static final Color MUTED = new Color(139, 119, 108);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
    private static final Path DATA = Paths.get(System.getProperty("user.home"), ".soop-collector", TEST_MODE ? "data-test.json" : "data.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private State state = this.load();
    private final JTextField streamer = new JTextField();
    private final JLabel status = new StatusLabel("\u25cf  \uc624\ud504\ub77c\uc778");
    private final JButton account = new JButton("\ub85c\uadf8\uc778");
    private final JButton connect = new JButton("\uc5f0\uacb0");
    private final DefaultTableModel roomModel = Main.model("\uc2dc\uac04", "\ud6c4\uc6d0 \uc885\ub958", "\ub2c9\ub124\uc784", "\uc544\uc774\ub514", "\ud6c4\uc6d0\uc218", "\uba54\uc2dc\uc9c0", "\uba54\ubaa8", "\ubc29\uc1a1\uad6d");
    private final DefaultTableModel reverseModel = Main.model("\uc2dc\uac04", "\ud6c4\uc6d0 \uc885\ub958", "\ub2c9\ub124\uc784", "\uc544\uc774\ub514", "\ud37c\uc13c\ud2b8", "\ud6c4\uc6d0\uc218", "\uba54\uc2dc\uc9c0", "\uacc4\uc0b0\uc218", "\ubc29\uc1a1\uad6d");
    private final DefaultTableModel pinModel = Main.model("\uc2dc\uac04", "\ud6c4\uc6d0 \uc885\ub958", "\ub2c9\ub124\uc784", "\uc544\uc774\ub514", "\ud6c4\uc6d0\uc218", "\uba54\uc2dc\uc9c0", "\uacf5", "\ubc29\uc1a1\uad6d");
    private final JPanel ballSummary = new JPanel();
    private final JPanel roomList = Main.collectionList();
    private final JPanel reverseList = Main.collectionList();
    private final JPanel pinList = Main.collectionList();
    private final JPanel roomSummary = new JPanel(new FlowLayout(0, 7, 3));
    private final JTextField roomSearch = new JTextField();
    private final JTextField reverseSearch = new JTextField();
    private final JTextField pinSearch = new JTextField();
    private final Map<String, List<Pending>> pending = new HashMap<String, List<Pending>>();
    private final LinkedHashMap<String, ChatViewerType> chatViewerTypes = new LinkedHashMap<String, ChatViewerType>();
    private final AtomicLong donationSequence = new AtomicLong();
    private final LinkedHashSet<Long> claimedRoomDonations = new LinkedHashSet();
    private RouletteOverlay rouletteOverlay;
    private JComponent nashiBallPanel;
    private SoopChat chat;
    private SoopSessionManager.Session session = SoopSessionManager.load();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main.setLook();
            new Main().setVisible(true);
        });
    }

    private Main() {
        super(TEST_MODE ? "\uce74\ub098\uc2dc \uc218\uc9d1\ud568 \u00b7 \ud14c\uc2a4\ud2b8" : "\uce74\ub098\uc2dc \uc218\uc9d1\ud568 v1.2.0");
        String[][] nav;
        this.setIconImages(Main.loadAppIcons());
        this.setDefaultCloseOperation(0);
        this.setMinimumSize(new Dimension(1100, 700));
        this.setSize(1320, 840);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(BG);
        this.setLayout(new BorderLayout());
        this.rouletteOverlay = RouletteOverlay.start();
        this.add((Component)this.header(), "North");
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        cards.add((Component)this.roomTab(), "room");
        cards.add((Component)this.reverseTab(), "reverse");
        cards.add((Component)this.pinballTab(), "pin");
        cards.add((Component)this.nashiBallTab(), "nashiball");
        JPanel navigation = new JPanel(new FlowLayout(0, 8, 6));
        navigation.setOpaque(false);
        navigation.setBorder(new EmptyBorder(5, 22, 3, 22));
        ButtonGroup group = new ButtonGroup();
        nav = new String[][]{{"\ubc29\uc140", "room"}, {"\uc5ed\ud32c", "reverse"}, {"\uacf5 \uc218\uc9d1", "pin"}, {"\ub098\uc2dc\ubcfc", "nashiball"}};
        for (String[] item : nav) {
            JToggleButton button = new JToggleButton(item[0]);
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.setPreferredSize(new Dimension(96, 40));
            button.setFont(button.getFont().deriveFont(1, 14.0f));
            button.addActionListener(e -> cardLayout.show(cards, item[1]));
            group.add(button);
            navigation.add(button);
            if (!item[1].equals("room")) continue;
            button.setSelected(true);
        }
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add((Component)navigation, "North");
        body.add((Component)cards, "Center");
        this.add((Component)body, "Center");
        this.consolidateState();
        this.restoreTables();
        this.refreshCollectionLists();
        Main.styleControls(this.getContentPane());
        if (!TEST_MODE) {
            Timer autoConnect = new Timer(350, e -> this.connect());
            autoConnect.setRepeats(false);
            autoConnect.start();
            Timer updateCheck = new Timer(2200, e -> UpdateService.check(this, false));
            updateCheck.setRepeats(false);
            updateCheck.start();
        }
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                Main.this.disconnect();
                if (Main.this.rouletteOverlay != null) {
                    Main.this.rouletteOverlay.close();
                }
                if (Main.this.nashiBallPanel instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable)Main.this.nashiBallPanel).close();
                    } catch (Exception ignored) {
                    }
                }
                Main.this.save();
                Main.this.dispose();
            }
        });
    }

    private JComponent header() {
        HeaderPanel p = new HeaderPanel(new BorderLayout(18, 0));
        p.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel brandWrap = new JPanel(new FlowLayout(0, 13, 0));
        brandWrap.setOpaque(false);
        AvatarLabel logo = new AvatarLabel();
        brandWrap.add(logo);
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, 1));
        JLabel title = new JLabel("\uce74\ub098\uc2dc \uc218\uc9d1\ud568");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(1, 25.0f));
        JLabel sub = new JLabel("\uc624\ub298\uc758 \ud6c4\uc6d0\uc744 \uac00\ubccd\uace0 \uc608\uc058\uac8c \ubaa8\uc544\ubd10\uc694");
        sub.setForeground(MUTED);
        sub.setFont(sub.getFont().deriveFont(12.0f));
        brand.add(title);
        brand.add(Box.createVerticalStrut(3));
        brand.add(sub);
        brandWrap.add(brand);
        p.add((Component)brandWrap, "West");
        JPanel controls = new JPanel(new FlowLayout(2, 10, 0));
        controls.setOpaque(false);
        JPanel versionWrap = new JPanel();
        versionWrap.setOpaque(false);
        versionWrap.setLayout(new BoxLayout(versionWrap, 1));
        JLabel version = new JLabel(TEST_MODE ? "Kanashi Collector TEST" : "Kanashi Collector v" + UpdateService.CURRENT_VERSION);
        version.setForeground(TEXT);
        version.setFont(version.getFont().deriveFont(1, 12.0f));
        JLabel updateHint = new JLabel(TEST_MODE ? "\uc790\ub3d9 \uc5c5\ub370\uc774\ud2b8 \uaebc\uc9d0" : "\uc2e4\ud589 \uc2dc \uc5c5\ub370\uc774\ud2b8 \uc790\ub3d9 \ud655\uc778");
        updateHint.setForeground(MUTED);
        updateHint.setFont(updateHint.getFont().deriveFont(11.0f));
        versionWrap.add(version);
        versionWrap.add(Box.createVerticalStrut(2));
        versionWrap.add(updateHint);
        controls.add(versionWrap);
        if (!TEST_MODE) {
            JButton update = new JButton("\u21bb");
            update.setToolTipText("\ucd5c\uc2e0 \ubc84\uc804 \ud655\uc778");
            update.getAccessibleContext().setAccessibleName("\ucd5c\uc2e0 \ubc84\uc804 \ud655\uc778");
            update.putClientProperty("JButton.buttonType", "roundRect");
            update.setPreferredSize(new Dimension(38, 38));
            update.setFont(update.getFont().deriveFont(1, 19.0f));
            update.addActionListener(e -> UpdateService.check(this, true));
            controls.add(update);
        } else {
            this.streamer.setText(this.state.streamerId == null ? "" : this.state.streamerId);
            this.streamer.putClientProperty("JTextField.placeholderText", "\uc2a4\ud2b8\ub9ac\uba38 ID");
            this.streamer.setPreferredSize(new Dimension(150, 38));
            this.streamer.addActionListener(e -> this.connect(false));
            this.connect.setText("\uc5f0\uacb0");
            this.connect.setPreferredSize(new Dimension(66, 38));
            this.connect.addActionListener(e -> this.connect(false));
            controls.add(this.streamer);
            controls.add(this.connect);
        }
        this.refreshAccountButton();
        this.account.putClientProperty("JButton.buttonType", "roundRect");
        this.account.setToolTipText("SOOP \uacc4\uc815 \ub85c\uadf8\uc778, \ub85c\uadf8\uc544\uc6c3 \ub610\ub294 \uacc4\uc815 \ubcc0\uacbd");
        this.account.addActionListener(e -> this.accountAction());
        controls.add(this.account);
        this.status.setOpaque(false);
        this.status.setBackground(new Color(245, 239, 233));
        this.status.setForeground(MUTED);
        this.status.setBorder(new EmptyBorder(10, 18, 10, 18));
        this.status.setText("\u25cf  \uc624\ud504\ub77c\uc778");
        controls.add(this.status);
        p.add((Component)controls, "East");
        return p;
    }

    private JComponent roomTab() {
        Runnable[] refresh = new Runnable[1];
        JPanel rules = Main.settingsCard("\ubc29\uc140 \uc124\uc815", "\uc6d0\ud558\ub294 \ud6c4\uc6d0 \uc218\ub7c9\uc744 \uc5ec\ub7ec \uac1c \ub4f1\ub85d\ud574\uc694");
        rules.add(Main.section("\uc218\uc9d1\ud560 \ud6c4\uc6d0"));
        this.addSourceSelectors(rules, "room");
        rules.add(Box.createVerticalStrut(14));
        JTextField value = Main.smallField();
        JTextField item = Main.smallField();
        value.putClientProperty("JTextField.placeholderText", "\uc218\ub7c9");
        item.putClientProperty("JTextField.placeholderText", "\ud488\ubaa9\uba85");
        JButton add = new JButton("\ud488\ubaa9 \ucd94\uac00");
        JPanel badges = new JPanel();
        badges.setLayout(new BoxLayout(badges, 1));
        badges.setOpaque(false);
        JScrollPane badgeScroll = new JScrollPane(badges, 20, 31);
        badgeScroll.setBorder(null);
        badgeScroll.setOpaque(false);
        badgeScroll.getViewport().setOpaque(false);
        badgeScroll.getVerticalScrollBar().setUnitIncrement(12);
        badgeScroll.setAlignmentX(0.0f);
        badgeScroll.setPreferredSize(new Dimension(250, 165));
        badgeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        refresh[0] = () -> {
            badges.removeAll();
            if (this.state.roomRules.isEmpty()) {
                JLabel empty = new JLabel("\ub4f1\ub85d\ub41c \ud488\ubaa9\uc774 \uc5c6\uc5b4\uc694");
                empty.setForeground(MUTED);
                empty.setAlignmentX(0.0f);
                badges.add(empty);
            } else {
                for (Integer amount : this.state.roomRules) {
                    String name = this.state.roomRuleNames.getOrDefault(amount, "\ud488\ubaa9 \ubbf8\uc9c0\uc815");
                    JButton badge = new JButton(amount + "\uac1c \u00b7 " + name + "  \u00d7");
                    badge.putClientProperty("JButton.buttonType", "roundRect");
                    badge.setBackground(new Color(255, 232, 213));
                    badge.setForeground(ACCENT_DARK);
                    badge.setToolTipText(amount + "\uac1c " + name + " \uaddc\uce59 \uc0ad\uc81c");
                    badge.setAlignmentX(0.0f);
                    Main.styleButton(badge);
                    badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                    badge.addActionListener(e -> {
                        this.state.roomRules.remove(amount);
                        this.state.roomRuleNames.remove(amount);
                        refresh[0].run();
                        this.save();
                    });
                    badges.add(badge);
                    badges.add(Box.createVerticalStrut(6));
                }
            }
            badges.revalidate();
            badges.repaint();
        };
        refresh[0].run();
        add.addActionListener(e -> {
            String name = item.getText().trim();
            if (name.isBlank()) {
                this.error("\ud488\ubaa9\uba85\uc744 \uc785\ub825\ud558\uc138\uc694.");
                return;
            }
            this.integer(value, v -> {
                if (!this.state.roomRules.contains(v)) {
                    this.state.roomRules.add(v);
                }
                this.state.roomRuleNames.put(v, name);
                Collections.sort(this.state.roomRules);
                item.setText("");
                refresh[0].run();
                this.save();
            });
        });
        rules.add(Main.section("\uc218\ub7c9 / \ud488\ubaa9\uba85"));
        rules.add(Main.fieldRow(value, item));
        rules.add(Box.createVerticalStrut(8));
        add.setAlignmentX(0.0f);
        rules.add(add);
        rules.add(Box.createVerticalStrut(10));
        rules.add(Main.section("\ub4f1\ub85d\ub41c \ud488\ubaa9"));
        rules.add(badgeScroll);
        rules.add(Box.createVerticalStrut(13));
        rules.add(Main.section("\ub370\uc774\ud130 \ub0b4\ubcf4\ub0b4\uae30"));
        JLabel sheetsHint = new JLabel("\uc218\uc9d1 \ub370\uc774\ud130\ub97c \uc9c0\uc815\ub41c \uc2dc\ud2b8\uc5d0 \uc800\uc7a5\ud574\uc694");
        sheetsHint.setForeground(MUTED);
        sheetsHint.setFont(sheetsHint.getFont().deriveFont(11.0f));
        sheetsHint.setAlignmentX(0.0f);
        rules.add(sheetsHint);
        rules.add(Box.createVerticalStrut(7));
        GradientButton sheetsExport = new GradientButton("\ub0b4\ubcf4\ub0b4\uae30");
        sheetsExport.setAlignmentX(0.0f);
        sheetsExport.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sheetsExport.addActionListener(e -> this.exportRoomToGoogleSheets(sheetsExport, sheetsHint));
        rules.add(sheetsExport);
        return Main.dashboard(rules, this.collectionArea(this.roomList, "room"), null);
    }

    private void exportRoomToGoogleSheets(JButton button, JLabel statusLabel) {
        if (this.state.roomRows.isEmpty()) {
            this.error("\ub0b4\ubcf4\ub0bc \ubc29\uc140 \uc218\uc9d1 \ub370\uc774\ud130\uac00 \uc5c6\uc2b5\ub2c8\ub2e4.");
            return;
        }
        String exportedAt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        List<GoogleSheetsExporter.ExportRow> rows = this.state.roomRows.stream().map(row -> {
            String userId = Objects.toString(row.id, "");
            return new GoogleSheetsExporter.ExportRow(exportedAt, Objects.toString(row.time, ""), Objects.toString(row.source, ""), Objects.toString(row.nick, ""), userId, row.amount, Main.roomItemText(row), Objects.toString(row.message, ""), "https://www.sooplive.com/station/" + userId);
        }).toList();
        String original = button.getText();
        button.setEnabled(false);
        button.setText("\ub0b4\ubcf4\ub0b4\ub294 \uc911...");
        statusLabel.setText("\uad6c\uae00 \uc2dc\ud2b8\uc5d0 \uc800\uc7a5 \uc911...");
        statusLabel.setForeground(ACCENT_DARK);
        GoogleSheetsExporter.export(GOOGLE_SHEETS_WEB_APP_URL, GOOGLE_SHEETS_ACCESS_KEY, rows).whenComplete((result, failure) -> Main.ui(() -> {
            button.setEnabled(true);
            button.setText(original);
            if (failure != null) {
                statusLabel.setText("\ub0b4\ubcf4\ub0b4\uae30 \uc2e4\ud328");
                statusLabel.setForeground(new Color(180, 65, 35));
                this.error("\uad6c\uae00 \uc2dc\ud2b8 \ub0b4\ubcf4\ub0b4\uae30\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4: " + failure.getMessage());
                return;
            }
            statusLabel.setText(result.success() ? "\ucd5c\uadfc \ub0b4\ubcf4\ub0b4\uae30 \uc644\ub8cc" : "\ub0b4\ubcf4\ub0b4\uae30 \uc2e4\ud328");
            statusLabel.setForeground(result.success() ? new Color(29, 138, 78) : new Color(180, 65, 35));
            JOptionPane.showMessageDialog(this, result.message(), result.success() ? "\ub0b4\ubcf4\ub0b4\uae30 \uc644\ub8cc" : "\ub0b4\ubcf4\ub0b4\uae30 \uc2e4\ud328", result.success() ? 1 : 0);
        }));
    }

    private void showRouletteDialog() {
        JDialog dialog = new JDialog((Window)this, "\ubc29\uc140 \ub8f0\ub81b \uc124\uc815", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(2);
        dialog.setIconImages(Main.loadAppIcons());
        JPanel root = Main.page();
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add((Component)this.roulettePanel(), "Center");
        JPanel footer = new JPanel(new FlowLayout(2, 0, 12));
        footer.setOpaque(false);
        GradientButton close = new GradientButton("\uc124\uc815 \uc644\ub8cc");
        close.setPreferredSize(new Dimension(130, 44));
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        root.add((Component)footer, "South");
        dialog.setContentPane(root);
        dialog.setMinimumSize(new Dimension(820, 540));
        dialog.setSize(960, 640);
        dialog.setLocationRelativeTo(this);
        Main.styleControls(dialog.getContentPane());
        dialog.setVisible(true);
    }

    private JComponent roulettePanel() {
        Runnable[] refresh = new Runnable[1];
        Runnable[] refreshTriggers = new Runnable[1];
        RoundedPanel wrap = new RoundedPanel(new BorderLayout(18, 12));
        wrap.setBorder(new EmptyBorder(16, 22, 16, 22));
        wrap.setPreferredSize(new Dimension(900, 500));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, 1));
        JLabel tag = new JLabel("BROWSER SOURCE ROULETTE");
        tag.setForeground(ACCENT);
        tag.setFont(tag.getFont().deriveFont(1, 10.0f));
        JLabel title = new JLabel("\ubc29\uc140 \ub8f0\ub81b");
        title.setFont(title.getFont().deriveFont(1, 19.0f));
        heading.add(tag);
        heading.add(Box.createVerticalStrut(3));
        heading.add(title);
        header.add((Component)heading, "West");
        JCheckBox enabled = new JCheckBox("\ud6c4\uc6d0 \uc77c\uce58 \uc2dc \uc790\ub3d9 \uc2e4\ud589", this.state.rouletteEnabled);
        enabled.setOpaque(false);
        enabled.addActionListener(e -> {
            this.state.rouletteEnabled = enabled.isSelected();
            this.save();
        });
        header.add((Component)enabled, "East");
        wrap.add((Component)header, "North");
        JPanel config = new JPanel();
        config.setOpaque(false);
        config.setLayout(new BoxLayout(config, 1));
        config.setPreferredSize(new Dimension(390, 10));
        JTextField amount = Main.smallField();
        JTextField spins = Main.smallField();
        spins.setText("1");
        amount.putClientProperty("JTextField.placeholderText", "\ud6c4\uc6d0 \uac1c\uc218");
        spins.putClientProperty("JTextField.placeholderText", "\ub3cc\uc544\uac08 \ud69f\uc218");
        JButton saveSettings = new JButton("\uaddc\uce59 \ucd94\uac00");
        JPanel triggerList = new JPanel();
        triggerList.setOpaque(false);
        triggerList.setLayout(new BoxLayout(triggerList, 1));
        refreshTriggers[0] = () -> {
            triggerList.removeAll();
            if (this.state.rouletteTriggers.isEmpty()) {
                JLabel empty = new JLabel("\ub4f1\ub85d\ub41c \uc2e4\ud589 \uaddc\uce59\uc774 \uc5c6\uc5b4\uc694");
                empty.setForeground(MUTED);
                empty.setBorder(new EmptyBorder(7, 2, 7, 2));
                triggerList.add(empty);
            } else {
                for (RouletteTrigger trigger : this.state.rouletteTriggers) {
                    JPanel row = new JPanel(new BorderLayout(8, 0));
                    row.setOpaque(false);
                    row.setBorder(new EmptyBorder(3, 2, 3, 2));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                    SoftBadge label = new SoftBadge(trigger.amount + "\uac1c \ud6c4\uc6d0 \u00b7 " + trigger.spins + "\ud68c", new Color(255, 232, 213), ACCENT_DARK);
                    JButton remove = new JButton("\u00d7");
                    remove.setForeground(MUTED);
                    remove.setToolTipText("\uc774 \ub8f0\ub81b \uc2e4\ud589 \uaddc\uce59 \uc0ad\uc81c");
                    Main.styleBareButton(remove);
                    remove.addActionListener(e -> {
                        this.state.rouletteTriggers.remove(trigger);
                        this.save();
                        refreshTriggers[0].run();
                    });
                    row.add((Component)label, "Center");
                    row.add((Component)remove, "East");
                    triggerList.add(row);
                }
            }
            triggerList.revalidate();
            triggerList.repaint();
        };
        refreshTriggers[0].run();
        saveSettings.addActionListener(e -> {
            try {
                int a = Integer.parseInt(amount.getText().trim());
                int s = Integer.parseInt(spins.getText().trim());
                if (a <= 0 || s <= 0 || s > 20) {
                    throw new NumberFormatException();
                }
                this.state.rouletteTriggers.removeIf(trigger -> trigger.amount == a);
                this.state.rouletteTriggers.add(new RouletteTrigger(a, s));
                this.state.rouletteTriggers.sort(Comparator.comparingInt(trigger -> trigger.amount));
                amount.setText("");
                spins.setText("1");
                this.save();
                refreshTriggers[0].run();
            }
            catch (NumberFormatException ex) {
                this.error("\ud6c4\uc6d0 \uac1c\uc218\ub294 \uc591\uc218, \ub3cc\uc544\uac08 \ud69f\uc218\ub294 1~20\uc73c\ub85c \uc785\ub825\ud558\uc138\uc694.");
            }
        });
        config.add(Main.section("\ud6c4\uc6d0 \uac1c\uc218 / \ub3cc\uc544\uac08 \ud69f\uc218"));
        config.add(Main.fieldRow(amount, spins, saveSettings));
        config.add(Box.createVerticalStrut(7));
        JScrollPane triggerScroll = new JScrollPane(triggerList, 20, 31);
        triggerScroll.setBorder(null);
        triggerScroll.setOpaque(false);
        triggerScroll.getViewport().setOpaque(false);
        triggerScroll.getVerticalScrollBar().setUnitIncrement(12);
        triggerScroll.setAlignmentX(0.0f);
        triggerScroll.setPreferredSize(new Dimension(390, 110));
        triggerScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        config.add(triggerScroll);
        config.add(Box.createVerticalStrut(10));
        config.add(Main.section("OBS \ube0c\ub77c\uc6b0\uc800 \uc18c\uc2a4 \uc8fc\uc18c"));
        JTextField url = new JTextField(this.rouletteOverlay == null ? "\uc624\ubc84\ub808\uc774 \uc11c\ubc84 \uc2dc\uc791 \uc2e4\ud328" : this.rouletteOverlay.url());
        url.setEditable(false);
        url.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        url.setAlignmentX(0.0f);
        config.add(url);
        config.add(Box.createVerticalStrut(5));
        JLabel overlaySize = new JLabel("OBS \uad8c\uc7a5 \ud06c\uae30  \u00b7  \ub108\ube44 800 \u00d7 \ub192\uc774 600");
        overlaySize.setForeground(MUTED);
        overlaySize.setFont(overlaySize.getFont().deriveFont(11.0f));
        overlaySize.setAlignmentX(0.0f);
        config.add(overlaySize);
        config.add(Box.createVerticalStrut(8));
        JPanel sourceActions = new JPanel(new FlowLayout(0, 7, 0));
        sourceActions.setOpaque(false);
        sourceActions.setAlignmentX(0.0f);
        JButton copy = new JButton("\uc8fc\uc18c \ubcf5\uc0ac");
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url.getText()), null));
        JButton test = new JButton("\uc2ac\ub86f \ud14c\uc2a4\ud2b8");
        test.setBackground(new Color(230, 248, 236));
        test.addActionListener(e -> {
            int testSpins;
            int n = testSpins = this.state.rouletteTriggers.isEmpty() ? 1 : this.state.rouletteTriggers.get((int)0).spins;
            if (!this.queueRoulette("\ud14c\uc2a4\ud2b8 \ud6c4\uc6d0\uc790", "", "\uc218\ub3d9 \ud14c\uc2a4\ud2b8", 0, testSpins, false, 0L).queued()) {
                this.error("\ud655\ub960\uc774 0\ubcf4\ub2e4 \ud070 \ub8f0\ub81b \ud56d\ubaa9\uc744 \uba3c\uc800 \ub4f1\ub85d\ud558\uc138\uc694.");
            }
        });
        sourceActions.add(copy);
        sourceActions.add(test);
        config.add(sourceActions);
        wrap.add((Component)config, "West");
        JPanel rulesArea = new JPanel(new BorderLayout(0, 7));
        rulesArea.setOpaque(false);
        JPanel rulesHeader = new JPanel(new BorderLayout());
        rulesHeader.setOpaque(false);
        JLabel rulesTitle = new JLabel("\ub8f0\ub81b \uac12 / \ud655\ub960 / \ub2f9\ucca8 \uc218\uc9d1");
        rulesTitle.setFont(rulesTitle.getFont().deriveFont(1, 13.0f));
        JLabel totalLabel = new JLabel();
        totalLabel.setForeground(ACCENT_DARK);
        rulesHeader.add((Component)rulesTitle, "West");
        rulesHeader.add((Component)totalLabel, "East");
        rulesArea.add((Component)rulesHeader, "North");
        JPanel ruleList = new JPanel();
        ruleList.setOpaque(false);
        ruleList.setLayout(new BoxLayout(ruleList, 1));
        refresh[0] = () -> {
            ruleList.removeAll();
            double total = 0.0;
            for (final RouletteRule rule : this.state.rouletteRules) {
                total += Math.max(0.0, rule.probability);
                JPanel row = new JPanel(new BorderLayout(7, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(3, 0, 3, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
                final JTextField textField = new JTextField(rule.text == null ? "" : rule.text);
                textField.putClientProperty("JTextField.placeholderText", "\ub8f0\ub81b \ud14d\uc2a4\ud2b8");
                Main.styleInput(textField);
                JTextField probability = new JTextField(Main.trim(rule.probability));
                probability.setHorizontalAlignment(4);
                probability.setPreferredSize(new Dimension(74, 34));
                Main.styleInput(probability);
                final Runnable commit = () -> {
                    rule.text = textField.getText().trim();
                    try {
                        double value = Double.parseDouble(probability.getText().trim());
                        rule.probability = Math.max(0.0, value);
                        this.save();
                        refresh[0].run();
                    }
                    catch (NumberFormatException ex) {
                        probability.setText(Main.trim(rule.probability));
                    }
                };
                textField.addActionListener(e -> {
                    rule.text = textField.getText().trim();
                    this.save();
                });
                textField.addFocusListener(new FocusAdapter(){

                    @Override
                    public void focusLost(FocusEvent e) {
                        rule.text = textField.getText().trim();
                        Main.this.save();
                    }
                });
                probability.addActionListener(e -> commit.run());
                probability.addFocusListener(new FocusAdapter(){

                    @Override
                    public void focusLost(FocusEvent e) {
                        commit.run();
                    }
                });
                JPanel probabilityWrap = new JPanel(new BorderLayout(4, 0));
                probabilityWrap.setOpaque(false);
                probabilityWrap.add((Component)probability, "Center");
                probabilityWrap.add((Component)new JLabel("%"), "East");
                JCheckBox collect = new JCheckBox("\ub2f9\ucca8 \uc218\uc9d1", rule.collect);
                collect.setOpaque(false);
                collect.setToolTipText("\uccb4\ud06c\ud55c \uacb0\uacfc\uac00 \ub098\uc624\uba74 \ubc29\uc140 \uc218\uc9d1 \ubaa9\ub85d\uc5d0 \ucd94\uac00\ud569\ub2c8\ub2e4");
                collect.addActionListener(e -> {
                    rule.collect = collect.isSelected();
                    this.save();
                });
                Main.styleButton(collect);
                JPanel options = new JPanel(new FlowLayout(2, 8, 0));
                options.setOpaque(false);
                options.add(probabilityWrap);
                options.add(collect);
                JButton remove = new JButton("\u00d7");
                remove.setForeground(MUTED);
                Main.styleBareButton(remove);
                remove.addActionListener(e -> {
                    this.state.rouletteRules.remove(rule);
                    this.save();
                    refresh[0].run();
                });
                row.add((Component)textField, "Center");
                row.add((Component)options, "East");
                row.add((Component)remove, "West");
                ruleList.add(row);
            }
            totalLabel.setText("\ud569\uacc4 " + Main.trim(total) + "%");
            totalLabel.setForeground(Math.abs(total - 100.0) < 0.001 ? new Color(36, 139, 80) : ACCENT_DARK);
            ruleList.revalidate();
            ruleList.repaint();
        };
        refresh[0].run();
        JScrollPane ruleScroll = new JScrollPane(ruleList);
        ruleScroll.setBorder(null);
        ruleScroll.setOpaque(false);
        ruleScroll.getViewport().setOpaque(false);
        ruleScroll.getVerticalScrollBar().setUnitIncrement(12);
        rulesArea.add((Component)ruleScroll, "Center");
        JButton addRule = new JButton("+ \ud56d\ubaa9 \ucd94\uac00");
        addRule.addActionListener(e -> {
            this.state.rouletteRules.add(new RouletteRule("\uc0c8 \ud56d\ubaa9", 0.0));
            this.save();
            refresh[0].run();
        });
        rulesArea.add((Component)addRule, "South");
        wrap.add((Component)rulesArea, "Center");
        return wrap;
    }

    private JComponent reverseTab() {
        JPanel rules = Main.settingsCard("\uc5ed\ud32c \uc124\uc815", "\uad6c\uac04\ubcc4 \uacc4\uc0b0 \ube44\uc728\uc744 \ub4f1\ub85d\ud574\uc694");
        rules.add(Main.section("\uc218\uc9d1\ud560 \ud6c4\uc6d0"));
        this.addSourceSelectors(rules, "reverse");
        rules.add(Box.createVerticalStrut(14));
        JTextField min = Main.smallField();
        JTextField percent = Main.smallField();
        JComboBox existing = new JComboBox();
        Runnable refresh = () -> {
            existing.removeAllItems();
            this.state.reverseRules.stream().sorted(Comparator.comparingInt(r -> r.minimum)).forEach(r -> existing.addItem(r.minimum + "\uac1c \uc774\uc0c1 / " + Main.trim(r.percent) + "%"));
        };
        refresh.run();
        JButton add = new JButton("\uaddc\uce59 \ucd94\uac00");
        add.addActionListener(e -> this.integer(min, v -> this.decimal(percent, pct -> {
            this.state.reverseRules.removeIf(r -> r.minimum == v);
            this.state.reverseRules.add(new ReverseRule(v, pct));
            refresh.run();
            this.save();
        })));
        JButton del = new JButton("\uc120\ud0dd \uc0ad\uc81c");
        del.addActionListener(e -> {
            int i = existing.getSelectedIndex();
            if (i >= 0) {
                List<ReverseRule> sorted = this.state.reverseRules.stream().sorted(Comparator.comparingInt(r -> r.minimum)).toList();
                this.state.reverseRules.remove(sorted.get(i));
                refresh.run();
                this.save();
            }
        });
        add.setAlignmentX(0.0f);
        del.setAlignmentX(0.0f);
        existing.setAlignmentX(0.0f);
        existing.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        rules.add(Main.section("\ucd5c\uc18c \uc218\ub7c9 / \ud37c\uc13c\ud2b8"));
        rules.add(Main.fieldRow(min, percent));
        rules.add(Box.createVerticalStrut(8));
        rules.add(add);
        rules.add(Box.createVerticalStrut(14));
        rules.add(Main.section("\ub4f1\ub85d\ub41c \uaddc\uce59"));
        rules.add(existing);
        rules.add(Box.createVerticalStrut(8));
        rules.add(del);
        return Main.dashboard(rules, this.collectionArea(this.reverseList, "reverse"), null);
    }

    private JComponent pinballTab() {
        JPanel top = Main.settingsCard("\ud540\ubcfc \uc124\uc815", "\uae30\uc900 \uc218\ub7c9\ub9c8\ub2e4 \uacf5\uc744 \ud558\ub098\uc529 \uacc4\uc0b0\ud574\uc694");
        JPanel donationSettings = new JPanel();
        donationSettings.setOpaque(false);
        donationSettings.setLayout(new BoxLayout(donationSettings, BoxLayout.Y_AXIS));
        donationSettings.setAlignmentX(0.0f);
        JTextField unit = Main.smallField();
        donationSettings.add(Main.section("\uc218\uc9d1\ud560 \ud6c4\uc6d0"));
        this.addSourceSelectors(donationSettings, "pin");
        donationSettings.add(Box.createVerticalStrut(14));
        unit.setText(this.state.pinballUnit > 0 ? String.valueOf(this.state.pinballUnit) : "");
        JLabel current = new JLabel(this.pinText());
        JButton saveRule = new JButton("\uaddc\uce59 \uc800\uc7a5");
        saveRule.addActionListener(e -> this.integer(unit, v -> {
            this.state.pinballUnit = v;
            current.setText(this.pinText());
            this.save();
        }));
        GradientButton copyAll = new GradientButton("\uc218\uc9d1\ud55c \uacf5 \uc804\uccb4 \ubcf5\uc0ac");
        copyAll.setPreferredSize(new Dimension(210, 54));
        copyAll.addActionListener(e -> this.copyBallSummary());
        donationSettings.add(Main.section("\uacf5 1\uac1c\uc758 \uae30\uc900 \uc218\ub7c9"));
        donationSettings.add(Main.fieldRow(unit, saveRule));
        donationSettings.add(Box.createVerticalStrut(12));
        current.setForeground(ACCENT_DARK);
        current.setAlignmentX(0.0f);
        current.setMaximumSize(new Dimension(Integer.MAX_VALUE, current.getPreferredSize().height));
        donationSettings.add(current);

        JPanel chatSettings = new JPanel();
        chatSettings.setOpaque(false);
        chatSettings.setLayout(new BoxLayout(chatSettings, BoxLayout.Y_AXIS));
        chatSettings.setAlignmentX(0.0f);
        JToggleButton collectChat = new JToggleButton(this.state.pinChatEnabled ? "\uc77c\ubc18 \ucc44\ud305 \uc218\uc9d1 ON" : "\uc77c\ubc18 \ucc44\ud305 \uc218\uc9d1 OFF");
        collectChat.setSelected(this.state.pinChatEnabled);
        collectChat.setAlignmentX(0.0f);
        collectChat.setPreferredSize(new Dimension(170, 38));
        collectChat.setMaximumSize(new Dimension(170, 38));
        ToggleSwitch duplicateChat = new ToggleSwitch("\uc911\ubcf5 \uc218\uc9d1");
        duplicateChat.setSelected(this.state.pinChatDuplicateEnabled);
        duplicateChat.setEnabled(this.state.pinChatEnabled);
        duplicateChat.addActionListener(e -> {
            this.state.pinChatDuplicateEnabled = duplicateChat.isSelected();
            this.save();
        });
        collectChat.addActionListener(e -> {
            this.state.pinChatEnabled = collectChat.isSelected();
            collectChat.setText(this.state.pinChatEnabled ? "\uc77c\ubc18 \ucc44\ud305 \uc218\uc9d1 ON" : "\uc77c\ubc18 \ucc44\ud305 \uc218\uc9d1 OFF");
            duplicateChat.setEnabled(this.state.pinChatEnabled);
            this.save();
        });
        chatSettings.add(Main.section("\uc77c\ubc18 \ucc44\ud305 \uc218\uc9d1"));
        chatSettings.add(collectChat);
        chatSettings.add(Box.createVerticalStrut(18));
        chatSettings.add(Main.section("\uc218\uc9d1\ud560 \ucc44\ud305"));
        this.addChatViewerSelectors(chatSettings);
        chatSettings.add(Box.createVerticalStrut(14));
        JPanel duplicateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        duplicateRow.setOpaque(false);
        duplicateRow.setAlignmentX(0.0f);
        duplicateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel duplicateLabel = new JLabel("\uc911\ubcf5 \uc218\uc9d1");
        duplicateLabel.setForeground(TEXT);
        duplicateRow.add(duplicateLabel);
        duplicateRow.add(duplicateChat);
        chatSettings.add(duplicateRow);

        CardLayout settingsLayout = new CardLayout();
        JPanel settingsCards = new JPanel(settingsLayout);
        settingsCards.setOpaque(false);
        settingsCards.setAlignmentX(0.0f);
        settingsCards.add(donationSettings, "donation");
        settingsCards.add(chatSettings, "chat");
        JPanel settingsTabs = new JPanel(new GridLayout(1, 2, 8, 0));
        settingsTabs.setOpaque(false);
        settingsTabs.setAlignmentX(0.0f);
        settingsTabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JToggleButton donationTab = new JToggleButton("\ud6c4\uc6d0");
        JToggleButton chatTab = new JToggleButton("\uc77c\ubc18 \ucc44\ud305");
        ButtonGroup settingsGroup = new ButtonGroup();
        settingsGroup.add(donationTab);
        settingsGroup.add(chatTab);
        donationTab.setSelected(true);
        donationTab.addActionListener(e -> settingsLayout.show(settingsCards, "donation"));
        chatTab.addActionListener(e -> settingsLayout.show(settingsCards, "chat"));
        settingsTabs.add(donationTab);
        settingsTabs.add(chatTab);
        top.add(settingsTabs);
        top.add(Box.createVerticalStrut(16));
        top.add(settingsCards);
        this.ballSummary.setLayout(new BoxLayout(this.ballSummary, 1));
        this.ballSummary.setOpaque(false);
        this.refreshBallSummary();
        RoundedPanel bottom = new RoundedPanel(new BorderLayout(18, 0));
        bottom.setBorder(new EmptyBorder(18, 24, 18, 24));
        bottom.add((Component)this.ballSummary, "Center");
        bottom.add((Component)copyAll, "East");
        return Main.dashboard(top, this.collectionArea(this.pinList, "pin"), bottom);
    }

    private JComponent nashiBallTab() {
        try {
            Class<?> panelClass = Class.forName("app.soopcollector.NashiBallBrowserPanel");
            Supplier<String> entries = this::nashiBallEntries;
            this.nashiBallPanel = (JComponent)panelClass.getConstructor(Supplier.class).newInstance(entries);
            return this.nashiBallPanel;
        } catch (ReflectiveOperationException ex) {
            JPanel unavailable = Main.page();
            JLabel message = new JLabel("\ub098\uc2dc\ubcfc \ubaa8\ub4c8\uc744 \ubd88\ub7ec\uc624\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.", 0);
            message.setForeground(MUTED);
            unavailable.add(message, BorderLayout.CENTER);
            return unavailable;
        }
    }

    private String nashiBallEntries() {
        if (!SwingUtilities.isEventDispatchThread()) {
            CompletableFuture<String> result = new CompletableFuture<String>();
            SwingUtilities.invokeLater(() -> result.complete(this.nashiBallEntries()));
            return result.join();
        }
        return this.state.gameBalls.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .map(e -> e.getKey() + "*" + e.getValue())
                .reduce((a, b) -> a + "\n" + b).orElse("");
    }

    private JComponent tableArea(JTable table, boolean actions) {
        RoundedPanel wrap = new RoundedPanel(new BorderLayout(0, 12));
        wrap.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, 1));
        JLabel eyebrow = new JLabel("LIVE COLLECTION");
        eyebrow.setForeground(ACCENT);
        eyebrow.setFont(eyebrow.getFont().deriveFont(1, 10.0f));
        JLabel title = new JLabel("\uc218\uc9d1\ub41c \ud6c4\uc6d0 \ubaa9\ub85d");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(1, 20.0f));
        words.add(eyebrow);
        words.add(Box.createVerticalStrut(3));
        words.add(title);
        heading.add((Component)words, "West");
        if (actions) {
            JPanel b = new JPanel(new FlowLayout(2, 8, 0));
            b.setOpaque(false);
            JButton clear = new JButton("\ubaa8\ub450 \uc9c0\uc6b0\uae30");
            clear.addActionListener(e -> this.clearTable(table));
            JButton test = new JButton("\uc218\ub3d9 \ud14c\uc2a4\ud2b8");
            test.addActionListener(e -> this.testDialog());
            test.setBackground(new Color(255, 237, 220));
            clear.setBackground(Color.WHITE);
            b.add(test);
            b.add(clear);
            heading.add((Component)b, "East");
        }
        wrap.add((Component)heading, "North");
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(245, 170, 121)));
        wrap.add((Component)scroll, "Center");
        return wrap;
    }

    private JComponent collectionArea(JPanel list, String type) {
        RoundedPanel wrap = new RoundedPanel(new BorderLayout(0, 12));
        wrap.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, 1));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.setAlignmentX(0.0f);
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, 1));
        JLabel eyebrow = new JLabel("LIVE COLLECTION");
        eyebrow.setForeground(ACCENT);
        eyebrow.setFont(eyebrow.getFont().deriveFont(1, 10.0f));
        JLabel title = new JLabel(type.equals("pin") ? "\uc218\uc9d1\ub41c \ud540\ubcfc \ubaa9\ub85d" : "\uc218\uc9d1\ub41c \ud6c4\uc6d0 \ubaa9\ub85d");
        title.setFont(title.getFont().deriveFont(1, 20.0f));
        JTextField search = this.searchField(type);
        words.add(eyebrow);
        words.add(Box.createVerticalStrut(3));
        words.add(title);
        words.add(Box.createVerticalStrut(10));
        words.add(search);
        heading.add((Component)words, "West");
        JPanel actions = new JPanel(new FlowLayout(2, 8, 0));
        actions.setOpaque(false);
        JButton test = new JButton("\uc218\ub3d9 \ud14c\uc2a4\ud2b8");
        test.setBackground(new Color(232, 248, 237));
        test.addActionListener(e -> this.testDialog());
        JButton clear = new JButton("\ubaa8\ub450 \uc9c0\uc6b0\uae30");
        clear.addActionListener(e -> this.clearArea(type));
        if (type.equals("room")) {
            JButton roulette = new JButton("\ub8f0\ub81b \uc124\uc815");
            roulette.setBackground(ACCENT);
            roulette.setForeground(Color.WHITE);
            roulette.setFont(roulette.getFont().deriveFont(1, 13.0f));
            roulette.addActionListener(e -> this.showRouletteDialog());
            actions.add(roulette);
        }
        actions.add(test);
        actions.add(clear);
        heading.add((Component)actions, "East");
        top.add(heading);
        if (type.equals("room")) {
            this.roomSummary.setOpaque(false);
            JScrollPane totals = new JScrollPane(this.roomSummary, 21, 30);
            totals.setBorder(null);
            totals.setOpaque(false);
            totals.getViewport().setOpaque(false);
            totals.getHorizontalScrollBar().setUnitIncrement(14);
            totals.setAlignmentX(0.0f);
            totals.setPreferredSize(new Dimension(10, 53));
            totals.setMaximumSize(new Dimension(Integer.MAX_VALUE, 53));
            top.add(Box.createVerticalStrut(10));
            top.add(totals);
        }
        wrap.add((Component)top, "North");
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 221, 197)));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        wrap.add((Component)scroll, "Center");
        return wrap;
    }

    private JTextField searchField(final String type) {
        JTextField field = type.equals("room") ? this.roomSearch : (type.equals("reverse") ? this.reverseSearch : this.pinSearch);
        field.putClientProperty("JTextField.placeholderText", "\uc218\ub7c9, \ud488\ubaa9 \ub610\ub294 \uba54\uc2dc\uc9c0 \uac80\uc0c9");
        field.setPreferredSize(new Dimension(360, 36));
        field.setMaximumSize(new Dimension(360, 36));
        field.setAlignmentX(0.0f);
        if (!Boolean.TRUE.equals(field.getClientProperty("kanashi.search.ready"))) {
            field.putClientProperty("kanashi.search.ready", true);
            field.getDocument().addDocumentListener(new DocumentListener(){

                @Override
                public void insertUpdate(DocumentEvent e) {
                    this.refresh();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    this.refresh();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    this.refresh();
                }

                private void refresh() {
                    if (type.equals("room")) {
                        Main.this.renderRoomList();
                    } else if (type.equals("reverse")) {
                        Main.this.renderReverseList();
                    } else {
                        Main.this.renderPinList();
                    }
                }
            });
        }
        return field;
    }

    private static JPanel collectionList() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, 1));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(8, 4, 8, 4));
        return p;
    }

    private void refreshCollectionLists() {
        this.renderRoomList();
        this.renderReverseList();
        this.renderPinList();
    }

    private void renderRoomList() {
        this.roomList.removeAll();
        this.refreshRoomSummary();
        boolean shown = false;
        for (int i = 0; i < this.state.roomRows.size(); ++i) {
            int index = i;
            RoomRow r = this.state.roomRows.get(i);
            if (!Main.matches(this.roomSearch, r.nick, r.id, r.source, String.valueOf(r.amount), Main.roomItemText(r), r.message)) continue;
            shown = true;
            this.roomList.add(this.donorEntryRow(r.nick, r.id, r.message, Main.roomItemText(r), text -> {
                r.message = text;
                this.save();
            }, r.id, () -> this.deleteEntry("room", index)));
            this.roomList.add(Box.createVerticalStrut(8));
        }
        if (!shown) {
            Main.addListPlaceholder(this.roomList, this.state.roomRows.isEmpty() ? "\uc544\uc9c1 \uc218\uc9d1\ub41c \ud6c4\uc6d0\uc774 \uc5c6\uc5b4\uc694" : "\uac80\uc0c9 \uacb0\uacfc\uac00 \uc5c6\uc5b4\uc694");
        }
        Main.finishList(this.roomList);
    }

    private void renderReverseList() {
        this.reverseList.removeAll();
        boolean shown = false;
        for (int i = 0; i < this.state.reverseRows.size(); ++i) {
            int index = i;
            ReverseRow r = this.state.reverseRows.get(i);
            if (!Main.matches(this.reverseSearch, r.nick, r.id, r.source, String.valueOf(r.amount), Main.trim(r.percent), String.valueOf(r.calculated), r.message)) continue;
            shown = true;
            String donationInfo = r.amount + "\uac1c \u00b7 " + Main.trim(r.percent) + "% \u00b7 " + r.calculated + "\uac1c \uacc4\uc0b0";
            this.reverseList.add(this.donorEntryRow(r.nick, r.id, r.message, donationInfo, text -> {
                r.message = text;
                this.save();
            }, r.id, () -> this.deleteEntry("reverse", index)));
            this.reverseList.add(Box.createVerticalStrut(8));
        }
        if (!shown) {
            Main.addListPlaceholder(this.reverseList, this.state.reverseRows.isEmpty() ? "\uc544\uc9c1 \uc218\uc9d1\ub41c \ud6c4\uc6d0\uc774 \uc5c6\uc5b4\uc694" : "\uac80\uc0c9 \uacb0\uacfc\uac00 \uc5c6\uc5b4\uc694");
        }
        Main.finishList(this.reverseList);
    }

    private void renderPinList() {
        this.pinList.removeAll();
        boolean shown = false;
        for (int i = 0; i < this.state.pinRows.size(); ++i) {
            int index = i;
            PinRow r = this.state.pinRows.get(i);
            if (!Main.matches(this.pinSearch, r.nick, r.id, r.source, String.valueOf(r.amount), String.valueOf(r.balls), r.game)) continue;
            shown = true;
            this.renderPinListRow(i, index, r);
        }
        if (!shown) {
            Main.addListPlaceholder(this.pinList, this.state.pinRows.isEmpty() ? "\uc544\uc9c1 \uc218\uc9d1\ub41c \ud6c4\uc6d0\uc774\ub098 \ucc44\ud305\uc774 \uc5c6\uc5b4\uc694" : "\uac80\uc0c9 \uacb0\uacfc\uac00 \uc5c6\uc5b4\uc694");
        }
        Main.finishList(this.pinList);
    }

    private void renderPinListRow(int i, int index, PinRow r) {
        String detail = r.amount > 0 ? r.source + " \u00b7 \ud6c4\uc6d0 " + r.amount + "\uac1c" : r.source;
        this.pinList.add(this.entryRow(i + 1, Main.safe(r.game), Main.safe(r.nick) + " \u00b7 " + r.id, detail, r.balls + "\uacf5", r.id, null, () -> this.deleteEntry("pin", index), true));
    }

    private JComponent donorEntryRow(String nick, String id, String message, String donationInfo, Consumer<String> messageUpdate, String stationId, Runnable remove) {
        List<String> messages = Main.messageParts(message);
        int height = 82 + Math.max(1, messages.size()) * 46;
        DonorCardPanel card = new DonorCardPanel(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(13, 16, 13, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        card.setPreferredSize(new Dimension(10, height));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, 0));
        JLabel name = new JLabel(nick == null || nick.isBlank() ? "\uc774\ub984 \uc5c6\uc74c" : nick);
        name.setFont(name.getFont().deriveFont(1, 16.0f));
        JLabel userId = new JLabel(" (" + id + ")");
        userId.setForeground(new Color(74, 62, 56));
        userId.setFont(userId.getFont().deriveFont(1, 13.0f));
        identity.add(name);
        identity.add(userId);
        header.add((Component)identity, "West");
        JPanel actions = new JPanel(new FlowLayout(2, 7, 0));
        actions.setOpaque(false);
        JButton station = new JButton("\ubc29\uc1a1\uad6d \u2197");
        station.setBackground(new Color(255, 235, 216));
        station.setForeground(ACCENT_DARK);
        Main.styleButton(station);
        station.addActionListener(e -> this.openStation(stationId));
        JButton del = new JButton("\u00d7");
        del.setForeground(MUTED);
        Main.styleBareButton(del);
        del.setToolTipText("\uc774 \ud56d\ubaa9 \uc0ad\uc81c");
        del.addActionListener(e -> remove.run());
        actions.add(station);
        actions.add(del);
        header.add((Component)actions, "East");
        card.add((Component)header, "North");
        JPanel view = new JPanel();
        view.setOpaque(false);
        view.setLayout(new BoxLayout(view, 1));
        for (int i = 0; i < messages.size(); ++i) {
            int messageIndex = i;
            JPanel messageRow = new JPanel(new BorderLayout(12, 0));
            messageRow.setOpaque(false);
            messageRow.setBorder(new EmptyBorder(5, 2, 5, 2));
            JLabel number = new JLabel(i + 1 + ".");
            number.setForeground(new Color(87, 69, 59));
            number.setFont(number.getFont().deriveFont(14.0f));
            number.setBorder(new EmptyBorder(0, 0, 0, 8));
            JPanel messageEditor = new JPanel(new CardLayout());
            messageEditor.setOpaque(false);
            JLabel line = new JLabel(messages.get(i));
            line.setForeground(new Color(87, 69, 59));
            line.setFont(line.getFont().deriveFont(14.0f));
            JTextField inlineEditor = new JTextField(messages.get(i));
            inlineEditor.setFont(inlineEditor.getFont().deriveFont(14.0f));
            Main.styleInput(inlineEditor);
            messageEditor.add((Component)line, "view");
            messageEditor.add((Component)inlineEditor, "edit");
            JPanel messageText = new JPanel(new BorderLayout());
            messageText.setOpaque(false);
            messageText.add((Component)number, "West");
            messageText.add((Component)messageEditor, "Center");
            messageRow.add((Component)messageText, "Center");
            JPanel rowActions = new JPanel(new FlowLayout(2, 7, 0));
            rowActions.setOpaque(false);
            JButton edit = new JButton(new FlatSVGIcon("icons/edit-pen.svg", 18, 18));
            edit.setToolTipText("\uc774 \uba54\uc2dc\uc9c0 \uc218\uc815");
            Main.styleBareButton(edit);
            CardLayout editorMode = (CardLayout)messageEditor.getLayout();
            boolean[] editing = new boolean[]{false};
            Runnable commit = () -> {
                ArrayList<String> changed = new ArrayList<String>(messages);
                String trimmed = inlineEditor.getText().trim();
                if (trimmed.isBlank()) {
                    changed.remove(messageIndex);
                } else {
                    changed.set(messageIndex, trimmed);
                }
                messageUpdate.accept(changed.isEmpty() ? "" : String.join((CharSequence)" / ", changed));
                this.refreshCollectionLists();
            };
            edit.addActionListener(e -> {
                if (!editing[0]) {
                    editing[0] = true;
                    editorMode.show(messageEditor, "edit");
                    edit.setIcon(new FlatSVGIcon("icons/save-check.svg", 18, 18));
                    edit.setToolTipText("\uc218\uc815 \ub0b4\uc6a9 \uc800\uc7a5");
                    inlineEditor.requestFocusInWindow();
                    inlineEditor.selectAll();
                } else {
                    commit.run();
                }
            });
            inlineEditor.addActionListener(e -> commit.run());
            rowActions.add(edit);
            rowActions.add(new LargeDonationBadge(donationInfo));
            messageRow.add((Component)rowActions, "East");
            messageRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            view.add(messageRow);
        }
        card.add((Component)view, "Center");
        return card;
    }

    private JComponent entryRow(int number, String title, String idLine, String detail, String metric, String id, JComponent extra, Runnable remove, boolean showNumber) {
        ListRowPanel row = new ListRowPanel(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(10, 8, 10, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        row.setPreferredSize(new Dimension(10, 88));
        if (showNumber) {
            row.add((Component)new NumberBadge(number), "West");
        }
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, 1));
        JLabel name = new JLabel(title == null || title.isBlank() ? "\uc774\ub984 \uc5c6\uc74c" : title);
        name.setFont(name.getFont().deriveFont(1, 17.0f));
        if (showNumber) {
            JLabel userId = new JLabel(idLine);
            userId.setForeground(new Color(118, 105, 98));
            userId.setFont(userId.getFont().deriveFont(11.0f));
            JLabel meta = new JLabel(detail);
            meta.setForeground(MUTED);
            meta.setFont(meta.getFont().deriveFont(11.0f));
            words.add(name);
            words.add(Box.createVerticalStrut(3));
            words.add(userId);
            words.add(Box.createVerticalStrut(4));
            words.add(meta);
        } else {
            JPanel headline = new JPanel();
            headline.setOpaque(false);
            headline.setLayout(new BoxLayout(headline, 0));
            headline.setAlignmentX(0.0f);
            JLabel info = new JLabel(detail);
            info.setForeground(new Color(105, 78, 62));
            info.setFont(info.getFont().deriveFont(0, 17.0f));
            headline.add(name);
            headline.add(Box.createHorizontalStrut(14));
            headline.add(info);
            headline.add(Box.createHorizontalGlue());
            JLabel userId = new JLabel(idLine);
            userId.setForeground(MUTED);
            userId.setFont(userId.getFont().deriveFont(11.0f));
            words.add(headline);
            words.add(Box.createVerticalStrut(7));
            words.add(userId);
        }
        row.add((Component)words, "Center");
        JPanel buttons = new JPanel(new FlowLayout(2, 7, 9));
        buttons.setOpaque(false);
        if (metric != null) {
            SoftBadge badge = new SoftBadge(metric, new Color(242, 237, 255), new Color(105, 82, 178));
            buttons.add(badge);
        }
        if (extra != null) {
            if (extra instanceof JTextField) {
                JTextField f = (JTextField)extra;
                Main.styleInput(f);
            }
            buttons.add(extra);
        }
        JButton station = new JButton("\ubc29\uc1a1\uad6d");
        station.setBackground(new Color(255, 235, 216));
        station.setForeground(ACCENT_DARK);
        Main.styleButton(station);
        station.addActionListener(e -> this.openStation(id));
        JButton del = new JButton("\u00d7");
        del.setBackground(new Color(248, 245, 242));
        del.setForeground(MUTED);
        Main.styleButton(del);
        del.setToolTipText("\uc774 \ud56d\ubaa9 \uc0ad\uc81c");
        del.addActionListener(e -> remove.run());
        buttons.add(station);
        buttons.add(del);
        row.add((Component)buttons, "East");
        return row;
    }

    private static void addListPlaceholder(JPanel list, String text) {
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setLayout(new BoxLayout(empty, 1));
        empty.setBorder(new EmptyBorder(95, 20, 95, 20));
        JLabel title = new JLabel(text);
        title.setFont(title.getFont().deriveFont(1, 16.0f));
        title.setAlignmentX(0.5f);
        JLabel sub = new JLabel(text.startsWith("\uac80\uc0c9") ? "\ub2e4\ub978 \uc218\ub7c9, \ud488\ubaa9 \ub610\ub294 \uba54\uc2dc\uc9c0\ub85c \uac80\uc0c9\ud574\ubcf4\uc138\uc694" : "\uc870\uac74\uc5d0 \ub9de\ub294 \ud6c4\uc6d0\uc774 \ub4e4\uc5b4\uc624\uba74 \uc5ec\uae30\uc5d0 \ud45c\uc2dc\ub3fc\uc694");
        sub.setForeground(MUTED);
        sub.setAlignmentX(0.5f);
        empty.add(title);
        empty.add(Box.createVerticalStrut(7));
        empty.add(sub);
        list.add(empty);
    }

    private static void finishList(JPanel list) {
        list.add(Box.createVerticalGlue());
        list.revalidate();
        list.repaint();
    }

    private void deleteEntry(String type, int index) {
        if (type.equals("room") && index < this.state.roomRows.size()) {
            this.state.roomRows.remove(index);
        } else if (type.equals("reverse") && index < this.state.reverseRows.size()) {
            this.state.reverseRows.remove(index);
        } else if (type.equals("pin") && index < this.state.pinRows.size()) {
            this.state.pinRows.remove(index);
            this.rebuildGameBalls();
            this.refreshBallSummary();
        }
        this.rebuildModels();
        this.save();
        this.refreshCollectionLists();
    }

    private void clearArea(String type) {
        if (JOptionPane.showConfirmDialog(this, "\uc774 \ubaa9\ub85d\uc744 \ubaa8\ub450 \uc9c0\uc6b8\uae4c\uc694?", "\ud655\uc778", 0) != 0) {
            return;
        }
        if (type.equals("room")) {
            this.state.roomRows.clear();
        } else if (type.equals("reverse")) {
            this.state.reverseRows.clear();
        } else {
            this.state.pinRows.clear();
            this.state.gameBalls.clear();
            this.refreshBallSummary();
        }
        this.rebuildModels();
        this.save();
        this.refreshCollectionLists();
    }

    private void rebuildModels() {
        this.roomModel.setRowCount(0);
        this.reverseModel.setRowCount(0);
        this.pinModel.setRowCount(0);
        this.restoreTables();
    }

    private void rebuildGameBalls() {
        this.state.gameBalls.clear();
        for (PinRow row : this.state.pinRows) {
            this.state.gameBalls.merge(Main.safe(row.game), row.balls, Integer::sum);
        }
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "\uba54\uc2dc\uc9c0 \uc5c6\uc74c" : value;
    }

    private static String roomItemText(RoomRow row) {
        Map<String, Integer> counts = Main.roomItemCounts(row);
        if (counts.isEmpty()) {
            return row.items == null || row.items.isBlank() ? row.amount + " \uc218\ub7c9 \ud488\ubaa9" : row.items;
        }
        return counts.entrySet().stream().map(entry -> (Integer)entry.getValue() > 1 ? (String)entry.getKey() + " \u00d7 " + entry.getValue() : (String)entry.getKey()).reduce((a, b) -> a + " / " + b).orElse("");
    }

    private static boolean matches(JTextField search, String ... values) {
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isBlank()) {
            return true;
        }
        for (String value : values) {
            if (value == null || !value.toLowerCase(Locale.ROOT).contains(query)) continue;
            return true;
        }
        return false;
    }

    private void refreshRoomSummary() {
        this.roomSummary.removeAll();
        JLabel title = new JLabel("\ud488\ubaa9\ubcc4 \ud569\uacc4");
        title.setFont(title.getFont().deriveFont(1, 13.0f));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(8, 0, 0, 4));
        this.roomSummary.add(title);
        LinkedHashMap<String, Integer> totals = new LinkedHashMap<String, Integer>();
        for (RoomRow row : this.state.roomRows) {
            for (Map.Entry<String, Integer> entry : Main.roomItemCounts(row).entrySet()) {
                totals.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        if (totals.isEmpty()) {
            JLabel empty = new JLabel("\uc544\uc9c1 \uc9d1\uacc4\ub41c \ud488\ubaa9\uc774 \uc5c6\uc5b4\uc694");
            empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(8, 4, 0, 0));
            this.roomSummary.add(empty);
        } else {
            totals.forEach((item, count) -> {
                int total = Main.itemAmount(item) * count;
                String text = item + " \u00d7 " + count + "\uac74" + (String)(total > 0 ? " \u00b7 \ucd1d " + String.format("%,d", total) + "\uac1c" : "");
                this.roomSummary.add(new LargeDonationBadge(text));
            });
        }
        this.roomSummary.revalidate();
        this.roomSummary.repaint();
    }

    private static Map<String, Integer> roomItemCounts(RoomRow row) {
        if (row.itemCounts == null) {
            row.itemCounts = new LinkedHashMap<String, Integer>();
        }
        if (row.itemCounts.isEmpty()) {
            List<String> items = Main.messageParts(row.items);
            if (items.size() == 1) {
                String item = items.get(0);
                int unit = Main.itemAmount(item);
                int count = unit > 0 && row.amount >= unit && row.amount % unit == 0 ? row.amount / unit : 1;
                row.itemCounts.put(item, count);
            } else {
                for (String item : items) {
                    row.itemCounts.merge(item, 1, Integer::sum);
                }
            }
        }
        return row.itemCounts;
    }

    private static int itemAmount(String item) {
        if (item == null) {
            return 0;
        }
        String first = item.trim().split("\\s+", 2)[0].replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(first);
        }
        catch (Exception ignored) {
            return 0;
        }
    }

    private static List<String> messageParts(String value) {
        if (value == null || value.isBlank()) {
            return List.of("\uba54\uc2dc\uc9c0 \uc5c6\uc74c");
        }
        ArrayList<String> result = new ArrayList<String>();
        for (String part : value.split("\\s*/\\s*")) {
            String text = part.trim();
            if (text.isBlank()) continue;
            result.add(text);
        }
        return result.isEmpty() ? List.of("\uba54\uc2dc\uc9c0 \uc5c6\uc74c") : result;
    }

    private static String normalizeEditedMessage(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        ArrayList<String> lines = new ArrayList<String>();
        for (String line : value.split("\\R")) {
            String text = line.trim();
            if (text.isBlank()) continue;
            lines.add(text);
        }
        return String.join((CharSequence)" / ", lines);
    }

    private void toggleConnection() {
        if (this.chat != null) {
            this.disconnect();
        } else {
            this.connect(false);
        }
    }

    private void accountAction() {
        if (this.session == null) {
            this.showLoginDialog();
            return;
        }
        Object[] options = {"\uacc4\uc815 \ubcc0\uacbd", "\ub85c\uadf8\uc544\uc6c3", "\ucde8\uc18c"};
        int selected = JOptionPane.showOptionDialog(this,
                this.session.userId() + " \uacc4\uc815\uc73c\ub85c \ub85c\uadf8\uc778\ub418\uc5b4 \uc788\uc2b5\ub2c8\ub2e4.",
                "SOOP \uacc4\uc815", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[2]);
        if (selected == 0) {
            this.showLoginDialog();
        } else if (selected == 1) {
            this.disconnect();
            SoopSessionManager.clear();
            this.session = null;
            this.refreshAccountButton();
            this.connect(false);
        }
    }

    private void showLoginDialog() {
        JTextField userId = new JTextField(this.session == null ? "" : this.session.userId(), 22);
        JPasswordField password = new JPasswordField(22);
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("SOOP \uc544\uc774\ub514"));
        panel.add(userId);
        panel.add(new JLabel("\ube44\ubc00\ubc88\ud638 (\uc800\uc7a5\ud558\uc9c0 \uc54a\uc74c)"));
        panel.add(password);
        panel.add(new JLabel("\ub85c\uadf8\uc778 \uc138\uc158\ub9cc Windows \uc0ac\uc6a9\uc790 \uacc4\uc815\uc73c\ub85c \uc554\ud638\ud654\ud574 \uc800\uc7a5\ud569\ub2c8\ub2e4."));
        if (JOptionPane.showConfirmDialog(this, panel, "SOOP \ub85c\uadf8\uc778",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String id = userId.getText().trim();
        char[] secret = password.getPassword();
        password.setText("");
        if (id.isBlank() || secret.length == 0) {
            java.util.Arrays.fill(secret, '\0');
            JOptionPane.showMessageDialog(this, "\uc544\uc774\ub514\uc640 \ube44\ubc00\ubc88\ud638\ub97c \uc785\ub825\ud574 \uc8fc\uc138\uc694.", "\ub85c\uadf8\uc778", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.account.setEnabled(false);
        this.account.setText("\ub85c\uadf8\uc778 \uc911\u2026");
        CompletableFuture.supplyAsync(() -> {
            try {
                return SoopSessionManager.login(id, secret);
            } catch (Exception e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }).whenComplete((signedIn, failure) -> Main.ui(() -> {
            this.account.setEnabled(true);
            if (failure != null) {
                this.refreshAccountButton();
                Throwable cause = rootCause(failure);
                String detail = cause == null || cause.getMessage() == null ? "\ub85c\uadf8\uc778\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4." : cause.getMessage();
                JOptionPane.showMessageDialog(this, detail, "SOOP \ub85c\uadf8\uc778 \uc2e4\ud328", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.disconnect();
            this.session = signedIn;
            this.refreshAccountButton();
            this.connect(true);
        }));
    }

    private void refreshAccountButton() {
        this.account.setText(this.session == null ? "\ub85c\uadf8\uc778" : this.session.userId() + " \uacc4\uc815");
    }

    private void connect() {
        this.connect(false);
    }

    private void connect(boolean useSession) {
        String id = TEST_MODE ? this.streamer.getText().trim() : FIXED_STREAMER_ID;
        if (id.isBlank()) {
            this.error("\uc2a4\ud2b8\ub9ac\uba38 ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
            return;
        }
        this.disconnect();
        this.connectingStatus();
        this.state.streamerId = id;
        this.save();
        CompletableFuture.runAsync(() -> {
            try {
                SoopSessionManager.Session currentSession = useSession ? this.session : null;
                String cookieHeader = currentSession == null ? null : currentSession.cookieHeader();
                SoopLive live = new SoopLive(new SoopHttpClient(), SoopUrls.LIVE_BASE_URL, cookieHeader);
                if (TEST_MODE && !live.isOnline(id)) {
                    throw new StreamerOfflineException(id);
                }
                final SoopChat c = SoopClient.builder().build().chat(id).liveApi(live).autoReconnect(true).build();
                c.addListener(new SoopChatAdapter(){

                    @Override
                    public void onEnterChatRoom(EnterChatRoomEvent e) {
                        Main.ui(() -> {
                            Main.this.chat = c;
                            Main.this.connectedStatus();
                        });
                    }

                    @Override
                    public void onDonation(DonationEvent e) {
                        String source = e.getType() == DonationType.AD_BALLOON ? "\uc560\ub4dc\ubc8c\ub8ec" : "\ubcc4\ud48d\uc120";
                        Main.ui(() -> Main.this.donation(source, e.getFrom(), e.getFromUsername(), e.getAmount()));
                    }

                    @Override
                    public void onRaw(RawEvent e) {
                        Main.this.rememberChatViewerType(e);
                    }

                    @Override
                    public void onChat(ChatEvent e) {
                        ChatViewerType viewerType = Main.this.chatViewerType(e.getUserId());
                        Main.ui(() -> Main.this.chatMessage(e.getUserId(), e.getUsername(), e.getMessage(), viewerType));
                    }

                    @Override
                    public void onUnknown(UnknownEvent e) {
                        ChallengeGift gift = Main.this.parseChallengeGift(e);
                        if (gift != null) {
                            Main.ui(() -> Main.this.donation("\ub3c4\uc804\ubbf8\uc158", gift.userId, gift.nickname, gift.count));
                        }
                    }

                    @Override
                    public void onDisconnect(DisconnectEvent e) {
                        Main.ui(() -> {
                            if (Main.this.chat == c) {
                                Main.this.chat = null;
                                Main.this.offlineStatus();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Main.ui(() -> Main.this.failureStatus(e));
                    }
                });
                c.connect();
            }
            catch (Exception ex) {
                if (!useSession && this.session != null && rootCause(ex) instanceof AuthenticationRequiredException) {
                    Main.ui(() -> this.connect(true));
                    return;
                }
                Main.ui(() -> this.failureStatus(ex));
            }
        });
    }

    private void disconnect() {
        SoopChat old = this.chat;
        this.chat = null;
        if (old != null) {
            old.disconnect();
        }
        this.offlineStatus();
    }

    private void connectingStatus() {
        this.connect.setEnabled(false);
        this.status.setText("\u25cb  \uc5f0\uacb0 \uc911");
        this.status.setForeground(MUTED);
        this.status.setBackground(new Color(245, 239, 233));
        this.status.setToolTipText(null);
    }

    private void connectedStatus() {
        this.connect.setEnabled(true);
        this.status.setText("\u25cf  \uc5f0\ub3d9\ub428");
        this.status.setForeground(new Color(29, 138, 78));
        this.status.setBackground(new Color(226, 247, 235));
        this.status.setToolTipText(this.session == null ? "\ube44\ub85c\uadf8\uc778 \uc5f0\uacb0" : this.session.userId() + " \uacc4\uc815\uc73c\ub85c \uc5f0\uacb0");
    }

    private void offlineStatus() {
        this.connect.setEnabled(true);
        this.status.setText("\u25cf  \uc624\ud504\ub77c\uc778");
        this.status.setForeground(new Color(112, 112, 112));
        this.status.setBackground(new Color(238, 238, 238));
        this.status.setToolTipText("\ubc29\uc1a1\uc774 \uc624\ud504\ub77c\uc778\uc774\uac70\ub098 \uc5f0\uacb0\uc744 \ub04a\uc5c8\uc2b5\ub2c8\ub2e4.");
    }

    private void failureStatus(Throwable error) {
        this.connect.setEnabled(true);
        Throwable cause = rootCause(error);
        if (cause instanceof StreamerOfflineException) {
            this.offlineStatus();
            return;
        }
        if (cause instanceof AuthenticationRequiredException) {
            AuthenticationRequiredException auth = (AuthenticationRequiredException)cause;
            this.status.setText("\u25cf  \uc5f0\ub3d9\uc2e4\ud328 \u00b7 \ub85c\uadf8\uc778 \ud544\uc694");
            this.status.setForeground(new Color(190, 104, 0));
            this.status.setBackground(new Color(255, 241, 214));
            this.status.setToolTipText(auth.getReason().isBlank()
                    ? "\uc131\uc778 \uc778\uc99d \ub610\ub294 \uad6c\ub3c5 \uad8c\ud55c\uc774 \uc788\ub294 SOOP \uacc4\uc815\uc73c\ub85c \ub85c\uadf8\uc778\ud558\uc138\uc694."
                    : auth.getReason());
            if (this.session != null) {
                this.account.setText("\uc7ac\ub85c\uadf8\uc778");
            }
            return;
        }
        String message = cause == null || cause.getMessage() == null ? "\uc54c \uc218 \uc5c6\ub294 \uc624\ub958" : cause.getMessage();
        boolean network = message.contains("HTTP") || message.contains("timeout") || message.contains("\uc2dc\uac04 \ucd08\uacfc")
                || message.contains("connect") || message.contains("\uc5f0\uacb0");
        this.status.setText("\u25cf  \uc5f0\ub3d9\uc2e4\ud328 \u00b7 " + (network ? "\ub124\ud2b8\uc6cc\ud06c \uc624\ub958" : "\uc11c\ubc84 \uc624\ub958"));
        this.status.setForeground(new Color(176, 62, 38));
        this.status.setBackground(new Color(255, 235, 226));
        this.status.setToolTipText(message);
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current != null && current.getCause() != null && current.getCause() != current) current = current.getCause();
        return current;
    }

    private void donation(String source, String id, String nick, int amount) {
        boolean reverseMatched;
        RouletteTrigger trigger;
        String now = TIME.format(LocalDateTime.now());
        long donationToken = this.donationSequence.incrementAndGet();
        boolean rouletteCollected = false;
        if (this.state.rouletteEnabled && this.sourceEnabled("room", source) && (trigger = (RouletteTrigger)this.state.rouletteTriggers.stream().filter(candidate -> candidate.amount == amount).findFirst().orElse(null)) != null) {
            rouletteCollected = this.queueRoulette(nick, id, source, amount, trigger.spins, true, donationToken).collected();
        }
        if (this.sourceEnabled("reverse", source)) {
            this.state.reverseRules.stream().filter(r -> amount >= r.minimum).max(Comparator.comparingInt(r -> r.minimum)).ifPresent(r -> {
                int i = Main.indexById(this.state.reverseRows, id);
                int calc = (int)Math.floor((double)amount * r.percent / 100.0);
                if (i < 0) {
                    ReverseRow row = new ReverseRow(now, source, nick, id, r.percent, amount, "", calc);
                    this.state.reverseRows.add(row);
                    this.reverseModel.addRow(row.values());
                } else {
                    ReverseRow row = this.state.reverseRows.get(i);
                    row.time = now;
                    row.source = Main.mergeMessage(row.source, source);
                    row.nick = nick;
                    row.percent = r.percent;
                    row.amount += amount;
                    row.calculated += calc;
                    Main.replaceRow(this.reverseModel, i, row.values());
                }
                this.save();
            });
        }
        List<Pending> p = this.pending.computeIfAbsent(id, k -> new ArrayList<Pending>());
        boolean bl = reverseMatched = this.sourceEnabled("reverse", source) && this.state.reverseRules.stream().anyMatch(r -> amount >= r.minimum);
        if (reverseMatched) {
            p.add(new Pending("reverse", source, now, nick, id, amount, 0, "", System.currentTimeMillis(), 0L, false));
        }
        if (this.sourceEnabled("room", source) && this.state.roomRules.contains(amount)) {
            p.add(new Pending("room", source, now, nick, id, amount, 1, amount + " " + this.state.roomRuleNames.getOrDefault(amount, "\ud488\ubaa9 \ubbf8\uc9c0\uc815"), System.currentTimeMillis(), rouletteCollected ? donationToken : 0L, rouletteCollected));
        }
        int balls = RuleEngine.pinballBalls(amount, this.state.pinballUnit);
        if (this.sourceEnabled("pin", source) && balls > 0) {
            p.add(new Pending("pin", source, now, nick, id, amount, balls, "", System.currentTimeMillis(), 0L, false));
        }
        p.removeIf(x -> System.currentTimeMillis() - x.created > 120000L);
        this.refreshCollectionLists();
    }

    private RouletteQueueResult queueRoulette(String nickname, String id, String source, int amount, int spins, boolean collectWins, long donationToken) {
        if (this.rouletteOverlay == null || !this.rouletteOverlay.isRunning()) {
            return new RouletteQueueResult(false, false);
        }
        List<RouletteRule> valid = this.state.rouletteRules.stream().filter(r -> r.text != null && !r.text.isBlank() && r.probability > 0.0).toList();
        double total = valid.stream().mapToDouble(r -> r.probability).sum();
        if (valid.isEmpty() || total <= 0.0) {
            return new RouletteQueueResult(false, false);
        }
        List<String> labels = valid.stream().map(r -> r.text.trim()).toList();
        List<Double> probabilities = valid.stream().map(r -> r.probability).toList();
        ArrayList<Integer> results = new ArrayList<Integer>();
        LinkedHashMap<String, Integer> wins = new LinkedHashMap<String, Integer>();
        for (int spin = 0; spin < Math.max(1, spins); ++spin) {
            double pick = ThreadLocalRandom.current().nextDouble(total);
            double cursor = 0.0;
            int selected = valid.size() - 1;
            for (int i = 0; i < valid.size(); ++i) {
                if (!(pick < (cursor += valid.get((int)i).probability))) continue;
                selected = i;
                break;
            }
            results.add(selected);
            RouletteRule selectedRule = valid.get(selected);
            if (!collectWins || !selectedRule.collect) continue;
            wins.merge(selectedRule.text.trim(), 1, Integer::sum);
        }
        Runnable completion = wins.isEmpty() ? null : () -> Main.ui(() -> this.collectRouletteWins(source, nickname, id, amount, new LinkedHashMap<String, Integer>(wins), donationToken));
        this.rouletteOverlay.publish(nickname == null || nickname.isBlank() ? "\uc775\uba85 \ud6c4\uc6d0\uc790" : nickname, labels, probabilities, results, completion);
        return new RouletteQueueResult(true, !wins.isEmpty());
    }

    private void collectRouletteWins(String source, String nickname, String id, int amount, Map<String, Integer> wins, long donationToken) {
        String now = TIME.format(LocalDateTime.now());
        String resultText = Main.rouletteResultText(wins);
        LinkedHashMap<String, Integer> items = new LinkedHashMap<String, Integer>();
        wins.forEach((label, count) -> items.put("\ub8f0\ub81b \u00b7 " + label, (Integer)count));
        int index = Main.indexById(this.state.roomRows, id);
        boolean addAmount = this.claimRoomDonation(donationToken);
        if (index < 0) {
            String first = (String)items.keySet().iterator().next();
            RoomRow row = new RoomRow(now, source, nickname, id, amount, resultText, "", first);
            row.itemCounts.clear();
            row.itemCounts.putAll(items);
            row.items = String.join((CharSequence)" / ", items.keySet());
            this.state.roomRows.add(row);
            this.roomModel.addRow(row.values());
        } else {
            RoomRow row = this.state.roomRows.get(index);
            row.time = now;
            row.source = Main.mergeMessage(row.source, source);
            row.nick = nickname;
            if (addAmount) {
                row.amount += amount;
            }
            row.message = Main.mergeMessage(row.message, resultText);
            items.forEach((item, count) -> Main.roomItemCounts(row).merge((String)item, (Integer)count, Integer::sum));
            for (String item2 : items.keySet()) {
                row.items = Main.mergeMessage(row.items, item2);
            }
            Main.replaceRow(this.roomModel, index, row.values());
        }
        this.save();
        this.refreshCollectionLists();
    }

    private boolean claimRoomDonation(long donationToken) {
        if (donationToken <= 0L) {
            return true;
        }
        boolean first = this.claimedRoomDonations.add(donationToken);
        while (this.claimedRoomDonations.size() > 5000) {
            Iterator iterator = this.claimedRoomDonations.iterator();
            iterator.next();
            iterator.remove();
        }
        return first;
    }

    static String rouletteResultText(Map<String, Integer> wins) {
        return wins.entrySet().stream().map(entry -> (Integer)entry.getValue() > 1 ? (String)entry.getKey() + " \u00d7 " + entry.getValue() : (String)entry.getKey()).reduce((a, b) -> a + " / " + b).orElse("");
    }

    private synchronized void rememberChatViewerType(RawEvent event) {
        try {
            byte[] raw = event.getRawData();
            if (raw == null) {
                return;
            }
            String packet = new String(raw, StandardCharsets.UTF_8);
            if (packet.length() < 14 || !"0005".equals(packet.substring(2, 6))) {
                return;
            }
            String[] parts = packet.split("\\f", -1);
            if (parts.length <= 8 || parts[2] == null || parts[2].isBlank()) {
                return;
            }
            this.chatViewerTypes.put(parts[2], Main.classifyChatViewer(parts[7], parts[8]));
            while (this.chatViewerTypes.size() > 5000) {
                Iterator<String> oldest = this.chatViewerTypes.keySet().iterator();
                oldest.next();
                oldest.remove();
            }
        }
        catch (Exception ignored) {
        }
    }

    private synchronized ChatViewerType chatViewerType(String id) {
        return this.chatViewerTypes.getOrDefault(id, ChatViewerType.NORMAL);
    }

    static ChatViewerType classifyChatViewer(String userFlag, String subscriptionMonth) {
        long primary = 0L;
        try {
            String rawPrimary = userFlag == null ? "" : userFlag.split("\\|", -1)[0].trim();
            primary = Long.parseLong(rawPrimary);
        }
        catch (Exception ignored) {
        }
        if ((primary & 32768L) != 0L) {
            return ChatViewerType.TOP_FAN;
        }
        try {
            if (subscriptionMonth != null && Integer.parseInt(subscriptionMonth.trim()) >= 0) {
                return ChatViewerType.SUBSCRIBER;
            }
        }
        catch (Exception ignored) {
        }
        if ((primary & 32L) != 0L) {
            return ChatViewerType.FAN;
        }
        return ChatViewerType.NORMAL;
    }

    private void chatMessage(String id, String nickname, String message, ChatViewerType viewerType) {
        boolean pinDonationCollected = this.nextChat(id, message);
        if (this.state.pinChatEnabled && !pinDonationCollected && this.chatViewerTypeEnabled(viewerType)) {
            this.collectPinChat(id, nickname, message);
        }
    }

    private boolean nextChat(String id, String message) {
        List<Pending> items = this.pending.remove(id);
        if (items == null) {
            return false;
        }
        boolean pinCollected = false;
        for (Pending p : items) {
            Object row;
            int i;
            if (System.currentTimeMillis() - p.created > 120000L) continue;
            if (p.kind.equals("room")) {
                RoomRow row2;
                i = Main.indexById(this.state.roomRows, p.id);
                String collectedMessage = p.skipMessage ? "" : message;
                boolean addAmount = this.claimRoomDonation(p.donationToken);
                if (i < 0) {
                    row2 = new RoomRow(p.time, p.source, p.nick, p.id, p.amount, collectedMessage, "", p.item);
                    this.state.roomRows.add(row2);
                    this.roomModel.addRow(row2.values());
                    continue;
                }
                row2 = this.state.roomRows.get(i);
                row2.time = p.time;
                row2.source = Main.mergeMessage(row2.source, p.source);
                row2.nick = p.nick;
                if (addAmount) {
                    row2.amount += p.amount;
                }
                row2.items = Main.mergeMessage(row2.items, p.item);
                Main.roomItemCounts(row2).merge(p.item, 1, Integer::sum);
                if (!p.skipMessage) {
                    row2.message = Main.mergeMessage(row2.message, message);
                }
                Main.replaceRow(this.roomModel, i, row2.values());
                continue;
            }
            if (p.kind.equals("reverse")) {
                i = Main.indexById(this.state.reverseRows, p.id);
                if (i < 0) continue;
                row = this.state.reverseRows.get(i);
                ((ReverseRow)row).message = Main.mergeMessage(((ReverseRow)row).message, message);
                Main.replaceRow(this.reverseModel, i, ((ReverseRow)row).values());
                continue;
            }
            pinCollected = true;
            i = Main.indexById(this.state.pinRows, p.id);
            if (i < 0) {
                row = new PinRow(p.time, p.source, p.nick, p.id, p.amount, message, p.balls);
                this.state.pinRows.add((PinRow)row);
                this.pinModel.addRow(((PinRow)row).values());
            } else {
                row = this.state.pinRows.get(i);
                ((PinRow)row).time = p.time;
                ((PinRow)row).source = Main.mergeMessage(((PinRow)row).source, p.source);
                ((PinRow)row).nick = p.nick;
                ((PinRow)row).amount += p.amount;
                ((PinRow)row).balls += p.balls;
                ((PinRow)row).game = Main.mergeMessage(((PinRow)row).game, message);
                Main.replaceRow(this.pinModel, i, ((PinRow)row).values());
            }
            this.state.gameBalls.merge(message.isBlank() ? "\ubbf8\uc785\ub825" : message.trim(), p.balls, Integer::sum);
            this.refreshBallSummary();
        }
        this.save();
        this.refreshCollectionLists();
        return pinCollected;
    }

    private void collectPinChat(String id, String nickname, String message) {
        if (id == null || id.isBlank() || message == null || message.isBlank()) {
            return;
        }
        String now = TIME.format(LocalDateTime.now());
        String collectedName = nickname == null || nickname.isBlank() ? id : nickname.trim();
        int index = Main.indexById(this.state.pinRows, id);
        if (!this.state.pinChatDuplicateEnabled && index >= 0
                && Main.messageParts(this.state.pinRows.get(index).source).contains("\uc77c\ubc18 \ucc44\ud305")) {
            return;
        }
        if (index < 0) {
            PinRow row = new PinRow(now, "\uc77c\ubc18 \ucc44\ud305", collectedName, id, 0, collectedName, 1);
            this.state.pinRows.add(row);
            this.pinModel.addRow(row.values());
        } else {
            PinRow row = this.state.pinRows.get(index);
            row.time = now;
            row.source = Main.mergeMessage(row.source, "\uc77c\ubc18 \ucc44\ud305");
            if (nickname != null && !nickname.isBlank()) {
                row.nick = nickname;
            }
            row.balls += 1;
            row.game = Main.mergeMessage(row.game, collectedName);
            Main.replaceRow(this.pinModel, index, row.values());
        }
        this.state.gameBalls.merge(collectedName, 1, Integer::sum);
        this.refreshBallSummary();
        this.save();
        this.refreshCollectionLists();
    }

    private static String mergeMessage(String oldValue, String newValue) {
        String n;
        String string = n = newValue == null || newValue.isBlank() ? "\ubbf8\uc785\ub825" : newValue.trim();
        if (oldValue == null || oldValue.isBlank()) {
            return n;
        }
        for (String part : oldValue.split(" / ")) {
            if (!part.equals(n)) continue;
            return oldValue;
        }
        return oldValue + " / " + n;
    }

    private static int indexById(List<?> rows, String id) {
        for (int i = 0; i < rows.size(); ++i) {
            String rid;
            Object r = rows.get(i);
            if (r instanceof RoomRow) {
                RoomRow room = (RoomRow)r;
                rid = room.id;
            } else if (r instanceof ReverseRow) {
                ReverseRow reverse = (ReverseRow)r;
                rid = reverse.id;
            } else {
                rid = ((PinRow)r).id;
            }
            if (!Objects.equals(rid, id)) continue;
            return i;
        }
        return -1;
    }

    private static void replaceRow(DefaultTableModel model, int row, Object[] values) {
        for (int c = 0; c < values.length; ++c) {
            model.setValueAt(values[c], row, c);
        }
    }

    private void consolidateState() {
        LinkedHashMap<String, RoomRow> rooms = new LinkedHashMap<String, RoomRow>();
        for (RoomRow roomRow : this.state.roomRows) {
            if (roomRow.source == null) {
                roomRow.source = "\ubcc4\ud48d\uc120";
            }
            if (roomRow.items == null || roomRow.items.isBlank()) {
                roomRow.items = roomRow.amount + " \uc218\ub7c9 \ud488\ubaa9";
            }
            Main.roomItemCounts(roomRow);
            RoomRow x = (RoomRow)rooms.get(roomRow.id);
            if (x == null) {
                rooms.put(roomRow.id, roomRow);
                continue;
            }
            x.time = roomRow.time;
            x.source = Main.mergeMessage(x.source, roomRow.source);
            x.nick = roomRow.nick;
            x.amount += roomRow.amount;
            x.items = Main.mergeMessage(x.items, roomRow.items);
            Main.roomItemCounts(roomRow).forEach((item, count) -> Main.roomItemCounts(x).merge((String)item, (Integer)count, Integer::sum));
            x.message = Main.mergeMessage(x.message, roomRow.message);
            if (x.memo != null && !x.memo.isBlank() || roomRow.memo == null) continue;
            x.memo = roomRow.memo;
        }
        this.state.roomRows = new ArrayList(rooms.values());
        LinkedHashMap<String, ReverseRow> reverses = new LinkedHashMap<String, ReverseRow>();
        for (ReverseRow r : this.state.reverseRows) {
            ReverseRow x;
            if (r.source == null) {
                r.source = "\ubcc4\ud48d\uc120";
            }
            if ((x = (ReverseRow)reverses.get(r.id)) == null) {
                reverses.put(r.id, r);
                continue;
            }
            x.time = r.time;
            x.source = Main.mergeMessage(x.source, r.source);
            x.nick = r.nick;
            x.percent = r.percent;
            x.amount += r.amount;
            x.calculated += r.calculated;
            x.message = Main.mergeMessage(x.message, r.message);
        }
        this.state.reverseRows = new ArrayList(reverses.values());
        LinkedHashMap<String, PinRow> linkedHashMap = new LinkedHashMap<String, PinRow>();
        for (PinRow r : this.state.pinRows) {
            PinRow x;
            if (r.source == null) {
                r.source = "\ubcc4\ud48d\uc120";
            }
            if ((x = (PinRow)linkedHashMap.get(r.id)) == null) {
                linkedHashMap.put(r.id, r);
                continue;
            }
            x.time = r.time;
            x.source = Main.mergeMessage(x.source, r.source);
            x.nick = r.nick;
            x.amount += r.amount;
            x.balls += r.balls;
            x.game = Main.mergeMessage(x.game, r.game);
        }
        this.state.pinRows = new ArrayList(linkedHashMap.values());
        this.save();
    }

    private void testDialog() {
        JTextField id = new JTextField("test_user");
        JTextField nick = new JTextField("\ud14c\uc2a4\ud2b8\ud6c4\uc6d0\uc790");
        JTextField amount = new JTextField("100");
        JTextField message = new JTextField("\ub864");
        JComboBox<String> source = new JComboBox<String>(new String[]{"\ubcc4\ud48d\uc120", "\uc560\ub4dc\ubc8c\ub8ec", "\ub3c4\uc804\ubbf8\uc158"});
        Object[] fields = new Object[]{"\ud6c4\uc6d0 \uc885\ub958", source, "\ud6c4\uc6d0\uc790 ID", id, "\ub2c9\ub124\uc784", nick, "\ud6c4\uc6d0\uc218", amount, "\ub2e4\uc74c \ucc44\ud305", message};
        if (JOptionPane.showConfirmDialog(this, fields, "\ud14c\uc2a4\ud2b8 \ud6c4\uc6d0", 2) == 0) {
            try {
                this.donation(String.valueOf(source.getSelectedItem()), id.getText().trim(), nick.getText().trim(), Integer.parseInt(amount.getText().trim()));
                this.nextChat(id.getText().trim(), message.getText());
            }
            catch (NumberFormatException e) {
                this.error("\ud6c4\uc6d0\uc218\ub294 \uc815\uc218\ub85c \uc785\ub825\ud558\uc138\uc694.");
            }
        }
    }

    private void addSourceSelectors(JPanel panel, String area) {
        for (String source : new String[]{"\ubcc4\ud48d\uc120", "\uc560\ub4dc\ubc8c\ub8ec", "\ub3c4\uc804\ubbf8\uc158"}) {
            JCheckBox box = new JCheckBox(source, this.sourceEnabled(area, source));
            box.setOpaque(false);
            box.setForeground(TEXT);
            box.setAlignmentX(0.0f);
            box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            box.addActionListener(e -> {
                this.setSourceEnabled(area, source, box.isSelected());
                this.save();
            });
            panel.add(box);
        }
    }

    private void addChatViewerSelectors(JPanel panel) {
        String[] labels = new String[]{"\uac74\ube75", "\ud32c", "\uad6c\ub3c5\uc790", "\uc5f4\ud608"};
        ChatViewerType[] types = ChatViewerType.values();
        for (int i = 0; i < labels.length; ++i) {
            ChatViewerType type = types[i];
            JCheckBox box = new JCheckBox(labels[i], this.chatViewerTypeEnabled(type));
            box.setOpaque(false);
            box.setForeground(TEXT);
            box.setAlignmentX(0.0f);
            box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            box.addActionListener(e -> {
                this.setChatViewerTypeEnabled(type, box.isSelected());
                this.save();
            });
            panel.add(box);
        }
    }

    private boolean chatViewerTypeEnabled(ChatViewerType type) {
        return switch (type) {
            case NORMAL -> this.state.pinChatNormal;
            case FAN -> this.state.pinChatFan;
            case SUBSCRIBER -> this.state.pinChatSubscriber;
            case TOP_FAN -> this.state.pinChatTopFan;
        };
    }

    private void setChatViewerTypeEnabled(ChatViewerType type, boolean enabled) {
        switch (type) {
            case NORMAL -> this.state.pinChatNormal = enabled;
            case FAN -> this.state.pinChatFan = enabled;
            case SUBSCRIBER -> this.state.pinChatSubscriber = enabled;
            case TOP_FAN -> this.state.pinChatTopFan = enabled;
        }
    }

    private boolean sourceEnabled(String area, String source) {
        if (area.equals("room")) {
            return source.equals("\ubcc4\ud48d\uc120") ? this.state.roomStar : (source.equals("\uc560\ub4dc\ubc8c\ub8ec") ? this.state.roomAd : this.state.roomChallenge);
        }
        if (area.equals("reverse")) {
            return source.equals("\ubcc4\ud48d\uc120") ? this.state.reverseStar : (source.equals("\uc560\ub4dc\ubc8c\ub8ec") ? this.state.reverseAd : this.state.reverseChallenge);
        }
        return source.equals("\ubcc4\ud48d\uc120") ? this.state.pinStar : (source.equals("\uc560\ub4dc\ubc8c\ub8ec") ? this.state.pinAd : this.state.pinChallenge);
    }

    private void setSourceEnabled(String area, String source, boolean value) {
        if (area.equals("room")) {
            if (source.equals("\ubcc4\ud48d\uc120")) {
                this.state.roomStar = value;
            } else if (source.equals("\uc560\ub4dc\ubc8c\ub8ec")) {
                this.state.roomAd = value;
            } else {
                this.state.roomChallenge = value;
            }
        } else if (area.equals("reverse")) {
            if (source.equals("\ubcc4\ud48d\uc120")) {
                this.state.reverseStar = value;
            } else if (source.equals("\uc560\ub4dc\ubc8c\ub8ec")) {
                this.state.reverseAd = value;
            } else {
                this.state.reverseChallenge = value;
            }
        } else if (source.equals("\ubcc4\ud48d\uc120")) {
            this.state.pinStar = value;
        } else if (source.equals("\uc560\ub4dc\ubc8c\ub8ec")) {
            this.state.pinAd = value;
        } else {
            this.state.pinChallenge = value;
        }
    }

    private ChallengeGift parseChallengeGift(UnknownEvent event) {
        try {
            String raw = event.getRawPacket();
            if (raw == null || raw.length() < 6 || !"0121".equals(raw.substring(2, 6))) {
                return null;
            }
            List<String> parts = event.getParts();
            if (parts.size() < 2) {
                return null;
            }
            JsonObject json = JsonParser.parseString(parts.get(1)).getAsJsonObject();
            if (!json.has("type") || !"CHALLENGE_GIFT".equalsIgnoreCase(json.get("type").getAsString())) {
                return null;
            }
            String id = json.has("user_id") ? json.get("user_id").getAsString() : "";
            String nick = json.has("user_nick") ? json.get("user_nick").getAsString() : id;
            int count = json.has("gift_count") ? json.get("gift_count").getAsInt() : 0;
            return id.isBlank() || count <= 0 ? null : new ChallengeGift(id, nick, count);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private void clearTable(JTable table) {
        if (JOptionPane.showConfirmDialog(this, "\uc774 \ud0ed\uc758 \uc218\uc9d1 \ub0b4\uc5ed\uc744 \ubaa8\ub450 \uc9c0\uc6b8\uae4c\uc694?", "\ud655\uc778", 0) != 0) {
            return;
        }
        DefaultTableModel m = (DefaultTableModel)table.getModel();
        m.setRowCount(0);
        if (m == this.roomModel) {
            this.state.roomRows.clear();
        } else if (m == this.reverseModel) {
            this.state.reverseRows.clear();
        } else {
            this.state.pinRows.clear();
            this.state.gameBalls.clear();
            this.refreshBallSummary();
        }
        this.save();
    }

    private void restoreTables() {
        this.state.roomRows.forEach(r -> this.roomModel.addRow(r.values()));
        this.state.reverseRows.forEach(r -> this.reverseModel.addRow(r.values()));
        this.state.pinRows.forEach(r -> this.pinModel.addRow(r.values()));
    }

    private String ballSummaryText() {
        return this.state.gameBalls.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> (String)e.getKey() + "*" + e.getValue()).reduce((a, b) -> a + "," + b).orElse("");
    }

    private void refreshBallSummary() {
        this.ballSummary.removeAll();
        JLabel h = new JLabel("\uc218\uc9d1\ud55c \uacf5");
        h.setFont(h.getFont().deriveFont(1, 18.0f));
        this.ballSummary.add(h);
        this.ballSummary.add(Box.createVerticalStrut(10));
        String value = this.ballSummaryText();
        JTextArea output = new JTextArea(value.isBlank() ? "\uc544\uc9c1 \uc218\uc9d1\ub41c \uacf5\uc774 \uc5c6\uc5b4\uc694" : value);
        output.setEditable(false);
        output.setOpaque(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(output.getFont().deriveFont(16.0f));
        output.setForeground(value.isBlank() ? MUTED : TEXT);
        output.setBorder(new EmptyBorder(5, 0, 5, 0));
        output.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        this.ballSummary.add(output);
        this.ballSummary.revalidate();
        this.ballSummary.repaint();
    }

    private void copyBallSummary() {
        if (this.state.gameBalls.isEmpty()) {
            this.error("\ubcf5\uc0ac\ud560 \uacf5\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
            return;
        }
        String value = this.ballSummaryText();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
        JOptionPane.showMessageDialog(this, "\uc218\uc9d1\ud55c \uacf5 \ubaa9\ub85d\uc744 \ubcf5\uc0ac\ud588\uc2b5\ub2c8\ub2e4.", "\ubcf5\uc0ac \uc644\ub8cc", 1);
    }

    private static String stationUrl(String id) {
        return "https://www.sooplive.com/station/" + id;
    }

    private void openStation(String id) {
        try {
            Desktop.getDesktop().browse(URI.create(Main.stationUrl(id)));
        }
        catch (Exception e) {
            this.error("\ube0c\ub77c\uc6b0\uc800\ub97c \uc5f4 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: " + e.getMessage());
        }
    }

    private void installStationButtons(final JTable table) {
        int col = table.getColumnCount() - 1;
        table.getColumnModel().getColumn(col).setPreferredWidth(105);
        table.getColumnModel().getColumn(col).setMaxWidth(125);
        table.getColumnModel().getColumn(col).setCellRenderer((t, v, selected, focus, row, column) -> {
            JButton b = new JButton("\ubc29\uc1a1\uad6d \uc774\ub3d9");
            b.setForeground(Color.WHITE);
            b.setBackground(ACCENT);
            b.putClientProperty("JButton.buttonType", "roundRect");
            return b;
        });
        table.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0 && viewCol == table.getColumnCount() - 1) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    Main.this.openStation(String.valueOf(table.getModel().getValueAt(modelRow, 3)));
                }
            }
        });
    }

    private State load() {
        try {
            if (Files.exists(DATA, new LinkOption[0])) {
                State s = this.gson.fromJson(Files.readString(DATA), State.class);
                return s == null ? new State() : s.normalize();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new State();
    }

    private void save() {
        try {
            Files.createDirectories(DATA.getParent(), new FileAttribute[0]);
            Files.writeString(DATA, (CharSequence)this.gson.toJson(this.state), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            this.status.setText("\uc800\uc7a5 \uc2e4\ud328");
        }
    }

    private static void ui(Runnable r) {
        SwingUtilities.invokeLater(r);
    }

    private static List<Image> loadAppIcons() {
        ImageIcon source = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/assets/kanashi-avatar-v3-rounded.png")));
        ArrayList<Image> icons = new ArrayList<Image>();
        for (int size : new int[]{16, 24, 32, 48, 64, 128, 256}) {
            icons.add(source.getImage().getScaledInstance(size, size, 4));
        }
        return icons;
    }

    private static void styleControls(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)component;
                Main.styleButton(button);
            }
            if (component instanceof JTextField) {
                JTextField field = (JTextField)component;
                Main.styleInput(field);
            }
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                combo.setBorder(new EmptyBorder(3, 10, 3, 10));
                combo.setBackground(new Color(255, 247, 240));
                combo.putClientProperty("JComponent.roundRect", true);
            }
            if (!(component instanceof Container)) continue;
            Container child = (Container)component;
            Main.styleControls(child);
        }
    }

    private static void styleButton(AbstractButton button) {
        if (Boolean.TRUE.equals(button.getClientProperty("kanashi.bare"))) {
            Main.styleBareButton(button);
            return;
        }
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(12));
        if (button instanceof JCheckBox) {
            button.setOpaque(false);
            return;
        }
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        if (button instanceof GradientButton) {
            return;
        }
        if (button.getBackground() == null || button.getBackground().equals(UIManager.getColor("Button.background"))) {
            button.setBackground(new Color(255, 239, 226));
        }
        button.setUI(new RoundedButtonUI());
    }

    private static void styleBareButton(AbstractButton button) {
        button.putClientProperty("kanashi.bare", true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(6, 8, 6, 8));
        button.setCursor(Cursor.getPredefinedCursor(12));
        button.setUI(new BasicButtonUI());
    }

    private static void styleInput(JTextField field) {
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setBackground(new Color(255, 247, 240));
        field.putClientProperty("JComponent.roundRect", true);
    }

    private static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG);
        return p;
    }

    private static JPanel panel(LayoutManager l) {
        JPanel p = new JPanel(l);
        p.setBackground(PANEL);
        return p;
    }

    private static JPanel settingsCard(String title, String description) {
        RoundedPanel p = new RoundedPanel();
        p.setLayout(new BoxLayout(p, 1));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));
        p.setPreferredSize(new Dimension(300, 10));
        JLabel tag = new JLabel("COLLECT SETTING");
        tag.setForeground(ACCENT);
        tag.setFont(tag.getFont().deriveFont(1, 10.0f));
        JLabel h = new JLabel(title);
        h.setForeground(TEXT);
        h.setFont(h.getFont().deriveFont(1, 20.0f));
        JLabel d = new JLabel("<html>" + description + "</html>");
        d.setForeground(MUTED);
        d.setFont(d.getFont().deriveFont(12.0f));
        tag.setAlignmentX(0.0f);
        h.setAlignmentX(0.0f);
        d.setAlignmentX(0.0f);
        tag.setMaximumSize(new Dimension(Integer.MAX_VALUE, tag.getPreferredSize().height));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, h.getPreferredSize().height));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.getPreferredSize().height));
        p.add(tag);
        p.add(Box.createVerticalStrut(4));
        p.add(h);
        p.add(Box.createVerticalStrut(5));
        p.add(d);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private static JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(l.getFont().deriveFont(1, 13.0f));
        l.setBorder(new EmptyBorder(0, 0, 7, 0));
        l.setAlignmentX(0.0f);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height));
        return l;
    }

    private static JPanel fieldRow(Component ... items) {
        JPanel p = new JPanel(new GridLayout(1, items.length, 8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.setAlignmentX(0.0f);
        for (Component item : items) {
            p.add(item);
        }
        return p;
    }

    private static JComponent dashboard(JComponent settings, JComponent collection, JComponent bottom) {
        JPanel root = Main.page();
        root.setBorder(new EmptyBorder(10, 20, 18, 20));
        JPanel main = new JPanel(new BorderLayout(14, 0));
        main.setOpaque(false);
        main.add((Component)settings, "West");
        main.add((Component)collection, "Center");
        root.add((Component)main, "Center");
        if (bottom != null) {
            int height = Math.max(132, bottom.getPreferredSize().height);
            bottom.setPreferredSize(new Dimension(10, height));
            root.add((Component)bottom, "South");
        }
        return root;
    }

    private static JComponent infoStrip(String title, String description, JLabel detail) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(24, 0));
        p.setBorder(new EmptyBorder(20, 26, 20, 26));
        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, 1));
        JLabel tag = new JLabel("QUICK GUIDE");
        tag.setForeground(ACCENT);
        tag.setFont(tag.getFont().deriveFont(1, 10.0f));
        JLabel h = new JLabel(title + " \uc0ac\uc6a9 \uc548\ub0b4");
        h.setFont(h.getFont().deriveFont(1, 18.0f));
        JLabel d = new JLabel(description);
        d.setForeground(MUTED);
        words.add(tag);
        words.add(Box.createVerticalStrut(3));
        words.add(h);
        words.add(Box.createVerticalStrut(5));
        words.add(d);
        p.add((Component)words, "Center");
        if (detail != null) {
            JLabel copy = new JLabel(detail.getText());
            copy.setForeground(ACCENT_DARK);
            copy.setFont(copy.getFont().deriveFont(1, 14.0f));
            p.add((Component)copy, "East");
        }
        return p;
    }

    private static JPanel rulePanel(String hint) {
        RoundedPanel p = new RoundedPanel(new FlowLayout(0, 10, 10));
        p.setBorder(new EmptyBorder(8, 14, 8, 14));
        JLabel tag = new JLabel("\uc218\uc9d1 \uc124\uc815");
        tag.setForeground(ACCENT);
        tag.setFont(tag.getFont().deriveFont(1, 12.0f));
        tag.setBorder(new EmptyBorder(0, 0, 0, 8));
        JLabel l = new JLabel(hint);
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(0, 0, 0, 12));
        p.add(tag);
        p.add(l);
        return p;
    }

    private static JTextField smallField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(92, 36));
        f.putClientProperty("JComponent.roundRect", true);
        return f;
    }

    private static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(42);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setGridColor(new Color(247, 232, 221));
        t.setSelectionBackground(new Color(255, 224, 201));
        t.setSelectionForeground(TEXT);
        t.getTableHeader().setPreferredSize(new Dimension(10, 42));
        t.getTableHeader().setFont(t.getFont().deriveFont(1));
        return t;
    }

    private static DefaultTableModel model(String ... cols) {
        return new DefaultTableModel(cols, 0){

            @Override
            public boolean isCellEditable(int r, int c) {
                return "\uba54\ubaa8".equals(this.getColumnName(c));
            }
        };
    }

    private void integer(JTextField f, IntConsumer ok) {
        try {
            int v = Integer.parseInt(f.getText().trim());
            if (v <= 0) {
                throw new Exception();
            }
            ok.accept(v);
            f.setText("");
        }
        catch (Exception e) {
            this.error("0\ubcf4\ub2e4 \ud070 \uc815\uc218\ub97c \uc785\ub825\ud558\uc138\uc694.");
        }
    }

    private void decimal(JTextField f, DoubleConsumer ok) {
        try {
            double v = Double.parseDouble(f.getText().trim());
            if (v < 0.0) {
                throw new Exception();
            }
            ok.accept(v);
            f.setText("");
        }
        catch (Exception e) {
            this.error("0 \uc774\uc0c1\uc758 \ud37c\uc13c\ud2b8\ub97c \uc785\ub825\ud558\uc138\uc694.");
        }
    }

    private void chooseRemove(List<Integer> values, Runnable refresh) {
        if (values.isEmpty()) {
            return;
        }
        Object v = JOptionPane.showInputDialog(this, "\uc0ad\uc81c\ud560 \uc218\ub7c9", "\uc218\ub7c9 \uc0ad\uc81c", -1, null, values.toArray(), values.get(0));
        if (v != null) {
            values.remove(v);
            refresh.run();
            this.save();
        }
    }

    private void error(String s) {
        JOptionPane.showMessageDialog(this, s, "\uc54c\ub9bc", 2);
    }

    private String pinText() {
        return this.state.pinballUnit > 0 ? this.state.pinballUnit + "\uac1c\ub2f9 \uacf5 1\uac1c (\uc815\ud655\ud55c \ubc30\uc218\ub9cc \uc218\uc9d1)" : "\uaddc\uce59 \uc5c6\uc74c";
    }

    private static String trim(double d) {
        return d == (double)((long)d) ? String.valueOf((long)d) : String.valueOf(d);
    }

    private static void setLook() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Malgun Gothic", 0, 14));
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Component.arc", 18);
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Button.arc", 18);
        UIManager.put("Button.borderWidth", 0);
        UIManager.put("Button.innerFocusWidth", 0);
        UIManager.put("TextComponent.arc", 18);
        UIManager.put("TextComponent.borderWidth", 0);
        UIManager.put("ProgressBar.arc", 999);
        UIManager.put("Button.default.background", ACCENT);
        UIManager.put("ToggleButton.selectedBackground", ACCENT);
        UIManager.put("ToggleButton.selectedForeground", Color.WHITE);
        UIManager.put("ToggleButton.toolbar.selectedBackground", ACCENT);
        UIManager.put("Table.background", PANEL);
        UIManager.put("TableHeader.background", new Color(255, 241, 230));
        UIManager.put("Table.alternateRowColor", new Color(255, 251, 247));
        UIManager.put("ScrollPane.background", PANEL);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 11);
    }

    static final class State {
        String streamerId = "";
        List<Integer> roomRules = new ArrayList<Integer>();
        Map<Integer, String> roomRuleNames = new LinkedHashMap<Integer, String>();
        List<ReverseRule> reverseRules = new ArrayList<ReverseRule>();
        int pinballUnit = 0;
        boolean roomStar = true;
        boolean roomAd = true;
        boolean roomChallenge = true;
        boolean reverseStar = true;
        boolean reverseAd = true;
        boolean reverseChallenge = true;
        boolean pinStar = true;
        boolean pinAd = true;
        boolean pinChallenge = true;
        boolean pinChatEnabled = false;
        boolean pinChatDuplicateEnabled = false;
        boolean pinChatNormal = true;
        boolean pinChatFan = true;
        boolean pinChatSubscriber = true;
        boolean pinChatTopFan = true;
        boolean rouletteEnabled = false;
        int rouletteAmount = 0;
        int rouletteSpins = 1;
        List<RouletteTrigger> rouletteTriggers = new ArrayList<RouletteTrigger>();
        List<RouletteRule> rouletteRules = new ArrayList<RouletteRule>();
        List<RoomRow> roomRows = new ArrayList<RoomRow>();
        List<ReverseRow> reverseRows = new ArrayList<ReverseRow>();
        List<PinRow> pinRows = new ArrayList<PinRow>();
        Map<String, Integer> gameBalls = new LinkedHashMap<String, Integer>();

        State() {
        }

        State normalize() {
            if (this.roomRules == null) {
                this.roomRules = new ArrayList<Integer>();
            }
            if (this.roomRuleNames == null) {
                this.roomRuleNames = new LinkedHashMap<Integer, String>();
            }
            for (Integer amount : this.roomRules) {
                this.roomRuleNames.putIfAbsent(amount, "\ud488\ubaa9 \ubbf8\uc9c0\uc815");
            }
            if (this.reverseRules == null) {
                this.reverseRules = new ArrayList<ReverseRule>();
            }
            if (this.rouletteRules == null) {
                this.rouletteRules = new ArrayList<RouletteRule>();
            }
            if (this.rouletteTriggers == null) {
                this.rouletteTriggers = new ArrayList<RouletteTrigger>();
            }
            if (this.rouletteTriggers.isEmpty() && this.rouletteAmount > 0) {
                this.rouletteTriggers.add(new RouletteTrigger(this.rouletteAmount, Math.max(1, this.rouletteSpins)));
            }
            this.rouletteAmount = 0;
            this.rouletteSpins = 1;
            this.rouletteTriggers.removeIf(trigger -> trigger.amount <= 0 || trigger.spins <= 0);
            this.rouletteTriggers.sort(Comparator.comparingInt(trigger -> trigger.amount));
            if (this.roomRows == null) {
                this.roomRows = new ArrayList<RoomRow>();
            }
            for (RoomRow row : this.roomRows) {
                if (row.items == null || row.items.isBlank()) {
                    row.items = row.amount + " \uc218\ub7c9 \ud488\ubaa9";
                }
                Main.roomItemCounts(row);
            }
            if (this.reverseRows == null) {
                this.reverseRows = new ArrayList<ReverseRow>();
            }
            if (this.pinRows == null) {
                this.pinRows = new ArrayList<PinRow>();
            }
            if (this.gameBalls == null) {
                this.gameBalls = new LinkedHashMap<String, Integer>();
            }
            if (this.streamerId == null) {
                this.streamerId = "";
            }
            return this;
        }
    }

    static final class StatusLabel
    extends JLabel {
        StatusLabel(String s) {
            super(s);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(this.getBackground());
            x.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.getHeight(), this.getHeight());
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class HeaderPanel
    extends JPanel {
        HeaderPanel(LayoutManager l) {
            super(l);
            this.setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setPaint(new GradientPaint(0.0f, 0.0f, Color.WHITE, this.getWidth(), this.getHeight(), new Color(255, 232, 215)));
            x.fillRect(0, 0, this.getWidth(), this.getHeight());
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class AvatarLabel
    extends JLabel {
        private final Image avatar = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/assets/kanashi-avatar-v3-rounded.png"))).getImage();

        AvatarLabel() {
            this.setPreferredSize(new Dimension(62, 62));
            this.setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(this.getWidth(), this.getHeight()) - 5;
            int left = (this.getWidth() - size) / 2;
            int top = (this.getHeight() - size) / 2;
            x.setColor(new Color(190, 82, 26, 28));
            x.fillRoundRect(left + 2, top + 3, size, size, 17, 17);
            x.drawImage(this.avatar, left, top, size, size, this);
            x.setColor(new Color(255, 171, 108));
            x.setStroke(new BasicStroke(2.0f));
            x.drawRoundRect(left, top, size - 1, size - 1, 16, 16);
            x.dispose();
        }
    }

    static final class ToggleSwitch
    extends JToggleButton {
        ToggleSwitch(String accessibleName) {
            this.setPreferredSize(new Dimension(68, 32));
            this.setMinimumSize(new Dimension(68, 32));
            this.setMaximumSize(new Dimension(68, 32));
            this.setFocusPainted(false);
            this.setBorderPainted(false);
            this.setContentAreaFilled(false);
            this.setToolTipText(accessibleName + " ON/OFF");
            this.getAccessibleContext().setAccessibleName(accessibleName);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean on = this.isSelected();
            Color track = on ? ACCENT : new Color(205, 198, 193);
            if (!this.isEnabled()) {
                track = on ? new Color(244, 177, 145) : new Color(226, 222, 219);
            }
            int height = this.getHeight();
            int knob = height - 8;
            int knobX = on ? this.getWidth() - knob - 4 : 4;
            x.setColor(track);
            x.fillRoundRect(0, 0, this.getWidth(), height, height, height);
            x.setColor(Color.WHITE);
            x.fillOval(knobX, 4, knob, knob);
            x.setFont(this.getFont().deriveFont(Font.BOLD, 10.0f));
            String stateText = on ? "ON" : "OFF";
            int textX = on ? 10 : knob + 10;
            int textY = (height - x.getFontMetrics().getHeight()) / 2 + x.getFontMetrics().getAscent();
            x.setColor(Color.WHITE);
            x.drawString(stateText, textX, textY);
            x.dispose();
        }
    }

    static final class GradientButton
    extends JButton {
        GradientButton(String text) {
            super(text);
            this.setForeground(Color.WHITE);
            this.setFont(this.getFont().deriveFont(1, 14.0f));
            this.setFocusPainted(false);
            this.setBorderPainted(false);
            this.setContentAreaFilled(false);
            this.setCursor(Cursor.getPredefinedCursor(12));
            this.setBorder(new EmptyBorder(12, 22, 12, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color start = this.getModel().isPressed() ? new Color(226, 83, 31) : new Color(255, 145, 80);
            Color end = this.getModel().isPressed() ? new Color(199, 61, 22) : new Color(238, 82, 35);
            x.setPaint(new GradientPaint(0.0f, 0.0f, start, this.getWidth(), this.getHeight(), end));
            x.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 18, 18);
            if (this.getModel().isRollover()) {
                x.setColor(new Color(255, 255, 255, 30));
                x.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 18, 18);
            }
            x.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedPanel
    extends JPanel {
        RoundedPanel() {
            this(new FlowLayout());
        }

        RoundedPanel(LayoutManager l) {
            super(l);
            this.setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(new Color(96, 57, 31, 20));
            x.fillRoundRect(5, 7, this.getWidth() - 10, this.getHeight() - 10, 30, 30);
            x.setColor(Color.WHITE);
            x.fillRoundRect(2, 2, this.getWidth() - 10, this.getHeight() - 10, 30, 30);
            x.setColor(new Color(255, 205, 169));
            x.setStroke(new BasicStroke(1.0f));
            x.drawRoundRect(2, 2, this.getWidth() - 10, this.getHeight() - 10, 30, 30);
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class RoomRow {
        String time;
        String source;
        String nick;
        String id;
        int amount;
        String message;
        String memo;
        String items;
        Map<String, Integer> itemCounts = new LinkedHashMap<String, Integer>();

        RoomRow(String t, String s, String n, String i, int a, String x, String m, String item) {
            this.time = t;
            this.source = s;
            this.nick = n;
            this.id = i;
            this.amount = a;
            this.message = x;
            this.memo = m;
            this.items = item;
            this.itemCounts.put(item, 1);
        }

        Object[] values() {
            return new Object[]{this.time, this.source, this.nick, this.id, this.amount, this.message, this.memo, "\ubc29\uc1a1\uad6d \uc774\ub3d9"};
        }
    }

    static final class ReverseRow {
        String time;
        String source;
        String nick;
        String id;
        String message;
        double percent;
        int amount;
        int calculated;

        ReverseRow(String t, String s, String n, String i, double p, int a, String m, int c) {
            this.time = t;
            this.source = s;
            this.nick = n;
            this.id = i;
            this.percent = p;
            this.amount = a;
            this.message = m;
            this.calculated = c;
        }

        Object[] values() {
            return new Object[]{this.time, this.source, this.nick, this.id, Main.trim(this.percent) + "%", this.amount, this.message, this.calculated, "\ubc29\uc1a1\uad6d \uc774\ub3d9"};
        }
    }

    static final class PinRow {
        String time;
        String source;
        String nick;
        String id;
        int amount;
        String game;
        int balls;

        PinRow(String t, String s, String n, String i, int a, String g, int b) {
            this.time = t;
            this.source = s;
            this.nick = n;
            this.id = i;
            this.amount = a;
            this.game = g;
            this.balls = b;
        }

        Object[] values() {
            return new Object[]{this.time, this.source, this.nick, this.id, this.amount, this.game, this.balls, "\ubc29\uc1a1\uad6d \uc774\ub3d9"};
        }
    }

    static final class DonorCardPanel
    extends JPanel {
        DonorCardPanel(LayoutManager layout) {
            super(layout);
            this.setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(new Color(126, 75, 39, 18));
            x.fillRoundRect(3, 5, this.getWidth() - 7, this.getHeight() - 7, 22, 22);
            x.setColor(Color.WHITE);
            x.fillRoundRect(1, 1, this.getWidth() - 6, this.getHeight() - 7, 22, 22);
            x.setColor(new Color(255, 207, 172));
            x.setStroke(new BasicStroke(1.0f));
            x.drawRoundRect(1, 1, this.getWidth() - 6, this.getHeight() - 7, 22, 22);
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class LargeDonationBadge
    extends JLabel {
        LargeDonationBadge(String text) {
            super(text, 0);
            this.setForeground(ACCENT_DARK);
            this.setFont(this.getFont().deriveFont(1, 14.0f));
            this.setBorder(new EmptyBorder(9, 15, 9, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(new Color(255, 230, 210));
            x.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 20, 20);
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class ListRowPanel
    extends JPanel {
        ListRowPanel(LayoutManager l) {
            super(l);
            this.setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D x = (Graphics2D)g.create();
            x.setColor(new Color(244, 232, 223));
            x.drawLine(8, this.getHeight() - 1, this.getWidth() - 8, this.getHeight() - 1);
            x.dispose();
        }
    }

    static final class NumberBadge
    extends JLabel {
        NumberBadge(int n) {
            super(String.valueOf(n), 0);
            this.setPreferredSize(new Dimension(36, 36));
            this.setForeground(new Color(112, 85, 176));
            this.setFont(this.getFont().deriveFont(1, 12.0f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(new Color(242, 237, 255));
            x.fillRoundRect(3, 3, this.getWidth() - 6, this.getHeight() - 6, 14, 14);
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class SoftBadge
    extends JLabel {
        private final Color fill;

        SoftBadge(String text, Color fill, Color foreground) {
            super(text);
            this.fill = fill;
            this.setForeground(foreground);
            this.setFont(this.getFont().deriveFont(1, 11.0f));
            this.setBorder(new EmptyBorder(7, 10, 7, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(this.fill);
            x.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 16, 16);
            x.dispose();
            super.paintComponent(g);
        }
    }

    static final class RouletteTrigger {
        int amount;
        int spins;

        RouletteTrigger(int amount, int spins) {
            this.amount = amount;
            this.spins = spins;
        }
    }

    record RouletteQueueResult(boolean queued, boolean collected) {
    }

    enum ChatViewerType {
        NORMAL,
        FAN,
        SUBSCRIBER,
        TOP_FAN
    }

    record Pending(String kind, String source, String time, String nick, String id, int amount, int balls, String item, long created, long donationToken, boolean skipMessage) {
    }

    static final class RouletteRule {
        String text;
        double probability;
        boolean collect;

        RouletteRule(String text, double probability) {
            this.text = text;
            this.probability = probability;
        }
    }

    record ChallengeGift(String userId, String nickname, int count) {
    }

    static final class RoundedButtonUI
    extends BasicButtonUI {
        RoundedButtonUI() {
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Color fill;
            AbstractButton button = (AbstractButton)c;
            Graphics2D x = (Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = fill = button.getModel().isSelected() ? ACCENT : button.getBackground();
            if (button.getModel().isPressed()) {
                fill = fill.darker();
            } else if (button.getModel().isRollover()) {
                fill = new Color(Math.min(255, fill.getRed() + 7), Math.min(255, fill.getGreen() + 7), Math.min(255, fill.getBlue() + 7));
            }
            x.setColor(fill);
            x.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
            x.dispose();
            super.paint(g, c);
        }
    }

    static final class ReverseRule {
        int minimum;
        double percent;

        ReverseRule(int m, double p) {
            this.minimum = m;
            this.percent = p;
        }
    }

    static final class PillTab
    extends JLabel {
        private boolean selected;

        PillTab(String s) {
            super(s, 0);
            this.setFont(this.getFont().deriveFont(1, 15.0f));
            this.setBorder(new EmptyBorder(10, 24, 10, 24));
            this.setOpaque(false);
        }

        void setSelected(boolean v) {
            this.selected = v;
            this.setForeground(v ? Color.WHITE : MUTED);
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.selected) {
                Graphics2D x = (Graphics2D)g.create();
                x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                x.setColor(ACCENT);
                x.fillRoundRect(0, 1, this.getWidth(), this.getHeight() - 2, this.getHeight(), this.getHeight());
                x.dispose();
            }
            super.paintComponent(g);
        }
    }
}
