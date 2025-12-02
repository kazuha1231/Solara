package com.defendersofsolara.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI Theme with Medieval Fantasy Color Palette
 * 
 * Colors applied throughout the UI:
 * - Background: Deep dark brown (#1B1A17)
 * - Panels: Dark brown (#2A2520)
 * - Borders/Highlights: Brass/Gold (#C2A36A)
 * - Text: Parchment cream (#F2E6C9)
 * - HP Bar: Deep red (#B23A48)
 * - MP Bar: Royal blue (#3C67C5)
 */
public class UITheme {

    // ==================== SCALING ====================
    private static float scaleFactor = 1.0f;

    public static void applyScale(float factor) {
        scaleFactor = Math.max(0.75f, Math.min(factor, 2.5f));
        refreshTypography();
        refreshDimensions();
    }

    public static float getScaleFactor() {
        return scaleFactor;
    }

    private static int scale(int value) {
        return Math.round(value * scaleFactor);
    }

    // ==================== WINDOW SIZES ====================
    public static final int MENU_WIDTH = 900;
    public static final int MENU_HEIGHT = 600;
    public static final int BATTLE_WIDTH = 1200;
    public static final int BATTLE_HEIGHT = 700;

    // ==================== MEDIEVAL FANTASY COLOR THEME ====================
    // Background: Deep dark brown (#1B1A17)
    public static final Color BACKGROUND = new Color(27, 26, 23);
    
    // Panels: Dark brown (#2A2520)
    public static final Color PANEL = new Color(42, 37, 32);
    
    // Borders/Highlights: Brass/Gold (#C2A36A)
    public static final Color BRASS = new Color(194, 163, 106);
    public static final Color BRASS_HIGHLIGHT = new Color(212, 184, 122); // Lighter brass for hover
    
    // Text: Parchment cream (#F2E6C9)
    public static final Color TEXT = new Color(242, 230, 201);
    public static final Color TEXT_DIM = new Color(200, 190, 170); // Slightly dimmer text
    
    // HP Bar: Deep red (#B23A48)
    public static final Color HP_BAR = new Color(178, 58, 72);
    
    // MP Bar: Royal blue (#3C67C5)
    public static final Color MP_BAR = new Color(60, 103, 197);
    
    // Legacy color mappings (for backward compatibility)
    public static final Color PRIMARY_GREEN = BRASS;   // Use brass instead of green
    public static final Color PRIMARY_ORANGE = new Color(220, 120, 60);   // Keep orange for accents
    public static final Color PRIMARY_CYAN = MP_BAR;    // Use MP blue instead of cyan
    public static final Color PRIMARY_YELLOW = BRASS;   // Use brass instead of yellow
    public static final Color PRIMARY_WHITE = TEXT;     // Use parchment instead of white
    public static final Color PRIMARY_RED = HP_BAR;     // Use HP red

    // Background colors (medieval theme)
    public static final Color BG_DARK_TEAL = BACKGROUND;      // Main background
    public static final Color BG_CHARCOAL = new Color(24, 22, 20);  // Slightly lighter dark
    public static final Color BG_PANEL = PANEL;           // Panel background
    public static final Color BG_CARD = new Color(50, 45, 40);  // Card background (slightly lighter)
    public static final Color BG_BUTTON = BRASS;          // Button background (brass)
    public static final Color BG_BUTTON_TEXT = new Color(30, 25, 20);  // Dark text on brass button
    public static final Color BG_INVENTORY_SLOT = new Color(60, 55, 50); // Inventory slot
    public static final Color BG_PLAYER = PANEL;
    public static final Color BG_ENEMY = new Color(50, 35, 35);  // Slightly red-tinted for enemies
    public static final Color BG_OVERLAY = new Color(15, 14, 12);  // Dark overlay

    // Borders (brass theme)
    public static final Color BORDER_NORMAL = BRASS;      // Brass border
    public static final Color BORDER_HIGHLIGHT = BRASS_HIGHLIGHT; // Lighter brass for hover
    public static final Color BORDER_HOVER = new Color(230, 210, 160); // Very light brass for hover

    // Status colors
    public static final Color HP_GREEN = HP_BAR;          // HP red
    public static final Color MANA_BLUE = MP_BAR;         // MP blue
    public static final Color DEAD_GRAY = new Color(60, 55, 50);  // Dead character gray
    public static final Color TEXT_GRAY = TEXT_DIM;       // Dimmed text
    public static final Color LOG_TEXT = TEXT;             // Action log text

    // ==================== FONTS ====================
    private static Font scaleFont(String name, int style, int size) {
        return new Font(name, style, scale(size));
    }

    // Use a serif family to better match the medieval aesthetic
    public static Font FONT_TITLE;
    public static Font FONT_SUBTITLE;
    public static Font FONT_HEADER;
    public static Font FONT_BUTTON;
    public static Font FONT_BUTTON_SMALL;
    public static Font FONT_TEXT;
    public static Font FONT_TEXT_LARGE;
    public static Font FONT_SMALL;
    public static Font FONT_LOG;
    public static Font FONT_SKILL;
    public static Font FONT_CARD_NAME;

    // ==================== DIMENSIONS ====================
    public static Dimension CHARACTER_CARD;
    public static Dimension SKILL_BUTTON;
    public static Dimension BUTTON_SIZE;
    public static Dimension BUTTON_SMALL;

    // ==================== UTILITY GETTERS ====================
    public static Font getFontHeader()      { return FONT_HEADER; }
    public static Font getFontSubtitle()    { return FONT_SUBTITLE; }
    public static Font getFontButton()      { return FONT_BUTTON; }
    public static Font getFontButtonSmall() { return FONT_BUTTON_SMALL; }
    public static Font getFontText()        { return FONT_TEXT; }
    public static Font getFontTextLarge()   { return FONT_TEXT_LARGE; }
    public static Font getFontSmall()       { return FONT_SMALL; }
    public static Font getFontLog()         { return FONT_LOG; }
    public static Font getFontSkill()       { return FONT_SKILL; }
    public static Font getFontCardName()    { return FONT_CARD_NAME; }

    public static Dimension getCharacterCardDim() { return CHARACTER_CARD; }
    public static Dimension getSkillButtonDim()   { return SKILL_BUTTON; }

    // ==================== COMPONENT FACTORIES ====================

    public static JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Medieval gradient: dark brown to slightly lighter dark brown
                GradientPaint gp = new GradientPaint(
                    0, 0, BACKGROUND,
                    0, getHeight(), BG_CHARCOAL
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    public static JLabel createTitle(String text) {
        // Create a beautiful title with divider fade underline
        return new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                FontRenderingUtil.applyTextRenderingHints(g2d);
                g2d.setFont(getFont());
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Draw text shadow for depth (dark shadow)
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.drawString(getText(), x + 2, y + 2);
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Draw main text with brass glow
                g2d.setColor(new Color(BRASS.getRed(), BRASS.getGreen(), BRASS.getBlue(), 200));
                g2d.drawString(getText(), x, y - 1);
                g2d.setColor(TEXT); // Parchment color
                g2d.drawString(getText(), x, y);
                
                // Draw beautiful divider fade underline
                BufferedImage dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-002.png");
                if (dividerFade == null) {
                    dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png");
                }
                
                if (dividerFade != null) {
                    int underlineY = y + fm.getDescent() + 10;
                    int underlineWidth = Math.max(textWidth + 50, 150);
                    int underlineX = (getWidth() - underlineWidth) / 2;
                    int underlineHeight = Math.max(dividerFade.getHeight(), 6);
                    
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
                    PixelArtUI.drawNineSlice(g2d, dividerFade, underlineX, underlineY, underlineWidth, underlineHeight);
                    g2d.setComposite(AlphaComposite.SrcOver);
                }
                
                g2d.dispose();
            }
        };
    }

    public static JButton createButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Use mixed rendering for buttons (text + pixel art borders)
                FontRenderingUtil.applyMixedRenderingHints(g2d);
                
                // Brass button background (medieval theme)
                Color bg = BG_BUTTON; // Brass color
                if (getModel().isPressed()) {
                    bg = new Color(BRASS.getRed() - 20, BRASS.getGreen() - 20, BRASS.getBlue() - 20);
                } else if (getModel().isRollover()) {
                    bg = BRASS_HIGHLIGHT; // Lighter brass on hover
                }
                g2d.setColor(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Pixel-art border for button (use border asset for buttons)
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: brass border
                    g2d.setColor(BORDER_NORMAL);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(BG_BUTTON_TEXT); // Dark text on brass button
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }
        });
        return btn;
    }

    public static JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON_SMALL);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(TEXT); // Parchment color
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BRASS)); // Brass underline
        btn.setPreferredSize(BUTTON_SMALL);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(BRASS_HIGHLIGHT); // Lighter brass on hover
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BRASS_HIGHLIGHT));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BRASS));
            }
        });
        return btn;
    }

    public static Border createCyanBorder(int thickness) {
        return BorderFactory.createLineBorder(BRASS, thickness); // Use brass instead of cyan
    }

    public static Border createTitledBorder(String title, Color textColor, Color borderColor) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor, 3), title
        );
        tb.setTitleFont(FONT_HEADER);
        tb.setTitleColor(textColor);
        return tb;
    }

    public static JPanel createOverlayPanel() {
        // Pixel-art panel matching reference style
        return PixelArtUI.createPixelPanel();
    }
    
    /**
     * Create a pixel-art card panel matching reference style.
     */
    public static JPanel createCardPanel() {
        return PixelArtUI.createPixelPanel();
    }

    private static void refreshTypography() {
        // Use serif fonts for medieval aesthetic (can be changed to sans-serif if preferred)
        FONT_TITLE = scaleFont(Font.SERIF, Font.BOLD, 32);
        FONT_SUBTITLE = scaleFont(Font.SERIF, Font.BOLD, 24);
        FONT_HEADER = scaleFont(Font.SERIF, Font.BOLD, 18);
        FONT_BUTTON = scaleFont(Font.SERIF, Font.PLAIN, 20);
        FONT_BUTTON_SMALL = scaleFont(Font.SERIF, Font.PLAIN, 16);
        FONT_TEXT = scaleFont(Font.SERIF, Font.PLAIN, 16);
        FONT_TEXT_LARGE = scaleFont(Font.SERIF, Font.PLAIN, 18);
        FONT_SMALL = scaleFont(Font.SERIF, Font.PLAIN, 14);
        FONT_LOG = scaleFont(Font.SERIF, Font.ITALIC, 13); // Italic for action log
        FONT_SKILL = scaleFont(Font.SERIF, Font.PLAIN, 14);
        FONT_CARD_NAME = scaleFont(Font.SERIF, Font.BOLD, 16);
    }

    private static void refreshDimensions() {
        CHARACTER_CARD = new Dimension(scale(280), scale(100));
        SKILL_BUTTON = new Dimension(scale(280), scale(40));
        BUTTON_SIZE = new Dimension(scale(250), scale(50));
        BUTTON_SMALL = new Dimension(scale(120), scale(40));
    }

    static {
        refreshTypography();
        refreshDimensions();
    }
}
