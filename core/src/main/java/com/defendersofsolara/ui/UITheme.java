package com.defendersofsolara.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
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

    // ==================== COLORS ====================
    public static final Color PRIMARY_CYAN = new Color(0, 255, 255);
    public static final Color PRIMARY_YELLOW = new Color(255, 215, 0);
    public static final Color PRIMARY_WHITE = Color.WHITE;
    public static final Color PRIMARY_RED = new Color(255, 100, 100);

    public static final Color BG_GRADIENT_START = new Color(10, 10, 40);
    public static final Color BG_GRADIENT_END = new Color(50, 10, 80);
    public static final Color BG_START = BG_GRADIENT_START;
    public static final Color BG_END = BG_GRADIENT_END;
    public static final Color BG_DARK = new Color(20, 20, 40);
    public static final Color BG_PLAYER = new Color(30, 30, 60);
    public static final Color BG_ENEMY = new Color(60, 30, 30);
    public static final Color BG_BUTTON = Color.BLACK;
    public static final Color BG_OVERLAY = new Color(0, 0, 0, 180);

    public static final Color BORDER_NORMAL = Color.LIGHT_GRAY;
    public static final Color BORDER_HIGHLIGHT = new Color(255, 215, 0);
    public static final Color BORDER_HOVER = new Color(255, 255, 100);

    public static final Color HP_GREEN = new Color(50, 200, 50);
    public static final Color MANA_BLUE = new Color(100, 100, 255);
    public static final Color DEAD_GRAY = new Color(50, 50, 50);
    public static final Color TEXT_GRAY = Color.GRAY;
    public static final Color LOG_TEXT = new Color(200, 200, 255);

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
                    0, 0, BG_GRADIENT_START,
                    0, getHeight(), BG_GRADIENT_END
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    public static JLabel createTitle(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(PRIMARY_CYAN);
        return lbl;
    }

    public static JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(PRIMARY_CYAN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 150, 190, 180)));
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(PRIMARY_WHITE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(120, 210, 255)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(PRIMARY_CYAN);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 150, 190, 180)));
            }
        });
        return btn;
    }

    public static JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON_SMALL);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(PRIMARY_CYAN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 150, 190, 180)));
        btn.setPreferredSize(BUTTON_SMALL);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(PRIMARY_WHITE);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(120, 210, 255)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(PRIMARY_CYAN);
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 150, 190, 180)));
            }
        });
        return btn;
    }

    public static Border createCyanBorder(int thickness) {
        return BorderFactory.createLineBorder(PRIMARY_CYAN, thickness);
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
        JPanel p = new JPanel();
        p.setBackground(BG_OVERLAY);
        p.setOpaque(true);
        return p;
    }

    private static void refreshTypography() {
        FONT_TITLE = scaleFont("Serif", Font.BOLD, 32);
        FONT_SUBTITLE = scaleFont("Serif", Font.BOLD, 24);
        FONT_HEADER = scaleFont("Serif", Font.BOLD, 18);
        FONT_BUTTON = scaleFont("Serif", Font.PLAIN, 20);
        FONT_BUTTON_SMALL = scaleFont("Serif", Font.PLAIN, 16);
        FONT_TEXT = scaleFont("Serif", Font.PLAIN, 16);
        FONT_TEXT_LARGE = scaleFont("Serif", Font.PLAIN, 18);
        FONT_SMALL = scaleFont("Serif", Font.PLAIN, 14);
        FONT_LOG = scaleFont("Serif", Font.PLAIN, 13);
        FONT_SKILL = scaleFont("Serif", Font.PLAIN, 14);
        FONT_CARD_NAME = scaleFont("Serif", Font.BOLD, 16);
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
