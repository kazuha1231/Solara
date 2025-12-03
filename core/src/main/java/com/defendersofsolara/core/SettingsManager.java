package com.defendersofsolara.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages game settings persistence.
 * Saves and loads display and audio settings to/from settings.properties file.
 */
public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.properties";
    private static final Path SETTINGS_PATH = Paths.get(SETTINGS_FILE);
    
    // Default values
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    private static final boolean DEFAULT_FULLSCREEN = false;
    private static final float DEFAULT_MASTER_VOLUME = 0.75f;
    private static final float DEFAULT_MUSIC_VOLUME = 0.75f;
    private static final float DEFAULT_SFX_VOLUME = 0.75f;
    private static final boolean DEFAULT_MUTED = false;
    
    // Current settings
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private boolean fullscreen = DEFAULT_FULLSCREEN;
    private float masterVolume = DEFAULT_MASTER_VOLUME;
    private float musicVolume = DEFAULT_MUSIC_VOLUME;
    private float sfxVolume = DEFAULT_SFX_VOLUME;
    private boolean muted = DEFAULT_MUTED;
    
    private static SettingsManager instance;
    
    private SettingsManager() {
        load();
    }
    
    public static SettingsManager getInstance() {
        if (instance == null) {
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    instance = new SettingsManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load settings from file.
     */
    public void load() {
        if (!Files.exists(SETTINGS_PATH)) {
            // Use defaults if file doesn't exist
            return;
        }
        
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(SETTINGS_PATH)) {
            props.load(is);
            
            width = parseInt(props.getProperty("width"), DEFAULT_WIDTH);
            height = parseInt(props.getProperty("height"), DEFAULT_HEIGHT);
            fullscreen = parseBoolean(props.getProperty("fullscreen"), DEFAULT_FULLSCREEN);
            masterVolume = parseFloat(props.getProperty("masterVolume"), DEFAULT_MASTER_VOLUME);
            musicVolume = parseFloat(props.getProperty("musicVolume"), DEFAULT_MUSIC_VOLUME);
            sfxVolume = parseFloat(props.getProperty("sfxVolume"), DEFAULT_SFX_VOLUME);
            muted = parseBoolean(props.getProperty("muted"), DEFAULT_MUTED);
            
            // Clamp values to valid ranges
            width = Math.max(800, Math.min(3840, width));
            height = Math.max(600, Math.min(2160, height));
            masterVolume = Math.max(0.0f, Math.min(1.0f, masterVolume));
            musicVolume = Math.max(0.0f, Math.min(1.0f, musicVolume));
            sfxVolume = Math.max(0.0f, Math.min(1.0f, sfxVolume));
            
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            // Use defaults on error
        }
    }
    
    /**
     * Save settings to file.
     */
    public void save() {
        Properties props = new Properties();
        props.setProperty("width", String.valueOf(width));
        props.setProperty("height", String.valueOf(height));
        props.setProperty("fullscreen", String.valueOf(fullscreen));
        props.setProperty("masterVolume", String.valueOf(masterVolume));
        props.setProperty("musicVolume", String.valueOf(musicVolume));
        props.setProperty("sfxVolume", String.valueOf(sfxVolume));
        props.setProperty("muted", String.valueOf(muted));
        
        try (OutputStream os = Files.newOutputStream(SETTINGS_PATH)) {
            props.store(os, "Defenders of Solara Game Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    // Getters and setters
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = Math.max(800, Math.min(3840, width));
        save();
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = Math.max(600, Math.min(2160, height));
        save();
    }
    
    public boolean isFullscreen() {
        return fullscreen;
    }
    
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        save();
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        save();
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        save();
    }
    
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        save();
    }
    
    public boolean isMuted() {
        return muted;
    }
    
    public void setMuted(boolean muted) {
        this.muted = muted;
        save();
    }
    
    // Helper methods
    private float parseFloat(String value, float defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}

