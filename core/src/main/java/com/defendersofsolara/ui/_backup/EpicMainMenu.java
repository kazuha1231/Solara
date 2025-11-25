package com.defendersofsolara.ui._backup;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Epic Main Menu - Inspired by Professional Game Design
 * Features:
 * - Sci-fi hexagonal buttons
 * - Glowing effects
 * - Animated background
 * - Professional styling
 */
public class EpicMainMenu extends JPanel {

    private BufferedImage backgroundImage;
    private MenuButton selectedButton = null;
    private Timer animationTimer;
    private float glowPulse = 0f;

    // Menu buttons
    private MenuButton newGameBtn;
    private MenuButton loadGameBtn;
    private MenuButton settingsBtn;
    private MenuButton galleryBtn;
    private MenuButton creditsBtn;
    private MenuButton exitBtn;

    public EpicMainMenu() {
        setLayout(null);
        setPreferredSize(new Dimension(1400, 800));

        // Create menu buttons
        createButtons();

        // Start animation
        startAnimation();

        // Add mouse motion listener for hover effects
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHover(e.getPoint());
            }
        });
    }

    private void createButtons() {
        int centerX = 300;
        int startY = 250;
        int spacing = 70;

        newGameBtn = new MenuButton("NEW GAME", centerX, startY, true);
        loadGameBtn = new MenuButton("LOAD GAME", centerX, startY + spacing);
        settingsBtn = new MenuButton("SETTINGS", centerX, startY + spacing * 2);
        galleryBtn = new MenuButton("GALLERY", centerX, startY + spacing * 3);
        creditsBtn = new MenuButton("CREDITS", centerX, startY + spacing * 4);
        exitBtn = new MenuButton("EXIT GAME", centerX, startY + spacing * 5);

        // Add click listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getPoint());
            }
        });
    }

    private void startAnimation() {
        animationTimer = new Timer(50, e -> {
            glowPulse += 0.1f;
            if (glowPulse > Math.PI * 2) glowPulse = 0;
            repaint();
        });
        animationTimer.start();
    }

    private void checkHover(Point p) {
        MenuButton oldSelected = selectedButton;
        selectedButton = null;

        if (newGameBtn.contains(p)) selectedButton = newGameBtn;
        else if (loadGameBtn.contains(p)) selectedButton = loadGameBtn;
        else if (settingsBtn.contains(p)) selectedButton = settingsBtn;
        else if (galleryBtn.contains(p)) selectedButton = galleryBtn;
        else if (creditsBtn.contains(p)) selectedButton = creditsBtn;
        else if (exitBtn.contains(p)) selectedButton = exitBtn;

        if (oldSelected != selectedButton) {
            setCursor(selectedButton != null ?
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void handleClick(Point p) {
        if (newGameBtn.contains(p)) {
            System.out.println("NEW GAME clicked");
            // TODO: Start new game
        } else if (loadGameBtn.contains(p)) {
            System.out.println("LOAD GAME clicked");
            // TODO: Load game
        } else if (settingsBtn.contains(p)) {
            System.out.println("SETTINGS clicked");
            // TODO: Open settings
        } else if (galleryBtn.contains(p)) {
            System.out.println("GALLERY clicked");
            // TODO: Open gallery
        } else if (creditsBtn.contains(p)) {
            System.out.println("CREDITS clicked");
            // TODO: Show credits
        } else if (exitBtn.contains(p)) {
            int choice = JOptionPane.showConfirmDialog(
                this,
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

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw background
        drawBackground(g2d);

        // Draw title
        drawTitle(g2d);

        // Draw subtitle
        drawSubtitle(g2d);

        // Draw menu buttons
        drawButton(g2d, newGameBtn);
        drawButton(g2d, loadGameBtn);
        drawButton(g2d, settingsBtn);
        drawButton(g2d, galleryBtn);
        drawButton(g2d, creditsBtn);
        drawButton(g2d, exitBtn);

        // Draw decorative elements
        drawDecorations(g2d);
    }

    private void drawBackground(Graphics2D g2d) {
        // Dark gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(10, 15, 25),
            0, getHeight(), new Color(20, 30, 45)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Add subtle grid pattern
        g2d.setColor(new Color(30, 40, 60, 30));
        for (int x = 0; x < getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 50) {
            g2d.drawLine(0, y, getWidth(), y);
        }

        // Add glowing particles
        g2d.setColor(new Color(0, 200, 255, 50));
        for (int i = 0; i < 20; i++) {
            int x = (int) (Math.sin(glowPulse + i) * 100 + getWidth() / 2);
            int y = (int) (Math.cos(glowPulse * 0.5 + i) * 100 + getHeight() / 2);
            int size = 3 + (int) (Math.sin(glowPulse * 2 + i) * 2);
            g2d.fillOval(x, y, size, size);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        // Main title
        Font titleFont = new Font("Arial Black", Font.BOLD, 60);
        g2d.setFont(titleFont);

        String title = "DEFENDERS OF SOLARA";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        int y = 120;

        // Glow effect
        g2d.setColor(new Color(0, 200, 255, 100));
        for (int i = 3; i > 0; i--) {
            g2d.drawString(title, x - i, y - i);
            g2d.drawString(title, x + i, y + i);
        }

        // Main text
        g2d.setColor(new Color(0, 255, 255));
        g2d.drawString(title, x, y);

        // Tech accent on 'D'
        g2d.setColor(new Color(0, 200, 255, 150));
        g2d.fillOval(x - 30, y - 60, 15, 15);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x - 22, y - 52, x - 10, y - 35);
    }

    private void drawSubtitle(Graphics2D g2d) {
        Font subtitleFont = new Font("Arial", Font.PLAIN, 20);
        g2d.setFont(subtitleFont);

        String subtitle = "VEIL SYSTEM CONFLICT";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(subtitle)) / 2;
        int y = 160;

        g2d.setColor(new Color(150, 200, 255));
        g2d.drawString(subtitle, x, y);
    }

    private void drawButton(Graphics2D g2d, MenuButton btn) {
        boolean isHovered = btn == selectedButton;

        // Create hexagonal shape
        Path2D hexagon = createHexagon(btn.x, btn.y, btn.width, btn.height);

        // Background
        if (btn.isPrimary && !isHovered) {
            g2d.setColor(new Color(40, 80, 120, 180));
        } else if (isHovered) {
            g2d.setColor(new Color(60, 120, 180, 220));
        } else {
            g2d.setColor(new Color(30, 40, 50, 180));
        }
        g2d.fill(hexagon);

        // Glow effect for hovered button
        if (isHovered) {
            g2d.setColor(new Color(0, 200, 255,
                (int)(100 * (0.7 + 0.3 * Math.sin(glowPulse)))));
            g2d.setStroke(new BasicStroke(8));
            g2d.draw(hexagon);
        }

        // Border
        g2d.setColor(isHovered ? new Color(0, 255, 255) : new Color(80, 120, 160));
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(hexagon);

        // Tech corners
        drawTechCorners(g2d, btn.x, btn.y, btn.width, btn.height, isHovered);

        // Button text
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = btn.x + (btn.width - fm.stringWidth(btn.text)) / 2;
        int textY = btn.y + (btn.height + fm.getAscent()) / 2 - 5;

        // Text shadow
        if (isHovered) {
            g2d.setColor(new Color(0, 200, 255, 150));
            g2d.drawString(btn.text, textX - 1, textY - 1);
        }

        // Main text
        g2d.setColor(isHovered ? new Color(255, 255, 255) : new Color(150, 200, 255));
        g2d.drawString(btn.text, textX, textY);

        // Animated highlight for primary button
        if (btn.isPrimary && !isHovered) {
            float pulse = (float) Math.sin(glowPulse * 0.5);
            g2d.setColor(new Color(0, 200, 255, (int) (50 + pulse * 30)));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(hexagon);
        }
    }

    private Path2D createHexagon(int x, int y, int width, int height) {
        Path2D path = new Path2D.Double();

        // Left angle
        path.moveTo(x - 20, y + height / 2);
        path.lineTo(x + 10, y);
        path.lineTo(x + width - 30, y);

        // Right angle
        path.lineTo(x + width + 20, y + height / 2);
        path.lineTo(x + width - 30, y + height);
        path.lineTo(x + 10, y + height);

        path.closePath();
        return path;
    }

    private void drawTechCorners(Graphics2D g2d, int x, int y, int width, int height, boolean isHovered) {
        Color color = isHovered ? new Color(0, 255, 255) : new Color(80, 120, 160);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));

        int cornerSize = 10;

        // Top-left tech corner
        g2d.drawLine(x + 10, y - 5, x + 10 + cornerSize, y - 5);
        g2d.drawLine(x + 10, y - 5, x + 10, y + cornerSize);

        // Top-right tech corner
        g2d.drawLine(x + width - 30, y - 5, x + width - 30 + cornerSize, y - 5);
        g2d.drawLine(x + width - 30 + cornerSize, y - 5, x + width - 30 + cornerSize, y + cornerSize);

        // Bottom-right tech corner
        g2d.drawLine(x + width - 30 + cornerSize, y + height - cornerSize, x + width - 30 + cornerSize, y + height + 5);
        g2d.drawLine(x + width - 30, y + height + 5, x + width - 30 + cornerSize, y + height + 5);
    }

    private void drawDecorations(Graphics2D g2d) {
        // Bottom center tech decoration
        int centerX = getWidth() / 2;
        int bottomY = getHeight() - 50;

        g2d.setColor(new Color(0, 200, 255, 150));
        g2d.fillOval(centerX - 10, bottomY - 10, 20, 20);

        // Animated glow
        float pulse = (float) Math.sin(glowPulse);
        g2d.setColor(new Color(0, 200, 255, (int) (50 + pulse * 50)));
        g2d.fillOval(centerX - 15, bottomY - 15, 30, 30);

        // Bottom corner arrow (right)
        int arrowX = getWidth() - 80;
        int arrowY = getHeight() - 80;

        g2d.setColor(new Color(150, 200, 255));
        g2d.setStroke(new BasicStroke(3));
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(arrowX, arrowY);
        arrow.lineTo(arrowX + 30, arrowY + 15);
        arrow.lineTo(arrowX, arrowY + 30);
        g2d.draw(arrow);
    }

    // ==================== MENU BUTTON CLASS ====================

    private class MenuButton {
        String text;
        int x, y, width, height;
        boolean isPrimary;

        MenuButton(String text, int x, int y) {
            this(text, x, y, false);
        }

        MenuButton(String text, int x, int y, boolean isPrimary) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = 450;
            this.height = 55;
            this.isPrimary = isPrimary;
        }

        boolean contains(Point p) {
            // Simplified bounds check (hexagon approximation)
            return p.x >= x - 20 && p.x <= x + width + 20 &&
                p.y >= y && p.y <= y + height;
        }
    }

    // ==================== TEST METHOD ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Defenders of Solara");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new EpicMainMenu());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
