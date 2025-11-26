package com.defendersofsolara.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Character implements Serializable {
    public String name;
    public int maxHP, currentHP;
    public int maxMana, currentMana;
    public int level, maxLevel;
    public int baseAttack, baseDefense, baseSpeed;
    public int currentAttack, currentDefense, currentSpeed;
    public List<Skill> skills;
    public List<StatusEffect> activeEffects;
    public boolean isAlive;


    public Character(String name, int hp, int mana, int attack, int defense, int speed) {
        this.name = name;
        this.maxHP = currentHP = hp;
        this.maxMana = currentMana = mana;
        this.baseAttack = currentAttack = attack;
        this.baseDefense = currentDefense = defense;
        this.baseSpeed = currentSpeed = speed;
        this.level = 1;
        this.maxLevel = 30;
        this.skills = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.isAlive = true;
        initializeSkills();
    }

    public abstract void initializeSkills();

    public void takeDamage(int damage) {
        // Balanced damage formula: defense reduces damage by 40% (was 50%), minimum 1 damage
        int defenseReduction = (int) Math.round(currentDefense * 0.4);
        int actualDamage = Math.max(1, damage - defenseReduction);
        currentHP = Math.max(0, currentHP - actualDamage);
        if (currentHP == 0) isAlive = false;
    }

    public void restoreHealth(int amount) {
        currentHP = Math.min(maxHP, currentHP + amount);
    }

    public void restoreMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    public void gainExp(int exp) {
        if (level < maxLevel) {
            levelUpStats();
        }
    }

    protected void levelUpStats() {
        if (level >= maxLevel) return;
        level++;
        maxHP += 55;  // Increased from 40 - heroes get more HP per level
        maxMana += 40; // Increased from 30 - heroes get more mana per level
        baseAttack += 9; // Increased from 6 - heroes get more attack per level
        baseDefense += 7; // Increased from 4 - heroes get more defense per level
        baseSpeed += 1;
        currentHP = maxHP;
        currentMana = maxMana;
        currentAttack = baseAttack;
        currentDefense = baseDefense;
        currentSpeed = baseSpeed;
    }

    public void syncToLevel(int targetLevel) {
        targetLevel = Math.max(1, Math.min(targetLevel, maxLevel));
        while (level < targetLevel) {
            levelUpStats();
        }
    }

    public void applyStatMultiplier(double hpMult, double manaMult, double attackMult, double defenseMult) {
        maxHP = (int) Math.round(maxHP * hpMult);
        currentHP = maxHP;
        maxMana = (int) Math.round(maxMana * manaMult);
        currentMana = maxMana;
        baseAttack = currentAttack = (int) Math.round(baseAttack * attackMult);
        baseDefense = currentDefense = (int) Math.round(baseDefense * defenseMult);
    }

    public void applyEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    public void updateEffects() {
        activeEffects.removeIf(effect -> {
            effect.duration--;
            return effect.duration <= 0;
        });
    }

    public void resetStats() {
        currentAttack = baseAttack;
        currentDefense = baseDefense;
        currentSpeed = baseSpeed;
        for (StatusEffect effect : activeEffects) {
            if (effect.type.equals("buff")) {
                currentAttack += effect.value;
                currentDefense += effect.value;
            } else if (effect.type.equals("debuff")) {
                currentAttack -= effect.value;
            }
        }
    }

    public Skill getSkill(int index) {
        return index >= 0 && index < skills.size() ? skills.get(index) : null;
    }

    public String getStatus() {
        return String.format("%s | Lvl: %d | HP: %d/%d | Mana: %d/%d",
                name, level, currentHP, maxHP, currentMana, maxMana);
    }

    public boolean isAlive() {
        return isAlive && currentHP > 0;
    }
}
