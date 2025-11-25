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
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background: reuse the frame's background image / gradient
        parent.paintBackground(g2d, getWidth(), getHeight());

        // Subtle vignette to focus the center (no colored circle overlay)
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Title + subtitle
        drawTitle(g2d);

        // Buttons
        layoutButtons();
        drawButton(g2d, startGameBtn);
        drawButton(g2d, optionsBtn);
        drawButton(g2d, creditsBtn);
        drawButton(g2d, exitBtn);
    }

    private void drawTitle(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int top = 150;

        // Main title
        Font titleFont = new Font("Serif", Font.BOLD, 64);
        g2d.setFont(titleFont);
        String title = "DEFENDERS OF SOLARA";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int left = centerX - titleWidth / 2;

        // Glow border
        g2d.setColor(new Color(0, 200, 255, 150));
        for (int i = 3; i > 0; i--) {
            g2d.drawString(title, left - i, top - i);
            g2d.drawString(title, left + i, top + i);
        }

        // Main title text
        g2d.setColor(new Color(235, 250, 255));
        g2d.drawString(title, left, top);

        // Ornamental line under title
        int lineY = top + 12;
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(120, 210, 255, 180));
        int lineMargin = 40;
        g2d.drawLine(left - lineMargin, lineY, left + titleWidth + lineMargin, lineY);

        // Subtitle
        Font subtitleFont = new Font("Serif", Font.PLAIN, 24);
        g2d.setFont(subtitleFont);
        g2d.setColor(new Color(200, 240, 255));
        String subtitle = "VEIL SYSTEM CONFLICT";
        FontMetrics sfm = g2d.getFontMetrics();
        int subLeft = centerX - sfm.stringWidth(subtitle) / 2;
        g2d.drawString(subtitle, subLeft, top + 45);
    }

    private void drawButton(Graphics2D g2d, MenuButton btn) {
        boolean isHovered = btn == hoveredButton;

        // Underline-only style buttons, text centered at (btn.x, btn.y)
        Font bodyFont = new Font("Serif", Font.PLAIN, 26);
        g2d.setFont(bodyFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(btn.text);
        int tx = btn.x - textWidth / 2;
        int ty = btn.y;

        // Animated glow circle behind hovered text only
        if (isHovered) {
            int glowRadius = 80;
            float glowAlpha = 0.25f;
            g2d.setPaint(new RadialGradientPaint(
                new Point(tx + textWidth / 2, ty - 8),
                glowRadius,
                new float[]{0f, 1f},
                new Color[]{new Color(80, 160, 220, (int) (255 * glowAlpha)),
                    new Color(80, 160, 220, 0)}
            ));
            g2d.fillOval(tx + textWidth / 2 - glowRadius, ty - glowRadius, glowRadius * 2, glowRadius * 2);
        }

        g2d.setFont(bodyFont.deriveFont(isHovered ? Font.BOLD : Font.PLAIN));
        g2d.setColor(isHovered ? new Color(230, 245, 255) : new Color(210, 225, 240));
        g2d.drawString(btn.text, tx, ty);

        // Underline
        int lineY = ty + 6;
        int linePad = 4;
        if (isHovered) {
            g2d.setColor(new Color(120, 210, 255));
            g2d.setStroke(new BasicStroke(2.5f));
        } else {
            g2d.setColor(new Color(120, 150, 190, 180));
            g2d.setStroke(new BasicStroke(1.5f));
        }
        g2d.drawLine(tx - linePad, lineY, tx + textWidth + linePad, lineY);
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


