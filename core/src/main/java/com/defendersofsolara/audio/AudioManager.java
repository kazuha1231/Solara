package com.defendersofsolara.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javax.sound.sampled.*;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unified audio manager for all game audio (music and sound effects).
 * Singleton pattern with master volume, music volume, SFX volume, and mute control.
 */
public class AudioManager {
    private static AudioManager instance;
    
    // Volume settings (0.0 to 1.0)
    private float masterVolume = 0.75f;
    private float musicVolume = 0.75f;
    private float sfxVolume = 0.75f;
    private boolean isMuted = false;
    
    // Music playback
    private SourceDataLine musicLine;
    private Thread musicThread;
    private final AtomicBoolean musicPlaying = new AtomicBoolean(false);
    private final AtomicBoolean musicShouldStop = new AtomicBoolean(false);
    private String currentMusicTrack = null;
    private FloatControl musicGainControl;
    
    // Sound effects
    private final ExecutorService sfxExecutor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<String, Clip> activeSFXClips = new ConcurrentHashMap<>();
    
    private AudioManager() {
        // Private constructor for singleton
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }
    
    // ==================== MUSIC PLAYBACK ====================
    
    /**
     * Play background music with looping.
     * @param resourcePath Path to music file (e.g., "music/menu/track.mp3")
     */
    public void playMusic(String resourcePath) {
        // If same track is already playing, don't restart
        if (resourcePath.equals(currentMusicTrack) && musicPlaying.get()) {
            return;
        }
        
        // Stop any currently playing music
        stopMusic();
        
        // Small delay to ensure previous music has stopped
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        currentMusicTrack = resourcePath;
        musicShouldStop.set(false);
        
        musicThread = new Thread(() -> {
            while (!musicShouldStop.get() && !Thread.currentThread().isInterrupted()) {
                InputStream is = null;
                Bitstream bitstream = null;
                try {
                    is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                    if (is == null) {
                        System.err.println("Music file not found: " + resourcePath);
                        break;
                    }
                    
                    // Use JLayer decoder with Java Sound API for volume control
                    bitstream = new Bitstream(is);
                    Decoder decoder = new Decoder();
                    
                    // Read first frame to get audio format
                    Header header = bitstream.readFrame();
                    if (header == null) {
                        System.err.println("Invalid MP3 file");
                        break;
                    }
                    
                    int sampleRate = header.frequency();
                    int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
                    AudioFormat audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                    
                    if (!AudioSystem.isLineSupported(info)) {
                        System.err.println("Audio format not supported: " + audioFormat);
                        break;
                    }
                    
                    musicLine = (SourceDataLine) AudioSystem.getLine(info);
                    musicLine.open(audioFormat);
                    
                    // Get volume control
                    if (musicLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        musicGainControl = (FloatControl) musicLine.getControl(FloatControl.Type.MASTER_GAIN);
                    } else if (musicLine.isControlSupported(FloatControl.Type.VOLUME)) {
                        musicGainControl = (FloatControl) musicLine.getControl(FloatControl.Type.VOLUME);
                    }
                    
                    updateMusicVolume();
                    
                    musicLine.start();
                    musicPlaying.set(true);
                    
                    // Play all frames
                    do {
                        if (musicShouldStop.get() || Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        
                        SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                        short[] samples = output.getBuffer();
                        int length = output.getBufferLength();
                        byte[] byteBuffer = new byte[length * 2];
                        for (int i = 0; i < length; i++) {
                            byteBuffer[i * 2] = (byte) (samples[i] & 0xFF);
                            byteBuffer[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
                        }
                        musicLine.write(byteBuffer, 0, byteBuffer.length);
                        bitstream.closeFrame();
                    } while ((header = bitstream.readFrame()) != null);
                    
                    // If we reach here, the track finished. Check if we should loop.
                    if (musicShouldStop.get() || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    // Loop by continuing the while loop - InputStream will be recreated
                } catch (Exception e) {
                    // Check if this was an interrupt (expected when stopping)
                    if (Thread.currentThread().isInterrupted() || musicShouldStop.get()) {
                        break;
                    }
                    System.err.println("Error playing music: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } finally {
                    // Close bitstream
                    if (bitstream != null) {
                        try {
                            bitstream.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    // Close the InputStream
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    // Close audio line if it still exists
                    if (musicLine != null) {
                        try {
                            if (musicLine.isRunning()) {
                                musicLine.stop();
                            }
                            musicLine.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                        musicLine = null;
                        musicGainControl = null;
                    }
                }
            }
            musicPlaying.set(false);
            musicLine = null;
            musicGainControl = null;
        });
        
        musicThread.setDaemon(true);
        musicThread.start();
    }
    
    /**
     * Stop the currently playing music.
     */
    public void stopMusic() {
        musicShouldStop.set(true);
        musicPlaying.set(false);
        
        // Close the audio line first to stop audio immediately
        if (musicLine != null) {
            try {
                musicLine.stop();
                musicLine.close();
            } catch (Exception e) {
                // Ignore
            }
            musicLine = null;
            musicGainControl = null;
        }
        
        // Interrupt the thread to stop the loop
        if (musicThread != null && musicThread.isAlive()) {
            try {
                musicThread.interrupt();
                musicThread.join(100); // Wait up to 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        currentMusicTrack = null;
    }
    
    // ==================== SOUND EFFECTS ====================
    
    /**
     * Play a sound effect.
     * @param resourcePath Path to sound file (e.g., "sounds/attack.wav")
     * @param loop Whether to loop the sound
     */
    public void playSFX(String resourcePath, boolean loop) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return;
        }
        
        sfxExecutor.submit(() -> {
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
                activeSFXClips.put(clipId, clip);
                
                // Remove from tracking when done (if not looping)
                if (!loop) {
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                            activeSFXClips.remove(clipId);
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
    public void playSFX(String resourcePath) {
        playSFX(resourcePath, false);
    }
    
    /**
     * Stop all playing sound effects.
     */
    public void stopAllSFX() {
        for (Clip clip : activeSFXClips.values()) {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        activeSFXClips.clear();
    }
    
    // ==================== VOLUME CONTROL ====================
    
    /**
     * Update music volume based on current settings.
     */
    private void updateMusicVolume() {
        if (musicGainControl == null) {
            return;
        }
        
        try {
            // Final music volume = masterVolume × musicVolume
            // If muted, volume = 0
            float finalVolume = isMuted ? 0.0f : (masterVolume * musicVolume);
            
            if (musicGainControl.getType() == FloatControl.Type.MASTER_GAIN) {
                // MASTER_GAIN is in decibels
                float minGain = musicGainControl.getMinimum();
                float maxGain = musicGainControl.getMaximum();
                // Convert 0.0-1.0 to decibels
                if (finalVolume <= 0.0f) {
                    musicGainControl.setValue(minGain);
                } else {
                    // dB = 20 * log10(volume)
                    float db = (float)(20.0 * Math.log10(finalVolume));
                    // Clamp to valid range
                    float gain = Math.max(minGain, Math.min(maxGain, db));
                    musicGainControl.setValue(gain);
                }
            } else if (musicGainControl.getType() == FloatControl.Type.VOLUME) {
                // VOLUME is linear, range is typically 0.0 to 1.0
                musicGainControl.setValue(finalVolume);
            }
        } catch (Exception e) {
            // Ignore volume control errors
        }
    }
    
    /**
     * Apply volume to a sound effect clip.
     */
    private void applyVolumeToClip(Clip clip) {
        if (clip == null) return;
        
        try {
            // Final SFX volume = masterVolume × sfxVolume
            // If muted, volume = 0
            float finalVolume = isMuted ? 0.0f : (masterVolume * sfxVolume);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float minGain = gainControl.getMinimum();
                float maxGain = gainControl.getMaximum();
                // Convert 0.0-1.0 to decibels
                if (finalVolume <= 0.0f) {
                    gainControl.setValue(minGain);
                } else {
                    // dB = 20 * log10(volume)
                    float db = (float)(20.0 * Math.log10(finalVolume));
                    // Clamp to valid range
                    float gain = Math.max(minGain, Math.min(maxGain, db));
                    gainControl.setValue(gain);
                }
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue(finalVolume);
            }
        } catch (Exception e) {
            // Ignore volume control errors
        }
    }
    
    /**
     * Update volume for all active sound effect clips.
     */
    private void updateAllSFXVolumes() {
        for (Clip clip : activeSFXClips.values()) {
            if (clip.isOpen() && clip.isRunning()) {
                applyVolumeToClip(clip);
            }
        }
    }
    
    // ==================== SETTERS AND GETTERS ====================
    
    /**
     * Set master volume (0.0 to 1.0).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateMusicVolume();
        updateAllSFXVolumes();
    }
    
    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateMusicVolume();
    }
    
    /**
     * Set SFX volume (0.0 to 1.0).
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateAllSFXVolumes();
    }
    
    /**
     * Set mute state (true = muted, false = unmuted).
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        updateMusicVolume();
        updateAllSFXVolumes();
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
     * Get SFX volume.
     */
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Get mute state.
     */
    public boolean isMuted() {
        return isMuted;
    }
    
    /**
     * Check if music is currently playing.
     */
    public boolean isMusicPlaying() {
        return musicPlaying.get();
    }
    
    /**
     * Get the currently playing music track path.
     */
    public String getCurrentMusicTrack() {
        return currentMusicTrack;
    }
    
    /**
     * Shutdown the audio manager and release resources.
     */
    public void shutdown() {
        stopMusic();
        stopAllSFX();
        sfxExecutor.shutdown();
    }
}

