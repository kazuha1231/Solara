package com.defendersofsolara.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for metadata.json files that contain animation frame information.
 * 
 * This class extracts animation data from metadata.json files located in
 * character resource folders (e.g., /dravik/credits/metadata.json).
 * 
 * The metadata.json structure contains:
 * - standardAnimations.exported: Lists of layer filenames for each animation type
 * - frameSize: Size of each frame (typically 64)
 * - frameCounts: Number of frames per animation (if available)
 */
public class MetadataParser {
    
    private static final Gson GSON = new Gson();
    private static final Map<String, AnimationMetadata> CACHE = new HashMap<>();
    
    /**
     * Data class containing parsed animation metadata for a character.
     */
    public static class AnimationMetadata {
        public final int frameSize;
        public final Map<String, List<String>> walkLayers;
        public final Map<String, List<String>> idleLayers;
        public final Map<String, List<String>> runLayers;
        public final Map<String, Integer> frameCounts;
        
        public AnimationMetadata(int frameSize, Map<String, List<String>> walkLayers,
                                Map<String, List<String>> idleLayers,
                                Map<String, List<String>> runLayers,
                                Map<String, Integer> frameCounts) {
            this.frameSize = frameSize;
            this.walkLayers = walkLayers;
            this.idleLayers = idleLayers;
            this.runLayers = runLayers;
            this.frameCounts = frameCounts;
        }
    }
    
    /**
     * Parses metadata.json for a character and returns animation metadata.
     * Results are cached for performance.
     * 
     * @param heroFolderName Folder name of the hero (e.g., "dravik", "lyra", "orin")
     * @return AnimationMetadata containing frame data, or null if parsing fails
     */
    public static AnimationMetadata parseMetadata(String heroFolderName) {
        String cacheKey = heroFolderName.toLowerCase();
        
        // Check cache first
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        
        try {
            // Try credits/metadata.json first (standard location)
            String metadataPath = "/" + heroFolderName + "/credits/metadata.json";
            InputStream is = MetadataParser.class.getResourceAsStream(metadataPath);
            
            if (is == null) {
                // Fallback: try metadata.json in root of hero folder
                metadataPath = "/" + heroFolderName + "/metadata.json";
                is = MetadataParser.class.getResourceAsStream(metadataPath);
            }
            
            if (is == null) {
                System.err.println("Could not find metadata.json for " + heroFolderName);
                return null;
            }
            
            // Use try-with-resources for automatic stream cleanup
            JsonObject root;
            try (InputStream inputStream = is;
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                root = GSON.fromJson(reader, JsonObject.class);
            }
            
            // Extract frame size
            int frameSize = 64; // Default
            if (root.has("frameSize")) {
                frameSize = root.get("frameSize").getAsInt();
            }
            
            // Extract frame counts if available
            Map<String, Integer> frameCounts = new HashMap<>();
            if (root.has("frameCounts")) {
                JsonObject frameCountsObj = root.getAsJsonObject("frameCounts");
                for (Map.Entry<String, JsonElement> entry : frameCountsObj.entrySet()) {
                    frameCounts.put(entry.getKey(), entry.getValue().getAsInt());
                }
            }
            
            // Extract standard animations
            Map<String, List<String>> walkLayers = new HashMap<>();
            Map<String, List<String>> idleLayers = new HashMap<>();
            Map<String, List<String>> runLayers = new HashMap<>();
            
            if (root.has("standardAnimations")) {
                JsonObject standardAnimations = root.getAsJsonObject("standardAnimations");
                
                if (standardAnimations.has("exported")) {
                    JsonObject exported = standardAnimations.getAsJsonObject("exported");
                    
                    // Extract walk animation layers
                    if (exported.has("walk")) {
                        walkLayers = extractLayerList(exported.getAsJsonArray("walk"));
                    }
                    
                    // Extract idle animation layers
                    if (exported.has("idle")) {
                        idleLayers = extractLayerList(exported.getAsJsonArray("idle"));
                    }
                    
                    // Extract run animation layers
                    if (exported.has("run")) {
                        runLayers = extractLayerList(exported.getAsJsonArray("run"));
                    }
                }
            }
            
            AnimationMetadata metadata = new AnimationMetadata(frameSize, walkLayers, idleLayers, runLayers, frameCounts);
            CACHE.put(cacheKey, metadata);
            
            System.out.println("✓ Parsed metadata.json for " + heroFolderName + 
                             " (frameSize: " + frameSize + 
                             ", walk layers: " + walkLayers.size() + 
                             ", idle layers: " + idleLayers.size() + ")");
            
            return metadata;
            
        } catch (Exception e) {
            System.err.println("Error parsing metadata.json for " + heroFolderName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extracts layer filenames from a JSON array.
     * Converts the array to a map for easier lookup.
     */
    private static Map<String, List<String>> extractLayerList(JsonArray array) {
        Map<String, List<String>> layers = new HashMap<>();
        List<String> layerList = new ArrayList<>();
        
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                String fileName = element.getAsString();
                layerList.add(fileName);
                // Also index by z-position prefix (e.g., "010", "020")
                if (fileName.length() >= 3 && Character.isDigit(fileName.charAt(0))) {
                    String zPos = fileName.substring(0, 3);
                    layers.putIfAbsent(zPos, new ArrayList<>());
                    layers.get(zPos).add(fileName);
                }
            }
        }
        
        // Store full list under "all" key
        layers.put("all", layerList);
        
        return layers;
    }
    
    /**
     * Gets the list of layer filenames for a specific animation type.
     * 
     * @param metadata The parsed metadata
     * @param animationType Animation type ("walk", "idle", "run")
     * @return List of layer filenames, or empty list if not found
     */
    public static List<String> getAnimationLayers(AnimationMetadata metadata, String animationType) {
        if (metadata == null) {
            return new ArrayList<>();
        }
        
        Map<String, List<String>> layers;
        switch (animationType.toLowerCase()) {
            case "walk":
                layers = metadata.walkLayers;
                break;
            case "idle":
                layers = metadata.idleLayers;
                break;
            case "run":
                layers = metadata.runLayers;
                break;
            default:
                return new ArrayList<>();
        }
        
        return layers.getOrDefault("all", new ArrayList<>());
    }
    
    /**
     * Clears the metadata cache.
     * Useful for reloading metadata after changes.
     */
    public static void clearCache() {
        CACHE.clear();
    }
}

