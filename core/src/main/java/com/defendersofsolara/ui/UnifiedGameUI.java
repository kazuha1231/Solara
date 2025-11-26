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
import java.awt.AlphaComposite;
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
    private boolean settingsOpenedFromPause = false; // Track if settings was opened from pause menu

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

    // Balanced multipliers - normal difficulty, enemies killable but challenging in later worlds
    // Enemies have high HP but low damage that scales with hero HP
    private static final double[] WORLD_HP_MULT = {1.0, 1.1, 1.2, 1.35, 1.5};
    private static final double[] WORLD_MANA_MULT = {1.0, 1.1, 1.2, 1.3, 1.4};
    private static final double[] WORLD_ATK_MULT = {1.0, 1.02, 1.04, 1.06, 1.08}; // Much lower attack scaling
    private static final double[] WORLD_DEF_MULT = {1.0, 1.05, 1.1, 1.15, 1.2};
    // Enemy levels now scale with player level (base + world offset)
    private static final int[] WORLD_ENEMY_LEVEL_OFFSET = {0, 1, 2, 3, 4};
    private static final int WAVES_PER_WORLD = 5;
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
    private JPanel battleCharacterListPanel; // Left side character portrait list
    private JPanel battleCharacterDetailsPanel; // Bottom left character details
    private JPanel battleAttackDetailsPanel; // Bottom right attack details
    private JPanel battleEnemyDetailsPanel; // Bottom right enemy stats
    private JPanel battleEventLogPanel; // Bottom right event log

    private final Random random = new Random();
    private List<WaveEncounter> currentWavePlan = new ArrayList<>();
    private int activeWaveIndex = 0;
    
    // Background image
    private BufferedImage menuBackground = null;

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
        loadMenuBackground();
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
            // End session for previous profile
            if (playerProgress != null) {
                playerProgress.endSession();
            }
            saveActiveProfile(); // Save previous profile before switching
            activeProfile = idx;
            
            // Load profile from disk if not already in memory
            if (profileSlots[activeProfile] == null) {
                Path file = SAVE_DIR.resolve("profile" + (activeProfile + 1) + ".dat");
                PlayerProgress loaded = PlayerProgress.load(file);
                if (loaded != null) {
                    profileSlots[activeProfile] = loaded;
                    System.out.println("Loaded profile " + (activeProfile + 1) + " from disk: Level " + loaded.getPlayerLevel() + 
                        ", EXP " + loaded.getCurrentExp() + "/" + loaded.getExpToNext() + 
                        ", Worlds: " + loaded.getClearedWorldCount());
                } else {
                    profileSlots[activeProfile] = new PlayerProgress();
                }
            }
            
            playerProgress = profileSlots[activeProfile];
            // Start session for new profile
            if (playerProgress != null) {
                playerProgress.startSession();
            }
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
        if (playerProgress == null || !playerProgress.canEnterWorld(worldId)) {
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
        if (playerProgress == null || !playerProgress.canEnterWorld(worldId)) {
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

        // Start session when entering battle
        if (playerProgress != null) {
            playerProgress.startSession();
        }
        
        JPanel battlePanel = createBattle(worldId);
        battlePanel.setName(SCREEN_BATTLE);
        mainContainer.add(battlePanel, SCREEN_BATTLE);
        showScreen(SCREEN_BATTLE);
    }

    // ==================== BACKGROUND ====================

    /**
     * Creates a beautiful readable label with text shadow and divider fade underline.
     */
    private JLabel createReadableLabel(String text, Font font, Color color, int alignment) {
        JLabel label = new JLabel(text, alignment) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setFont(getFont());
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = alignment == SwingConstants.CENTER ? (getWidth() - textWidth) / 2 : 
                        alignment == SwingConstants.RIGHT ? getWidth() - textWidth - 5 : 5;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Draw text shadow for readability (multiple layers for depth)
                g2d.setColor(new Color(0, 0, 0, 200)); // Strong shadow
                g2d.drawString(getText(), x + 2, y + 2);
                g2d.setColor(new Color(0, 0, 0, 120)); // Softer shadow
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Draw main text with slight glow effect
                g2d.setColor(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), 200));
                g2d.drawString(getText(), x, y - 1);
                g2d.setColor(getForeground());
                g2d.drawString(getText(), x, y);
                
                // Draw beautiful divider fade underline below text
                // Try different divider fade variants based on font size
                String dividerPath = "/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png";
                if (font.getSize() >= 24) {
                    dividerPath = "/kennyresources/PNG/Default/Divider Fade/divider-fade-002.png"; // Thicker for large titles
                } else if (font.getSize() >= 18) {
                    dividerPath = "/kennyresources/PNG/Default/Divider Fade/divider-fade-001.png"; // Medium for subtitles
                }
                
                BufferedImage dividerFade = PixelArtUI.loadImage(dividerPath);
                if (dividerFade == null) {
                    dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png");
                }
                
                if (dividerFade != null) {
                    int underlineY = y + fm.getDescent() + 8; // Position below text with nice spacing
                    int underlineWidth = Math.max(textWidth + 40, 120); // Wider than text for elegance
                    int underlineX = alignment == SwingConstants.CENTER ? (getWidth() - underlineWidth) / 2 :
                                    alignment == SwingConstants.RIGHT ? getWidth() - underlineWidth - 5 : 5;
                    int underlineHeight = Math.max(dividerFade.getHeight(), 4); // Ensure minimum height
                    
                    // Draw with slight transparency for elegance
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                    PixelArtUI.drawNineSlice(g2d, dividerFade, underlineX, underlineY, underlineWidth, underlineHeight);
                    g2d.setComposite(AlphaComposite.SrcOver);
                }
                
                g2d.dispose();
            }
        };
        label.setFont(font);
        label.setForeground(color);
        label.setOpaque(false);
        return label;
    }
    
    private void loadMenuBackground() {
        try {
            java.net.URL url = getClass().getResource("/image/menu.png");
            if (url != null) {
                menuBackground = javax.imageio.ImageIO.read(url);
                System.out.println("✓ Loaded menu background: " + menuBackground.getWidth() + "x" + menuBackground.getHeight());
            } else {
                System.err.println("✗ Menu background not found: /image/menu.png");
            }
        } catch (Exception e) {
            System.err.println("ERROR loading menu background: " + e.getMessage());
            e.printStackTrace();
                }
    }

    private void initializeProfiles() {
        try {
            Files.createDirectories(SAVE_DIR);
        } catch (IOException e) {
            System.err.println("Error creating save directory: " + e.getMessage());
        }

        for (int i = 0; i < PROFILE_SLOTS; i++) {
            Path file = SAVE_DIR.resolve("profile" + (i + 1) + ".dat");
            PlayerProgress data = PlayerProgress.load(file);
            if (data == null) {
                data = new PlayerProgress();
            }
            profileSlots[i] = data;
        }
        // Set first profile as active if none selected
        if (activeProfile < 0) {
            activeProfile = 0;
        }
        if (activeProfile >= 0 && activeProfile < PROFILE_SLOTS) {
            playerProgress = profileSlots[activeProfile];
            if (playerProgress != null) {
                // Don't auto-start session on initialization
                // Session will start when entering battle or selecting profile
            }
        }
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
        // Draw menu.png background if available, otherwise use solid color
        if (menuBackground != null) {
            // Scale and center the background image
            int imgW = menuBackground.getWidth();
            int imgH = menuBackground.getHeight();
            double scaleX = (double) width / imgW;
            double scaleY = (double) height / imgH;
            double scale = Math.max(scaleX, scaleY); // Cover entire area
            
            int scaledW = (int) (imgW * scale);
            int scaledH = (int) (imgH * scale);
            int x = (width - scaledW) / 2;
            int y = (height - scaledH) / 2;

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(menuBackground, x, y, scaledW, scaledH, null);
        } else {
            // Fallback: Very dark blue-gray/charcoal background
            g2d.setColor(UITheme.BG_DARK_TEAL);
            g2d.fillRect(0, 0, width, height);
            }
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
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Draw background with reduced opacity
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                paintBackground(g2d, getWidth(), getHeight());
                
                // Draw dark overlay for better text readability
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setComposite(AlphaComposite.SrcOver);
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        JLabel credits = new JLabel(
            "<html><center>" +
                "<font size='8' color='#FFFFFF'><b>DEFENDERS OF SOLARA</b></font><br><br>" +
                "<font size='5' color='#FFFFFF'>" +
                "<b>Developer/Members:</b><br>" +
                "John Warren Pansacala<br>" +
                "Veinz Pius N. Escuzar<br>" +
                "Denzel B. Valendez<br>" +
                "Kim Kyle M. Paran<br>" +
                "Rushaine A. Tura<br><br>" +
                "<b>Credits:</b><br>" +
                "Assets: Kenny Assets<br>" +
                "Planet Assets: Deep-Fold source (Itch.io)<br>" +
                "Background Menu: Gemini AI<br><br>" +
                "Engine: Java Swing<br>" +
                "Version: 1.0" +
                "</font></center></html>"
        );
        credits.setOpaque(false);
        gbc.gridy = 0;
        panel.add(credits, gbc);

        JButton backBtn = UITheme.createSmallButton("BACK");
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

        JButton applyVideoBtn = UITheme.createSmallButton("APPLY VIDEO");
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
        backBtn.addActionListener(e -> {
            if (settingsOpenedFromPause) {
                // Return to pause menu instead of main menu
                settingsOpenedFromPause = false;
                showPauseMenu();
            } else {
                returnToMainMenu();
            }
        });
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
        // Ensure playerProgress is set - if not, load it from the active profile
        if (playerProgress == null && activeProfile >= 0) {
            if (profileSlots[activeProfile] == null) {
                Path file = SAVE_DIR.resolve("profile" + (activeProfile + 1) + ".dat");
                PlayerProgress loaded = PlayerProgress.load(file);
                if (loaded != null) {
                    profileSlots[activeProfile] = loaded;
                    playerProgress = loaded;
                } else {
                    profileSlots[activeProfile] = new PlayerProgress();
                    playerProgress = profileSlots[activeProfile];
                }
            } else {
                playerProgress = profileSlots[activeProfile];
            }
        }
        
        // Safety check - if still null, create a new progress
        if (playerProgress == null) {
            playerProgress = new PlayerProgress();
            if (activeProfile >= 0) {
                profileSlots[activeProfile] = playerProgress;
            }
        }
        
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                paintBackground(g2d, getWidth(), getHeight());
                
                // Very dark vignette overlay (matching profile menu)
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle orange glow behind the world cards (from palette)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setPaint(new RadialGradientPaint(
                    new Point(getWidth() / 2, getHeight() / 2),
                    Math.max(getWidth(), getHeight()) / 3f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(220, 120, 60, 120), new Color(0, 0, 0, 0)}
                ));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setComposite(AlphaComposite.SrcOver);
            }
        };
        panel.setOpaque(false);

        JLabel title = UITheme.createTitle("SELECT YOUR WORLD");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        title.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Secret cheat: Click title to level up by 3 levels and unlock worlds
                if (playerProgress != null) {
                    int currentLevel = playerProgress.getPlayerLevel();
                    int targetLevel = Math.min(30, currentLevel + 3);
                    
                    // Calculate EXP needed to reach target level
                    int expNeeded = 0;
                    int tempLevel = currentLevel;
                    int tempExp = playerProgress.getCurrentExp();
                    int tempExpToNext = playerProgress.getExpToNext();
                    
                    while (tempLevel < targetLevel && tempLevel < 50) {
                        int neededForThisLevel = tempExpToNext - tempExp;
                        expNeeded += neededForThisLevel;
                        tempLevel++;
                        tempExp = 0;
                        // Calculate next level's exp requirement (same formula as PlayerProgress)
                        tempExpToNext = Math.round(tempExpToNext * 1.25f) + 50;
                    }
                    
                    // Add the EXP to level up
                    playerProgress.addExp(expNeeded);
                    
                    // Unlock all worlds that are now accessible
                    int newLevel = playerProgress.getPlayerLevel();
                    for (int worldId = 1; worldId <= 5; worldId++) {
                        int requiredLevel = playerProgress.getWorldRequirement(worldId);
                        if (newLevel >= requiredLevel) {
                            // Unlock this world and all previous worlds
                            for (int w = 1; w <= worldId; w++) {
                                if (!playerProgress.hasClearedWorld(w)) {
                                    playerProgress.recordWorldClear(w);
                                }
                            }
                        }
                    }
                    
                    // Unlock Zyra if World 2 is cleared (either through cheat or normal play)
                    if (playerProgress.hasClearedWorld(2) && !playerProgress.isZyraUnlocked()) {
                        playerProgress.unlockZyra();
                    }
                    
                    // Save progress to persist Zyra unlock
                    saveActiveProfile();
                    
                    // Refresh the world selection screen
                    refreshWorldSelection();
                }
            }
        });

        JLabel levelInfo = createReadableLabel(
            String.format("Level %d | EXP %d / %d",
                playerProgress.getPlayerLevel(),
                playerProgress.getCurrentExp(),
                playerProgress.getExpToNext()
            ),
            UITheme.FONT_TEXT,
            UITheme.PRIMARY_ORANGE,
            SwingConstants.CENTER
        );

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

        JButton backBtn = UITheme.createSmallButton("BACK TO MENU");
        backBtn.setPreferredSize(new Dimension(180, 40));
        backBtn.setMinimumSize(new Dimension(180, 40));
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
                
                // Dark background (matching profile menu style - solid, not transparent)
                g2d.setColor(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Use only panel border (not full panel)
                java.awt.image.BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    float alpha = isUnlocked ? (isHovered[0] ? 1.0f : 0.9f) : 0.6f;
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: simple border
                    Color borderColor = isUnlocked && isHovered[0] 
                        ? UITheme.BORDER_HIGHLIGHT 
                        : isUnlocked 
                            ? UITheme.BORDER_NORMAL
                            : new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 120);
                    g2d.setColor(borderColor);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
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

        // Create labels with text shadows for readability on transparent background
        JLabel worldLabel = createReadableLabel("WORLD " + worldId, UITheme.FONT_HEADER, isUnlocked ? UITheme.PRIMARY_GREEN : UITheme.TEXT_GRAY, SwingConstants.CENTER);
        JLabel nameLabel = createReadableLabel(name, UITheme.FONT_TEXT, isUnlocked ? UITheme.PRIMARY_WHITE : UITheme.TEXT_GRAY, SwingConstants.CENTER);
        JLabel coreLabel = createReadableLabel("Core: " + core, UITheme.FONT_TEXT, isUnlocked ? UITheme.PRIMARY_YELLOW : UITheme.TEXT_GRAY, SwingConstants.CENTER);

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
        JLabel statusLabel = createReadableLabel(statusText, UITheme.FONT_SMALL, isUnlocked ? UITheme.PRIMARY_ORANGE : UITheme.TEXT_GRAY, SwingConstants.CENTER);
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
        // Get unique story for each world
        String story = getWorldStory(worldId);
        
        // Create a custom panel with black background and fade animation
        StoryPanel storyPanel = new StoryPanel(story, worldId);
        
        return storyPanel;
    }
    
    private class StoryPanel extends JPanel {
        private final String story;
        private final int worldId;
        private float textAlpha = 0f;
        private int currentSentenceIndex = 0;
        private String[] sentences;
        private javax.swing.Timer fadeTimer;
        private boolean isFadingIn = true;
        private boolean isFadingOut = false;
        private boolean storyComplete = false;
        
        public StoryPanel(String story, int worldId) {
            this.story = story;
            this.worldId = worldId;
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(Color.BLACK);
            
            // Add mouse listener to skip animation on click
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Skip to battle immediately
                    if (fadeTimer != null) {
                        fadeTimer.stop();
                    }
                    showBattle(worldId);
                }
            });
            
            // Initialize sentences when component is shown
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    initializeSentences();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Fill entire screen with black
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw current sentence with fade
            if (sentences != null && currentSentenceIndex < sentences.length && !storyComplete) {
                String sentence = sentences[currentSentenceIndex].trim();
                if (!sentence.isEmpty()) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
                    
                    // Use large, readable font
                    Font storyFont = new Font(Font.SANS_SERIF, Font.PLAIN, 28);
                    g2d.setFont(storyFont);
                    g2d.setColor(UITheme.PRIMARY_ORANGE);
                    
                    // Center the text
                    FontMetrics fm = g2d.getFontMetrics();
                    String[] lines = wrapText(sentence, getWidth() - 120, fm);
                    int totalHeight = lines.length * (fm.getHeight() + 10);
                    int startY = (getHeight() - totalHeight) / 2 + fm.getAscent();
                    
                    for (int i = 0; i < lines.length; i++) {
                        int textWidth = fm.stringWidth(lines[i]);
                        int x = (getWidth() - textWidth) / 2;
                        int y = startY + i * (fm.getHeight() + 10);
                        g2d.drawString(lines[i], x, y);
                    }
                }
            }
            
            g2d.dispose();
        }
        
        private String[] wrapText(String text, int maxWidth, FontMetrics fm) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                String testLine = currentLine.length() > 0 
                    ? currentLine.toString() + " " + word 
                    : word;
                int width = fm.stringWidth(testLine);
                
                if (width > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            return lines.toArray(new String[0]);
        }
        
        private void initializeSentences() {
            // Split story into sentences (by periods, exclamation, question marks, and newlines)
            String[] splitByNewline = story.split("\n\n");
            List<String> sentenceList = new ArrayList<>();
            
            for (String paragraph : splitByNewline) {
                // Split by sentence endings
                String[] parts = paragraph.split("(?<=[.!?])\\s+");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        sentenceList.add(trimmed);
                    }
                }
            }
            
            sentences = sentenceList.toArray(new String[0]);
            currentSentenceIndex = 0;
            textAlpha = 0f;
            isFadingIn = true;
            isFadingOut = false;
            storyComplete = false;
            
            if (sentences.length > 0) {
                startFadeAnimation();
            } else {
                // No sentences, go straight to battle
                storyComplete = true;
                javax.swing.Timer delayTimer = new javax.swing.Timer(500, e -> {
                    showBattle(worldId);
                    ((javax.swing.Timer) e.getSource()).stop();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        }
        
        private void startFadeAnimation() {
            if (fadeTimer != null && fadeTimer.isRunning()) {
                fadeTimer.stop();
            }
            
                fadeTimer = new javax.swing.Timer(16, null); // ~60 FPS
                fadeTimer.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (storyComplete) {
                            fadeTimer.stop();
                            return;
                        }
                        
                        if (isFadingIn) {
                            // Fade in current sentence
                            textAlpha += 0.03f;
                            if (textAlpha >= 1f) {
                                textAlpha = 1f;
                                isFadingIn = false;
                                // Hold for 3 seconds
                                javax.swing.Timer holdTimer = new javax.swing.Timer(3000, evt -> {
                                    isFadingOut = true;
                                    ((javax.swing.Timer) evt.getSource()).stop();
                                });
                                holdTimer.setRepeats(false);
                                holdTimer.start();
                            }
                        } else if (isFadingOut) {
                            // Fade out current sentence
                            textAlpha -= 0.03f;
                            if (textAlpha <= 0f) {
                                textAlpha = 0f;
                                isFadingOut = false;
                                // Move to next sentence
                                currentSentenceIndex++;
                                if (currentSentenceIndex >= sentences.length) {
                                    // All sentences shown, transition to battle
                                    storyComplete = true;
                                    fadeTimer.stop();
                                    // Small delay before transitioning
                                    javax.swing.Timer transitionTimer = new javax.swing.Timer(500, evt -> {
                                        showBattle(worldId);
                                        ((javax.swing.Timer) evt.getSource()).stop();
                                    });
                                    transitionTimer.setRepeats(false);
                                    transitionTimer.start();
                                } else {
                                    // Start fading in next sentence
                                    isFadingIn = true;
                                }
                            }
                        }
                        repaint();
                    }
                });
                fadeTimer.start();
        }
    }
    
    private String getWorldStory(int worldId) {
        switch (worldId) {
            case 1: // Chronovale - Time Core
                return "WORLD 1: CHRONOVALE\n" +
                       " Core of Time\n\n" +
                       "The first world of the Veil System, Chronovale was once a realm where time flowed " +
                       "in perfect harmony. Ancient civilizations built monuments that stood for millennia, " +
                       "their history preserved in crystalline structures that recorded every moment.\n\n" +
                       "But Xyrrak's forces have arrived. Bio-mechanical horrors now stalk the temporal plains, " +
                       "their very presence causing time to fracture and loop. The Time Core pulses erratically, " +
                       "sending ripples of temporal chaos across the planet.\n\n" +
                       "Your team arrives to find Chronovale's guardians already fallen. The Core's chamber " +
                       "lies ahead, but Xyrrak's minions have twisted the flow of time itself into a weapon. " +
                       "You must fight through temporal anomalies and corrupted guardians to reach the Core.\n\n" +
                       "If the Time Core falls, the very foundation of causality will shatter. The galaxy " +
                       "cannot afford to lose this first line of defense.";
                       
            case 2: // Gravemire - Gravity Core
                return "WORLD 2: GRAVEMIRE\n" +
                       "Core of Gravity\n\n" +
                       "Gravemire, the second world, was a planet of impossible architecture—cities built " +
                       "on floating islands that defied gravity, connected by bridges of pure gravitational force. " +
                       "The Gravity Core maintained perfect balance, allowing life to thrive in a world where " +
                       "up and down were mere suggestions.\n\n" +
                       "Xyrrak's corruption has already begun. The floating cities now drift aimlessly, " +
                       "their gravitational fields unstable. Some islands crash into each other, while others " +
                       "spin wildly into the void. The Core's chamber itself has become a labyrinth of " +
                       "shifting gravity wells.\n\n" +
                       "Reports speak of a massive bio-mechanical construct that has fused with the Core's " +
                       "chamber, using gravity as both shield and weapon. Your team must navigate the " +
                       "chaotic gravitational fields and face the corrupted guardian.\n\n" +
                       "Time is running out. With each passing moment, Xyrrak's influence grows stronger.";
                       
            case 3: // Aetherion - Energy Core
                return "WORLD 3: AETHERION\n" +
                       "Core of Energy\n\n" +
                       "Aetherion was the most vibrant of the Veil worlds—a planet where pure energy flowed " +
                       "like rivers of light. The Energy Core powered entire civilizations, its power so vast " +
                       "that it could reshape matter itself. Here, technology and magic merged seamlessly.\n\n" +
                       "But Xyrrak's ambition has turned Aetherion into a nightmare. The energy flows have " +
                       "become corrupted, twisting into violent storms of raw power. The Core itself has " +
                       "begun to overload, its energy bleeding into reality in chaotic bursts.\n\n" +
                       "Your team discovers that Xyrrak has sent one of his most powerful lieutenants to " +
                       "harness the Core's energy. The chamber is a maelstrom of unstable power, and the " +
                       "guardian has been transformed into a being of pure, corrupted energy.\n\n" +
                       "As you prepare to enter the Core chamber, you sense a new presence—a warrior who " +
                       "has been fighting Xyrrak's forces alone. Perhaps an ally will join your cause...";
                       
            case 4: // Elarion - Life Core
                return "WORLD 4: ELARION\n" +
                       "Core of Life\n\n" +
                       "Elarion was the heart of the Veil System—a world of unparalleled beauty where life " +
                       "flourished in impossible forms. The Life Core sustained all living things, its power " +
                       "flowing through every plant, every creature, every breath. It was said that on Elarion, " +
                       "death itself was merely a transition.\n\n" +
                       "But Xyrrak's corruption has struck Elarion hardest. The Life Core has been twisted, " +
                       "its power perverted to create abominations—living things fused with mechanical parts, " +
                       "creatures that should not exist. The once-beautiful forests now writhe with " +
                       "bio-mechanical horrors.\n\n" +
                       "The Core's chamber has become a nightmare of organic and mechanical fusion. The " +
                       "guardian, once a protector of all life, has been transformed into a monstrous " +
                       "amalgamation of flesh and steel.\n\n" +
                       "This is the last Core before Umbros. If you fail here, Xyrrak will have all the " +
                       "power he needs to complete his ascension. The fate of the galaxy hangs in the balance.";
                       
            case 5: // Umbros - Void Core (Xyrrak's domain)
                return "WORLD 5: UMBROS\n" +
                       "Core of Void\n\n" +
                       "Umbros was never meant to be a world. It is a void—a place where reality itself " +
                       "becomes uncertain. The Void Core exists here, not as a source of power, but as a " +
                       "balance to all existence. It is the emptiness that gives meaning to everything else.\n\n" +
                       "But Xyrrak has made Umbros his throne. Here, in the heart of nothingness, he has " +
                       "built his fortress. The Void Core has been corrupted beyond recognition, its power " +
                       "twisted to serve Xyrrak's will. The other four Cores, already weakened, are being " +
                       "drawn here, their energy feeding Xyrrak's transformation.\n\n" +
                       "Your team stands at the threshold of the final battle. Through the void, you can " +
                       "see Xyrrak the Devourer—no longer a warlord, but something far more terrible. " +
                       "He has begun to fuse the Cores, ascending toward godhood.\n\n" +
                       "This is it. The final stand. If you fail, Xyrrak will remake reality in his image, " +
                       "and the Veil System—and all existence—will be lost forever. But if you succeed, " +
                       "you will restore the Cores, purify what has been corrupted, and save the galaxy.\n\n" +
                       "The four heroes, once divided, now stand united. Together, you are the last hope " +
                       "of the Veil System. Together, you will face Xyrrak.";
                       
            default:
                return "WORLD " + worldId + "\n\nPrepare for battle...";
        }
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

        // Dark battle background panel
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Very dark background for battle
                g2d.setColor(new Color(5, 8, 12)); // Almost black
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        panel.setOpaque(true);
        panel.setBackground(new Color(5, 8, 12));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Restructured battle UI to match reference layout
        panel.add(createBattleTopBar(), BorderLayout.NORTH);
        panel.add(createBattleMainArea(), BorderLayout.CENTER);
        panel.add(createBattleBottomDetails(), BorderLayout.SOUTH);

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
        int index = Math.max(0, Math.min(worldId - 1, WORLD_ENEMY_LEVEL_OFFSET.length - 1));
        
        // Scale with player level instead of fixed levels
        int playerLevel = playerProgress != null ? playerProgress.getPlayerLevel() : 1;
        int levelOffset = WORLD_ENEMY_LEVEL_OFFSET[index];
        int levelTarget = Math.min(30, Math.max(1, playerLevel + levelOffset + (waveNumber / 2)));
        double difficulty = 1.0 + (waveNumber - 1) * 0.02; // Minimal difficulty scaling per wave

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
                // HP, Mana, Defense scale with difficulty; Attack will be scaled to hero HP in applyEnemyScaling
                minion.applyStatMultiplier(difficulty, difficulty, 1.0, difficulty);
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
            // Scale supporter level with player level
            int playerLevel = playerProgress != null ? playerProgress.getPlayerLevel() : 1;
            int index = Math.min(worldId - 1, WORLD_ENEMY_LEVEL_OFFSET.length - 1);
            int levelOffset = WORLD_ENEMY_LEVEL_OFFSET[index];
            int supporterLevel = Math.min(30, Math.max(1, playerLevel + levelOffset + 1));
            
            for (int i = 0; i < supporters; i++) {
                MinionTemplate template = pool.get(random.nextInt(pool.size()));
                roster.add(template.instantiate(supporterLevel, 1.1)); // Slight difficulty increase
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
            () -> new AegisPulseSkill("Call of Dominion", 50)
        );
        // Boss level now scales with player level (will be adjusted in applyEnemyScaling)
        int playerLevel = playerProgress != null ? playerProgress.getPlayerLevel() : 1;
        int bossLevel = Math.min(30, Math.max(1, playerLevel + 2));
        boss.syncToLevel(bossLevel);
        // Balanced base multipliers - HP and defense only (attack will be scaled to hero HP)
        boss.applyStatMultiplier(1.15, 1.1, 1.0, 1.05);
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
                () -> new AegisPulseSkill("Serene Glow", 40)
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
                () -> new AegisPulseSkill("Mud Ward", 45)
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
                () -> new AegisPulseSkill("Shield Matrix", 50)
            ),
            new MinionTemplate("Skyblade", 600, 260, 74, 32, 46,
                () -> new SavageSwipeSkill("Skyfall", 1.25)
            )
        );
        pools.add(world3);

        List<MinionTemplate> world4 = List.of(
            new MinionTemplate("Elarion Sentinel", 780, 300, 90, 44, 38,
                () -> new SavageSwipeSkill("Spear Barrage", 1.3),
                () -> new AegisPulseSkill("Renewing Chant", 55)
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
                () -> new AegisPulseSkill("Rite of Night", 60)
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

        // Check if Zyra should be unlocked
        if (playerProgress != null) {
            // Unlock Zyra if World 2 is cleared (can enter World 3) or if already unlocked
            if (playerProgress.canEnterWorld(3) && !playerProgress.isZyraUnlocked()) {
                // World 3 is accessible, which means World 2 is cleared - unlock Zyra
                playerProgress.unlockZyra();
                saveActiveProfile(); // Save the unlock
            }
            
            // Add Zyra if unlocked
            if (playerProgress.isZyraUnlocked()) {
                roster.add(new ZyraKathun());
            }
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
        double defenseMultiplier = WORLD_DEF_MULT[index];
        int levelOffset = WORLD_ENEMY_LEVEL_OFFSET[index];
        
        // Calculate average hero HP to scale enemy damage proportionally
        int totalHeroHP = 0;
        int heroCount = 0;
        if (playerTeam != null) {
            for (Character hero : playerTeam) {
                if (hero != null && hero.isAlive) {
                    totalHeroHP += hero.maxHP;
                    heroCount++;
                }
            }
        }
        int avgHeroHP = heroCount > 0 ? totalHeroHP / heroCount : 500; // Default if no heroes
        
        // Scale enemy level with player level (capped to keep it balanced)
        int playerLevel = playerProgress != null ? playerProgress.getPlayerLevel() : 1;
        int baseEnemyLevel = Math.min(30, Math.max(1, playerLevel + levelOffset));
        
        // Minimal wave scaling for HP only (not attack)
        double waveScalar = wave != null ? 1.0 + (wave.waveNumber - 1) * 0.03 : 1.0;
        if (wave != null && wave.bossWave) {
            waveScalar += 0.1; // Small boss HP bonus
        }

        for (int i = 0; i < enemyTeam.length; i++) {
            Character enemy = enemyTeam[i];
            // Enemy level scales with player, with small variation per enemy
            int levelTarget = Math.min(30, baseEnemyLevel + (i * 1));
            enemy.syncToLevel(levelTarget);

            // Apply HP and defense multipliers with wave scaling
            double hpMult = hpMultiplier * waveScalar;
            double defMult = defenseMultiplier * waveScalar;
            
            // Boss gets more HP and defense, but attack scales with hero HP
            if (i == enemyTeam.length - 1) {
                hpMult += 0.15; // Boss gets more HP
                defMult += 0.08; // Boss gets slightly more defense
            }
            
            // Scale enemy attack to be proportional to hero HP (5-7% per basic attack)
            // This ensures enemies deal fair damage regardless of hero HP
            double attackPercentOfHeroHP = 0.05 + (index * 0.005); // 5% to 7% based on world
            if (i == enemyTeam.length - 1) {
                attackPercentOfHeroHP += 0.01; // Boss deals slightly more (6-8%)
            }
            int targetAttack = (int) Math.round(avgHeroHP * attackPercentOfHeroHP);
            
            // Apply HP, Mana, Defense multipliers
            enemy.applyStatMultiplier(hpMult, manaMultiplier, 1.0, defMult);
            
            // Set attack directly based on hero HP scaling
            enemy.baseAttack = enemy.currentAttack = targetAttack;
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
        // Unlock Zyra when world 2 is cleared, so she's available for worlds 3, 4, and 5
        if (worldId == 2 && !playerProgress.isZyraUnlocked()) {
            playerProgress.unlockZyra();
            unlockedZyra = true;
        }
        // Save progress after battle victory
        saveActiveProfile();
        refreshWorldSelection();

        StringBuilder message = new StringBuilder("Victory! You have cleared World ")
            .append(worldId)
            .append(".\n\nRewards:\n+ ")
            .append(expEarned)
            .append(" EXP");

        if (playerProgress.getPlayerLevel() > previousLevel) {
            message.append("\nLevel Up! Now Level ").append(playerProgress.getPlayerLevel()).append("!");
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

    // New battle layout matching reference
    private JPanel createBattleTopBar() {
        // Top bar with turn info and pause icon
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark background
                Color bgColor = new Color(8, 10, 12, 240);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.setPreferredSize(new Dimension(getWidth(), 60));

        // Center - Turn info and "SELECT AN ATTACK"
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        battleTurnLabel = new JLabel("Preparing...", SwingConstants.CENTER);
        battleTurnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        battleTurnLabel.setForeground(new Color(UITheme.PRIMARY_ORANGE.getRed(), UITheme.PRIMARY_ORANGE.getGreen(), UITheme.PRIMARY_ORANGE.getBlue(), 200));
        battleTurnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        battleInstructionLabel = new JLabel("SELECT AN ATTACK", SwingConstants.CENTER);
        battleInstructionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        battleInstructionLabel.setForeground(new Color(100, 150, 255)); // Light blue
        battleInstructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(battleTurnLabel);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(battleInstructionLabel);

        // Right side - Pause icon button
        JPanel rightIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightIcons.setOpaque(false);
        JButton pauseBtn = new JButton("⏸") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 100));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.setColor(UITheme.PRIMARY_WHITE);
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        pauseBtn.setContentAreaFilled(false);
        pauseBtn.setOpaque(false);
        pauseBtn.setBorder(null);
        pauseBtn.setPreferredSize(new Dimension(40, 40));
        pauseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pauseBtn.addActionListener(e -> showPauseMenu());
        rightIcons.add(pauseBtn);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightIcons, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBattleMainArea() {
        // Main area with character list on left, battlefield in center, skill buttons
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Left side - Character portrait list (vertical)
        battleCharacterListPanel = new JPanel();
        battleCharacterListPanel.setLayout(new BoxLayout(battleCharacterListPanel, BoxLayout.Y_AXIS));
        battleCharacterListPanel.setOpaque(false);
        battleCharacterListPanel.setPreferredSize(new Dimension(80, 0));
        battleCharacterListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buildCharacterList();

        // Center - Battlefield with characters and skill buttons
        JPanel battlefieldPanel = new JPanel(new BorderLayout());
        battlefieldPanel.setOpaque(false);

        // Top: Character panels (players left, enemies right)
        JPanel characterArea = new JPanel(new BorderLayout(20, 10));
        characterArea.setOpaque(false);
        // Let character area auto-adjust to available space
        characterArea.setMinimumSize(new Dimension(550, 200));

        // Player characters on left
        battlePlayerPanel = new JPanel();
        battlePlayerPanel.setLayout(new BoxLayout(battlePlayerPanel, BoxLayout.Y_AXIS));
        battlePlayerPanel.setOpaque(false);
        // Auto-adjust width based on content, minimum to fit character cards
        battlePlayerPanel.setMinimumSize(new Dimension(260, 0));
        battlePlayerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Enemy characters on right
        battleEnemyPanel = new JPanel();
        battleEnemyPanel.setLayout(new BoxLayout(battleEnemyPanel, BoxLayout.Y_AXIS));
        battleEnemyPanel.setOpaque(false);
        // Auto-adjust width based on content, minimum to fit character cards
        battleEnemyPanel.setMinimumSize(new Dimension(260, 0));
        battleEnemyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        buildBattleCharacterPanels();
        
        // Wrap panels in scroll panes to prevent overflow and ensure they fit in their containers
        // Scroll panes will auto-adjust to available space
        JScrollPane playerScroll = new JScrollPane(battlePlayerPanel);
        playerScroll.setOpaque(false);
        playerScroll.getViewport().setOpaque(false);
        playerScroll.setBorder(null);
        playerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        playerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Auto-adjust: minimum width to fit cards, height adjusts to available space
        playerScroll.setMinimumSize(new Dimension(260, 150));
        
        JScrollPane enemyScroll = new JScrollPane(battleEnemyPanel);
        enemyScroll.setOpaque(false);
        enemyScroll.getViewport().setOpaque(false);
        enemyScroll.setBorder(null);
        enemyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        enemyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Auto-adjust: minimum width to fit cards, height adjusts to available space
        enemyScroll.setMinimumSize(new Dimension(260, 150));
        
        characterArea.add(playerScroll, BorderLayout.WEST);
        characterArea.add(enemyScroll, BorderLayout.EAST);

        // No skill buttons in center - they go in bottom right panel
        battlefieldPanel.add(characterArea, BorderLayout.CENTER);

        panel.add(battleCharacterListPanel, BorderLayout.WEST);
        panel.add(battlefieldPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBattleBottomDetails() {
        // Bottom panel: left = character stats, right = enemy stats + event log + skills
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Set size to ensure panels are visible without blocking heroes and enemy panels
        // Reduced height to give more space to character area
        panel.setPreferredSize(new Dimension(getWidth(), 200));
        panel.setMinimumSize(new Dimension(800, 180));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Left: Selected character details panel
        battleCharacterDetailsPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = new Color(12, 15, 18, 220);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        battleCharacterDetailsPanel.setOpaque(false);
        battleCharacterDetailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        battleCharacterDetailsPanel.setPreferredSize(new Dimension(200, 0));
        updateCharacterDetails();

        // Right: Container for battle log, skills, and enemy stats in a horizontal row
        JPanel rightContainer = new JPanel(new BorderLayout(5, 5));
        rightContainer.setOpaque(false);

        // Center: Battle log panel
        battleEventLogPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = new Color(12, 15, 18, 240);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        battleEventLogPanel.setOpaque(true);
        battleEventLogPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        battleEventLogPanel.setPreferredSize(new Dimension(400, 160));
        battleEventLogPanel.setMinimumSize(new Dimension(350, 140));
        
        // Battle log
        battleLog = new JTextArea();
        battleLog.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBackground(new Color(8, 10, 12));
        battleLog.setForeground(new Color(UITheme.LOG_TEXT.getRed(), UITheme.LOG_TEXT.getGreen(), UITheme.LOG_TEXT.getBlue(), 200));
        battleLog.setOpaque(true);
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        logScroll.setBorder(null);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Set size for battle log to avoid blocking other panels
        logScroll.setPreferredSize(new Dimension(350, 150));
        logScroll.setMinimumSize(new Dimension(300, 120));
        battleEventLogPanel.add(logScroll, BorderLayout.CENTER);

        // Skills panel (right of battle log)
        battleAttackDetailsPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = new Color(12, 15, 18, 240);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        battleAttackDetailsPanel.setOpaque(true);
        battleAttackDetailsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        battleAttackDetailsPanel.setPreferredSize(new Dimension(200, 160));
        battleAttackDetailsPanel.setMinimumSize(new Dimension(180, 140));
        
        // Initialize skill panel container
        JPanel initialSkillPanel = new JPanel();
        initialSkillPanel.setLayout(new BoxLayout(initialSkillPanel, BoxLayout.Y_AXIS));
        initialSkillPanel.setOpaque(false);
        initialSkillPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        battleSkillPanel = initialSkillPanel;
        JScrollPane skillScroll = new JScrollPane(initialSkillPanel);
        skillScroll.setOpaque(false);
        skillScroll.getViewport().setOpaque(false);
        skillScroll.setBorder(null);
        skillScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        skillScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        battleAttackDetailsPanel.add(skillScroll, BorderLayout.CENTER);
        
        // Update will populate skills
        updateAttackDetails();

        // Enemy stats panel (right of skills)
        battleEnemyDetailsPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = new Color(12, 15, 18, 240);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        battleEnemyDetailsPanel.setOpaque(true);
        battleEnemyDetailsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        battleEnemyDetailsPanel.setPreferredSize(new Dimension(180, 160));
        battleEnemyDetailsPanel.setMinimumSize(new Dimension(160, 140));
        updateEnemyDetails();

        // Create a container for skills and enemy stats on the right side
        JPanel rightSidePanel = new JPanel(new BorderLayout(5, 5));
        rightSidePanel.setOpaque(false);
        rightSidePanel.setPreferredSize(new Dimension(390, 160));
        rightSidePanel.setMinimumSize(new Dimension(350, 140));
        rightSidePanel.add(battleAttackDetailsPanel, BorderLayout.WEST);  // Skills on left
        rightSidePanel.add(battleEnemyDetailsPanel, BorderLayout.EAST);  // Enemy stats on right
        
        // Add panels to right container: Battle Log (center, takes most space) | Skills + Enemy Stats (east)
        rightContainer.add(battleEventLogPanel, BorderLayout.CENTER);
        rightContainer.add(rightSidePanel, BorderLayout.EAST);
        
        // Ensure right container has proper size constraints
        rightContainer.setPreferredSize(new Dimension(800, 160));
        rightContainer.setMinimumSize(new Dimension(750, 140));

        panel.add(battleCharacterDetailsPanel, BorderLayout.WEST);
        panel.add(rightContainer, BorderLayout.EAST);

        return panel;
    }
    
    private void updateEnemyDetails() {
        battleEnemyDetailsPanel.removeAll();
        if (enemyTeam == null || enemyTeam.length == 0) return;

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        JLabel titleLabel = new JLabel("ENEMIES");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(UITheme.PRIMARY_RED);
        details.add(titleLabel);
        details.add(Box.createVerticalStrut(5));

        for (Character e : enemyTeam) {
            if (e != null && e.isAlive()) {
                // Use HTML to enable text wrapping for long enemy names
                String displayName = e.name.length() > 15 ? e.name.substring(0, 12) + "..." : e.name;
                JLabel enemyLabel = new JLabel("<html><div style='width:160px;'>" + displayName + "</div></html>");
                enemyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                enemyLabel.setForeground(UITheme.PRIMARY_WHITE);
                details.add(enemyLabel);
                
                JLabel hpLabel = new JLabel("❤ " + e.currentHP + "/" + e.maxHP);
                hpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                hpLabel.setForeground(UITheme.PRIMARY_WHITE);
                details.add(hpLabel);
                
                JLabel manaLabel = new JLabel("💙 " + e.currentMana + "/" + e.maxMana);
                manaLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                manaLabel.setForeground(UITheme.PRIMARY_WHITE);
                details.add(manaLabel);
                
                details.add(Box.createVerticalStrut(5));
            }
        }

        // Wrap details in a scroll pane to prevent overflow
        JScrollPane enemyScroll = new JScrollPane(details);
        enemyScroll.setOpaque(false);
        enemyScroll.getViewport().setOpaque(false);
        enemyScroll.setBorder(null);
        enemyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        enemyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        battleEnemyDetailsPanel.add(enemyScroll, BorderLayout.CENTER);
        battleEnemyDetailsPanel.revalidate();
        battleEnemyDetailsPanel.repaint();
    }

    private void buildCharacterList() {
        battleCharacterListPanel.removeAll();
        if (playerTeam == null) return;

        for (int i = 0; i < playerTeam.length; i++) {
            final int index = i;
            Character c = playerTeam[i];
            JPanel portraitPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    boolean isSelected = (currentPlayerIndex == index);
                    if (isSelected) {
                        g2d.setColor(new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 150));
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                    g2d.dispose();
                }
            };
            portraitPanel.setOpaque(false);
            portraitPanel.setPreferredSize(new Dimension(60, 60));
            portraitPanel.setBorder(BorderFactory.createLineBorder(
                currentPlayerIndex == index ? UITheme.PRIMARY_GREEN : UITheme.BORDER_NORMAL, 
                currentPlayerIndex == index ? 3 : 1
            ));

            JLabel nameLabel = new JLabel(c.name, SwingConstants.CENTER);
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            nameLabel.setForeground(UITheme.PRIMARY_WHITE);
            portraitPanel.add(nameLabel, BorderLayout.CENTER);

            battleCharacterListPanel.add(portraitPanel);
            battleCharacterListPanel.add(Box.createVerticalStrut(5));
        }

        battleCharacterListPanel.revalidate();
        battleCharacterListPanel.repaint();
    }

    private void updateCharacterDetails() {
        battleCharacterDetailsPanel.removeAll();
        if (playerTeam == null || currentPlayerIndex >= playerTeam.length) return;

        Character c = playerTeam[currentPlayerIndex];
        
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        JLabel nameLabel = new JLabel(c.name);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        nameLabel.setForeground(UITheme.PRIMARY_WHITE);
        details.add(nameLabel);

        JLabel typeLabel = new JLabel("Level " + c.level);
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        typeLabel.setForeground(new Color(UITheme.PRIMARY_WHITE.getRed(), UITheme.PRIMARY_WHITE.getGreen(), UITheme.PRIMARY_WHITE.getBlue(), 180));
        details.add(typeLabel);

        details.add(Box.createVerticalStrut(10));

        JLabel hpLabel = new JLabel("❤ " + c.currentHP + "/" + c.maxHP);
        hpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        hpLabel.setForeground(UITheme.PRIMARY_WHITE);
        details.add(hpLabel);

        JLabel manaLabel = new JLabel("💙 " + c.currentMana + "/" + c.maxMana);
        manaLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        manaLabel.setForeground(UITheme.PRIMARY_WHITE);
        details.add(manaLabel);

        // Get attack and defense from character stats
        JLabel atkLabel = new JLabel("⚔ " + c.currentAttack);
        atkLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        atkLabel.setForeground(UITheme.PRIMARY_WHITE);
        details.add(atkLabel);

        JLabel defLabel = new JLabel("🛡 " + c.currentDefense);
        defLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        defLabel.setForeground(UITheme.PRIMARY_WHITE);
        details.add(defLabel);

        battleCharacterDetailsPanel.add(details, BorderLayout.CENTER);
        battleCharacterDetailsPanel.revalidate();
        battleCharacterDetailsPanel.repaint();
    }

    private void updateAttackDetails() {
        if (battleSkillPanel == null || playerTeam == null || currentPlayerIndex >= playerTeam.length) return;

        Character c = playerTeam[currentPlayerIndex];
        
        // Clear existing skills
        battleSkillPanel.removeAll();

        if (c.skills != null && !c.skills.isEmpty()) {
            loadBattleSkillButtons(c);
        }

        battleSkillPanel.revalidate();
        battleSkillPanel.repaint();
        battleAttackDetailsPanel.revalidate();
        battleAttackDetailsPanel.repaint();
    }

    // Old method kept for reference but not used
    private JPanel createBattleTopPanel(int worldId) {
        // Darker top panel
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark background
                Color bgColor = new Color(8, 10, 12, 220);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle border
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(UITheme.BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = createReadableLabel("WORLD " + worldId + " - BATTLE", UITheme.FONT_SUBTITLE, new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 180), SwingConstants.CENTER);

        battleTurnLabel = new JLabel("Preparing...", SwingConstants.CENTER);
        battleTurnLabel.setFont(UITheme.FONT_HEADER);
        battleTurnLabel.setForeground(new Color(UITheme.PRIMARY_ORANGE.getRed(), UITheme.PRIMARY_ORANGE.getGreen(), UITheme.PRIMARY_ORANGE.getBlue(), 200));

        battleWaveLabel = new JLabel("", SwingConstants.CENTER);
        battleWaveLabel.setFont(UITheme.FONT_SMALL);
        battleWaveLabel.setForeground(new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 160));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(battleTurnLabel, BorderLayout.CENTER);
        panel.add(battleWaveLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBattleCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Darker player panel
        battlePlayerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                // Dark background
                Color bgColor = new Color(12, 15, 18, 200);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle border
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(UITheme.BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2d.dispose();
            }
        };
        battlePlayerPanel.setLayout(new BoxLayout(battlePlayerPanel, BoxLayout.Y_AXIS));
        battlePlayerPanel.setOpaque(false);
        battlePlayerPanel.setBorder(UITheme.createTitledBorder("YOUR TEAM", new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 180), new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 100)));

        // Darker battle log
        battleLog = new JTextArea();
        battleLog.setFont(UITheme.FONT_LOG);
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        // Dark background
        battleLog.setBackground(new Color(8, 10, 12));
        battleLog.setForeground(new Color(UITheme.LOG_TEXT.getRed(), UITheme.LOG_TEXT.getGreen(), UITheme.LOG_TEXT.getBlue(), 200));
        battleLog.setOpaque(true);
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setPreferredSize(new Dimension(400, 400));

        // Darker enemy panel
        battleEnemyPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                // Dark background
                Color bgColor = new Color(18, 12, 15, 200);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle border
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(UITheme.BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2d.dispose();
            }
        };
        battleEnemyPanel.setLayout(new BoxLayout(battleEnemyPanel, BoxLayout.Y_AXIS));
        battleEnemyPanel.setOpaque(false);
        battleEnemyPanel.setBorder(UITheme.createTitledBorder("ENEMIES", new Color(UITheme.PRIMARY_RED.getRed(), UITheme.PRIMARY_RED.getGreen(), UITheme.PRIMARY_RED.getBlue(), 180), new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 100)));

        buildBattleCharacterPanels();

        panel.add(battlePlayerPanel, BorderLayout.WEST);
        panel.add(logScroll, BorderLayout.CENTER);
        panel.add(battleEnemyPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBattleBottomPanel() {
        // Darker bottom panel
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark background
                Color bgColor = new Color(10, 12, 15, 220);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle border
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(UITheme.BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        battleInstructionLabel = new JLabel("Select a skill", SwingConstants.CENTER);
        battleInstructionLabel.setFont(UITheme.FONT_BUTTON_SMALL);
        battleInstructionLabel.setForeground(new Color(UITheme.PRIMARY_ORANGE.getRed(), UITheme.PRIMARY_ORANGE.getGreen(), UITheme.PRIMARY_ORANGE.getBlue(), 200));

        battleSkillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        battleSkillPanel.setOpaque(false);

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
        String text = String.format("Wave %d / %d%s",
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

    private class PausePanel extends JPanel {
        private float overlayAlpha = 0f;
        private javax.swing.Timer animation;

        PausePanel() {
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0)); // Fully transparent background
            setLayout(new BorderLayout());
            startAnimation();
        }

        private void startAnimation() {
            // Start with a small initial alpha to avoid white flash
            overlayAlpha = 0.01f;
            animation = new javax.swing.Timer(16, e -> {
                overlayAlpha = Math.min(0.95f, overlayAlpha + 0.05f); // Almost black (95% opacity)
                repaint();
                if (overlayAlpha >= 0.95f) {
                    animation.stop();
                }
            });
            animation.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Don't call super.paintComponent to avoid default background painting
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Transparent black overlay - battle barely visible (no white flash)
            Color overlayColor = new Color(0, 0, 0, (int)(255 * overlayAlpha));
            g2.setColor(overlayColor);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw game title in top left
            Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 32);
            g2.setFont(titleFont);
            float titleAlpha = Math.min(1f, overlayAlpha * 1.2f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
            g2.setColor(UITheme.PRIMARY_WHITE);
            g2.drawString("DEFENDERS OF SOLARA", 20, 40);
            
            g2.dispose();
        }
        
        @Override
        protected void paintChildren(Graphics g) {
            // Buttons fade in
            Graphics2D g2 = (Graphics2D) g.create();
            float buttonAlpha = Math.min(1f, overlayAlpha * 1.2f); // Buttons appear slightly faster
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, buttonAlpha));
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
            // Balanced scaling - HP scales with difficulty, mana scales less, attack will be overridden by applyEnemyScaling
            double hpMult = difficulty;
            double manaMult = 1.0 + Math.max(0, difficulty - 1) * 0.25; // Reduced mana scaling
            double atkMult = 1.0; // Attack will be scaled to hero HP in applyEnemyScaling, so keep base
            double defMult = 1.0 + Math.max(0, difficulty - 1) * 0.2; // Lower defense scaling
            enemy.applyStatMultiplier(hpMult, manaMult, atkMult, defMult);
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
            this.description = "Heals all allies for " + healAmount + " HP (only when caster HP ≤ 5%).";
            this.targetType = TargetType.ALL_ALLIES;
        }

        @Override
        public void execute(Character user, Character[] targets) {
            if (targets == null || targets.length == 0) return;
            if (user.currentMana < manaCost) return;
            
            // Priest enemies only heal when their HP is 5% or below
            double hpPercent = (double) user.currentHP / user.maxHP;
            if (hpPercent > 0.05) {
                // HP is above 5%, don't heal
                return;
            }
            
            // Low to medium chance (35% chance) to actually heal
            if (Math.random() > 0.35) {
                // Failed chance, don't heal
                return;
            }
            
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
                
                // Much darker background
                Color bgColor = new Color(8, 10, 12, 200);
                g2d.setColor(bgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle border
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.setColor(UITheme.BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        // Ensure card has proper size to show all bars
        Dimension cardSize = new Dimension(Math.max(250, UITheme.CHARACTER_CARD.width), Math.max(120, UITheme.CHARACTER_CARD.height));
        card.setPreferredSize(cardSize);
        card.setMinimumSize(cardSize);
        card.setMaximumSize(new Dimension(cardSize.width, Integer.MAX_VALUE)); // Allow vertical expansion

        JLabel nameLabel = new JLabel(c.name);
        nameLabel.setFont(UITheme.FONT_CARD_NAME);
        nameLabel.setForeground(UITheme.PRIMARY_WHITE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar hpBar = new JProgressBar(0, c.maxHP);
        hpBar.setValue(c.currentHP);
        hpBar.setStringPainted(true);
        hpBar.setString(c.currentHP + " / " + c.maxHP);
        hpBar.setForeground(UITheme.HP_GREEN);
        hpBar.setPreferredSize(new Dimension(0, 25)); // Ensure proper height
        hpBar.setMinimumSize(new Dimension(0, 25));

        JProgressBar manaBar = new JProgressBar(0, c.maxMana);
        manaBar.setValue(c.currentMana);
        manaBar.setStringPainted(true);
        manaBar.setString(c.currentMana + " / " + c.maxMana);
        manaBar.setForeground(UITheme.MANA_BLUE);
        manaBar.setPreferredSize(new Dimension(0, 25)); // Ensure proper height
        manaBar.setMinimumSize(new Dimension(0, 25));

        JPanel barsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        barsPanel.setOpaque(false);
        barsPanel.setPreferredSize(new Dimension(0, 60)); // Ensure bars have space
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
        if (battleTurnLabel != null) {
        battleTurnLabel.setText("PLAYER TURN: " + current.name);
        }
        battleInstructionLabel.setText("SELECT AN ATTACK");
        battleInstructionLabel.setForeground(new Color(100, 150, 255)); // Light blue

        updateBattleBars();
        updateCharacterDetails();
        updateEnemyDetails();
        buildCharacterList();
        updateAttackDetails(); // This will initialize battleSkillPanel and load buttons

        selectedSkill = null;
        waitingForTarget = false;
        clearBattleHighlights();
    }

    private void loadBattleSkillButtons(Character character) {
        if (battleSkillPanel == null) return;
        battleSkillPanel.removeAll();

        for (Skill skill : character.skills) {
            // Simple clickable skill panels for bottom right
            final Skill skillRef = skill;
            final boolean canUse = skill.canUse(character);
            
            JPanel skillPanel = new JPanel(new BorderLayout(5, 2)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Boolean hovered = (Boolean) getClientProperty("isHovered");
                    if (hovered != null && hovered && canUse) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(new Color(UITheme.PRIMARY_GREEN.getRed(), UITheme.PRIMARY_GREEN.getGreen(), UITheme.PRIMARY_GREEN.getBlue(), 30));
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                        g2d.dispose();
                    }
                }
            };
            skillPanel.setOpaque(false);
            skillPanel.setCursor(canUse ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            skillPanel.setPreferredSize(new Dimension(0, 45));
            skillPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            // Skill name
            JLabel nameLabel = new JLabel(skill.getName());
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            nameLabel.setForeground(canUse ? UITheme.PRIMARY_WHITE : new Color(UITheme.TEXT_GRAY.getRed(), UITheme.TEXT_GRAY.getGreen(), UITheme.TEXT_GRAY.getBlue(), 150));
            
            // Skill description
            JLabel descLabel = new JLabel(skill.getDescription());
            descLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            descLabel.setForeground(canUse ? new Color(UITheme.PRIMARY_WHITE.getRed(), UITheme.PRIMARY_WHITE.getGreen(), UITheme.PRIMARY_WHITE.getBlue(), 180) : new Color(UITheme.TEXT_GRAY.getRed(), UITheme.TEXT_GRAY.getGreen(), UITheme.TEXT_GRAY.getBlue(), 120));
            
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(descLabel);
            
            skillPanel.add(textPanel, BorderLayout.CENTER);
            
            // Mouse listeners for click and hover
            skillPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (canUse) {
                        onBattleSkillSelected(skillRef, character);
                        updateAttackDetails();
                    }
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (canUse) {
                        skillPanel.putClientProperty("isHovered", true);
                        skillPanel.repaint();
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    skillPanel.putClientProperty("isHovered", false);
                    skillPanel.repaint();
                }
            });
            
            // Initialize hover state
            skillPanel.putClientProperty("isHovered", false);
            
            battleSkillPanel.add(skillPanel);
            battleSkillPanel.add(Box.createVerticalStrut(3));
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

        // Auto-save after wave completion (time is recorded in saveActiveProfile)
        saveActiveProfile();
        appendBattleLog("\n💾 Progress auto-saved!");

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
        dialog.setSize(getWidth(), getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0)); // Fully transparent dialog background
        dialog.getRootPane().setOpaque(false); // Make root pane transparent

        PausePanel glassPanel = new PausePanel();
        
        // Center panel for buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(new Color(0, 0, 0, 0)); // Fully transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create menu-style buttons with hover underline (no panels, just text boxes)
        PauseMenuButton resumeBtn = new PauseMenuButton("RESUME", dialog, () -> dialog.dispose());
        gbc.gridy = 0;
        buttonPanel.add(resumeBtn, gbc);

        PauseMenuButton optionsBtn = new PauseMenuButton("OPTIONS", dialog, () -> {
            dialog.dispose();
            settingsOpenedFromPause = true; // Mark that we're opening settings from pause
            showScreen(SCREEN_SETTINGS);
        });
        gbc.gridy = 1;
        buttonPanel.add(optionsBtn, gbc);

        PauseMenuButton saveBtn = new PauseMenuButton("SAVE", dialog, () -> {
            saveActiveProfile();
            appendBattleLog("\n💾 Game saved!");
            // Refresh profile select screen to show updated data
            refreshProfileSelect();
            JOptionPane.showMessageDialog(dialog, "Game saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
        });
        gbc.gridy = 2;
        buttonPanel.add(saveBtn, gbc);

        PauseMenuButton exitMenuBtn = new PauseMenuButton("EXIT TO MENU", dialog, () -> {
            dialog.dispose();
            returnToMainMenu();
        });
        gbc.gridy = 3;
        buttonPanel.add(exitMenuBtn, gbc);

        glassPanel.add(buttonPanel, BorderLayout.CENTER);
        dialog.setContentPane(glassPanel);
        dialog.getContentPane().setBackground(new Color(0, 0, 0, 0)); // Ensure content pane is transparent
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    
    private static class PauseMenuButton extends JPanel {
        private final String text;
        private boolean isHovered = false;
        
        public PauseMenuButton(String text, JDialog parent, Runnable action) {
            this.text = text;
            setOpaque(false); // No background panel
            setPreferredSize(new Dimension(300, 40));
            setBorder(null); // No border
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Simple text button (no panel background)
            Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 26);
            g2d.setFont(buttonFont.deriveFont(isHovered ? Font.BOLD : Font.PLAIN));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int tx = (getWidth() - textWidth) / 2;
            int ty = getHeight() / 2 + fm.getAscent() / 2 - 5;
            
            // Text color changes on hover
            Color textColor = isHovered ? UITheme.PRIMARY_GREEN : UITheme.PRIMARY_WHITE;
            g2d.setColor(textColor);
            g2d.drawString(text, tx, ty);
            
            // Draw underline only when hovered
            if (isHovered) {
                int underlineY = ty + fm.getDescent() + 4;
                int underlineX = tx;
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(textColor);
                g2d.drawLine(underlineX, underlineY, underlineX + textWidth, underlineY);
            }
            
            g2d.dispose();
        }
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
        // Reload profile data from disk to ensure we show the latest saved data
        reloadProfilesFromDisk();
        for (Component comp : mainContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(SCREEN_PROFILE_SELECT)) {
                mainContainer.remove(comp);
                break;
            }
        }
        mainContainer.add(createProfileSelect(), SCREEN_PROFILE_SELECT);
    }

    private void reloadProfilesFromDisk() {
        // Save current active profile before reloading to ensure we don't lose unsaved changes
        PlayerProgress activeProfileData = null;
        if (activeProfile >= 0 && playerProgress != null) {
            // Sync current progress to profileSlots before saving
            profileSlots[activeProfile] = playerProgress;
            saveProfile(activeProfile);
            // Preserve the in-memory data for the active profile (it's the most up-to-date)
            activeProfileData = playerProgress;
        }
        
        for (int i = 0; i < PROFILE_SLOTS; i++) {
            // For the active profile, use the in-memory data if available (it's most recent)
            if (i == activeProfile && activeProfileData != null) {
                profileSlots[i] = activeProfileData;
                continue;
            }
            
            Path file = SAVE_DIR.resolve("profile" + (i + 1) + ".dat");
            PlayerProgress data = PlayerProgress.load(file);
            if (data != null) {
                profileSlots[i] = data;
                System.out.println("Loaded profile " + (i + 1) + " from disk: Level " + data.getPlayerLevel() + 
                    ", EXP " + data.getCurrentExp() + "/" + data.getExpToNext() + 
                    ", Worlds: " + data.getClearedWorldCount());
            } else {
                // If no file exists, keep the current in-memory data (don't overwrite with new empty progress)
                if (profileSlots[i] == null) {
                    profileSlots[i] = new PlayerProgress();
                }
            }
        }
        // Update playerProgress if we have an active profile
        if (activeProfile >= 0 && activeProfile < PROFILE_SLOTS) {
            playerProgress = profileSlots[activeProfile];
            // Note: Don't auto-start session here - session should start when entering battle
            // or when explicitly selecting a profile. This prevents time tracking issues.
        }
    }
    void resetProfileSlot(int profileIndex, boolean deleteFile) {
        int idx = Math.max(1, Math.min(PROFILE_SLOTS, profileIndex)) - 1;
        
        // End session if resetting the active profile
        if (activeProfile == idx && playerProgress != null) {
            playerProgress.endSession();
        }
        
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
                System.err.println("Error deleting profile file: " + e.getMessage());
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
        // End session and auto-save when returning to main menu
        if (playerProgress != null) {
            playerProgress.endSession();
        }
        saveActiveProfile();
        refreshProfileSelect(); // Refresh to show updated data
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
        // Record save time
        playerProgress.recordSave();
        // Always sync playerProgress changes back to profileSlots before saving
        profileSlots[activeProfile] = playerProgress;
        saveProfile(activeProfile);
        System.out.println("Saved profile " + (activeProfile + 1) + ": Level " + playerProgress.getPlayerLevel() + 
            ", EXP " + playerProgress.getCurrentExp() + "/" + playerProgress.getExpToNext() + 
            ", Worlds: " + playerProgress.getClearedWorldCount() + 
            ", Time: " + playerProgress.getFormattedPlayTime() +
            ", Last Save: " + playerProgress.getFormattedLastSaveDate());
    }

    private void saveProfile(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PROFILE_SLOTS) return;
        // Ensure we're saving the current playerProgress if this is the active profile
        if (slotIndex == activeProfile && playerProgress != null) {
            profileSlots[slotIndex] = playerProgress;
        }
        Path file = SAVE_DIR.resolve("profile" + (slotIndex + 1) + ".dat");
        try {
            if (profileSlots[slotIndex] != null) {
                profileSlots[slotIndex].save(file);
                System.out.println("Successfully wrote profile " + (slotIndex + 1) + " to " + file);
            } else {
                System.err.println("ERROR: profileSlots[" + slotIndex + "] is null!");
            }
        } catch (IOException e) {
            System.err.println("ERROR saving profile " + (slotIndex + 1) + ": " + e.getMessage());
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
        updateEnemyDetails(); // Update enemy stats panel
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
                    "%s uses %s → %s takes %d damage (HP: %d/%d)",
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

