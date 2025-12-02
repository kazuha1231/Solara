package com.defendersofsolara.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration class that maps hero names to their sprite data.
 * Contains sprite paths, frame dimensions, frame counts, and animation delays.
 */
public class SpriteConfig {
    
    /**
     * Data class containing sprite configuration for a hero.
     */
    public static class SpriteData {
        public final String resourcePath;
        public final int frameWidth;
        public final int frameHeight;
        public final int frameCount;
        public final int frameDelayMs;
        public final String animationType; // "idle", "walk", etc.
        
        public SpriteData(String resourcePath, int frameWidth, int frameHeight, 
                         int frameCount, int frameDelayMs, String animationType) {
            this.resourcePath = resourcePath;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameCount = frameCount;
            this.frameDelayMs = frameDelayMs;
            this.animationType = animationType;
        }
    }
    
    /**
     * Map of hero class names to their sprite configurations.
     * Uses resource paths relative to the resources folder.
     */
    private static final Map<String, SpriteData> HERO_SPRITES = new HashMap<>();
    
    static {
        // Ka - AoE DPS / Self-Heal
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.Ka", 
            new SpriteData("/ka/standard/idle/", 64, 64, 12, 120, "idle"));
        
        // Aric Stoneward (ZyraKathelDraven) - Guardian Knight
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.ZyraKathelDraven", 
            new SpriteData("/zyra/standard/idle/", 64, 64, 12, 120, "idle"));
        
        // Lyra Stormgale - Runeblade Monk
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.Lyra", 
            new SpriteData("/lyra/standard/idle/", 64, 64, 27, 120, "idle"));
        
        // Ylonne Kryx - Assassin
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.YlonneKryx", 
            new SpriteData("/ylonne/standard/idle/", 64, 64, 16, 120, "idle"));
        
        // Seraphina Vale - Arcane Tactician
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.Seraphina", 
            new SpriteData("/serphina/standard/idle/", 64, 64, 16, 120, "idle"));
        
        // Dravik Thorn - Bruiser
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.DravikThorn", 
            new SpriteData("/dravik/standard/idle/", 64, 64, 14, 120, "idle"));
        
        // Kaelen Mirethorn - Shadow Ranger
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.Kaelen", 
            new SpriteData("/kaelen/standard/idle/", 64, 64, 12, 120, "idle"));
        
        // Orin Kaelus - Tank/Support
        HERO_SPRITES.put("com.defendersofsolara.characters.heroes.OrinKaelus", 
            new SpriteData("/orin/standard/idle/", 64, 64, 10, 120, "idle"));
    }
    
    /**
     * Gets the sprite data for a hero class name.
     * 
     * @param heroClass The fully qualified hero class name
     * @return SpriteData for the hero, or null if not found
     */
    public static SpriteData getSpriteData(String heroClass) {
        return HERO_SPRITES.get(heroClass);
    }
    
    /**
     * Gets the resource path for a hero class name.
     * 
     * @param heroClass The fully qualified hero class name
     * @return Resource path string, or null if not found
     */
    public static String getResourcePath(String heroClass) {
        SpriteData data = getSpriteData(heroClass);
        return data != null ? data.resourcePath : null;
    }
    
    /**
     * Checks if a hero class has sprite configuration.
     * 
     * @param heroClass The fully qualified hero class name
     * @return true if configuration exists, false otherwise
     */
    public static boolean hasSpriteData(String heroClass) {
        return HERO_SPRITES.containsKey(heroClass);
    }
    
    /**
     * Data class for layer information parsed from character.json.
     */
    public static class LayerInfo {
        public final String fileName;
        public final int zPos;
        public final List<String> supportedAnimations;
        
        public LayerInfo(String fileName, int zPos, List<String> supportedAnimations) {
            this.fileName = fileName;
            this.zPos = zPos;
            this.supportedAnimations = supportedAnimations;
        }
    }
    
    /**
     * Parses character.json file for a hero and extracts layer information for a specific animation.
     * Uses simple regex-based parsing to avoid external JSON dependencies.
     * 
     * @param heroFolderName Folder name of the hero (e.g., "ka", "dravik", "lyra")
     * @param animationName Name of the animation (e.g., "idle", "walk")
     * @return List of LayerInfo objects sorted by zPos, or empty list if parsing fails
     */
    public static List<LayerInfo> parseCharacterJson(String heroFolderName, String animationName) {
        List<LayerInfo> layers = new ArrayList<>();
        
        try {
            String jsonPath = "/" + heroFolderName + "/character.json";
            InputStream is = SpriteConfig.class.getResourceAsStream(jsonPath);
            
            if (is == null) {
                System.err.println("Could not find character.json at: " + jsonPath);
                return layers;
            }
            
            // Read JSON content using try-with-resources for automatic cleanup
            StringBuilder jsonContent = new StringBuilder();
            try (InputStream inputStream = is) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    jsonContent.append(new String(buffer, 0, bytesRead, "UTF-8"));
                }
            }
            
            String json = jsonContent.toString();
            
            // Pattern to match layer objects: {"itemId":..., "fileName":..., "zPos":..., "supportedAnimations":[...]}
            Pattern layerPattern = Pattern.compile(
                "\\{\\s*\"itemId\"[^}]*\"fileName\"\\s*:\\s*\"([^\"]+)\"[^}]*\"zPos\"\\s*:\\s*(\\d+)[^}]*\"supportedAnimations\"\\s*:\\s*\\[([^\\]]+)\\]",
                Pattern.DOTALL
            );
            
            Matcher layerMatcher = layerPattern.matcher(json);
            
            while (layerMatcher.find()) {
                String fileName = layerMatcher.group(1);
                int zPos = Integer.parseInt(layerMatcher.group(2));
                String animsStr = layerMatcher.group(3);
                
                // Parse supported animations
                List<String> anims = new ArrayList<>();
                Pattern animPattern = Pattern.compile("\"([^\"]+)\"");
                Matcher animMatcher = animPattern.matcher(animsStr);
                while (animMatcher.find()) {
                    anims.add(animMatcher.group(1));
                }
                
                // Check if this layer supports the requested animation
                boolean supportsAnimation = anims.contains(animationName) || 
                                          anims.contains("idle") || 
                                          anims.contains("walk");
                
                if (supportsAnimation) {
                    layers.add(new LayerInfo(fileName, zPos, anims));
                }
            }
            
            // Sort by zPos (lower zPos = drawn first/behind)
            Collections.sort(layers, Comparator.comparingInt(l -> l.zPos));
            
        } catch (Exception e) {
            System.err.println("Error parsing character.json for " + heroFolderName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return layers;
    }
    
    /**
     * Gets the hero folder name for a hero class.
     */
    public static String getHeroFolderNameFromClass(String heroClass) {
        // Extract hero name from class and map to folder
        if (heroClass.contains("Ka") && !heroClass.contains("Kaelen")) return "ka";
        if (heroClass.contains("ZyraKathelDraven")) return "zyra";
        if (heroClass.contains("Lyra")) return "lyra";
        if (heroClass.contains("Ylonne")) return "ylonne";
        if (heroClass.contains("Seraphina")) return "serphina";
        if (heroClass.contains("Dravik")) return "dravik";
        if (heroClass.contains("Kaelen")) return "kaelen";
        if (heroClass.contains("Orin")) return "orin";
        return "ka"; // Default fallback
    }
}

