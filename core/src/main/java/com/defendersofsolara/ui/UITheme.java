package com.defendersofsolara.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    // ==================== COLORS (Matching Reference Image Exactly) ====================
    // Colors matching the reference image exactly
    public static final Color PRIMARY_GREEN = new Color(150, 220, 150);   // Light green for titles (like "Quest")
    public static final Color PRIMARY_ORANGE = new Color(220, 120, 60);   // Orange accent
    public static final Color PRIMARY_CYAN = new Color(100, 180, 200);    // Teal accent
    public static final Color PRIMARY_YELLOW = new Color(200, 160, 100);
    public static final Color PRIMARY_WHITE = new Color(255, 255, 255);    // Pure white for text
    public static final Color PRIMARY_RED = new Color(200, 100, 100);

    // Dark blue-gray backgrounds matching reference exactly
    public static final Color BG_DARK_TEAL = new Color(20, 25, 30);      // Very dark blue-gray/charcoal background
    public static final Color BG_CHARCOAL = new Color(18, 22, 28);        // Dark charcoal
    public static final Color BG_PANEL = new Color(35, 45, 55);           // Dark blue-gray panel background (like Quest panel)
    public static final Color BG_CARD = new Color(40, 50, 60);            // Slightly lighter card background
    public static final Color BG_BUTTON = new Color(255, 255, 255);        // White button background (like "Accept quest")
    public static final Color BG_BUTTON_TEXT = new Color(30, 30, 30);     // Dark gray/black text on white button
    public static final Color BG_INVENTORY_SLOT = new Color(200, 200, 200); // Light gray for inventory slots
    public static final Color BG_PLAYER = new Color(35, 45, 55);
    public static final Color BG_ENEMY = new Color(50, 35, 35);
    public static final Color BG_OVERLAY = new Color(15, 18, 22);

    // Pixel-art borders (light grey decorative borders from reference)
    public static final Color BORDER_NORMAL = new Color(180, 180, 180);   // Light grey pixel-art border
    public static final Color BORDER_HIGHLIGHT = new Color(220, 220, 220); // Lighter grey for hover
    public static final Color BORDER_HOVER = new Color(240, 240, 240);     // Very light grey for hover

    // Status colors - readable
    public static final Color HP_GREEN = new Color(80, 200, 100);
    public static final Color MANA_BLUE = new Color(100, 160, 220);
    public static final Color DEAD_GRAY = new Color(60, 60, 60);
    public static final Color TEXT_GRAY = new Color(200, 200, 200);        // Light grey text
    public static final Color LOG_TEXT = new Color(255, 255, 255);         // Pure white for readability

    // ==================== FONTS ====================
    private static Font scaleFont(String name, int style, int size) {
        return new Font(name, style, scale(size));
    }

    // Use a serif family to better match the main menu aesthetic
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
                GradientPaint gp = new GradientPaint(
                    0, 0, BG_DARK_TEAL,
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
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setFont(getFont());
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Draw text shadow for depth
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.drawString(getText(), x + 2, y + 2);
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Draw main text with slight glow
                g2d.setColor(new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), 200));
                g2d.drawString(getText(), x, y - 1);
                g2d.setColor(getForeground());
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
                // Use nearest neighbor for pixel-art look
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                // White button background (like reference "Accept quest" button)
                Color bg = BG_BUTTON;
                if (getModel().isPressed()) {
                    bg = new Color(240, 240, 240);
                } else if (getModel().isRollover()) {
                    bg = new Color(250, 250, 250);
                }
                g2d.setColor(bg);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Pixel-art border for button (use border asset for buttons)
                BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    PixelArtUI.drawNineSlice(g2d, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: simple border
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
        btn.setForeground(BG_BUTTON_TEXT); // Black text on white button (like reference)
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
        btn.setForeground(PRIMARY_ORANGE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 150, 190, 180)));
        btn.setPreferredSize(BUTTON_SMALL);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(PRIMARY_WHITE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(240, 150, 80)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(PRIMARY_ORANGE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 120, 60, 180)));
            }
        });
        return btn;
    }

    public static Border createCyanBorder(int thickness) {
        return BorderFactory.createLineBorder(BORDER_NORMAL, thickness);
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
        // Use sans-serif fonts matching the reference (clean, readable)
        FONT_TITLE = scaleFont(Font.SANS_SERIF, Font.BOLD, 32);
        FONT_SUBTITLE = scaleFont(Font.SANS_SERIF, Font.BOLD, 24);
        FONT_HEADER = scaleFont(Font.SANS_SERIF, Font.BOLD, 18);
        FONT_BUTTON = scaleFont(Font.SANS_SERIF, Font.PLAIN, 20);
        FONT_BUTTON_SMALL = scaleFont(Font.SANS_SERIF, Font.PLAIN, 16);
        FONT_TEXT = scaleFont(Font.SANS_SERIF, Font.PLAIN, 16);
        FONT_TEXT_LARGE = scaleFont(Font.SANS_SERIF, Font.PLAIN, 18);
        FONT_SMALL = scaleFont(Font.SANS_SERIF, Font.PLAIN, 14);
        FONT_LOG = scaleFont(Font.SANS_SERIF, Font.PLAIN, 13);
        FONT_SKILL = scaleFont(Font.SANS_SERIF, Font.PLAIN, 14);
        FONT_CARD_NAME = scaleFont(Font.SANS_SERIF, Font.BOLD, 16);
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
