package com.defendersofsolara;

import com.defendersofsolara.ui.UnifiedGameUI;
import javax.swing.*;

/**
 * Main entry point for Defenders of Solara: The Shattered Dungeons of Eldralune
 * 
 * Launches the Swing-based UI system with medieval fantasy color theme.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(UnifiedGameUI::new);
    }
}
