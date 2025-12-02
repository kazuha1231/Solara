package com.defendersofsolara.lwjgl3;

/**
 * Launches the desktop (LWJGL3) application.
 * 
 * NOTE: This launcher is currently disabled as the game uses the Swing UI system.
 * The main entry point is: com.defendersofsolara.Main
 * 
 * To run the game, use:
 * ./gradlew :core:run
 * OR
 * Run com.defendersofsolara.Main directly
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        
        System.err.println("Lwjgl3Launcher is not used. The game uses Swing UI (UnifiedGameUI).");
        System.err.println("Please run: com.defendersofsolara.Main instead.");
        System.err.println("Or use: ./gradlew :core:run");
        System.exit(1);
        
        // LibGDX UI system has been removed - using Swing UI instead
        // If you need LibGDX support, restore the libgdx UI classes
        /*
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Defenders of Solara: The Shattered Dungeons of Eldralune");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        new Lwjgl3Application(new DefendersOfSolaraGame(), config);
        */
    }
}
