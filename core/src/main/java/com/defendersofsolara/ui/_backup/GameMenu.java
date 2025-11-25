package com.defendersofsolara.ui._backup;

import com.defendersofsolara.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * REFACTORED GameMenu - Now using UITheme for easy visual editing!
 *
 * To change colors/fonts: Edit UITheme.java
 * To change layout/structure: Edit this file
 */
public class GameMenu extends JFrame {

    // ==================== CONSTANTS ====================

    private static final String SCREEN_MAIN = "main";
    private static final String SCREEN_CREDITS = "credits";
    private static final String SCREEN_SETTINGS = "settings";
    private static final String SCREEN_NARRATIVE = "narrative";

    // ==================== INSTANCE VARIABLES ====================

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;
    private BufferedImage bgImage;

    // ==================== CONSTRUCTOR ====================

    public GameMenu() {
        setTitle("Defenders of Solara");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UITheme.MENU_WIDTH, UITheme.MENU_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        loadBackground();

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Add all screens
        cardsPanel.add(createMainMenu(), SCREEN_MAIN);
        cardsPanel.add(createCreditsMenu(), SCREEN_CREDITS);
        cardsPanel.add(createSettingsMenu(), SCREEN_SETTINGS);
        cardsPanel.add(createNarrativeIntro(), SCREEN_NARRATIVE);

        setContentPane(cardsPanel);
        cardLayout.show(cardsPanel, SCREEN_MAIN);

        setVisible(true);
    }

    // ==================== BACKGROUND MANAGEMENT ====================

    private void loadBackground() {
        try {
            java.net.URL imgURL = getClass().getResource("/image/galaxy_bg.png");
            if (imgURL != null) {
                bgImage = ImageIO.read(imgURL);
                System.out.println("✓ Background loaded successfully!");
            } else {
                System.out.println("✗ Background image not found");
            }
        } catch (IOException e) {
            System.out.println("✗ Error loading background: " + e.getMessage());
        }
    }

    private JPanel createBackgroundPanel() {
        if (bgImage != null) {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            };
        } else {
            return UITheme.createGradientPanel();
        }
    }

    // ==================== SCREEN 1: MAIN MENU ====================

    private JPanel createMainMenu() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = UITheme.createTitle("DEFENDERS OF SOLARA");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 10, 60, 10);
        panel.add(title, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);

        // Start Game button
        JButton startBtn = UITheme.createButton("START GAME");
        startBtn.addActionListener(e -> cardLayout.show(cardsPanel, SCREEN_NARRATIVE));
        gbc.gridy = 1;
        panel.add(startBtn, gbc);

        // Credits button
        JButton creditsBtn = UITheme.createButton("CREDITS");
        creditsBtn.addActionListener(e -> cardLayout.show(cardsPanel, SCREEN_CREDITS));
        gbc.gridy = 2;
        panel.add(creditsBtn, gbc);

        // Settings button
        JButton settingsBtn = UITheme.createButton("SETTINGS");
        settingsBtn.addActionListener(e -> cardLayout.show(cardsPanel, SCREEN_SETTINGS));
        gbc.gridy = 3;
        panel.add(settingsBtn, gbc);

        // Exit button
        JButton exitBtn = UITheme.createButton("EXIT");
        exitBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        gbc.gridy = 4;
        panel.add(exitBtn, gbc);

        return panel;
    }

    // ==================== SCREEN 2: CREDITS ====================

    private JPanel createCreditsMenu() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        // Credits text (using UITheme colors)
        JLabel credits = new JLabel(
            "<html><center>" +
                "<font size='6' color='#00FFFF'><b>DEFENDERS OF SOLARA</b></font><br><br>" +
                "<font size='4' color='#00FFFF'>" +
                "Group Leader:<br>Veinz Pius N. Escuzar <br><br>" +
                "Members:<br>" +
                "John Warren Pansacala<br>" +
                "Denzel B. Valendez<br>" +
                "Kim Kyle M. Paran<br>" +
                "Rushaine A. Tura<br><br>" +
                "Engine: Java Swing<br>" +
                "Version: 1.0" +
                "</font></center></html>"
        );
        credits.setFont(UITheme.getFontText());
        gbc.gridy = 0;
        panel.add(credits, gbc);

        // Back button
        JButton backBtn = UITheme.createButton("BACK");
        backBtn.addActionListener(e -> cardLayout.show(cardsPanel, SCREEN_MAIN));
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 15, 15, 15);
        panel.add(backBtn, gbc);

        return panel;
    }

    // ==================== SCREEN 3: SETTINGS ====================

    private JPanel createSettingsMenu() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = new JLabel(
            "<html><center><font size='6' color='#00FFFF'><b>DISPLAY SETTINGS</b></font></center></html>"
        );
        title.setFont(UITheme.getFontSubtitle());
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 10, 30, 10);
        panel.add(title, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);

        // Fullscreen checkbox
        JCheckBox fullscreenCheck = new JCheckBox("Fullscreen");
        fullscreenCheck.setFont(UITheme.getFontButton());
        fullscreenCheck.setForeground(UITheme.PRIMARY_CYAN);
        fullscreenCheck.setOpaque(false);
        gbc.gridy = 1;
        panel.add(fullscreenCheck, gbc);

        // Resolution dropdown
        String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080"};
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions);
        resolutionBox.setFont(UITheme.getFontButton());
        resolutionBox.setForeground(UITheme.PRIMARY_CYAN);
        resolutionBox.setBackground(UITheme.BG_BUTTON);
        resolutionBox.setSelectedIndex(2);
        gbc.gridy = 2;
        panel.add(resolutionBox, gbc);

        // Apply button
        JButton applyBtn = UITheme.createButton("APPLY");
        applyBtn.addActionListener(e ->
            applySettings(fullscreenCheck.isSelected(), (String) resolutionBox.getSelectedItem())
        );
        gbc.gridy = 3;
        panel.add(applyBtn, gbc);

        // Back button
        JButton backBtn = UITheme.createButton("BACK");
        backBtn.addActionListener(e -> cardLayout.show(cardsPanel, SCREEN_MAIN));
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 15, 15, 15);
        panel.add(backBtn, gbc);

        return panel;
    }

    private void applySettings(boolean fullscreen, String resolution) {
        try {
            int width = UITheme.MENU_WIDTH;
            int height = UITheme.MENU_HEIGHT;

            if (!fullscreen && resolution != null) {
                String[] dims = resolution.split("x");
                width = Integer.parseInt(dims[0]);
                height = Integer.parseInt(dims[1]);
            }

            dispose();

            if (fullscreen) {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
                setUndecorated(true);
                setResizable(false);
                setSize(Toolkit.getDefaultToolkit().getScreenSize());
                gd.setFullScreenWindow(this);
            } else {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
                gd.setFullScreenWindow(null);
                setUndecorated(false);
                setResizable(true);
                setSize(width, height);
                setLocationRelativeTo(null);
            }

            setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error applying settings: " + ex.getMessage(),
                "Settings Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    // ==================== SCREEN 4: NARRATIVE INTRO ====================

    private JPanel createNarrativeIntro() {
        JPanel panel = createBackgroundPanel();
        panel.setLayout(new BorderLayout());

        // Story text area
        JTextArea storyText = new JTextArea();
        storyText.setFont(UITheme.getFontTextLarge());
        storyText.setForeground(UITheme.PRIMARY_CYAN);
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setOpaque(false);
        storyText.setMargin(new Insets(40, 60, 40, 60));

        // Semi-transparent overlay
        JPanel textPanel = UITheme.createOverlayPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        textPanel.add(storyText, BorderLayout.CENTER);

        // Skip button
        JButton skipBtn = UITheme.createSmallButton("SKIP >");
        skipBtn.addActionListener(e -> startGame());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(skipBtn);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Narrative story
        String[] storyLines = {
            "In the depths of space...\n\n",
            "A galactic warlord named Xyrrak the Devourer has unleashed ",
            "an army of bio-mechanical horrors to conquer the Veil System.\n\n",
            "Each of its five worlds holds a Core of Balance—\n",
            "ancient forces that uphold the laws of the galaxy:\n",
            "Time, Gravity, Energy, Life, and Void.\n\n",
            "If all are lost, the Veil will shatter,\n",
            "and reality itself will collapse.\n\n",
            "Only a band of heroes, the Defenders of Solara,\n",
            "can stop Xyrrak.\n\n",
            "From vastly different cultures,\n",
            "each wields unique power,\n",
            "but together they must reclaim the corrupted Cores,\n",
            "restore balance,\n",
            "and end Xyrrak's rise to godhood.\n\n",
            "The fate of the galaxy rests in their hands...\n\n"
        };

        // Typewriter effect
        Timer typewriterTimer = new Timer(50, null);
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
                    Timer autoStart = new Timer(2000, evt -> {
                        startGame();
                        ((Timer) evt.getSource()).stop();
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

    private void startGame() {
        dispose();
        SwingUtilities.invokeLater(WorldSelection::new);
    }

    // ==================== MAIN METHOD ====================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(GameMenu::new);
    }
}
