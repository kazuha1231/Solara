package com.defendersofsolara.audio;

import javazoom.jl.player.Player;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages background music playback with looping support.
 * Handles menu and dungeon music based on game state.
 */
public class MusicManager {
    private Player currentPlayer;
    private Thread playbackThread;
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private String currentTrack = null;
    
    private float masterVolume = 0.8f;
    private float musicVolume = 0.8f;
    
    /**
     * Play a music file with looping.
     * @param resourcePath Path to the music file in resources (e.g., "music/menu/StarlightOverTheSleepingFields-menu.mp3")
     */
    public void playMusic(String resourcePath) {
        // If same track is already playing, don't restart
        if (resourcePath.equals(currentTrack) && isPlaying.get()) {
            return;
        }
        
        // Stop any currently playing music and wait for it to fully stop
        stopMusic();
        
        // Small delay to ensure previous music has stopped
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Now start new music
        currentTrack = resourcePath;
        shouldStop.set(false);
        
        playbackThread = new Thread(() -> {
            while (!shouldStop.get() && !Thread.currentThread().isInterrupted()) {
                InputStream is = null;
                try {
                    is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                    if (is == null) {
                        System.err.println("Music file not found: " + resourcePath);
                        break;
                    }
                    
                    currentPlayer = new Player(is);
                    isPlaying.set(true);
                    currentPlayer.play();
                    
                    // If we reach here, the track finished. Check if we should loop.
                    if (shouldStop.get() || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    // Loop by continuing the while loop - InputStream will be recreated
                } catch (Exception e) {
                    // Check if this was an interrupt (expected when stopping)
                    if (Thread.currentThread().isInterrupted() || shouldStop.get()) {
                        break;
                    }
                    System.err.println("Error playing music: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } finally {
                    // Close the InputStream if it wasn't closed by the Player
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    // Close player if it still exists
                    if (currentPlayer != null) {
                        try {
                            currentPlayer.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                        currentPlayer = null;
                    }
                }
            }
            isPlaying.set(false);
            currentPlayer = null;
        });
        
        playbackThread.setDaemon(true);
        playbackThread.start();
    }
    
    /**
     * Stop the currently playing music.
     */
    public void stopMusic() {
        shouldStop.set(true);
        isPlaying.set(false);
        
        // Close the player first to stop audio immediately
        if (currentPlayer != null) {
            try {
                currentPlayer.close();
            } catch (Exception e) {
                // Ignore
            }
            currentPlayer = null;
        }
        
        // Interrupt the thread to stop the loop
        if (playbackThread != null && playbackThread.isAlive()) {
            try {
                playbackThread.interrupt();
                // Wait a bit for the thread to actually stop
                playbackThread.join(100); // Wait up to 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        currentTrack = null;
    }
    
    /**
     * Set master volume (0.0 to 1.0).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // Note: JLayer doesn't support volume control directly, 
        // but we store it for potential future use with a different library
    }
    
    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // Note: JLayer doesn't support volume control directly,
        // but we store it for potential future use with a different library
    }
    
    /**
     * Get master volume.
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Get music volume.
     */
    public float getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Check if music is currently playing.
     */
    public boolean isPlaying() {
        return isPlaying.get();
    }
    
    /**
     * Get the currently playing track path.
     */
    public String getCurrentTrack() {
        return currentTrack;
    }
}

