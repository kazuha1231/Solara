package com.defendersofsolara.ui._backup;

import com.defendersofsolara.ui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * WorldStory - FIXED VERSION (no more font errors even if UITheme is broken)
 */
public class WorldStory extends JFrame {
    private int worldId;

    public WorldStory(int worldId) {
        this.worldId = worldId;
        setTitle("Defenders of Solara - World " + worldId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Safe fallback size if UITheme fails
        try {
            setSize(UITheme.MENU_WIDTH, UITheme.MENU_HEIGHT);
        } catch (Exception e) {
            setSize(900, 600);
        }

        setLocationRelativeTo(null);

        JPanel storyPanel = createStoryPanel();
        setContentPane(storyPanel);
        setVisible(true);
    }

    private JPanel createStoryPanel() {

        JPanel panel;

        // Safe fallback in case UITheme.createGradientPanel breaks
        try {
            panel = UITheme.createGradientPanel();
        } catch (Exception e) {
            panel = new JPanel();
            panel.setBackground(Color.BLACK);
        }

        panel.setLayout(new BorderLayout());

        // Safe Story TextArea
        JTextArea storyText = new JTextArea();

        // Safe fallback font if UITheme.FONT_TEXT is NULL or throws an error
        Font safeFont = new Font("Arial", Font.PLAIN, 18);
        try {
            if (UITheme.getFontLog() != null) {
                storyText.setFont(UITheme.getFontLog());   // ✅ Use Font
            } else {
                storyText.setFont(safeFont);
            }
        } catch (Exception e) {
            storyText.setFont(safeFont);
        }


        try {
            storyText.setForeground(UITheme.PRIMARY_CYAN);
        } catch (Exception e) {
            storyText.setForeground(Color.CYAN);
        }

        storyText.setBackground(new Color(0, 0, 0, 0));
        storyText.setOpaque(false);
        storyText.setEditable(false);
        storyText.setLineWrap(true);
        storyText.setWrapStyleWord(true);
        storyText.setMargin(new Insets(40, 60, 40, 60));

        storyText.setText(getWorldStory(worldId));

        // Overlay Panel (safe)
        JPanel textPanel;
        try {
            textPanel = UITheme.createOverlayPanel();
        } catch (Exception e) {
            textPanel = new JPanel();
            textPanel.setBackground(new Color(0, 0, 0, 150));
        }

        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        textPanel.add(storyText, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.CENTER);

        // Continue button
        JButton continueBtn;

        try {
            continueBtn = UITheme.createButton("CONTINUE >");
        } catch (Exception e) {
            continueBtn = new JButton("CONTINUE >");
            continueBtn.setFont(safeFont);
            continueBtn.setBackground(Color.DARK_GRAY);
            continueBtn.setForeground(Color.WHITE);
        }

        continueBtn.setPreferredSize(new Dimension(200, 50));
        continueBtn.addActionListener(e -> startBattle());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(continueBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String getWorldStory(int worldId) {
        switch (worldId) {
            case 1:
                return "WORLD 1: CHRONOVALE\n" +
                    "Core of Time\n\n" +
                    "A fractured desert where time flows unevenly...";
            case 2:
                return "WORLD 2: GRAVEMIRE\nCore of Gravity\n\nProgress with caution...";
            case 3:
                return "WORLD 3: AETHERION\nCore of Energy\n\nPrepare for electrifying challenges...";
            case 4:
                return "WORLD 4: ELARION\nCore of Life\n\nNature has turned hostile...";
            case 5:
                return "WORLD 5: UMBROS\nCore of Void\n\nThe final challenge awaits...";
            default:
                return "Unknown world...";
        }
    }

    private void startBattle() {
        dispose();
        SwingUtilities.invokeLater(() -> new BattleUI(worldId));
    }
}
