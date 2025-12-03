package com.defendersofsolara.audio;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages sound effect playback with volume control.
 * Handles SFX volume separately from music volume.
 */
public class SoundEffectManager {
    private static SoundEffectManager instance;
    
    private float masterVolume = 0.8f;
    private float sfxVolume = 0.8f;
    private boolean muted = false;
    private final ExecutorService soundExecutor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<String, Clip> activeClips = new ConcurrentHashMap<>();
    
    private SoundEffectManager() {
        // Private constructor for singleton
    }
    
    public static SoundEffectManager getInstance() {
        if (instance == null) {
            synchronized (SoundEffectManager.class) {
                if (instance == null) {
                    instance = new SoundEffectManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Play a sound effect from a resource path.
     * @param resourcePath Path to the sound file (e.g., "sounds/attack.wav")
     * @param loop Whether to loop the sound (for ambient sounds)
     */
    public void playSound(String resourcePath, boolean loop) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return;
        }
        
        soundExecutor.submit(() -> {
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("Sound file not found: " + resourcePath);
                    return;
                }
                
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
                AudioFormat format = audioStream.getFormat();
                
                // Convert to PCM format if needed
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        16,
                        format.getChannels(),
                        format.getChannels() * 2,
                        format.getSampleRate(),
                        false
                    );
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                    format = targetFormat;
                }
                
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Audio format not supported for: " + resourcePath);
                    audioStream.close();
                    return;
                }
                
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(audioStream);
                
                // Apply volume
                applyVolumeToClip(clip);
                
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    clip.start();
                }
                
                // Track active clip
                String clipId = resourcePath + "_" + System.currentTimeMillis();
                activeClips.put(clipId, clip);
                
                // Remove from tracking when done (if not looping)
                if (!loop) {
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                            activeClips.remove(clipId);
                        }
                    });
                }
                
                audioStream.close();
            } catch (Exception e) {
                System.err.println("Error playing sound: " + resourcePath + " - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Play a sound effect once (non-looping).
     */
    public void playSound(String resourcePath) {
        playSound(resourcePath, false);
    }
    
    /**
     * Stop all playing sounds.
     */
    public void stopAllSounds() {
        for (Clip clip : activeClips.values()) {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        activeClips.clear();
    }
    
    /**
     * Apply volume to a clip.
     */
    private void applyVolumeToClip(Clip clip) {
        if (clip == null) return;
        
        try {
            // If muted, set volume to 0, otherwise use calculated volume
            float combinedVolume = muted ? 0.0f : (masterVolume * sfxVolume);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float minGain = gainControl.getMinimum();
                float maxGain = gainControl.getMaximum();
                float gain = minGain + (maxGain - minGain) * combinedVolume;
                gainControl.setValue(gain);
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue(combinedVolume);
            }
        } catch (Exception e) {
            // Ignore volume control errors
        }
    }
    
    /**
     * Update volume for all active clips.
     */
    private void updateAllVolumes() {
        for (Clip clip : activeClips.values()) {
            if (clip.isOpen() && clip.isRunning()) {
                applyVolumeToClip(clip);
            }
        }
    }
    
    /**
     * Set master volume (0.0 to 1.0).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateAllVolumes();
    }
    
    /**
     * Set SFX volume (0.0 to 1.0).
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateAllVolumes();
    }
    
    /**
     * Get master volume.
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Get SFX volume.
     */
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Set mute state (true = muted, false = unmuted).
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        updateAllVolumes();
    }
    
    /**
     * Get mute state.
     */
    public boolean isMuted() {
        return muted;
    }
    
    /**
     * Shutdown the sound effect manager and release resources.
     */
    public void shutdown() {
        stopAllSounds();
        soundExecutor.shutdown();
    }
}

