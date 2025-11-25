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

        JLabel title = new JLabel("SELECT PROFILE", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY_CYAN);
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(40, 40, 30, 40);
        add(title, gbc);

        int activeSlot = parent.getActiveProfileIndex();
        PlayerProgress progress = activeSlot >= 0 ? parent.getProfileProgress(activeSlot + 1) : null;
        String summary = (progress != null)
            ? String.format("ACTIVE PROFILE %d  •  %s", activeSlot + 1, progress.getProfileSummary())
            : "Select a profile to begin";
        JLabel levelInfo = new JLabel(summary, SwingConstants.CENTER);
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

        JButton backBtn = UITheme.createButton("BACK");
        backBtn.addActionListener(e -> parent.goToMainMenu());
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 40, 10);
        add(backBtn, gbc);
    }

    private JPanel createSlotPanel(int profileIndex, PlayerProgress progress) {
        boolean isNew = progress == null || (progress.getPlayerLevel() == 1
            && progress.getCurrentExp() == 0 && progress.getClearedWorldCount() == 0);

        final boolean[] hover = {false};
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(15, 15, 30, 200);
                if (hover[0]) {
                    base = base.brighter();
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                Color borderColor = hover[0] ? UITheme.PRIMARY_CYAN : new Color(120, 120, 160);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(240, 360));

        JLabel slotNumber = new JLabel(profileIndex + ".", SwingConstants.LEFT);
        slotNumber.setFont(UITheme.FONT_SUBTITLE);
        slotNumber.setForeground(new Color(220, 220, 240));
        slotNumber.setBorder(new EmptyBorder(10, 15, 0, 15));

        panel.add(slotNumber, BorderLayout.NORTH);

        JLabel body = new JLabel("", SwingConstants.CENTER);
        body.setForeground(Color.WHITE);
        body.setFont(UITheme.FONT_TEXT_LARGE);
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (isNew) {
            body.setText("NEW GAME");
        } else {
            body.setText(String.format("<html><center>Level %d<br/>EXP %d / %d<br/>Worlds Cleared %d / 5</center></html>",
                progress.getPlayerLevel(),
                progress.getCurrentExp(),
                progress.getExpToNext(),
                progress.getClearedWorldCount()
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        parent.paintBackground(g2d, getWidth(), getHeight());

        // Vignette overlay similar to main menu
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Soft central glow behind the profile slots
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g2d.setPaint(new RadialGradientPaint(
            new Point(getWidth() / 2, getHeight() / 2),
            Math.max(getWidth(), getHeight()) / 3f,
            new float[]{0f, 1f},
            new Color[]{new Color(20, 80, 140, 220), new Color(0, 0, 0, 0)}
        ));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(AlphaComposite.SrcOver);
    }
}


