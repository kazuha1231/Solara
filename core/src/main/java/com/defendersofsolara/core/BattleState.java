package com.defendersofsolara.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves the current battle state so players can resume battles after loading.
 */
public class BattleState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int worldId;
    private int activeWaveIndex;
    private int currentPlayerIndex;
    
    // Player team state - save character data
    private List<CharacterData> playerTeamData;
    
    // Enemy team state - save character data
    private List<CharacterData> enemyTeamData;
    
    // Wave plan - save which waves remain
    private List<WaveData> wavePlan;
    
    public BattleState() {
        playerTeamData = new ArrayList<>();
        enemyTeamData = new ArrayList<>();
        wavePlan = new ArrayList<>();
    }
    
    public int getWorldId() {
        return worldId;
    }
    
    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }
    
    public int getActiveWaveIndex() {
        return activeWaveIndex;
    }
    
    public void setActiveWaveIndex(int activeWaveIndex) {
        this.activeWaveIndex = activeWaveIndex;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }
    
    public List<CharacterData> getPlayerTeamData() {
        return playerTeamData;
    }
    
    public void setPlayerTeamData(List<CharacterData> playerTeamData) {
        this.playerTeamData = playerTeamData;
    }
    
    public List<CharacterData> getEnemyTeamData() {
        return enemyTeamData;
    }
    
    public void setEnemyTeamData(List<CharacterData> enemyTeamData) {
        this.enemyTeamData = enemyTeamData;
    }
    
    public List<WaveData> getWavePlan() {
        return wavePlan;
    }
    
    public void setWavePlan(List<WaveData> wavePlan) {
        this.wavePlan = wavePlan;
    }
    
    /**
     * Lightweight serializable character data (avoids serializing full Character objects with skills)
     */
    public static class CharacterData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private String className; // Hero class name or enemy class name
        private int level;
        private int maxHP;
        private int currentHP;
        private int maxMana;
        private int currentMana;
        private int baseAttack;
        private int baseDefense;
        private int baseSpeed;
        private int currentAttack;
        private int currentDefense;
        private int currentSpeed;
        private boolean isAlive;
        
        public CharacterData() {}
        
        public CharacterData(Character c) {
            this.name = c.name;
            this.className = c.getClass().getName();
            this.level = c.level;
            this.maxHP = c.maxHP;
            this.currentHP = c.currentHP;
            this.maxMana = c.maxMana;
            this.currentMana = c.currentMana;
            this.baseAttack = c.baseAttack;
            this.baseDefense = c.baseDefense;
            this.baseSpeed = c.baseSpeed;
            this.currentAttack = c.currentAttack;
            this.currentDefense = c.currentDefense;
            this.currentSpeed = c.currentSpeed;
            this.isAlive = c.isAlive;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        
        public int getMaxHP() { return maxHP; }
        public void setMaxHP(int maxHP) { this.maxHP = maxHP; }
        
        public int getCurrentHP() { return currentHP; }
        public void setCurrentHP(int currentHP) { this.currentHP = currentHP; }
        
        public int getMaxMana() { return maxMana; }
        public void setMaxMana(int maxMana) { this.maxMana = maxMana; }
        
        public int getCurrentMana() { return currentMana; }
        public void setCurrentMana(int currentMana) { this.currentMana = currentMana; }
        
        public int getBaseAttack() { return baseAttack; }
        public void setBaseAttack(int baseAttack) { this.baseAttack = baseAttack; }
        
        public int getBaseDefense() { return baseDefense; }
        public void setBaseDefense(int baseDefense) { this.baseDefense = baseDefense; }
        
        public int getBaseSpeed() { return baseSpeed; }
        public void setBaseSpeed(int baseSpeed) { this.baseSpeed = baseSpeed; }
        
        public int getCurrentAttack() { return currentAttack; }
        public void setCurrentAttack(int currentAttack) { this.currentAttack = currentAttack; }
        
        public int getCurrentDefense() { return currentDefense; }
        public void setCurrentDefense(int currentDefense) { this.currentDefense = currentDefense; }
        
        public int getCurrentSpeed() { return currentSpeed; }
        public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
        
        public boolean isAlive() { return isAlive; }
        public void setAlive(boolean alive) { isAlive = alive; }
    }
    
    /**
     * Wave data for serialization
     */
    public static class WaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int waveNumber;
        private boolean bossWave;
        private List<CharacterData> enemies;
        
        public WaveData() {
            enemies = new ArrayList<>();
        }
        
        public WaveData(int waveNumber, boolean bossWave, List<CharacterData> enemies) {
            this.waveNumber = waveNumber;
            this.bossWave = bossWave;
            this.enemies = enemies;
        }
        
        public int getWaveNumber() { return waveNumber; }
        public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
        
        public boolean isBossWave() { return bossWave; }
        public void setBossWave(boolean bossWave) { this.bossWave = bossWave; }
        
        public List<CharacterData> getEnemies() { return enemies; }
        public void setEnemies(List<CharacterData> enemies) { this.enemies = enemies; }
    }
}

