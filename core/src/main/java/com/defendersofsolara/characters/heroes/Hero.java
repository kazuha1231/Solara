package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;

public abstract class Hero extends Character {
    public int experience;
    public String heroClass;

    public Hero(String name, int hp, int mana, int attack, int defense, int speed, String heroClass) {
        super(name, hp, mana, attack, defense, speed);
        this.heroClass = heroClass;
        this.experience = 0;
    }

    @Override
    public abstract void initializeSkills();

    public void gainExp(int exp) {
        this.experience += exp;
        // Level up logic can be added here
        super.gainExp(exp);
    }

    public String getHeroClass() {
        return heroClass;
    }
}
