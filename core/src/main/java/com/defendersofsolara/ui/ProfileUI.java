package com.defendersofsolara.ui;

import com.defendersofsolara.core.PlayerProgress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Simple "Select Profile" screen with three save slots.
 * Each slot currently starts a new game and goes to world selection.
 */
class ProfileUI extends JPanel {

    private final UnifiedGameUI parent;

    ProfileUI(UnifiedGameUI parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create beautiful title with divider fade underline
        JLabel title = createTitleWithDivider("SELECT PROFILE", UITheme.FONT_SUBTITLE, UITheme.PRIMARY_GREEN);
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY_GREEN);
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(40, 40, 30, 40);
        add(title, gbc);

        int activeSlot = parent.getActiveProfileIndex();
        PlayerProgress progress = activeSlot >= 0 ? parent.getProfileProgress(activeSlot + 1) : null;
        String summary = (progress != null)
            ? String.format("ACTIVE PROFILE %d • %s", activeSlot + 1, progress.getProfileSummary())
            : "Select a profile to begin";
        JLabel levelInfo = createTitleWithDivider(summary, UITheme.FONT_TEXT, Color.WHITE);
        levelInfo.setFont(UITheme.FONT_TEXT);
        levelInfo.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 60, 25, 60);
        add(levelInfo, gbc);

        JPanel slotsPanel = new JPanel();
        slotsPanel.setOpaque(false);
        slotsPanel.setLayout(new GridLayout(1, parent.getProfileSlotCount(), 30, 0));

        for (int i = 1; i <= parent.getProfileSlotCount(); i++) {
            JPanel slot = createSlotPanel(i, parent.getProfileProgress(i));
            slotsPanel.add(slot);
        }

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 80, 20, 80);
        add(slotsPanel, gbc);

        JButton backBtn = UITheme.createSmallButton("BACK");
        backBtn.addActionListener(e -> parent.goToMainMenu());
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 40, 10);
        add(backBtn, gbc);
    }

    private JPanel createSlotPanel(int profileIndex, PlayerProgress progress) {
        // A profile is "new" if it's null, or if it has no progress AND has never been saved
        boolean isNew = progress == null || (progress.getPlayerLevel() == 1
            && progress.getCurrentExp() == 0 && progress.getClearedWorldCount() == 0
            && (progress.getLastSaveTime() == 0));
        boolean isActive = parent.getActiveProfileIndex() == profileIndex - 1;

        final boolean[] hover = {false};
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Use nearest neighbor for pixel-art look
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
                // Semi-transparent background with very low opacity (matching world selection)
                Color bg;
                if (hover[0]) {
                    bg = new Color(UITheme.BG_CARD.getRed() + 15, UITheme.BG_CARD.getGreen() + 15, UITheme.BG_CARD.getBlue() + 15);
                } else if (isActive) {
                    bg = UITheme.BG_CARD;
                } else {
                    bg = new Color(UITheme.BG_CARD.getRed() - 5, UITheme.BG_CARD.getGreen() - 5, UITheme.BG_CARD.getBlue() - 5);
                }
                Color bgColor = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 60); // Very low opacity (60/255)
                g2.setColor(bgColor);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Use only panel border (not full panel)
                java.awt.image.BufferedImage borderImg = PixelArtUI.loadImage("/kennyresources/PNG/Default/Border/panel-border-000.png");
                if (borderImg != null) {
                    float alpha = hover[0] ? 0.3f : (isActive ? 0.25f : 0.2f);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    PixelArtUI.drawNineSlice(g2, borderImg, 0, 0, getWidth(), getHeight());
                } else {
                    // Fallback: simple border
                    Color borderColor = hover[0] 
                        ? new Color(UITheme.BORDER_HIGHLIGHT.getRed(), UITheme.BORDER_HIGHLIGHT.getGreen(), UITheme.BORDER_HIGHLIGHT.getBlue(), 100)
                        : isActive 
                            ? new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 80)
                            : new Color(UITheme.BORDER_NORMAL.getRed(), UITheme.BORDER_NORMAL.getGreen(), UITheme.BORDER_NORMAL.getBlue(), 60);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(240, 360));

        JLabel slotNumber = new JLabel(profileIndex + ".", SwingConstants.LEFT);
        slotNumber.setFont(UITheme.FONT_SUBTITLE);
        slotNumber.setForeground(UITheme.PRIMARY_WHITE);
        slotNumber.setBorder(new EmptyBorder(10, 15, 0, 15));

        panel.add(slotNumber, BorderLayout.NORTH);

        JLabel body = new JLabel("", SwingConstants.CENTER);
        body.setForeground(UITheme.PRIMARY_WHITE);
        body.setFont(UITheme.FONT_TEXT_LARGE);
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (isNew) {
            body.setText("NEW GAME");
        } else {
            body.setText(String.format("<html><center>Level %d<br/>EXP %d / %d<br/>Worlds Cleared: %d / 5<br/><br/>Time: %s<br/>Last Save: %s</center></html>",
                progress.getPlayerLevel(),
                progress.getCurrentExp(),
                progress.getExpToNext(),
                progress.getClearedWorldCount(),
                progress.getFormattedPlayTime(),
                progress.getFormattedLastSaveDate()
            ));
        }

        panel.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel summary = new JLabel(isNew ? "Tap to begin" : progress.getProfileSummary(), SwingConstants.CENTER);
        summary.setFont(UITheme.FONT_SMALL);
        summary.setForeground(new Color(180, 220, 255));
        summary.setBorder(new EmptyBorder(0, 5, 5, 5));
        footer.add(summary, BorderLayout.CENTER);

        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        iconRow.setOpaque(false);

        JButton restartBtn = createIconButton("/image/restarticon.png", "Restart profile");
        restartBtn.addActionListener(e -> confirmRestart(profileIndex));
        iconRow.add(restartBtn);

        JButton deleteBtn = createIconButton("/image/deleteicon.png", "Delete profile");
        deleteBtn.addActionListener(e -> confirmDelete(profileIndex));
        iconRow.add(deleteBtn);

        footer.add(iconRow, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover[0] = true;
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover[0] = false;
                panel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                parent.onProfileSelected(profileIndex);
            }
        });

        return panel;
    }
    private JButton createIconButton(String resourcePath, String tooltip) {
        JButton button = new JButton();
        int size = Math.round(30 * UITheme.getScaleFactor());
        button.setPreferredSize(new Dimension(size, size));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setToolTipText(tooltip);
        java.net.URL iconUrl = getClass().getResource(resourcePath);
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            int iconSize = size - 6;
            Image scaled = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaled));
        } else {
            button.setText("?");
        }
        return button;
    }

    private void confirmRestart(int profileIndex) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Restart profile " + profileIndex + "? This will reset progress but keep the slot.",
            "Restart Profile",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            parent.resetProfileSlot(profileIndex, false);
        }
    }

    private void confirmDelete(int profileIndex) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Delete profile " + profileIndex + "? This removes all data.",
            "Delete Profile",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            parent.resetProfileSlot(profileIndex, true);
        }
    }

    /**
     * Creates a beautiful label with divider fade underline.
     */
    private JLabel createTitleWithDivider(String text, Font font, Color color) {
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
                String dividerPath = "/kennyresources/PNG/Default/Divider Fade/divider-fade-001.png";
                if (font.getSize() >= 24) {
                    dividerPath = "/kennyresources/PNG/Default/Divider Fade/divider-fade-002.png";
                }
                java.awt.image.BufferedImage dividerFade = PixelArtUI.loadImage(dividerPath);
                if (dividerFade == null) {
                    dividerFade = PixelArtUI.loadImage("/kennyresources/PNG/Default/Divider Fade/divider-fade-000.png");
                }
                
                if (dividerFade != null) {
                    int underlineY = y + fm.getDescent() + 8;
                    int underlineWidth = Math.max(textWidth + 40, 120);
                    int underlineX = (getWidth() - underlineWidth) / 2;
                    int underlineHeight = Math.max(dividerFade.getHeight(), 4);
                    
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.9f));
                    PixelArtUI.drawNineSlice(g2d, dividerFade, underlineX, underlineY, underlineWidth, underlineHeight);
                    g2d.setComposite(java.awt.AlphaComposite.SrcOver);
                }
                
                g2d.dispose();
            }
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        parent.paintBackground(g2d, getWidth(), getHeight());
        // No dark overlay - keep it bright like world selection
    }
}


