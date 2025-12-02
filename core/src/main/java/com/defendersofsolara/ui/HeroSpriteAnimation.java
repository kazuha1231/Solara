package com.defendersofsolara.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles sprite animation for hero characters in the character selection screen.
 * 
 * This class loads stripe animations (spritesheets) or frame sequences from the
 * resources folder and manages animation playback triggered by mouse hover events.
 * 
 * Features:
 * - Automatic detection of best-fitting animation (idle, walk, run)
 * - Smooth frame cycling on hover
 * - Pause/resume animation control
 * - Frame caching for performance
 */
public class HeroSpriteAnimation {
    
    // Animation frame cache to avoid reloading images
    private static final Map<String, List<BufferedImage>> frameCache = new HashMap<>();
    
    // Default animation preferences - prioritize walk for forward-facing animation
    private static final String[] ANIMATION_PRIORITY = {"walk", "idle", "run", "combat"};
    
    // Animation settings
    private static final int FRAME_DELAY_MS = 150; // Milliseconds between frames
    private static final int DEFAULT_FRAME_WIDTH = 64; // Default sprite frame width
    private static final int DEFAULT_FRAME_HEIGHT = 64; // Default sprite frame height
    
    private List<BufferedImage> frames;
    private int currentFrameIndex = 0;
    private javax.swing.Timer animationTimer;
    private boolean isAnimating = false;
    private String heroResourcePath;
    private String selectedAnimation;
    private MetadataParser.AnimationMetadata metadata;
    
    /**
     * Creates a new HeroSpriteAnimation for the specified hero.
     * 
     * @param heroResourcePath The resource path to the hero's folder (e.g., "/lyra/")
     */
    public HeroSpriteAnimation(String heroResourcePath) {
        this.heroResourcePath = heroResourcePath;
        this.frames = new ArrayList<>();
        
        // Extract hero folder name for metadata parsing
        String heroFolder = heroResourcePath.replace("/", "").replace("\\", "");
        if (heroFolder.isEmpty()) {
            heroFolder = heroResourcePath.substring(1, heroResourcePath.length() - 1);
        }
        
        // Load metadata.json for this hero
        this.metadata = MetadataParser.parseMetadata(heroFolder);
        
        loadAnimation();
    }
    
    /**
     * Loads the best-fitting animation for this hero.
     * Tries animations in priority order until one is found.
     */
    private void loadAnimation() {
        String cacheKey = heroResourcePath + "_" + selectedAnimation;
        
        // Check cache first
        if (frameCache.containsKey(cacheKey)) {
            frames = frameCache.get(cacheKey);
            return;
        }
        
        // Try to find an available animation
        for (String animName : ANIMATION_PRIORITY) {
            List<BufferedImage> loadedFrames = loadAnimationFrames(animName);
            if (!loadedFrames.isEmpty()) {
                frames = loadedFrames;
                selectedAnimation = animName;
                frameCache.put(cacheKey, frames);
                System.out.println("✓ Loaded animation '" + animName + "' for " + heroResourcePath + " (" + frames.size() + " frames)");
                return;
            }
        }
        
        // If no animation found, create a placeholder
        System.err.println("⚠ No animation found for " + heroResourcePath + ", using placeholder");
        frames = createPlaceholderFrame();
    }
    
    /**
     * Attempts to load frames for a specific animation type.
     * Now uses metadata.json when available for accurate frame loading.
     * 
     * @param animationName The name of the animation (e.g., "idle", "walk")
     * @return List of BufferedImage frames, empty if not found
     */
    private List<BufferedImage> loadAnimationFrames(String animationName) {
        List<BufferedImage> frameList = new ArrayList<>();
        
        // Strategy 1: Use metadata.json if available (most accurate)
        if (metadata != null) {
            List<String> layerFiles = MetadataParser.getAnimationLayers(metadata, animationName);
            if (!layerFiles.isEmpty()) {
                frameList = compositeFramesFromMetadata(animationName, layerFiles);
                if (!frameList.isEmpty()) {
                    return frameList;
                }
            }
        }
        
        // Strategy 2: Try loading from Free folder (pre-composited sprites)
        frameList = loadFromFreeFolder(animationName);
        if (!frameList.isEmpty()) {
            return frameList;
        }
        
        // Strategy 3: Try loading from standard animation folder
        String animationPath = heroResourcePath + "/standard/" + animationName + "/";
        
        // First, try to load a spritesheet (stripe animation)
        BufferedImage spritesheet = loadSpritesheet(animationPath);
        if (spritesheet != null) {
            frameList = extractFramesFromSpritesheet(spritesheet);
            if (!frameList.isEmpty()) {
                return frameList;
            }
        }
        
        // Strategy 4: Try loading individual complete frame files
        // Look for numbered frames (0.png, 1.png, etc.) or sequential files
        for (int i = 0; i < 20; i++) { // Try up to 20 frames
            String framePath = animationPath + i + ".png";
            BufferedImage frame = loadImage(framePath);
            if (frame != null) {
                frameList.add(frame);
            } else {
                // Try alternative naming (000.png, 001.png, etc.)
                String altPath = animationPath + String.format("%03d", i) + ".png";
                BufferedImage altFrame = loadImage(altPath);
                if (altFrame != null) {
                    frameList.add(altFrame);
                } else {
                    // If we've found some frames but hit a gap, break
                    if (!frameList.isEmpty()) {
                        break;
                    }
                }
            }
        }
        
        // Strategy 5: Try compositing layered sprites using character.json
        if (frameList.isEmpty()) {
            frameList = compositeLayeredFramesFromJson(animationPath, animationName);
        }
        
        return frameList;
    }
    
    /**
     * Composites animation frames using layer files from metadata.json.
     * This ensures accurate frame composition based on the metadata.
     * 
     * @param animationName The animation name (e.g., "walk")
     * @param layerFiles List of layer filenames from metadata
     * @return List of composited frames
     */
    private List<BufferedImage> compositeFramesFromMetadata(String animationName, List<String> layerFiles) {
        List<BufferedImage> frames = new ArrayList<>();
        String animationPath = heroResourcePath + "/standard/" + animationName + "/";
        
        int frameSize = (metadata != null) ? metadata.frameSize : DEFAULT_FRAME_WIDTH;
        
        // Determine frame count - try to detect from first layer file
        int frameCount = 12; // Default LPC walk cycle has 9 frames, but we'll try up to 12
        if (metadata != null && metadata.frameCounts.containsKey(animationName)) {
            frameCount = metadata.frameCounts.get(animationName);
        } else {
            // Try to detect frame count from first layer file
            if (!layerFiles.isEmpty()) {
                String firstLayer = layerFiles.get(0);
                // Try both .png and .png.png extensions
                BufferedImage testSheet = loadImage(animationPath + firstLayer);
                if (testSheet == null && firstLayer.endsWith(".png")) {
                    // Try with double .png extension
                    testSheet = loadImage(animationPath + firstLayer + ".png");
                }
                if (testSheet != null && testSheet.getWidth() >= frameSize) {
                    frameCount = testSheet.getWidth() / frameSize;
                }
            }
        }
        
        // Composite frames by loading each layer and extracting frames
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            List<BufferedImage> frameLayers = new ArrayList<>();
            
            // Load each layer for this frame
            for (String layerFile : layerFiles) {
                // Try both .png and .png.png extensions (Lyra files use .png.png)
                String layerPath = animationPath + layerFile;
                BufferedImage layerSheet = loadImage(layerPath);
                
                // If not found, try with double .png extension
                if (layerSheet == null && layerFile.endsWith(".png")) {
                    layerPath = animationPath + layerFile + ".png";
                    layerSheet = loadImage(layerPath);
                }
                
                if (layerSheet != null) {
                    int sheetWidth = layerSheet.getWidth();
                    int sheetHeight = layerSheet.getHeight();
                    int framesInSheet = sheetWidth / frameSize;
                    
                    if (frameIndex < framesInSheet) {
                        int x = frameIndex * frameSize;
                        if (x + frameSize <= sheetWidth) {
                            BufferedImage frame = layerSheet.getSubimage(x, 0, frameSize, sheetHeight);
                            frameLayers.add(frame);
                        }
                    }
                }
            }
            
            // Composite all layers for this frame
            if (!frameLayers.isEmpty()) {
                BufferedImage composite = SpriteSheetUtil.compositeLayers(frameLayers, frameSize, frameSize);
                frames.add(composite);
            }
        }
        
        return frames;
    }
    
    /**
     * Attempts to load pre-composited frames from the Free folder.
     * The Free folder typically contains pre-rendered sprite frames.
     */
    private List<BufferedImage> loadFromFreeFolder(String animationName) {
        List<BufferedImage> frames = new ArrayList<>();
        String freePath = heroResourcePath + "/Free/";
        
        // Try to find frames in Free folder Part subdirectories
        // Each Part folder typically contains 12 frames
        for (int partNum = 16; partNum <= 36; partNum++) {
            String partFolder = freePath + "Part " + partNum + "/";
            
            // Common base frame numbers for different parts (based on observed patterns)
            int[] baseNumbers = {766, 825, 861, 925, 975, 1010, 1053, 1101, 1156, 1224, 1250, 1326, 1350, 1405, 1471, 1506, 1572};
            int partIndex = partNum - 16;
            
            if (partIndex < baseNumbers.length) {
                int baseNum = baseNumbers[partIndex];
                boolean foundFramesInPart = false;
                
                // Try to load up to 12 frames from this part
                for (int offset = 0; offset < 12; offset++) {
                    int frameNum = baseNum + offset;
                    String framePath = partFolder + frameNum + ".png";
                    BufferedImage frame = loadImage(framePath);
                    if (frame != null) {
                        frames.add(frame);
                        foundFramesInPart = true;
                    } else if (foundFramesInPart) {
                        // Found some frames but hit a gap, move to next part
                        break;
                    }
                }
                
                // If we found a good set of frames (at least 3), return them
                if (frames.size() >= 3) {
                    return frames;
                }
            }
        }
        
        // Alternative: Try to find spritesheets in Free folder
        String[] spritesheetPaths = {
            freePath + "spritesheet.png",
            freePath + "sheet.png",
            freePath + animationName + "_sheet.png",
            freePath + animationName + ".png"
        };
        
        for (String sheetPath : spritesheetPaths) {
            BufferedImage sheet = loadImage(sheetPath);
            if (sheet != null) {
                List<BufferedImage> extracted = extractFramesFromSpritesheet(sheet);
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }
        }
        
        return frames;
    }
    
    /**
     * Attempts to load a spritesheet image.
     * Looks for common spritesheet filenames.
     */
    private BufferedImage loadSpritesheet(String animationPath) {
        String[] spritesheetNames = {
            animationPath + "spritesheet.png",
            animationPath + "sheet.png",
            animationPath + animationPath.substring(animationPath.lastIndexOf("/", animationPath.length() - 2) + 1).replace("/", "") + ".png",
            animationPath + "all.png"
        };
        
        for (String path : spritesheetNames) {
            BufferedImage img = loadImage(path);
            if (img != null) {
                return img;
            }
        }
        
        return null;
    }
    
    /**
     * Extracts individual frames from a horizontal spritesheet (stripe animation).
     * Assumes frames are arranged horizontally.
     */
    private List<BufferedImage> extractFramesFromSpritesheet(BufferedImage spritesheet) {
        List<BufferedImage> frameList = new ArrayList<>();
        
        if (spritesheet == null) {
            return frameList;
        }
        
        int frameWidth = DEFAULT_FRAME_WIDTH;
        int frameHeight = DEFAULT_FRAME_HEIGHT;
        
        // Try to detect frame dimensions
        // Common LPC sprite dimensions: 64x64 per frame
        int sheetWidth = spritesheet.getWidth();
        int sheetHeight = spritesheet.getHeight();
        
        // If height matches standard frame height, assume horizontal stripe
        if (sheetHeight == DEFAULT_FRAME_HEIGHT || sheetHeight == DEFAULT_FRAME_HEIGHT * 2) {
            frameWidth = DEFAULT_FRAME_WIDTH;
            frameHeight = sheetHeight;
        } else if (sheetWidth == DEFAULT_FRAME_WIDTH || sheetWidth == DEFAULT_FRAME_WIDTH * 2) {
            // Vertical stripe
            frameWidth = sheetWidth;
            frameHeight = DEFAULT_FRAME_HEIGHT;
        } else {
            // Try to infer from dimensions
            frameWidth = sheetWidth / (sheetWidth / DEFAULT_FRAME_WIDTH);
            frameHeight = sheetHeight / (sheetHeight / DEFAULT_FRAME_HEIGHT);
        }
        
        // Extract frames horizontally
        int numFrames = sheetWidth / frameWidth;
        for (int i = 0; i < numFrames; i++) {
            int x = i * frameWidth;
            if (x + frameWidth <= sheetWidth) {
                BufferedImage frame = spritesheet.getSubimage(x, 0, frameWidth, frameHeight);
                frameList.add(frame);
            }
        }
        
        return frameList;
    }
    
    /**
     * Attempts to composite layered sprite frames using character.json.
     * Loads layer files from standard/idle/ folder and composites them.
     */
    private List<BufferedImage> compositeLayeredFramesFromJson(String animationPath, String animationName) {
        List<BufferedImage> frames = new ArrayList<>();
        
        try {
            // Extract hero folder name from resource path (e.g., "/ka/" -> "ka")
            String heroFolder = heroResourcePath.replace("/", "").replace("\\", "");
            if (heroFolder.isEmpty()) {
                return frames;
            }
            
            // Parse character.json to get layer information and z-order
            List<SpriteConfig.LayerInfo> layers = SpriteConfig.parseCharacterJson(heroFolder, animationName);
            
            if (layers.isEmpty()) {
                // Fallback: try to load files directly from folder by z-position
                return loadLayersByZPosition(animationPath);
            }
            
            // Load layer files - files are named with z-position prefix (010, 020, etc.)
            // We'll try to match layers to files by z-position
            // For now, use the fallback method that tries common z-positions
            return loadLayersByZPosition(animationPath);
            
        } catch (Exception e) {
            System.err.println("Error compositing layered frames: " + e.getMessage());
            e.printStackTrace();
            return loadLayersByZPosition(animationPath);
        }
    }
    
    /**
     * Loads and composites sprite layers by trying common z-position file patterns.
     * This is a fallback when character.json parsing doesn't work perfectly.
     */
    private List<BufferedImage> loadLayersByZPosition(String animationPath) {
        List<BufferedImage> frames = new ArrayList<>();
        
        // Common z-positions for LPC sprites (sorted by draw order)
        // Also include single-digit and two-digit formats
        String[] zPositions = {"0-1", "000", "005", "009", "010", "015", "020", "025", "035", "055", "060", "065", "070", "085", "090", "100", "101", "106", "110", "114", "120", "125", "126", "130", "139", "140"};
        
        List<BufferedImage> layerSheets = new ArrayList<>();
        int frameCount = 0;
        
        // Try to load layer files
        for (String zPos : zPositions) {
            // Try different file naming patterns - files are named like "010 body_color__light_.png.png"
            // So we need to try patterns that match the start of the filename
            String[] basePatterns = {
                animationPath + zPos + " ",  // "010 " - matches files starting with zPos and space
                animationPath + zPos + "_",  // "010_" - matches files starting with zPos and underscore
                animationPath + zPos         // "010" - exact match
            };
            
            // Common file name patterns found in the resources
            String[] descriptions = {
                "body_color", "long_pants", "basic_boots", "longsleeve_2_vneck", "longsleeve", 
                "obi", "gloves", "solid", "human_male", "shadow", "neutral", 
                "bicorne_athwart_commodore", "bicorne", "great", "weapon",
                "waraxe", "armour", "fur_pants", "leather", "bauldron", "bracers",
                "thick_eyebrows", "basic_beard"
            };
            
            for (String basePattern : basePatterns) {
                boolean found = false;
                
                // First try exact patterns with common suffixes
                String[] suffixes = {".png.png", ".png", "_0.png.png", "_0.png"};
                for (String suffix : suffixes) {
                    BufferedImage sheet = loadImage(basePattern + suffix);
                    if (sheet != null) {
                        layerSheets.add(sheet);
                        if (frameCount == 0 && sheet.getWidth() >= 64) {
                            frameCount = sheet.getWidth() / 64;
                        }
                        found = true;
                        break;
                    }
                }
                
                // If exact match failed, try with common descriptions
                // Files are named like "010 body_color__light_.png.png" or "009 waraxe__waraxe_.png.png"
                if (!found && basePattern.endsWith(" ")) {
                    for (String desc : descriptions) {
                        // Try the exact pattern: "zPos description__.png.png"
                        // Common variants: "__light_", "__green_", "__dark_", "__forest_", "__leather_", "__shadow_", "__olive_", "__iron_", "__waraxe_", "__black_", "__brown_"
                        String[] variants = {
                            "__light_", "__green_", "__dark_", "__forest_", "__leather_", "__shadow_",
                            "__olive_", "__iron_", "__waraxe_", "__black_", "__brown_", "__fur_black_"
                        };
                        for (String variant : variants) {
                            // Try pattern: "zPos description__variant_.png.png"
                            String pattern = basePattern + desc + variant + ".png.png";
                            BufferedImage sheet = loadImage(pattern);
                            if (sheet != null) {
                                layerSheets.add(sheet);
                                if (frameCount == 0 && sheet.getWidth() >= 64) {
                                    frameCount = sheet.getWidth() / 64;
                                }
                                found = true;
                                break;
                            }
                        }
                        // Also try pattern where description is repeated (e.g., "waraxe__waraxe_")
                        if (!found) {
                            String pattern = basePattern + desc + "__" + desc + "_.png.png";
                            BufferedImage sheet = loadImage(pattern);
                            if (sheet != null) {
                                layerSheets.add(sheet);
                                if (frameCount == 0 && sheet.getWidth() >= 64) {
                                    frameCount = sheet.getWidth() / 64;
                                }
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                }
                
                if (found) {
                    break; // Found a layer for this z-position, move to next
                }
            }
        }
        
        if (layerSheets.isEmpty() || frameCount == 0) {
            return frames;
        }
        
        // Composite frames from all layers
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            List<BufferedImage> frameLayers = new ArrayList<>();
            
            for (BufferedImage sheet : layerSheets) {
                int frameWidth = 64;
                int frameHeight = sheet.getHeight();
                int framesInSheet = sheet.getWidth() / frameWidth;
                
                if (frameIndex < framesInSheet) {
                    int x = frameIndex * frameWidth;
                    if (x + frameWidth <= sheet.getWidth()) {
                        BufferedImage frame = sheet.getSubimage(x, 0, frameWidth, frameHeight);
                        frameLayers.add(frame);
                    }
                }
            }
            
            if (!frameLayers.isEmpty()) {
                BufferedImage composite = SpriteSheetUtil.compositeLayers(frameLayers, 64, 64);
                frames.add(composite);
            }
        }
        
        return frames;
    }
    
    /**
     * Loads an image from the resource path.
     */
    private BufferedImage loadImage(String resourcePath) {
        try {
            // Ensure path starts with /
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                return null;
            }
            
            BufferedImage img = ImageIO.read(url);
            if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
                return img;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Creates a placeholder frame when no animation is found.
     */
    private List<BufferedImage> createPlaceholderFrame() {
        List<BufferedImage> placeholder = new ArrayList<>();
        BufferedImage frame = new BufferedImage(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = frame.createGraphics();
        FontRenderingUtil.applyTextRenderingHints(g2d);
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.drawString("?", DEFAULT_FRAME_WIDTH / 2 - 5, DEFAULT_FRAME_HEIGHT / 2 + 5);
        g2d.dispose();
        placeholder.add(frame);
        return placeholder;
    }
    
    /**
     * Starts the animation loop.
     * Called when mouse enters the hero panel.
     */
    public void startAnimation() {
        if (isAnimating || frames.isEmpty()) {
            return;
        }
        
        isAnimating = true;
        currentFrameIndex = 0;
        
        if (animationTimer == null) {
            animationTimer = new javax.swing.Timer(FRAME_DELAY_MS, e -> {
                if (frames.isEmpty()) {
                    return;
                }
                currentFrameIndex = (currentFrameIndex + 1) % frames.size();
            });
        }
        
        animationTimer.start();
    }
    
    /**
     * Stops the animation.
     * Called when mouse leaves the hero panel.
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        currentFrameIndex = 0; // Reset to first frame
    }
    
    /**
     * Gets the current frame to display.
     * Returns a placeholder image if no frames are available.
     */
    public BufferedImage getCurrentFrame() {
        if (frames == null || frames.isEmpty()) {
            // Return a placeholder image instead of null
            return createPlaceholderImage();
        }
        // Ensure currentFrameIndex is within bounds
        if (currentFrameIndex < 0 || currentFrameIndex >= frames.size()) {
            currentFrameIndex = 0;
        }
        return frames.get(currentFrameIndex);
    }
    
    /**
     * Creates a placeholder image when animation fails to load.
     */
    private BufferedImage createPlaceholderImage() {
        // Create a simple 64x64 gray square with "?" as placeholder
        BufferedImage placeholder = new BufferedImage(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        FontRenderingUtil.applyTextRenderingHints(g2d);
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRect(0, 0, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (DEFAULT_FRAME_WIDTH - fm.stringWidth("?")) / 2;
        int textY = (DEFAULT_FRAME_HEIGHT + fm.getAscent()) / 2;
        g2d.drawString("?", textX, textY);
        g2d.dispose();
        return placeholder;
    }
    
    /**
     * Gets the number of frames in this animation.
     */
    public int getFrameCount() {
        return frames.size();
    }
    
    /**
     * Checks if animation is currently playing.
     */
    public boolean isAnimating() {
        return isAnimating;
    }
    
    /**
     * Sets the animation timer to notify a component for repainting.
     */
    public void setRepaintCallback(Runnable callback) {
        if (animationTimer != null) {
            animationTimer.removeActionListener(animationTimer.getActionListeners()[0]);
        }
        
        animationTimer = new javax.swing.Timer(FRAME_DELAY_MS, e -> {
            if (frames.isEmpty()) {
                return;
            }
            currentFrameIndex = (currentFrameIndex + 1) % frames.size();
            if (callback != null) {
                callback.run();
            }
        });
    }
    
    /**
     * Cleans up resources.
     */
    public void dispose() {
        stopAnimation();
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }
}

