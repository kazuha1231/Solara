package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;

public class ShockwaveSkill extends Skill {
    public ShockwaveSkill() {
        this.name = "Shockwave";
        this.manaCost = 95;
        this.cooldown = 3;
        this.description = "AoE damage 45-75%";
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        for (Character target : targets) {
            int damage = (int) (user.currentAttack * (0.45 + Math.random() * 0.3));
            target.takeDamage(damage);
            System.out.println("  → " + target.name + " takes " + damage + " damage!");
        }
        resetCooldown();
    }
}
