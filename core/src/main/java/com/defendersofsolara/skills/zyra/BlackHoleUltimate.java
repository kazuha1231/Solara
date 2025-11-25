package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;

public class BlackHoleUltimate extends Skill {
    public BlackHoleUltimate() {
        this.name = "Black Hole";
        this.manaCost = 220;
        this.cooldown = 8;
        this.description = "AoE damage 55-95%";
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        for (Character target : targets) {
            int damage = (int) (user.currentAttack * (0.55 + Math.random() * 0.4));
            target.takeDamage(damage);
            System.out.println("  → " + target.name + " takes " + damage + " damage!");
        }
        resetCooldown();
    }
}
