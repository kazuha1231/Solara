package com.defendersofsolara.ui;

import com.defendersofsolara.characters.enemies.*;
import com.defendersofsolara.characters.heroes.*;
import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.PlayerProgress;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ✅ COMPLETE UNIFIED GAME UI WITH EPIC MENU
 *
 * Features:
 * - Sci-fi hexagonal menu design
 * - Full turn-based battle system
 * - World progression
 * - Settings and credits
 */
public class UnifiedGameUI extends JFrame {

    // ==================== CONSTANTS ====================

    public static final String SCREEN_MAIN_MENU = "main_menu";
    public static final String SCREEN_CREDITS = "credits";
    public static final String SCREEN_SETTINGS = "settings";
    public static final String SCREEN_NARRATIVE = "narrative";
    public static final String SCREEN_PROFILE_SELECT = "profile_select";
    public static final String SCREEN_WORLD_SELECT = "world_select";
    public static final String SCREEN_WORLD_STORY = "world_story";
    public static final String SCREEN_BATTLE = "battle";

    // ==================== STATE ====================

    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private final Map<Integer, ImageIcon> worldIcons = new HashMap<>();
    private String currentScreen = SCREEN_MAIN_MENU;

    private int selectedWorldId = 1;
    private Character[] playerTeam;
    private Character[] enemyTeam;
    private int currentPlayerIndex = 0;
    private Skill selectedSkill = null;
    private boolean waitingForTarget = false;

    private int currentWidth = 1280;
    private int currentHeight = 720;

    private static final int PROFILE_SLOTS = 4;
    private static final Path SAVE_DIR = Paths.get("profiles");
    private final PlayerProgress[] profileSlots = new PlayerProgress[PROFILE_SLOTS];
    private int activeProfile = -1;
    private PlayerProgress playerProgress;

    private static final double[] WORLD_HP_MULT = {1.0, 1.45, 1.95, 2.6, 3.4};
    private static final double[] WORLD_MANA_MULT = {1.0, 1.3, 1.55, 1.9, 2.3};
    private static final double[] WORLD_ATK_MULT = {1.0, 1.35, 1.75, 2.2, 2.6};
    private static final double[] WORLD_DEF_MULT = {1.0, 1.3, 1.6, 2.0, 2.4};
    private static final int[] WORLD_ENEMY_LEVEL = {4, 7, 11, 15, 22};
    private static final int WAVES_PER_WORLD = 10;
    private static final List<List<MinionTemplate>> MINION_POOLS = createMinionPools();

    // Fade transition
    private float fadeAlpha = 0f;
    private boolean isFading = false;
    private String targetScreen = null;
    private javax.swing.Timer fadeTimer;

    // Battle components
    private JLabel battleTurnLabel;
    private JLabel battleInstructionLabel;
    private JLabel battleWaveLabel;
    private JTextArea battleLog;
    private JPanel battleSkillPanel;
    private JPanel battlePlayerPanel;
    private JPanel battleEnemyPanel;

    private final Random random = new Random();
    private List<WaveEncounter> currentWavePlan = new ArrayList<>();
    private int activeWaveIndex = 0;

    // ==================== CONSTRUCTOR ====================

    public UnifiedGameUI() {
        setTitle("Defenders of Solara");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(currentWidth, currentHeight);
        setLocationRelativeTo(null);
        setResizable(false);
        configureDisplayScale();
        initializeProfiles();
        loadWorldIcons();
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createEpicMainMenu(), SCREEN_MAIN_MENU);
        mainContainer.add(createCreditsMenu(), SCREEN_CREDITS);
        mainContainer.add(createSettingsMenu(), SCREEN_SETTINGS);
        mainContainer.add(createNarrativeIntro(), SCREEN_NARRATIVE);
        mainContainer.add(createProfileSelect(), SCREEN_PROFILE_SELECT);
        mainContainer.add(createWorldSelection(), SCREEN_WORLD_SELECT);

        setContentPane(mainContainer);

        // Glass pane for fade overlay
        JComponent glass = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isFading && fadeAlpha <= 0f) return;
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0f, 0f, 0f, Math.min(1f, Math.max(0f, fadeAlpha))));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        setGlassPane(glass);
        glass.setVisible(true);
        updateCurrentScreen(SCREEN_MAIN_MENU);
        showScreen(SCREEN_MAIN_MENU);

        setVisible(true);
        setupGlobalKeyBindings();
    }
    
    private void updateCurrentScreen(String screenName) {
        currentScreen = screenName;
    }

    // ==================== NAVIGATION ====================

    public void showScreen(String screenName) {
        if (SCREEN_PROFILE_SELECT.equals(screenName)) {
            refreshProfileSelect();
        } else if (SCREEN_WORLD_SELECT.equals(screenName)) {
            refreshWorldSelection();
        }
        startFadeTo(screenName);
    }

    PlayerProgress getPlayerProgress() {
        return playerProgress;
    }

    PlayerProgress getProfileProgress(int slotIndex) {
        int idx = slotIndex - 1;
        if (idx < 0 || idx >= PROFILE_SLOTS) return null;
        return profileSlots[idx];
    }

    int getActiveProfileIndex() {
        return activeProfile;
    }

    int getProfileSlotCount() {
        return PROFILE_SLOTS;
    }

    // for Start Game flow
    void onProfileSelected(int profileIndex) {
        int idx = Math.max(1, Math.min(PROFILE_SLOTS, profileIndex)) - 1;
        if (activeProfile != idx) {
            saveActiveProfile();
            activeProfile = idx;
            playerProgress = profileSlots[activeProfile];
        }
        showScreen(SCREEN_WORLD_SELECT);
    }

    private void startFadeTo(String screenName) {
        if (isFading) {
            // queue latest target
            targetScreen = screenName;
            return;
        }
        isFading = true;
        targetScreen = screenName;

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        fadeAlpha = 0f;
        getGlassPane().repaint();

        fadeTimer = new javax.swing.Timer(16, null); // ~60 FPS
        fadeTimer.addActionListener(new ActionListener() {
            private boolean switched = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!switched) {
                    fadeAlpha += 0.07f;
                    if (fadeAlpha >= 1f) {
                        fadeAlpha = 1f;
                        // switch screen at full black
                        cardLayout.show(mainContainer, targetScreen);
                        updateCurrentScreen(targetScreen);
                        switched = true;
                    }
                } else {
                    fadeAlpha -= 0.07f;
                    if (fadeAlpha <= 0f) {
                        fadeAlpha = 0f;
                        isFading = false;
                        fadeTimer.stop();
                    }
                }
                getGlassPane().repaint();
            }
        });
        fadeTimer.start();
    }

    private void showWorldStory(int worldId) {
        if (!playerProgress.canEnterWorld(worldId)) {
            JOptionPane.showMessageDialog(this,
                "You do not meet the requirements for World " + worldId + ".",
                "Locked",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        selectedWorldId = worldId;

        for (Component comp : mainContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(SCREEN_WORLD_STORY)) {
                mainContainer.remove(comp);
                break;
            }
        }

        JPanel worldStoryPanel = createWorldStory(worldId);
        worldStoryPanel.setName(SCREEN_WORLD_STORY);
        mainContainer.add(worldStoryPanel, SCREEN_WORLD_STORY);
        showScreen(SCREEN_WORLD_STORY);
    }

    private void showBattle(int worldId) {
        if (!playerProgress.canEnterWorld(worldId)) {
            JOptionPane.showMessageDialog(this,
                "You do not meet the requirements for this world.",
                "Locked",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        selectedWorldId = worldId;

        for (Component comp : mainContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(SCREEN_BATTLE)) {
                mainContainer.remove(comp);
                break;
            }
        }

        JPanel battlePanel = createBattle(worldId);
        battlePanel.setName(SCREEN_BATTLE);
        mainContainer.add(battlePanel, SCREEN_BATTLE);
        showScreen(SCREEN_BATTLE);
    }

    // ==================== BACKGROUND ====================
    // Background is now a solid dark color matching the reference style

    private void initializeProfiles() {
        try {
            Files.createDirectories(SAVE_DIR);
        } catch (IOException ignored) { }

        for (int i = 0; i < PROFILE_SLOTS; i++) {
            Path file = SAVE_DIR.resolve("profile" + (i + 1) + ".dat");
            PlayerProgress data = PlayerProgress.load(file);
            if (data == null) {
                data = new PlayerProgress();
            }
            profileSlots[i] = data;
        }
        activeProfile = 0;
        playerProgress = profileSlots[activeProfile];
    }

    private void loadWorldIcons() {
        worldIcons.clear();
        String[] resources = {
            "/image/ChronovaleWorld.gif",
            "/image/GravemireWorld.gif",
            "/image/AetherionWorld.gif",
            "/image/ElarionWorld.gif",
            "/image/Umbros.gif"
        };
        System.out.println("Loading world icons...");
        for (int i = 0; i < resources.length; i++) {
            ImageIcon icon = loadWorldIcon(resources[i]);
            if (icon != null) {
                worldIcons.put(i + 1, icon);
                System.out.println("  ✓ World " + (i + 1) + " icon loaded");
            } else {
                System.err.println("  ✗ World " + (i + 1) + " icon failed to load");
            }
        }
        System.out.println("Total icons loaded: " + worldIcons.size() + "/5");
    }

    private ImageIcon loadWorldIcon(String resourcePath) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("ERROR: Resource not found: " + resourcePath);
                return null;
            }
            
            // Load GIF directly as ImageIcon to preserve animation
            // Don't scale here - we'll handle sizing in the JLabel
            ImageIcon icon = new ImageIcon(url);
            
            // Check if icon loaded successfully
            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                System.err.println("ERROR: Invalid icon dimensions for: " + resourcePath);
                return null;
            }
            
            System.out.println("✓ Loaded: " + resourcePath + " (" + icon.getIconWidth() + "x" + icon.getIconHeight() + ")");
            return icon;
        } catch (Exception e) {
            System.err.println("ERROR loading world art: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a scaled ImageIcon that preserves GIF animation.
     * Uses a custom ImageIcon that scales during rendering without breaking animation.
     */
    private ImageIcon createScaledAnimatedIcon(ImageIcon original, int targetSize) {
        if (original == null || original.getIconWidth() <= 0) {
            return original;
        }
        
        // Store reference to original for animation
        final Image originalImage = original.getImage();
        
        // Create a custom ImageIcon that scales the animated image during painting
        // This preserves the animation because we're drawing the original animated Image
        return new ImageIcon() {
            @Override
            public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                if (originalImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Draw the animated image scaled - this preserves animation
                    g2d.drawImage(originalImage, x, y, targetSize, targetSize, c);
                    g2d.dispose();
                }
            }
            
            @Override
            public int getIconWidth() {
                return targetSize;
            }
            
            @Override
            public int getIconHeight() {
                return targetSize;
            }
            
            @Override
            public Image getImage() {
                return originalImage;
            }
        };
    }

    /**
     * Utility for other panels to paint the shared background consistently.
     */
    void paintBackground(Graphics2D g2d, int width, int height) {
        // Very dark blue-gray/charcoal background matching reference
        g2d.setColor(UITheme.BG_DARK_TEAL);
        g2d.fillRect(0, 0, width, height);
    }

    private JPanel createBackgroundPanel() {
        return new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

                paintBackground(g2d, getWidth(), getHeight());
            }
        };
            }

    // ==================== PROFILE SELECT ====================

    private JPanel createProfileSelect() {
        JPanel profile = new ProfileUI(this);
        profile.setName(SCREEN_PROFILE_SELECT);
        return profile;
    }

    // ==================== EPIC MAIN MENU ====================

    private JPanel createEpicMainMenu() {
        return new MenuUI(this);
    }

    // ==================== CREDITS ====================

    private JPanel createCreditsMenu() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        JLabel credits = new JLabel(
            "<html><center>" +
                "<font size='6' color='#00FFFF'><b>DEFENDERS OF SOLARA</b></font><br><br>" +
                "<font size='4' color='#00FFFF'>" +
                "Group Leader:<br>John Warren Pansacala<br><br>" +
                "Members:<br>" +
                "Veinz Pius N. Escuzar<br>" +
                "Denzel B. Valendez<br>" +
                "Kim Kyle M. Paran<br>" +
                "Rushaine A. Tura<br><br>" +
                "Engine: Java Swing<br>" +
                "Version: 1.0" +
                "</font></center></html>"
        );
        gbc.gridy = 0;
        panel.add(credits, gbc);

        JButton backBtn = UITheme.createButton("BACK");
        backBtn.addActionListener(e -> returnToMainMenu());
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 15, 15, 15);
        panel.add(backBtn, gbc);

        return panel;
    }

    // ==================== SETTINGS ====================

    private JPanel createSettingsMenu() {
        JPanel root = createBackgroundPanel();
        root.setLayout(new BorderLayout());

        JLabel title = new JLabel(
            "<html><center><font size='6' color='#00FFFF'><b>OPTIONS</b></font></center></html>"
        );
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(20, 10, 10, 10));

        // Transparent tabbed pane so the background art shows through
        JTabbedPane tabs = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                // Do not paint default white background
            }
        };
        tabs.setOpaque(false);
        tabs.setBackground(new Color(0, 0, 0, 0));

        // Video tab
        JPanel videoPanel = new JPanel(new GridBagLayout());
        videoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JCheckBox fullscreenCheck = new JCheckBox("Fullscreen (Borderless)");
        fullscreenCheck.setFont(UITheme.FONT_BUTTON);
        fullscreenCheck.setForeground(UITheme.PRIMARY_CYAN);
        fullscreenCheck.setOpaque(false);
        gbc.gridy = 0;
        videoPanel.add(fullscreenCheck, gbc);

        String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080"};
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions);
        resolutionBox.setFont(UITheme.FONT_BUTTON);
        resolutionBox.setForeground(UITheme.PRIMARY_CYAN);
        resolutionBox.setBackground(UITheme.BG_BUTTON);
        resolutionBox.setSelectedIndex(2);
        gbc.gridy = 1;
        videoPanel.add(resolutionBox, gbc);

        JButton applyVideoBtn = UITheme.createButton("APPLY VIDEO");
        applyVideoBtn.addActionListener(e ->
            applySettings(fullscreenCheck.isSelected(), (String) resolutionBox.getSelectedItem())
        );
        gbc.gridy = 2;
        videoPanel.add(applyVideoBtn, gbc);

        // Audio tab (simple sliders, no real audio backend yet)
        JPanel audioPanel = new JPanel();
        audioPanel.setOpaque(false);
        audioPanel.setLayout(new GridBagLayout());
        GridBagConstraints agbc = new GridBagConstraints();
        agbc.gridx = 0;
        agbc.insets = new Insets(10, 10, 10, 10);
        agbc.anchor = GridBagConstraints.WEST;

        JLabel masterLabel = new JLabel("Master Volume");
        masterLabel.setForeground(UITheme.PRIMARY_CYAN);
        agbc.gridy = 0;
        audioPanel.add(masterLabel, agbc);

        JSlider masterSlider = new JSlider(0, 100, 80);
        masterSlider.setOpaque(false);
        masterSlider.setPreferredSize(new Dimension(250, 40));
        agbc.gridy = 1;
        audioPanel.add(masterSlider, agbc);

        JLabel musicLabel = new JLabel("Music Volume");
        musicLabel.setForeground(UITheme.PRIMARY_CYAN);
        agbc.gridy = 2;
        audioPanel.add(musicLabel, agbc);

        JSlider musicSlider = new JSlider(0, 100, 80);
        musicSlider.setOpaque(false);
        musicSlider.setPreferredSize(new Dimension(250, 40));
        agbc.gridy = 3;
        audioPanel.add(musicSlider, agbc);

        JLabel sfxLabel = new JLabel("SFX Volume");
        sfxLabel.setForeground(UITheme.PRIMARY_CYAN);
        agbc.gridy = 4;
        audioPanel.add(sfxLabel, agbc);

        JSlider sfxSlider = new JSlider(0, 100, 80);
        sfxSlider.setOpaque(false);
        sfxSlider.setPreferredSize(new Dimension(250, 40));
        agbc.gridy = 5;
        audioPanel.add(sfxSlider, agbc);

        tabs.addTab("Video", videoPanel);
        tabs.addTab("Audio", audioPanel);

        JButton backBtn = UITheme.createButton("BACK");
        backBtn.addActionListener(e -> returnToMainMenu());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(backBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    private void applySettings(boolean fullscreen, String resolution) {
        try {
            if (!fullscreen && resolution != null) {
                String[] dims = resolution.split("x");
                currentWidth = Integer.parseInt(dims[0]);
                currentHeight = Integer.parseInt(dims[1]);
            }

            dispose();

            if (fullscreen) {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
                setUndecorated(true);
                setSize(Toolkit.getDefaultToolkit().getScreenSize());
                gd.setFullScreenWindow(this);
            } else {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
                gd.setFullScreenWindow(null);
                setUndecorated(false);
                setSize(currentWidth, currentHeight);
                setLocationRelativeTo(null);
            }

            setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Settings Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Public method for UIVisual compatibility
    public void applyResolution(int resIdx) {
        String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080"};
        boolean isFullscreen = (resIdx == 4);

        if (isFullscreen) {
            applySettings(true, null);
        } else if (resIdx >= 0 && resIdx < resolutions.length) {
            applySettings(false, resolutions[resIdx]);
        }
    }

    // ==================== NARRATIVE ====================

    private JPanel createNarrativeIntro() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new BorderLayout());

        JTextArea storyText = new JTextArea();
        storyText.setFont(UITheme.FONT_TEXT_LARGE);
        storyText.setForeground(UITheme.PRIMARY_ORANGE);
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setOpaque(false);
        storyText.setMargin(new Insets(40, 60, 40, 60));

        JPanel textPanel = UITheme.createOverlayPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        textPanel.add(storyText, BorderLayout.CENTER);

        JButton skipBtn = UITheme.createSmallButton("SKIP >");
        skipBtn.addActionListener(e -> showScreen(SCREEN_WORLD_SELECT));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(skipBtn);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        String[] storyLines = {
            "In the depths of space...\n\n",
            "A galactic warlord named Xyrrak the Devourer has unleashed ",
            "an army of bio-mechanical horrors to conquer the Veil System.\n\n",
            "Each of its five worlds holds a Core of Balance.\n\n",
            "The fate of the galaxy rests in your hands...\n\n"
        };

        javax.swing.Timer typewriterTimer = new javax.swing.Timer(50, null);
        typewriterTimer.addActionListener(new ActionListener() {
            private int lineIndex = 0;
            private int charIndex = 0;
            private StringBuilder currentText = new StringBuilder();

            @Override
            public void actionPerformed(ActionEvent e) {
                if (lineIndex < storyLines.length) {
                    if (charIndex < storyLines[lineIndex].length()) {
                        currentText.append(storyLines[lineIndex].charAt(charIndex));
                        storyText.setText(currentText.toString());
                        charIndex++;
                    } else {
                        lineIndex++;
                        charIndex = 0;
                    }
                } else {
                    typewriterTimer.stop();
                    javax.swing.Timer autoStart = new javax.swing.Timer(2000, evt -> {
                        showScreen(SCREEN_WORLD_SELECT);
                        ((javax.swing.Timer) evt.getSource()).stop();
                    });
                    autoStart.setRepeats(false);
                    autoStart.start();
                }
            }
        });

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                storyText.setText("");
                typewriterTimer.restart();
            }
        });

        return panel;
    }

    // ==================== WORLD SELECTION ====================

    private JPanel createWorldSelection() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JLabel title = UITheme.createTitle("SELECT YOUR WORLD");
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel levelInfo = new JLabel(
            String.format("Level %d   |   EXP %d / %d",
                playerProgress.getPlayerLevel(),
                playerProgress.getCurrentExp(),
                playerProgress.getExpToNext()
            ),
            SwingConstants.CENTER
        );
        levelInfo.setForeground(UITheme.PRIMARY_ORANGE);
        levelInfo.setFont(UITheme.FONT_TEXT);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(levelInfo);

        JPanel worldsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        worldsPanel.setOpaque(false);
        worldsPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        String[] worldNames = {"CHRONOVALE", "GRAVEMIRE", "AETHERION", "ELARION", "UMBROS"};
        String[] worldCores = {"Time", "Gravity", "Energy", "Life", "Void"};

        for (int i = 0; i < 5; i++) {
            final int worldId = i + 1;
            boolean isUnlocked = playerProgress.canEnterWorld(worldId);
            JPanel worldCard = createWorldCard(worldId, worldNames[i], worldCores[i], isUnlocked);
            worldsPanel.add(worldCard);
        }

        JButton backBtn = UITheme.createButton("BACK TO MENU");
        backBtn.addActionListener(e -> returnToMainMenu());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backBtn);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(worldsPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.setName(SCREEN_WORLD_SELECT);

        return panel;
    }

    private JPanel createWorldCard(int worldId, String name, String core, boolean isUnlocked) {
        // Simple palette-based card with transparency control
        final boolean[] isHovered = {false};
        int prefW = Math.round(220 * UITheme.getScaleFactor());
        int prefH = Math.round(320 * UITheme.getScaleFactor());
        
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Use nearest neighbor for pixel-art look
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                // Dark background
                Color bg = isUnlocked 
                    ? (isHovered[0] ? new Color(UITheme.BG_CARD.getRed() + 10, UITheme.BG_CARD.getGreen() + 10, UITheme.BG_CARD.getBlue() + 10)
                                     : UITheme.BG_CARD)
                    : new Color(UITheme.BG_CARD.getRed() - 10, UITheme.BG_CARD.getGreen() - 10, UITheme.BG_CARD.getBlue() - 10);
                
                g2d.setColor(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Use Panel asset (includes background and border) - matching reference style
                BufferedImage panelImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
                if (panelImg != null) {
                    float alpha = isUnlocked ? (isHovered[0] ? 1.0f : 0.9f) : 0.5f;
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    PixelArtUI.drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: dark background with border
                    g2d.setColor(UITheme.BG_CARD);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                    if (borderImg != null) {
                        float alpha = isUnlocked ? (isHovered[0] ? 1.0f : 0.9f) : 0.5f;
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                    } else {
                        Color borderColor = isUnlocked && isHovered[0] 
                            ? UITheme.BORDER_HIGHLIGHT 
                            : isUnlocked 
                                ? UITheme.BORDER_NORMAL
                                : new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 100);
                        g2d.setColor(borderColor);
                        g2d.setStroke(new BasicStroke(2f));
                        g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                    }
                }
                
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(prefW, prefH));
        card.setCursor(isUnlocked ?
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
            Cursor.getDefaultCursor()
        );

        JLabel worldLabel = new JLabel("WORLD " + worldId, SwingConstants.CENTER);
        worldLabel.setFont(UITheme.FONT_HEADER);
        worldLabel.setForeground(isUnlocked ? UITheme.PRIMARY_GREEN : UITheme.TEXT_GRAY);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(UITheme.FONT_TEXT);
        nameLabel.setForeground(isUnlocked ? UITheme.PRIMARY_WHITE : UITheme.TEXT_GRAY);

        JLabel coreLabel = new JLabel("Core: " + core, SwingConstants.CENTER);
        coreLabel.setFont(UITheme.FONT_TEXT);
        coreLabel.setForeground(isUnlocked ? UITheme.PRIMARY_YELLOW : UITheme.TEXT_GRAY);

        boolean meetsLevel = playerProgress.getPlayerLevel() >= playerProgress.getWorldRequirement(worldId);
        boolean clearedPrev = worldId == 1 || playerProgress.hasClearedWorld(worldId - 1);
        String statusText;
        if (isUnlocked) {
            statusText = "Ready for battle";
        } else if (!meetsLevel) {
            statusText = "Requires Level " + playerProgress.getWorldRequirement(worldId);
        } else if (!clearedPrev) {
            statusText = "Clear World " + (worldId - 1) + " first";
        } else {
            statusText = "Locked";
        }
        JLabel statusLabel = new JLabel(statusText, SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(isUnlocked ? UITheme.PRIMARY_ORANGE : UITheme.TEXT_GRAY);
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        ImageIcon animatedIcon = worldIcons.get(worldId);
        JLabel iconLabel = null;
        
        if (animatedIcon != null) {
            // Create a scaled version that preserves animation
            int targetSize = Math.round(110 * UITheme.getScaleFactor());
            final ImageIcon scaledAnimatedIcon = createScaledAnimatedIcon(animatedIcon, targetSize);
            
            System.out.println("Creating icon label for World " + worldId + " (size: " + targetSize + ")");
            
            // Always show the icon - will be animated GIF (works for both locked and unlocked)
            iconLabel = new JLabel(scaledAnimatedIcon, SwingConstants.CENTER);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
            iconLabel.setOpaque(false);
            iconLabel.setVisible(true); // Explicitly make it visible
            // Add icon FIRST so it appears at the top
            contentPanel.add(iconLabel);
            System.out.println("  ✓ Icon label added to World " + worldId + " card");
        } else {
            // Debug: check if icon failed to load
            System.err.println("Warning: No icon found for World " + worldId + " (total loaded: " + worldIcons.size() + ")");
            // Add spacer at the top if no icon
            contentPanel.add(Box.createVerticalStrut(20));
        }

        worldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        coreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(worldLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(coreLabel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(statusLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        // Store reference to iconLabel for hover animation control
        final JLabel finalIconLabel = iconLabel;
        
        if (isUnlocked) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showWorldStory(worldId);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered[0] = true;
                    card.repaint();
                    // Icon is already showing and animating
                    if (finalIconLabel != null) {
                        card.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered[0] = false;
                    card.repaint();
                    // Icon continues to show and animate
                    if (finalIconLabel != null) {
                        card.repaint();
                    }
                }
            });
        }

        return card;
    }

    // ==================== WORLD STORY ====================

    private JPanel createWorldStory(int worldId) {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new BorderLayout());

        JTextArea storyText = new JTextArea();
        storyText.setFont(UITheme.FONT_TEXT);
        storyText.setForeground(UITheme.PRIMARY_ORANGE);
        storyText.setBackground(new Color(0, 0, 0, 0));
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setMargin(new Insets(40, 60, 40, 60));
        storyText.setOpaque(false);
        storyText.setText("WORLD " + worldId + "\n\nPrepare for battle...");

        JPanel textPanel = UITheme.createOverlayPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        textPanel.add(storyText, BorderLayout.CENTER);

        JButton continueBtn = UITheme.createButton("CONTINUE >");
        continueBtn.addActionListener(e -> showBattle(worldId));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(continueBtn);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== BATTLE SYSTEM ====================

    private JPanel createBattle(int worldId) {
        playerTeam = createPlayerTeam();
        currentWavePlan = buildWaveSchedule(worldId);
        if (currentWavePlan.isEmpty()) {
            currentWavePlan.add(new WaveEncounter(1, false, legacyEnemyPack(worldId)));
        }
        activeWaveIndex = 0;
        WaveEncounter openingWave = currentWavePlan.get(0);
        enemyTeam = openingWave.enemies;
        applyEnemyScaling(worldId, openingWave);
        currentPlayerIndex = 0;
        selectedSkill = null;
        waitingForTarget = false;

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(createBattleTopPanel(worldId), BorderLayout.NORTH);
        panel.add(createBattleCenterPanel(), BorderLayout.CENTER);
        panel.add(createBattleBottomPanel(), BorderLayout.SOUTH);

        javax.swing.Timer startTimer = new javax.swing.Timer(500, e -> {
            startBattle();
            ((javax.swing.Timer) e.getSource()).stop();
        });
        startTimer.setRepeats(false);
        startTimer.start();

        return panel;
    }

    private List<WaveEncounter> buildWaveSchedule(int worldId) {
        List<WaveEncounter> waves = new ArrayList<>();
        for (int wave = 1; wave <= WAVES_PER_WORLD; wave++) {
            boolean bossWave = wave == WAVES_PER_WORLD;
            Character[] enemies = bossWave
                ? buildBossWave(worldId)
                : buildMinionWave(worldId, wave);
            waves.add(new WaveEncounter(wave, bossWave, enemies));
        }
        return waves;
    }

    private Character[] buildMinionWave(int worldId, int waveNumber) {
        List<MinionTemplate> pool = getMinionPool(worldId);
        int count = 2 + random.nextInt(4); // 2-5 minions
        Character[] enemies = new Character[count];
        int index = Math.max(0, Math.min(worldId - 1, WORLD_ENEMY_LEVEL.length - 1));
        int levelTarget = WORLD_ENEMY_LEVEL[index] + (waveNumber / 2);
        double difficulty = 1.0 + (waveNumber - 1) * 0.05;

        if (pool.isEmpty()) {
            // Fallback: create a basic enemy if pool is empty
            System.err.println("Warning: Minion pool is empty for world " + worldId);
            for (int i = 0; i < count; i++) {
                // Create a basic minion with default stats
                DynamicEnemy minion = new DynamicEnemy(
                    "Minion",
                    100 + levelTarget * 20,
                    50 + levelTarget * 10,
                    20 + levelTarget * 3,
                    15 + levelTarget * 2,
                    30 + levelTarget,
                    () -> new SavageSwipeSkill("Strike", 1.0)
                );
                minion.syncToLevel(levelTarget);
                minion.applyStatMultiplier(difficulty, difficulty, difficulty, difficulty);
                enemies[i] = minion;
            }
        } else {
            for (int i = 0; i < count; i++) {
                MinionTemplate template = pool.get(random.nextInt(pool.size()));
                enemies[i] = template.instantiate(levelTarget, difficulty);
            }
        }
        return enemies;
    }

    private Character[] buildBossWave(int worldId) {
        List<Character> roster = new ArrayList<>();
        Character boss = createBossForWorld(worldId);
        roster.add(boss);

        int supporters = 1 + random.nextInt(2); // 1-2 supports
        List<MinionTemplate> pool = getMinionPool(worldId);
        if (!pool.isEmpty()) {
            for (int i = 0; i < supporters; i++) {
                MinionTemplate template = pool.get(random.nextInt(pool.size()));
                roster.add(template.instantiate(
                    WORLD_ENEMY_LEVEL[Math.min(worldId - 1, WORLD_ENEMY_LEVEL.length - 1)] + 2,
                    1.25
                ));
            }
        }
        return roster.toArray(new Character[0]);
    }

    private Character createBossForWorld(int worldId) {
        switch (worldId) {
            case 1:
                return buildDynamicBoss("Chronovale Tyrant", 950, 280, 110, 55, 40);
            case 2:
                return buildDynamicBoss("Gravemire Behemoth", 1200, 320, 135, 65, 42);
            case 3:
                return buildDynamicBoss("Aetherion Ascendant", 1400, 380, 150, 75, 48);
            case 4:
                return buildDynamicBoss("Elarion Warden", 1650, 420, 170, 85, 52);
            case 5:
            default:
                return buildDynamicBoss("Umbros Voidcaller", 1900, 500, 195, 95, 58);
        }
    }

    private Character buildDynamicBoss(String name, int hp, int mana, int atk, int def, int speed) {
        DynamicEnemy boss = new DynamicEnemy(name, hp, mana, atk, def, speed,
            () -> new SavageSwipeSkill("Obliterate", 1.35),
            () -> new VenomSplashSkill("Cataclysm Pulse", 0.75, 40, 3),
            () -> new SoulDrainSkill("Soul Rend", 1.1, 0.4),
            () -> new AegisPulseSkill("Call of Dominion", 160)
        );
        boss.syncToLevel(20);
        boss.applyStatMultiplier(1.35, 1.2, 1.25, 1.2);
        return boss;
    }

    private List<MinionTemplate> getMinionPool(int worldId) {
        int index = Math.max(0, Math.min(worldId - 1, MINION_POOLS.size() - 1));
        return MINION_POOLS.get(index);
    }

    private static List<List<MinionTemplate>> createMinionPools() {
        List<List<MinionTemplate>> pools = new ArrayList<>();

        List<MinionTemplate> world1 = List.of(
            new MinionTemplate("Chrono Scout", 360, 120, 45, 18, 34,
                () -> new SavageSwipeSkill("Pulse Slash", 0.9),
                () -> new VenomSplashSkill("Time Burst", 0.55, 12, 2)
            ),
            new MinionTemplate("Veil Runner", 400, 150, 50, 22, 36,
                () -> new SavageSwipeSkill("Veil Strike", 1.0),
                () -> new SoulDrainSkill("Echo Drain", 0.8, 0.25)
            ),
            new MinionTemplate("Temporal Wisp", 320, 180, 38, 18, 40,
                () -> new VenomSplashSkill("Temporal Shock", 0.5, 18, 2),
                () -> new AegisPulseSkill("Serene Glow", 80)
            )
        );
        pools.add(world1);

        List<MinionTemplate> world2 = List.of(
            new MinionTemplate("Grave Stalker", 500, 160, 60, 26, 34,
                () -> new SavageSwipeSkill("Bone Cleaver", 1.05),
                () -> new SoulDrainSkill("Grave Leech", 0.9, 0.35)
            ),
            new MinionTemplate("Mire Shaman", 450, 220, 48, 24, 30,
                () -> new VenomSplashSkill("Mire Surge", 0.6, 22, 2),
                () -> new AegisPulseSkill("Mud Ward", 110)
            ),
            new MinionTemplate("Wailing Husk", 520, 150, 58, 28, 28,
                () -> new SavageSwipeSkill("Dirge Swipe", 1.1)
            )
        );
        pools.add(world2);

        List<MinionTemplate> world3 = List.of(
            new MinionTemplate("Aether Shade", 620, 240, 72, 36, 42,
                () -> new SavageSwipeSkill("Radiant Slice", 1.2),
                () -> new VenomSplashSkill("Nova Bloom", 0.7, 26, 2)
            ),
            new MinionTemplate("Arc Warden", 570, 300, 68, 34, 34,
                () -> new SoulDrainSkill("Arc Flay", 1.0, 0.35),
                () -> new AegisPulseSkill("Shield Matrix", 140)
            ),
            new MinionTemplate("Skyblade", 600, 260, 74, 32, 46,
                () -> new SavageSwipeSkill("Skyfall", 1.25)
            )
        );
        pools.add(world3);

        List<MinionTemplate> world4 = List.of(
            new MinionTemplate("Elarion Sentinel", 780, 300, 90, 44, 38,
                () -> new SavageSwipeSkill("Spear Barrage", 1.3),
                () -> new AegisPulseSkill("Renewing Chant", 160)
            ),
            new MinionTemplate("Verdant Binder", 720, 360, 84, 40, 34,
                () -> new VenomSplashSkill("Root Lash", 0.8, 30, 2),
                () -> new SoulDrainSkill("Bloom Sap", 1.0, 0.4)
            ),
            new MinionTemplate("Grove Phantom", 760, 320, 88, 42, 44,
                () -> new SavageSwipeSkill("Phantom Tear", 1.35)
            )
        );
        pools.add(world4);

        List<MinionTemplate> world5 = List.of(
            new MinionTemplate("Umbra Corsair", 900, 360, 105, 50, 44,
                () -> new SavageSwipeSkill("Umbra Rend", 1.4),
                () -> new VenomSplashSkill("Obsidian Torrent", 0.9, 34, 2)
            ),
            new MinionTemplate("Void Priest", 880, 420, 98, 48, 40,
                () -> new SoulDrainSkill("Void Siphon", 1.1, 0.45),
                () -> new AegisPulseSkill("Rite of Night", 200)
            ),
            new MinionTemplate("Night Harbinger", 940, 380, 110, 52, 46,
                () -> new SavageSwipeSkill("Harbinger Edge", 1.5)
            )
        );
        pools.add(world5);

        return pools;
    }

    private Character[] legacyEnemyPack(int worldId) {
        switch (worldId) {
            case 1: return new Character[]{new BiomechanicalAlien(), new GravityBeast()};
            case 2: return new Character[]{new GravityBeast(), new BiomechanicalAlien(), new GravityBeast()};
            case 3: return new Character[]{new XyrrakTheDevourer()};
            case 4: return new Character[]{new GravityBeast(), new XyrrakTheDevourer()};
            case 5: return new Character[]{new XyrrakTheDevourer(), new XyrrakTheDevourer()};
            default: return new Character[]{new BiomechanicalAlien()};
        }
    }

    private Character[] createPlayerTeam() {
        List<Character> roster = new ArrayList<>();
        roster.add(new KaelDraven());
        roster.add(new VioraNyla());
        roster.add(new YlonneKryx());

        if (playerProgress != null && playerProgress.isZyraUnlocked()) {
            roster.add(new ZyraKathun());
        }

        int playerLevel = playerProgress != null ? playerProgress.getPlayerLevel() : 1;
        for (Character hero : roster) {
            hero.syncToLevel(playerLevel);
        }
        return roster.toArray(new Character[0]);
    }

    private void applyEnemyScaling(int worldId, WaveEncounter wave) {
        int index = Math.max(0, Math.min(worldId - 1, WORLD_HP_MULT.length - 1));
        double hpMultiplier = WORLD_HP_MULT[index];
        double manaMultiplier = WORLD_MANA_MULT[index];
        double attackMultiplier = WORLD_ATK_MULT[index];
        double defenseMultiplier = WORLD_DEF_MULT[index];
        int baseLevel = WORLD_ENEMY_LEVEL[index];

        double waveScalar = wave != null ? 1.0 + (wave.waveNumber - 1) * 0.07 : 1.0;
        if (wave != null && wave.bossWave) {
            waveScalar += 0.25;
        }

        for (int i = 0; i < enemyTeam.length; i++) {
            Character enemy = enemyTeam[i];
            int levelTarget = Math.min(50, baseLevel + i * 2);
            enemy.syncToLevel(levelTarget);

            double hpMult = hpMultiplier * waveScalar;
            double atkMult = attackMultiplier * waveScalar;
            double defMult = defenseMultiplier * waveScalar;
            if (i == enemyTeam.length - 1) {
                hpMult += 0.3;
                atkMult += 0.25;
                defMult += 0.2;
            }
            enemy.applyStatMultiplier(hpMult, manaMultiplier, atkMult, defMult);
        }
    }

    private int calculateExpReward(int worldId) {
        int base = 150;
        int difficultyBonus = (worldId - 1) * 120;
        int enemyCountBonus = enemyTeam != null ? enemyTeam.length * 40 : 0;
        int waveBonus = WAVES_PER_WORLD * 50;
        return base + difficultyBonus + enemyCountBonus + waveBonus;
    }

    private void rewardBattleVictory(int worldId) {
        int expEarned = calculateExpReward(worldId);
        int previousLevel = playerProgress.getPlayerLevel();
        playerProgress.addExp(expEarned);
        playerProgress.recordWorldClear(worldId);
        boolean unlockedZyra = false;
        if (worldId == 3 && !playerProgress.isZyraUnlocked()) {
            playerProgress.unlockZyra();
            unlockedZyra = true;
        }
        refreshWorldSelection();

        StringBuilder message = new StringBuilder("Victory! You have cleared World ")
            .append(worldId)
            .append(".\n\nRewards:\n+ ")
            .append(expEarned)
            .append(" EXP");

        if (playerProgress.getPlayerLevel() > previousLevel) {
            message.append("\nLevel Up! Now Level ").append(playerProgress.getPlayerLevel());
        }

        message.append(String.format("\nEXP Progress: %d / %d",
            playerProgress.getCurrentExp(), playerProgress.getExpToNext()));

        if (worldId < 5) {
            message.append("\n\nMeet the level requirement to enter World ").append(worldId + 1).append(".");
        }

        if (unlockedZyra) {
            message.append("\n\nNew Ally Recruited: Zyra Kathun now joins your team!");
        }

        JOptionPane.showMessageDialog(this, message.toString(), "Victory", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createBattleTopPanel(int worldId) {
        // Simple panel with palette colors
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.BG_PANEL);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(UITheme.BORDER_NORMAL);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("WORLD " + worldId + " - BATTLE", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.PRIMARY_GREEN);

        battleTurnLabel = new JLabel("Preparing...", SwingConstants.CENTER);
        battleTurnLabel.setFont(UITheme.FONT_HEADER);
        battleTurnLabel.setForeground(UITheme.PRIMARY_WHITE);

        battleWaveLabel = new JLabel("", SwingConstants.CENTER);
        battleWaveLabel.setFont(UITheme.FONT_SMALL);
        battleWaveLabel.setForeground(UITheme.PRIMARY_GREEN);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(battleTurnLabel, BorderLayout.CENTER);
        panel.add(battleWaveLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBattleCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        battlePlayerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setColor(UITheme.BG_PLAYER);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Use Panel asset (includes background and border)
                BufferedImage panelImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
                if (panelImg != null) {
                    PixelArtUI.drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback
                    BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                    if (borderImg != null) {
                        PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                    } else {
                        g2d.setColor(UITheme.BORDER_NORMAL);
                        g2d.setStroke(new BasicStroke(2f));
                        g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                    }
                }
                g2d.dispose();
            }
        };
        battlePlayerPanel.setLayout(new BoxLayout(battlePlayerPanel, BoxLayout.Y_AXIS));
        battlePlayerPanel.setOpaque(false);
        battlePlayerPanel.setBorder(UITheme.createTitledBorder("YOUR TEAM", UITheme.PRIMARY_GREEN, UITheme.BORDER_NORMAL));

        battleLog = new JTextArea();
        battleLog.setFont(UITheme.FONT_LOG);
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        // Dark but readable background with good text contrast
        battleLog.setBackground(new Color(15, 20, 25));
        battleLog.setForeground(UITheme.LOG_TEXT);
        battleLog.setOpaque(true);
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setPreferredSize(new Dimension(400, 400));

        battleEnemyPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setColor(UITheme.BG_ENEMY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Use Panel asset (includes background and border)
                BufferedImage panelImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
                if (panelImg != null) {
                    PixelArtUI.drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback
                    BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                    if (borderImg != null) {
                        PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                    } else {
                        g2d.setColor(UITheme.BORDER_NORMAL);
                        g2d.setStroke(new BasicStroke(2f));
                        g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                    }
                }
                g2d.dispose();
            }
        };
        battleEnemyPanel.setLayout(new BoxLayout(battleEnemyPanel, BoxLayout.Y_AXIS));
        battleEnemyPanel.setOpaque(false);
        battleEnemyPanel.setBorder(UITheme.createTitledBorder("ENEMIES", UITheme.PRIMARY_GREEN, UITheme.BORDER_NORMAL));

        buildBattleCharacterPanels();

        panel.add(battlePlayerPanel, BorderLayout.WEST);
        panel.add(logScroll, BorderLayout.CENTER);
        panel.add(battleEnemyPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBattleBottomPanel() {
        // Simple panel with palette colors
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UITheme.BG_PANEL);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(UITheme.BORDER_NORMAL);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        battleInstructionLabel = new JLabel("Select a skill", SwingConstants.CENTER);
        battleInstructionLabel.setFont(UITheme.FONT_BUTTON_SMALL);
        battleInstructionLabel.setForeground(UITheme.PRIMARY_ORANGE);

        battleSkillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        battleSkillPanel.setBackground(new Color(30, 30, 50));
        battleSkillPanel.setBorder(UITheme.createCyanBorder(2));

        panel.add(battleInstructionLabel, BorderLayout.NORTH);
        panel.add(battleSkillPanel, BorderLayout.CENTER);

        return panel;
    }

    private void announceCurrentWave() {
        if (currentWavePlan == null || currentWavePlan.isEmpty()) return;
        WaveEncounter wave = currentWavePlan.get(Math.min(activeWaveIndex, currentWavePlan.size() - 1));
        String label = String.format("Wave %d / %d%s",
            wave.waveNumber,
            currentWavePlan.size(),
            wave.bossWave ? " • Boss" : ""
        );
        appendBattleLog("\n--- " + label + " ---");
        updateWaveLabel();
    }

    private void updateWaveLabel() {
        if (battleWaveLabel == null || currentWavePlan == null || currentWavePlan.isEmpty()) return;
        WaveEncounter wave = currentWavePlan.get(Math.min(activeWaveIndex, currentWavePlan.size() - 1));
        String text = String.format("Wave %d/%d%s",
            wave.waveNumber,
            currentWavePlan.size(),
            wave.bossWave ? " (Boss)" : ""
        );
        battleWaveLabel.setText(text);
    }

    private void buildBattleCharacterPanels() {
        battlePlayerPanel.removeAll();
        battleEnemyPanel.removeAll();

        for (Character c : playerTeam) {
            JPanel card = createBattleCharacterCard(c, true);
            battlePlayerPanel.add(card);
            battlePlayerPanel.add(Box.createVerticalStrut(10));
        }

        for (Character e : enemyTeam) {
            JPanel card = createBattleCharacterCard(e, false);
            battleEnemyPanel.add(card);
            battleEnemyPanel.add(Box.createVerticalStrut(10));
        }

        battlePlayerPanel.revalidate();
        battlePlayerPanel.repaint();
        battleEnemyPanel.revalidate();
        battleEnemyPanel.repaint();
    }

    // ==================== ENEMY BLUEPRINTS ====================

    private static class WaveEncounter {
        final int waveNumber;
        final boolean bossWave;
        final Character[] enemies;

        WaveEncounter(int waveNumber, boolean bossWave, Character[] enemies) {
            this.waveNumber = waveNumber;
            this.bossWave = bossWave;
            this.enemies = enemies;
        }
    }

    private static class PausePanel extends JPanel {
        private float alpha = 0f;
        private javax.swing.Timer animation;

        PausePanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(30, 40, 30, 40));
            setLayout(new GridBagLayout());
            startAnimation();
        }

        private void startAnimation() {
            animation = new javax.swing.Timer(16, e -> {
                alpha = Math.min(1f, alpha + 0.1f);
                repaint();
                if (alpha >= 1f) {
                    animation.stop();
                }
            });
            animation.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            
            // Dark background with fade
            float panelAlpha = 0.4f + 0.5f * alpha;
            Color bgColor = new Color(UITheme.BG_PANEL.getRed(), UITheme.BG_PANEL.getGreen(), UITheme.BG_PANEL.getBlue(), (int)(255 * panelAlpha));
            g2.setColor(bgColor);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Use Panel asset with fade
            BufferedImage panelImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
            if (panelImg != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                PixelArtUI.drawNineSlice(g2, panelImg, 0, 0, getWidth(), getHeight());
            } else {
                // Fallback: border with fade
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    PixelArtUI.drawNineSlice(g2, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    Color borderColor = new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), (int)(255 * alpha));
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
            }
            
            g2.dispose();
        }
        
        @Override
        protected void paintChildren(Graphics g) {
            // Fade in children (buttons and text) along with the background
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    @FunctionalInterface
    private interface SkillFactory {
        Skill create();
    }

    private static class MinionTemplate {
        final String name;
        final int hp;
        final int mana;
        final int attack;
        final int defense;
        final int speed;
        final SkillFactory[] factories;

        MinionTemplate(String name, int hp, int mana, int attack, int defense, int speed, SkillFactory... factories) {
            this.name = name;
            this.hp = hp;
            this.mana = mana;
            this.attack = attack;
            this.defense = defense;
            this.speed = speed;
            this.factories = factories;
        }

        Character instantiate(int levelTarget, double difficulty) {
            DynamicEnemy enemy = new DynamicEnemy(name, hp, mana, attack, defense, speed, factories);
            enemy.syncToLevel(Math.max(1, levelTarget));
            double manaMult = 1.0 + Math.max(0, difficulty - 1) * 0.4;
            enemy.applyStatMultiplier(difficulty, manaMult, difficulty, difficulty);
            return enemy;
        }
    }

    private static class DynamicEnemy extends Character {
        DynamicEnemy(String name, int hp, int mana, int attack, int defense, int speed, SkillFactory... factories) {
            super(name, hp, mana, attack, defense, speed);
            if (factories != null) {
                for (SkillFactory factory : factories) {
                    skills.add(factory.create());
                }
            }
        }

        @Override
        public void initializeSkills() {
            // Skills injected after construction
        }
    }

    private static class SavageSwipeSkill extends Skill {
        private final double multiplier;

        SavageSwipeSkill(String name, double multiplier) {
            this.name = name;
            this.multiplier = multiplier;
            this.manaCost = 0;
            this.cooldown = 0;
            this.description = "Deal " + Math.round(multiplier * 100) + "% attack damage to one foe.";
            this.targetType = TargetType.SINGLE_ENEMY;
        }

        @Override
        public void execute(Character user, Character[] targets) {
            if (targets == null || targets.length == 0 || targets[0] == null) return;
            Character target = targets[0];
            int damage = (int) Math.max(5, user.currentAttack * multiplier);
            target.takeDamage(damage);
        }
    }

    private static class VenomSplashSkill extends Skill {
        private final double multiplier;

        VenomSplashSkill(String name, double multiplier, int manaCost, int cooldown) {
            this.name = name;
            this.multiplier = multiplier;
            this.manaCost = manaCost;
            this.cooldown = cooldown;
            this.description = "Hits all foes for " + Math.round(multiplier * 100) + "% attack damage.";
            this.targetType = TargetType.ALL_ENEMIES;
        }

        @Override
        public void execute(Character user, Character[] targets) {
            if (targets == null || targets.length == 0) return;
            if (user.currentMana < manaCost) return;
            user.currentMana -= manaCost;
            for (Character target : targets) {
                if (target != null && target.isAlive()) {
                    int damage = (int) Math.max(4, user.currentAttack * multiplier);
                    target.takeDamage(damage);
                }
            }
            resetCooldown();
        }
    }

    private static class SoulDrainSkill extends Skill {
        private final double multiplier;
        private final double healRatio;

        SoulDrainSkill(String name, double multiplier, double healRatio) {
            this.name = name;
            this.multiplier = multiplier;
            this.healRatio = healRatio;
            this.manaCost = 18;
            this.cooldown = 2;
            this.description = "Damage one foe and restore health.";
            this.targetType = TargetType.SINGLE_ENEMY;
        }

        @Override
        public void execute(Character user, Character[] targets) {
            if (targets == null || targets.length == 0 || targets[0] == null) return;
            if (user.currentMana < manaCost) return;
            user.currentMana -= manaCost;
            Character target = targets[0];
            int damage = (int) Math.max(6, user.currentAttack * multiplier);
            target.takeDamage(damage);
            int heal = (int) Math.max(5, damage * healRatio);
            user.restoreHealth(heal);
            resetCooldown();
        }
    }

    private static class AegisPulseSkill extends Skill {
        private final int healAmount;

        AegisPulseSkill(String name, int healAmount) {
            this.name = name;
            this.healAmount = healAmount;
            this.manaCost = 25;
            this.cooldown = 3;
            this.description = "Heals all allies for " + healAmount + " HP.";
            this.targetType = TargetType.ALL_ALLIES;
        }

        @Override
        public void execute(Character user, Character[] targets) {
            if (targets == null || targets.length == 0) return;
            if (user.currentMana < manaCost) return;
            user.currentMana -= manaCost;
            for (Character ally : targets) {
                if (ally != null && ally.isAlive()) {
                    ally.restoreHealth(healAmount);
                }
            }
            resetCooldown();
        }
    }

    private JPanel createBattleCharacterCard(Character c, boolean isPlayer) {
        // Pixel-art character card
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                // Dark background
                Color bg = isPlayer ? UITheme.BG_PLAYER : UITheme.BG_ENEMY;
                g2d.setColor(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Use Panel asset (includes background and border)
                BufferedImage panelImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Panel/panel-000.png");
                if (panelImg != null) {
                    PixelArtUI.drawNineSlice(g2d, panelImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback
                    BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                    if (borderImg != null) {
                        PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                    } else {
                        g2d.setColor(UITheme.BORDER_NORMAL);
                        g2d.setStroke(new BasicStroke(2f));
                        g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                    }
                }
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(UITheme.CHARACTER_CARD);
        card.setMaximumSize(UITheme.CHARACTER_CARD);

        JLabel nameLabel = new JLabel(c.name);
        nameLabel.setFont(UITheme.FONT_CARD_NAME);
        nameLabel.setForeground(UITheme.PRIMARY_WHITE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar hpBar = new JProgressBar(0, c.maxHP);
        hpBar.setValue(c.currentHP);
        hpBar.setStringPainted(true);
        hpBar.setString(c.currentHP + " / " + c.maxHP);
        hpBar.setForeground(UITheme.HP_GREEN);

        JProgressBar manaBar = new JProgressBar(0, c.maxMana);
        manaBar.setValue(c.currentMana);
        manaBar.setStringPainted(true);
        manaBar.setString(c.currentMana + " / " + c.maxMana);
        manaBar.setForeground(UITheme.MANA_BLUE);

        JPanel barsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        barsPanel.setOpaque(false);
        barsPanel.add(hpBar);
        barsPanel.add(manaBar);

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(barsPanel, BorderLayout.CENTER);

        card.putClientProperty("character", c);
        card.putClientProperty("hpBar", hpBar);
        card.putClientProperty("manaBar", manaBar);
        card.putClientProperty("isPlayer", isPlayer);

        if (!isPlayer) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (waitingForTarget && c.isAlive()) {
                        onBattleTargetSelected(c);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (waitingForTarget && c.isAlive()) {
                        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HOVER, 3));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (waitingForTarget) {
                        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HIGHLIGHT, 2));
                    } else {
                        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
                    }
                }
            });
        }

        return card;
    }

    // ==================== BATTLE LOGIC ====================

    private void startBattle() {
        appendBattleLog("⚔ Battle Started! Waves incoming: " + currentWavePlan.size());
        announceCurrentWave();
        appendBattleLog(">>> " + playerTeam[0].name + "'s turn\n");
        prepareBattlePlayerTurn();
    }

    private void prepareBattlePlayerTurn() {
        while (currentPlayerIndex < playerTeam.length && !playerTeam[currentPlayerIndex].isAlive()) {
            currentPlayerIndex++;
        }

        if (currentPlayerIndex >= playerTeam.length) {
            currentPlayerIndex = 0;
            while (currentPlayerIndex < playerTeam.length && !playerTeam[currentPlayerIndex].isAlive()) {
                currentPlayerIndex++;
            }
        }

        if (currentPlayerIndex >= playerTeam.length || !playerTeam[currentPlayerIndex].isAlive()) {
            checkBattleEnd();
            return;
        }

        Character current = playerTeam[currentPlayerIndex];
        battleTurnLabel.setText("PLAYER TURN: " + current.name);
        battleInstructionLabel.setText("Select a skill for " + current.name);
        battleInstructionLabel.setForeground(UITheme.PRIMARY_ORANGE);

        loadBattleSkillButtons(current);
        updateBattleBars();

        selectedSkill = null;
        waitingForTarget = false;
        clearBattleHighlights();
    }

    private void loadBattleSkillButtons(Character character) {
        battleSkillPanel.removeAll();

        for (Skill skill : character.skills) {
            JButton skillBtn = new JButton(skill.getInfo());
            skillBtn.setFont(UITheme.FONT_SKILL);
            skillBtn.setForeground(UITheme.PRIMARY_ORANGE);
            skillBtn.setBackground(new Color(UITheme.BG_BUTTON.getRed(), UITheme.BG_BUTTON.getGreen(), UITheme.BG_BUTTON.getBlue(), 220));
            skillBtn.setOpaque(true);
            skillBtn.setFocusPainted(false);
            skillBtn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
            skillBtn.setPreferredSize(UITheme.SKILL_BUTTON);

            boolean canUse = skill.canUse(character);
            skillBtn.setEnabled(canUse);

            if (!canUse) {
                skillBtn.setForeground(new Color(UITheme.TEXT_GRAY.getRed(), UITheme.TEXT_GRAY.getGreen(), UITheme.TEXT_GRAY.getBlue(), 150));
                skillBtn.setBackground(new Color(UITheme.BG_BUTTON.getRed() - 10, UITheme.BG_BUTTON.getGreen() - 10, UITheme.BG_BUTTON.getBlue() - 10, 150));
                skillBtn.setBorder(BorderFactory.createLineBorder(new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 100), 2));
            }

            skillBtn.addActionListener(e -> onBattleSkillSelected(skill, character));
            battleSkillPanel.add(skillBtn);
        }

        battleSkillPanel.revalidate();
        battleSkillPanel.repaint();
    }

    private void onBattleSkillSelected(Skill skill, Character user) {
        if (!skill.canUse(user)) {
            appendBattleLog("⚠ Cannot use " + skill.name + "!");
            return;
        }

        selectedSkill = skill;
        TargetType targetType = skill.getTargetType();

        switch (targetType) {
            case SELF:
                appendBattleLog(user.name + " uses " + skill.getName() + " on self!");
                executeSkillWithLog(skill, user, new Character[]{user});
                endBattlePlayerTurn();
                break;

            case ALL_ALLIES:
                appendBattleLog(user.name + " uses " + skill.getName() + " on all allies!");
                executeSkillWithLog(skill, user, getAllAlive(playerTeam));
                endBattlePlayerTurn();
                break;

            case ALL_ENEMIES:
                appendBattleLog(user.name + " uses " + skill.getName() + " on all enemies!");
                executeSkillWithLog(skill, user, getAllAlive(enemyTeam));
                endBattlePlayerTurn();
                break;

            case SINGLE_ENEMY:
                battleInstructionLabel.setText("Click on an ENEMY to target!");
                battleInstructionLabel.setForeground(UITheme.PRIMARY_RED);
                waitingForTarget = true;
                setBattleSkillButtonsEnabled(false);
                highlightBattleEnemies();
                break;

            default:
                endBattlePlayerTurn();
                break;
        }
    }

    private void onBattleTargetSelected(Character target) {
        if (!waitingForTarget || selectedSkill == null) return;

        Character user = playerTeam[currentPlayerIndex];
        appendBattleLog(user.name + " uses " + selectedSkill.getName() + " on " + target.name + "!");
        executeSkillWithLog(selectedSkill, user, new Character[]{target});

        clearBattleHighlights();
        endBattlePlayerTurn();
    }

    private void endBattlePlayerTurn() {
        if (selectedSkill != null) {
            selectedSkill.resetCooldown();
        }

        selectedSkill = null;
        waitingForTarget = false;
        battleInstructionLabel.setText("Processing...");
        battleInstructionLabel.setForeground(UITheme.TEXT_GRAY);
        setBattleSkillButtonsEnabled(false);
        updateBattleBars();

        javax.swing.Timer delay = new javax.swing.Timer(800, e -> {
            if (checkBattleEnd()) return;

            currentPlayerIndex++;
            if (currentPlayerIndex >= playerTeam.length) {
                currentPlayerIndex = 0;
                battleEnemyTurn();
            } else {
                prepareBattlePlayerTurn();
            }
            ((javax.swing.Timer) e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void battleEnemyTurn() {
        battleTurnLabel.setText("ENEMY TURN");
        appendBattleLog("\n=== ENEMY TURN ===");

        javax.swing.Timer enemyDelay = new javax.swing.Timer(500, null);
        final int[] enemyIndex = {0};

        enemyDelay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enemyIndex[0] < enemyTeam.length) {
                    Character enemy = enemyTeam[enemyIndex[0]];
                    if (enemy.isAlive()) {
                        executeBattleEnemyAction(enemy);
                    }
                    enemyIndex[0]++;
                } else {
                    enemyDelay.stop();
                    reduceBattleCooldowns();

                    javax.swing.Timer endTurn = new javax.swing.Timer(500, evt -> {
                        if (!checkBattleEnd()) {
                            prepareBattlePlayerTurn();
                        }
                        ((javax.swing.Timer) evt.getSource()).stop();
                    });
                    endTurn.setRepeats(false);
                    endTurn.start();
                }
            }
        });

        enemyDelay.start();
    }

    private void executeBattleEnemyAction(Character enemy) {
        Skill skill = null;
        for (Skill s : enemy.skills) {
            if (s.canUse(enemy)) {
                skill = s;
                break;
            }
        }

        if (skill == null && !enemy.skills.isEmpty()) {
            skill = enemy.skills.get(0);
        }

        if (skill != null) {
            TargetType targetType = skill.getTargetType();
            switch (targetType) {
                case ALL_ENEMIES:
                    Character[] players = getAllAlive(playerTeam);
                    if (players.length > 0) {
                        appendBattleLog(enemy.name + " unleashes " + skill.getName() + " on your party!");
                        executeSkillWithLog(skill, enemy, players);
                    }
                    break;
                case ALL_ALLIES:
                    Character[] allies = getAllAlive(enemyTeam);
                    if (allies.length > 0) {
                        appendBattleLog(enemy.name + " empowers allies with " + skill.getName() + "!");
                        executeSkillWithLog(skill, enemy, allies);
                    }
                    break;
                case SELF:
                    appendBattleLog(enemy.name + " uses " + skill.getName() + "!");
                    executeSkillWithLog(skill, enemy, new Character[]{enemy});
                    break;
                default:
                    Character target = getRandomAlive(playerTeam);
                    if (target != null) {
                        appendBattleLog(enemy.name + " uses " + skill.getName() + " on " + target.name + "!");
                        executeSkillWithLog(skill, enemy, new Character[]{target});
                    }
                    break;
            }
            updateBattleBars();
        }
    }

    private boolean checkBattleEnd() {
        boolean playersAlive = anyAlive(playerTeam);
        boolean enemiesAlive = anyAlive(enemyTeam);

        if (!enemiesAlive) {
            if (advanceToNextWave()) {
                return true;
            }
            endBattle(true);
            return true;
        } else if (!playersAlive) {
            endBattle(false);
            return true;
        }

        return false;
    }

    private boolean advanceToNextWave() {
        if (currentWavePlan == null) return false;
        if (activeWaveIndex + 1 >= currentWavePlan.size()) {
            return false;
        }
        activeWaveIndex++;
        WaveEncounter next = currentWavePlan.get(activeWaveIndex);
        enemyTeam = next.enemies;
        applyEnemyScaling(selectedWorldId, next);
        buildBattleCharacterPanels();
        selectedSkill = null;
        waitingForTarget = false;
        announceCurrentWave();
        setBattleSkillButtonsEnabled(false);

        javax.swing.Timer resume = new javax.swing.Timer(900, e -> {
            currentPlayerIndex = 0;
            prepareBattlePlayerTurn();
            ((javax.swing.Timer) e.getSource()).stop();
        });
        resume.setRepeats(false);
        resume.start();
        return true;
    }

    private void endBattle(boolean victory) {
        setBattleSkillButtonsEnabled(false);
        currentWavePlan.clear();
        activeWaveIndex = 0;

        if (victory) {
            appendBattleLog("\n" + "=".repeat(50));
            appendBattleLog("⭐ VICTORY! ⭐");
            appendBattleLog("=".repeat(50));

            rewardBattleVictory(selectedWorldId);
            showScreen(SCREEN_WORLD_SELECT);
        } else {
            appendBattleLog("\n" + "=".repeat(50));
            appendBattleLog("💀 DEFEAT... 💀");
            appendBattleLog("=".repeat(50));

            JOptionPane.showMessageDialog(this, "Defeat... Try again!", "Defeat", JOptionPane.WARNING_MESSAGE);

            showScreen(SCREEN_WORLD_SELECT);
        }
    }

    // ==================== PAUSE MENU ====================

    private void setupGlobalKeyBindings() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pauseMenu");
        am.put("pauseMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only show pause menu when in battle
                if (SCREEN_BATTLE.equals(currentScreen)) {
                    showPauseMenu();
                }
            }
        });
    }

    private void showPauseMenu() {
        // Only show pause when in a "play" screen (battle or world selection/story)
        // but it's fine to allow from anywhere.
        JDialog dialog = new JDialog(this, "Pause", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        PausePanel glassPanel = new PausePanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel pauseLabel = new JLabel("PAUSED", SwingConstants.CENTER);
        pauseLabel.setFont(UITheme.FONT_SUBTITLE);
        pauseLabel.setForeground(UITheme.PRIMARY_GREEN);
        gbc.gridy = 0;
        glassPanel.add(pauseLabel, gbc);

        JButton resumeBtn = UITheme.createButton("RESUME");
        resumeBtn.setFont(UITheme.FONT_BUTTON);
        resumeBtn.addActionListener(e -> dialog.dispose());
        gbc.gridy = 1;
        glassPanel.add(resumeBtn, gbc);

        JButton optionsBtn = UITheme.createButton("OPTIONS");
        optionsBtn.setFont(UITheme.FONT_BUTTON);
        optionsBtn.addActionListener(e -> {
            dialog.dispose();
            showScreen(SCREEN_SETTINGS);
        });
        gbc.gridy = 2;
        glassPanel.add(optionsBtn, gbc);

        JButton exitMenuBtn = UITheme.createButton("EXIT TO MENU");
        exitMenuBtn.setFont(UITheme.FONT_BUTTON);
        exitMenuBtn.addActionListener(e -> {
            dialog.dispose();
            returnToMainMenu();
        });
        gbc.gridy = 3;
        glassPanel.add(exitMenuBtn, gbc);

        dialog.setContentPane(glassPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void refreshWorldSelection() {
        for (Component comp : mainContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(SCREEN_WORLD_SELECT)) {
                mainContainer.remove(comp);
                break;
            }
        }
        mainContainer.add(createWorldSelection(), SCREEN_WORLD_SELECT);
    }

    private void refreshProfileSelect() {
        for (Component comp : mainContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(SCREEN_PROFILE_SELECT)) {
                mainContainer.remove(comp);
                break;
            }
        }
        mainContainer.add(createProfileSelect(), SCREEN_PROFILE_SELECT);
    }
    void resetProfileSlot(int profileIndex, boolean deleteFile) {
        int idx = Math.max(1, Math.min(PROFILE_SLOTS, profileIndex)) - 1;
        PlayerProgress newProgress = new PlayerProgress();
        profileSlots[idx] = newProgress;
        if (activeProfile == idx) {
            playerProgress = newProgress;
        }

        if (deleteFile) {
            Path file = SAVE_DIR.resolve("profile" + profileIndex + ".dat");
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveProfile(idx);
        }

        refreshProfileSelect();
    }

    void goToMainMenu() {
        returnToMainMenu();
    }

    private void returnToMainMenu() {
        saveActiveProfile();
        showScreen(SCREEN_MAIN_MENU);
    }

    private void configureDisplayScale() {
        float dpiScale = 1.0f;
        try {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            dpiScale = dpi / 110f;
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
            dpiScale = Math.max(dpiScale, (float) tx.getScaleX());
        } catch (Exception ignored) {
        }
        UITheme.applyScale(Math.max(1.0f, Math.min(dpiScale, 1.6f)));
    }

    private void saveActiveProfile() {
        if (activeProfile < 0 || playerProgress == null) return;
        saveProfile(activeProfile);
    }

    private void saveProfile(int slotIndex) {
        Path file = SAVE_DIR.resolve("profile" + (slotIndex + 1) + ".dat");
        try {
            profileSlots[slotIndex].save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void highlightBattleEnemies() {
        for (Component comp : battleEnemyPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                Character c = (Character) card.getClientProperty("character");
                if (c != null && c.isAlive()) {
                    card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HIGHLIGHT, 2));
                }
            }
        }
    }

    private void clearBattleHighlights() {
        for (Component comp : battlePlayerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
            }
        }
        for (Component comp : battleEnemyPanel.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
            }
        }
    }

    private void updateBattleBars() {
        updateBarsForPanel(battlePlayerPanel);
        updateBarsForPanel(battleEnemyPanel);
    }

    private void updateBarsForPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                Character c = (Character) card.getClientProperty("character");
                JProgressBar hpBar = (JProgressBar) card.getClientProperty("hpBar");
                JProgressBar manaBar = (JProgressBar) card.getClientProperty("manaBar");

                if (c != null && hpBar != null && manaBar != null) {
                    hpBar.setMaximum(c.maxHP);
                    hpBar.setValue(Math.max(0, c.currentHP));
                    hpBar.setString(c.currentHP + " / " + c.maxHP);

                    manaBar.setMaximum(c.maxMana);
                    manaBar.setValue(Math.max(0, c.currentMana));
                    manaBar.setString(c.currentMana + " / " + c.maxMana);

                    if (!c.isAlive()) {
                        card.setBackground(UITheme.DEAD_GRAY);
                    }
                }
            }
        }
    }

    private void setBattleSkillButtonsEnabled(boolean enabled) {
        for (Component comp : battleSkillPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            }
        }
    }

    private void reduceBattleCooldowns() {
        for (Character c : playerTeam) {
            if (c != null) {
                for (Skill s : c.skills) {
                    s.reduceCooldown();
                }
            }
        }
        for (Character e : enemyTeam) {
            if (e != null) {
                for (Skill s : e.skills) {
                    s.reduceCooldown();
                }
            }
        }
    }

    private Character[] getAllAlive(Character[] team) {
        List<Character> alive = new ArrayList<>();
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                alive.add(c);
            }
        }
        return alive.toArray(new Character[0]);
    }

    private Character getRandomAlive(Character[] team) {
        List<Character> alive = new ArrayList<>();
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                alive.add(c);
            }
        }
        if (alive.isEmpty()) return null;
        return alive.get((int) (Math.random() * alive.size()));
    }

    private boolean anyAlive(Character[] team) {
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private void appendBattleLog(String text) {
        battleLog.append(text + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private Map<Character, Integer> snapshotHp() {
        Map<Character, Integer> map = new HashMap<>();
        if (playerTeam != null) {
            for (Character hero : playerTeam) {
                if (hero != null) map.put(hero, hero.currentHP);
            }
        }
        if (enemyTeam != null) {
            for (Character enemy : enemyTeam) {
                if (enemy != null) map.put(enemy, enemy.currentHP);
            }
        }
        return map;
    }

    private void logDamageDelta(String attacker, String skillName, Map<Character, Integer> before) {
        for (Map.Entry<Character, Integer> entry : before.entrySet()) {
            Character target = entry.getKey();
            int prev = entry.getValue();
            int delta = prev - target.currentHP;
            if (delta > 0) {
                appendBattleLog(String.format(
                    "%s uses %s → %s takes %d damage (HP %d/%d)",
                    attacker,
                    skillName,
                    target.name,
                    delta,
                    Math.max(0, target.currentHP),
                    target.maxHP
                ));
            }
        }
    }

    private void executeSkillWithLog(Skill skill, Character user, Character[] targets) {
        Map<Character, Integer> hpBefore = snapshotHp();
        skill.execute(user, targets);
        logDamageDelta(user.name, skill.getName(), hpBefore);
    }

    // ==================== MAIN METHOD ====================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(UnifiedGameUI::new);
    }
}
