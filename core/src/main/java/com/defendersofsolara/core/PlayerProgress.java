package com.defendersofsolara.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks player level/experience and world unlock progression.
 */
public class PlayerProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    private int playerLevel = 1;
    private int currentExp = 0;
    private int expToNext = 150;
    private final Set<Integer> clearedWorlds = new HashSet<>();
    private boolean zyraUnlocked = false;
    private String profileName = "";
    private long totalPlaytimeSeconds = 0; // Total playtime in seconds

    // Level requirements per world (index 0 -> world 1)
    private static final int[] WORLD_LEVEL_REQ = {1, 3, 6, 9, 12};

    public int getPlayerLevel() {
        return playerLevel;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public int getExpToNext() {
        return expToNext;
    }

    public void addExp(int amount) {
        currentExp += Math.max(0, amount);
        while (currentExp >= expToNext && playerLevel < 50) {
            currentExp -= expToNext;
            playerLevel++;
            expToNext = Math.round(expToNext * 1.25f) + 50;
        }
        if (playerLevel >= 50) {
            playerLevel = 50;
            currentExp = 0;
        }
    }

    public int getWorldRequirement(int worldId) {
        if (worldId < 1 || worldId > WORLD_LEVEL_REQ.length) return Integer.MAX_VALUE;
        return WORLD_LEVEL_REQ[worldId - 1];
    }

    public boolean hasClearedWorld(int worldId) {
        return clearedWorlds.contains(worldId);
    }

    public void recordWorldClear(int worldId) {
        clearedWorlds.add(worldId);
    }

    public boolean canEnterWorld(int worldId) {
        if (worldId < 1 || worldId > WORLD_LEVEL_REQ.length) return false;
        boolean meetsLevel = playerLevel >= getWorldRequirement(worldId);
        boolean hasProgression = worldId == 1 || hasClearedWorld(worldId - 1);
        return meetsLevel && hasProgression;
    }

    public int getClearedWorldCount() {
        return clearedWorlds.size();
    }

    public boolean isZyraUnlocked() {
        return zyraUnlocked;
    }

    public void unlockZyra() {
        zyraUnlocked = true;
    }

    public void save(Path path) throws IOException {
        if (path == null) return;
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(this);
        }
    }

    public static PlayerProgress load(Path path) {
        if (path == null || !Files.exists(path)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof PlayerProgress) {
                return (PlayerProgress) obj;
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
        return null;
    }

    public String getProfileSummary() {
        return String.format("Lvl %d  |  EXP %d/%d", playerLevel, currentExp, expToNext);
    }
    
    public String getProfileName() {
        if (profileName == null || profileName.trim().isEmpty()) {
            // Generate default name based on progress
            if (clearedWorlds.isEmpty()) {
                return "NEW ADVENTURE";
            } else if (clearedWorlds.size() == 1) {
                return "FIRST STEPS";
            } else if (clearedWorlds.size() == 2) {
                return "RISING HERO";
            } else if (clearedWorlds.size() == 3) {
                return "HALFWAY HOME";
            } else if (clearedWorlds.size() == 4) {
                return "NEAR VICTORY";
            } else {
                return "LEGENDARY";
            }
        }
        return profileName;
    }
    
    public void setProfileName(String name) {
        this.profileName = name != null ? name.trim() : "";
    }
    
    public long getTotalPlaytimeSeconds() {
        return totalPlaytimeSeconds;
    }
    
    public void addPlaytime(long seconds) {
        totalPlaytimeSeconds += Math.max(0, seconds);
    }
    
    public String getFormattedPlaytime() {
        long totalSeconds = totalPlaytimeSeconds;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%dH %dM", hours, minutes);
        } else {
            return String.format("%dM", minutes);
        }
    }
}

