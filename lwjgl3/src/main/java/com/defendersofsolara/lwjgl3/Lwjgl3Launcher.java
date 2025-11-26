package com.defendersofsolara.lwjgl3;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        // NOTE: This launcher is not used - the game uses Swing UI (UnifiedGameUI) instead
        // Main class is a console application, not a LibGDX Application
        // If you need LibGDX support, create a proper ApplicationAdapter class
        System.err.println("Lwjgl3Launcher is not used. The game uses Swing UI (UnifiedGameUI).");
        System.err.println("Please run: com.defendersofsolara.ui.UnifiedGameUI instead.");
    }
}