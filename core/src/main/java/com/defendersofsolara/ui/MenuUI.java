package com.defendersofsolara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main menu UI for {@link UnifiedGameUI}.
 * Minimal Hollow-Knight-style layout with centered options.
 */
class MenuUI extends JPanel {

    private final UnifiedGameUI parent;

    private MenuButton hoveredButton = null;
    private Timer animationTimer;
    private float glowPulse = 0f;

    private final MenuButton startGameBtn;
    private final MenuButton optionsBtn;
    private final MenuButton creditsBtn;
    private final MenuButton exitBtn;

    public MenuUI(UnifiedGameUI parent) {
        this.parent = parent;
        setLayout(null);

        startGameBtn = new MenuButton("Start Game", false);
        optionsBtn   = new MenuButton("Options", false);
        creditsBtn   = new MenuButton("Credits", false);
        exitBtn      = new MenuButton("Exit Game", false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutButtons();
            }
        });
        layoutButtons();

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHover(e.getPoint());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getPoint());
            }
        });

        animationTimer = new javax.swing.Timer(50, e -> {
            glowPulse += 0.1f;
            if (glowPulse > Math.PI * 2) glowPulse = 0;
            repaint();
        });
        animationTimer.start();
    }

    private void layoutButtons() {
        int centerX = getWidth() / 2;
        int startY = (int) (getHeight() * 0.45);
        int spacing = 48;
        startGameBtn.setPosition(centerX, startY);
        optionsBtn.setPosition(centerX, startY + spacing);
        creditsBtn.setPosition(centerX, startY + spacing * 2);
        exitBtn.setPosition(centerX, startY + spacing * 3);
    }

    private void checkHover(Point p) {
        MenuButton old = hoveredButton;
        hoveredButton = null;

        if (startGameBtn.contains(p)) hoveredButton = startGameBtn;
        else if (optionsBtn.contains(p)) hoveredButton = optionsBtn;
        else if (creditsBtn.contains(p)) hoveredButton = creditsBtn;
        else if (exitBtn.contains(p)) hoveredButton = exitBtn;

        if (old != hoveredButton) {
            setCursor(hoveredButton != null ?
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void handleClick(Point p) {
        if (startGameBtn.contains(p)) {
            parent.showScreen(UnifiedGameUI.SCREEN_PROFILE_SELECT);
        } else if (creditsBtn.contains(p)) {
            parent.showScreen(UnifiedGameUI.SCREEN_CREDITS);
        } else if (optionsBtn.contains(p)) {
            parent.showScreen(UnifiedGameUI.SCREEN_SETTINGS);
        } else if (exitBtn.contains(p)) {
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "Are you sure you want to exit?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        FontRenderingUtil.applyMixedRenderingHints(g2d);

        // Use menu.png background from parent
        if (parent != null) {
            parent.paintBackground(g2d, getWidth(), getHeight());
        } else {
            // Fallback: very dark blue-gray/charcoal matching reference
            g2d.setColor(UITheme.BG_DARK_TEAL);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Add dark overlay (same as world selection but brighter - less opacity)
        // World selection uses alpha 200, main menu uses alpha 100 for brightness
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Title + subtitle
        drawTitle(g2d);

        // Buttons
        layoutButtons();
        drawButton(g2d, startGameBtn);
        drawButton(g2d, optionsBtn);
        drawButton(g2d, creditsBtn);
        drawButton(g2d, exitBtn);
        
        g2d.dispose();
    }

    private void drawTitle(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int top = 150;

        // Font rendering hints already applied in paintComponent

        // Main title - beautiful white text with divider fade underline
        Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 48);
        g2d.setFont(titleFont);
        String title = "DEFENDERS OF SOLARA";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int left = centerX - titleWidth / 2;

        // Draw text shadow for depth
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.drawString(title, left + 3, top + 3);
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(title, left + 2, top + 2);
        
        // Draw main text with slight glow
        g2d.setColor(new Color(UITheme.PRIMARY_WHITE.getRed(), UITheme.PRIMARY_WHITE.getGreen(), UITheme.PRIMARY_WHITE.getBlue(), 220));
        g2d.drawString(title, left, top - 1);
        g2d.setColor(UITheme.PRIMARY_WHITE);
        g2d.drawString(title, left, top);

        // Draw beautiful divider fade underline for main title
        java.awt.image.BufferedImage dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-002.png");
        if (dividerFade == null) {
            dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png");
        }
        if (dividerFade != null) {
            int underlineY = top + fm.getDescent() + 12;
            int underlineWidth = Math.max(titleWidth + 60, 200);
            int underlineX = centerX - underlineWidth / 2;
            int underlineHeight = Math.max(dividerFade.getHeight(), 8);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.95f));
            PixelArtUI.drawNineSlice(g2d, dividerFade, underlineX, underlineY, underlineWidth, underlineHeight);
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
        }

        // Subtitle - beautiful white text with divider fade underline
        Font subtitleFont = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
        g2d.setFont(subtitleFont);
        String subtitle = "THE SHATTERED DUNGEONS OF ELDRALUNE";
        FontMetrics sfm = g2d.getFontMetrics();
        int subLeft = centerX - sfm.stringWidth(subtitle) / 2;
        int subTop = top + 55;

        // Draw subtitle shadow
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(subtitle, subLeft + 2, subTop + 2);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(subtitle, subLeft + 1, subTop + 1);
        
        // Draw subtitle text
        g2d.setColor(new Color(UITheme.PRIMARY_WHITE.getRed(), UITheme.PRIMARY_WHITE.getGreen(), UITheme.PRIMARY_WHITE.getBlue(), 200));
        g2d.drawString(subtitle, subLeft, subTop - 1);
        g2d.setColor(UITheme.PRIMARY_WHITE);
        g2d.drawString(subtitle, subLeft, subTop);

        // Draw divider fade underline for subtitle
        java.awt.image.BufferedImage dividerFadeSmall = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-001.png");
        if (dividerFadeSmall == null) {
            dividerFadeSmall = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png");
        }
        if (dividerFadeSmall != null) {
            int subUnderlineY = subTop + sfm.getDescent() + 8;
            int subUnderlineWidth = Math.max(sfm.stringWidth(subtitle) + 30, 100);
            int subUnderlineX = centerX - subUnderlineWidth / 2;
            int subUnderlineHeight = Math.max(dividerFadeSmall.getHeight(), 4);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.9f));
            PixelArtUI.drawNineSlice(g2d, dividerFadeSmall, subUnderlineX, subUnderlineY, subUnderlineWidth, subUnderlineHeight);
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
        }
    }

    private void drawButton(Graphics2D g2d, MenuButton btn) {
        boolean isHovered = btn == hoveredButton;

        // Simple text buttons matching reference style (sans-serif)
        Font bodyFont = new Font(Font.SANS_SERIF, Font.PLAIN, 26);
        g2d.setFont(bodyFont.deriveFont(isHovered ? Font.BOLD : Font.PLAIN));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(btn.text);
        int tx = btn.x - textWidth / 2;
        int ty = btn.y;

        // Simple white text (matching reference "Continue", "New game", "Options")
        Color textColor = isHovered ? UITheme.PRIMARY_GREEN : UITheme.PRIMARY_WHITE;
        g2d.setColor(textColor);
        g2d.drawString(btn.text, tx, ty);

        // Draw normal underline only when hovered
        if (isHovered) {
            int underlineY = ty + fm.getDescent() + 4;
            int underlineX = tx;
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(textColor);
            g2d.drawLine(underlineX, underlineY, underlineX + textWidth, underlineY);
        }
    }

    private static class MenuButton {
        String text;
        int x, y, width, height;

        MenuButton(String text, boolean unused) {
            this.text = text;
            this.width = 260;
            this.height = 36;
        }
        void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        boolean contains(Point p) {
            return p.x >= x - width / 2 && p.x <= x + width / 2 &&
                p.y >= y - height && p.y <= y + height;
        }
    }
}


