package com.defendersofsolara.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javax.sound.sampled.*;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages background music playback with looping support.
 * Handles menu and dungeon music based on game state.
 */
public class MusicManager {
    private SourceDataLine audioLine;
    private Thread playbackThread;
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private String currentTrack = null;
    
    private float masterVolume = 0.8f;
    private float musicVolume = 0.8f;
    
    private FloatControl gainControl;
    
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
                    
                    audioLine = (SourceDataLine) AudioSystem.getLine(info);
                    audioLine.open(audioFormat);
                    
                    // Get volume control
                    if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        gainControl = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                    } else if (audioLine.isControlSupported(FloatControl.Type.VOLUME)) {
                        gainControl = (FloatControl) audioLine.getControl(FloatControl.Type.VOLUME);
                    }
                    
                    updateVolume();
                    
                    audioLine.start();
                    isPlaying.set(true);
                    
                    // Play all frames
                    do {
                        if (shouldStop.get() || Thread.currentThread().isInterrupted()) {
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
                        audioLine.write(byteBuffer, 0, byteBuffer.length);
                        bitstream.closeFrame();
                    } while ((header = bitstream.readFrame()) != null);
                    
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
                    if (audioLine != null) {
                        try {
                            if (audioLine.isRunning()) {
                                audioLine.stop();
                            }
                            audioLine.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                        audioLine = null;
                        gainControl = null;
                    }
                }
            }
            isPlaying.set(false);
            audioLine = null;
            gainControl = null;
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
        
        // Close the audio line first to stop audio immediately
        if (audioLine != null) {
            try {
                audioLine.stop();
                audioLine.close();
            } catch (Exception e) {
                // Ignore
            }
            audioLine = null;
            gainControl = null;
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
        updateVolume();
    }
    
    /**
     * Set music volume (0.0 to 1.0).
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateVolume();
    }
    
    /**
     * Update the actual audio volume based on master and music volume settings.
     */
    private void updateVolume() {
        if (gainControl == null) {
            return;
        }
        
        try {
            float combinedVolume = masterVolume * musicVolume;
            
            if (gainControl.getType() == FloatControl.Type.MASTER_GAIN) {
                // MASTER_GAIN is in decibels, range is typically -80.0 to 6.0206
                float minGain = gainControl.getMinimum();
                float maxGain = gainControl.getMaximum();
                float gain = minGain + (maxGain - minGain) * combinedVolume;
                gainControl.setValue(gain);
            } else if (gainControl.getType() == FloatControl.Type.VOLUME) {
                // VOLUME is linear, range is typically 0.0 to 1.0
                gainControl.setValue(combinedVolume);
            }
        } catch (Exception e) {
            // Ignore volume control errors
        }
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

