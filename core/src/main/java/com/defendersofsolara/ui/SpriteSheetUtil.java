package com.defendersofsolara.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Utility class for working with sprite sheets and compositing layered sprites.
 * Handles splitting spritesheets and compositing LPC-style layered sprites.
 */
public class SpriteSheetUtil {
    
    /**
     * Splits a horizontal spritesheet into individual frames.
     * 
     * @param sheet The spritesheet image
     * @param frameWidth Width of each frame
     * @param frameHeight Height of each frame
     * @param totalFrames Number of frames to extract
     * @return Array of individual frame images
     */
    public static BufferedImage[] splitSpriteSheet(BufferedImage sheet, int frameWidth, int frameHeight, int totalFrames) {
        if (sheet == null) {
            return new BufferedImage[0];
        }
        
        BufferedImage[] frames = new BufferedImage[totalFrames];
        int cols = sheet.getWidth() / frameWidth;
        
        for (int i = 0; i < totalFrames; i++) {
            int col = i % cols;
            int row = i / cols;
            
            int x = col * frameWidth;
            int y = row * frameHeight;
            
            if (x + frameWidth <= sheet.getWidth() && y + frameHeight <= sheet.getHeight()) {
                frames[i] = sheet.getSubimage(x, y, frameWidth, frameHeight);
            } else {
                // Create empty frame if out of bounds
                frames[i] = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
            }
        }
        
        return frames;
    }
    
    /**
     * Composites multiple layered sprite images into a single frame.
     * Layers are drawn in order, with later layers on top.
     * 
     * @param layers List of layer images to composite (in z-order)
     * @param frameWidth Width of the output frame
     * @param frameHeight Height of the output frame
     * @return Composited frame image
     */
    public static BufferedImage compositeLayers(List<BufferedImage> layers, int frameWidth, int frameHeight) {
        BufferedImage composite = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = composite.createGraphics();
        
        // Enable alpha blending for transparency
        g2d.setComposite(java.awt.AlphaComposite.SrcOver);
        
        // Draw each layer in order
        for (BufferedImage layer : layers) {
            if (layer != null) {
                g2d.drawImage(layer, 0, 0, null);
            }
        }
        
        g2d.dispose();
        return composite;
    }
    
    /**
     * Loads an image from a resource path.
     * 
     * @param resourcePath Path to the resource (e.g., "/ka/standard/idle/010 body_color__light_.png.png")
     * @return Loaded image, or null if not found
     */
    public static BufferedImage loadImage(String resourcePath) {
        try {
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            
            java.net.URL url = SpriteSheetUtil.class.getResource(resourcePath);
            if (url == null) {
                return null;
            }
            
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Composites all frames of an animation from layered sprite files.
     * Assumes frames are numbered sequentially (0, 1, 2, ...) and layers are identified by z-position.
     * 
     * @param basePath Base resource path (e.g., "/ka/standard/idle/")
     * @param layerFiles List of layer file names (e.g., ["010 body_color__light_.png.png", "020 long_pants__green_.png.png"])
     * @param frameCount Number of frames in the animation
     * @param frameWidth Width of each frame
     * @param frameHeight Height of each frame
     * @return List of composited frame images
     */
    public static List<BufferedImage> compositeAnimationFrames(
            String basePath, 
            List<String> layerFiles, 
            int frameCount,
            int frameWidth, 
            int frameHeight) {
        
        List<BufferedImage> frames = new ArrayList<>();
        
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            List<BufferedImage> frameLayers = new ArrayList<>();
            
            // Load all layers for this frame
            for (String layerFile : layerFiles) {
                // Try different frame naming conventions
                String[] framePaths = {
                    basePath + layerFile.replace(".png.png", "_" + frameIndex + ".png.png"),
                    basePath + layerFile.replace(".png.png", frameIndex + ".png.png"),
                    basePath + String.format("%03d", frameIndex) + "_" + layerFile,
                    basePath + frameIndex + "_" + layerFile,
                    basePath + layerFile // Some layers might be shared across frames
                };
                
                BufferedImage layer = null;
                for (String path : framePaths) {
                    layer = loadImage(path);
                    if (layer != null) {
                        break;
                    }
                }
                
                if (layer != null) {
                    frameLayers.add(layer);
                }
            }
            
            // Composite this frame
            if (!frameLayers.isEmpty()) {
                BufferedImage frame = compositeLayers(frameLayers, frameWidth, frameHeight);
                frames.add(frame);
            }
        }
        
        return frames;
    }
}
