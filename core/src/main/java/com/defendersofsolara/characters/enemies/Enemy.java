package com.defendersofsolara.characters.enemies;

import com.defendersofsolara.core.Character;

public abstract class Enemy extends Character {
    public int experienceReward;
    public int goldReward;
    public String enemyType;

    public Enemy(String name, int hp, int mana, int attack, int defense, int speed, String enemyType) {
        super(name, hp, mana, attack, defense, speed);
        this.enemyType = enemyType;
        this.experienceReward = 100;
        this.goldReward = 50;
    }

    @Override
    public abstract void initializeSkills();

    public int getExperienceReward() {
        return experienceReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public String getEnemyType() {
        return enemyType;
    }
}
