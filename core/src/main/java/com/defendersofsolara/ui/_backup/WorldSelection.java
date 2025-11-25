package com.defendersofsolara.ui._backup;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WorldSelection extends JFrame {

    public WorldSelection() {
        setTitle("Defenders of Solara - World Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = createWorldPanel();
        setContentPane(mainPanel);
        setVisible(true);
    }

    private JPanel createWorldPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(10, 10, 40),
                    0, getHeight(), new Color(50, 10, 80)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("SELECT YOUR WORLD", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 40));
        title.setForeground(new Color(0, 255, 255));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        panel.add(title, BorderLayout.NORTH);

        // World cards panel
        JPanel worldsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        worldsPanel.setOpaque(false);
        worldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // World 1 - Chronovale (UNLOCKED)
        worldsPanel.add(createWorldCard(
            "WORLD 1",
            "CHRONOVALE",
            "Core of Time",
            true,
            1
        ));

        // World 2 - Gravemire (LOCKED)
        worldsPanel.add(createWorldCard(
            "WORLD 2",
            "GRAVEMIRE",
            "Core of Gravity",
            false,
            2
        ));

        // World 3 - Aetherion (LOCKED)
        worldsPanel.add(createWorldCard(
            "WORLD 3",
            "AETHERION",
            "Core of Energy",
            false,
            3
        ));

        // World 4 - Elarion (LOCKED)
        worldsPanel.add(createWorldCard(
            "WORLD 4",
            "ELARION",
            "Core of Life",
            false,
            4
        ));

        // World 5 - Umbros (LOCKED)
        worldsPanel.add(createWorldCard(
            "WORLD 5",
            "UMBROS",
            "Core of Void",
            false,
            5
        ));

        // Back button
        worldsPanel.add(createBackButton());

        panel.add(worldsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWorldCard(String worldNum, String worldName, String core, boolean unlocked, int worldId) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Card background
                if (unlocked) {
                    g2d.setColor(new Color(0, 100, 150, 200));
                } else {
                    g2d.setColor(new Color(50, 50, 50, 200));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Border
                g2d.setColor(unlocked ? new Color(0, 255, 255) : Color.GRAY);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };

        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setCursor(unlocked ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 10, 5, 10);

        // World number
        JLabel numLabel = new JLabel(worldNum);
        numLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        numLabel.setForeground(unlocked ? new Color(0, 255, 255) : Color.GRAY);
        gbc.gridy = 0;
        card.add(numLabel, gbc);

        // World name
        JLabel nameLabel = new JLabel(worldName);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        nameLabel.setForeground(unlocked ? Color.WHITE : Color.GRAY);
        gbc.gridy = 1;
        card.add(nameLabel, gbc);

        // Core info
        JLabel coreLabel = new JLabel(core);
        coreLabel.setFont(new Font("Monospaced", Font.ITALIC, 14));
        coreLabel.setForeground(unlocked ? new Color(150, 200, 255) : Color.DARK_GRAY);
        gbc.gridy = 2;
        card.add(coreLabel, gbc);

        // Status
        JLabel statusLabel = new JLabel(unlocked ? "CLICK TO ENTER" : "LOCKED");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(unlocked ? new Color(0, 255, 0) : Color.RED);
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 10, 5, 10);
        card.add(statusLabel, gbc);

        // Click handler for unlocked worlds
        if (unlocked) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    enterWorld(worldId);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    card.repaint();
                }
            });
        }

        return card;
    }

    private JPanel createBackButton() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(100, 50, 50, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(255, 100, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };

        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel backLabel = new JLabel("BACK TO MENU");
        backLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        backLabel.setForeground(Color.WHITE);
        card.add(backLabel);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(GameMenu::new);
            }
        });

        return card;
    }

    private void enterWorld(int worldId) {
        dispose();
        SwingUtilities.invokeLater(() -> new WorldStory(worldId));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorldSelection::new);
    }
}
