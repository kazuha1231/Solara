package com.defendersofsolara.core;

public abstract class Skill {
    public String name;
    public int manaCost;
    public int cooldown;
    public int currentCooldown = 0;
    public String description;
    protected TargetType targetType = TargetType.SINGLE_ENEMY; // Default

    public abstract void execute(Character user, Character[] targets);

    public boolean canUse(Character user) {
        return currentCooldown == 0 && user.currentMana >= manaCost;
    }

    public void reduceCooldown() {
        if (currentCooldown > 0) currentCooldown--;
    }

    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    public TargetType getTargetType() {
        return targetType;
    }


    public int getManaCost() {
        return manaCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public String getDescription() {
        return description;
    }

    public String getInfo() {
        return String.format("%s (Cost: %d | CD: %d/%d) - %s",
                name, manaCost, currentCooldown, cooldown, description);
    }

    public String getName() {
        return name;
    }

    protected int scaleAmount(Character user, double base, double perLevel) {
        if (user == null) return (int) Math.round(base);
        return (int) Math.round(base + perLevel * Math.max(0, user.level - 1));
    }

    protected double scalePercent(Character user, double basePercent, double perLevelPercent) {
        if (user == null) return basePercent;
        return basePercent + perLevelPercent * Math.max(0, user.level - 1);
    }
}
