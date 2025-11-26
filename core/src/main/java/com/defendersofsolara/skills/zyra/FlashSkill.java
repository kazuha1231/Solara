package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class FlashSkill extends Skill {
    public FlashSkill() {
        this.name = "Flash";
        this.manaCost = 65;
        this.cooldown = 2;
        this.description = "AoE damage + blind all enemies";
        this.targetType = TargetType.ALL_ENEMIES; // AOE - hits all enemies
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        System.out.println(user.name + " unleashes a blinding flash on all enemies!");
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                // Deal AOE damage (1.2x attack)
                int damage = (int) (user.currentAttack * 1.2);
                target.takeDamage(damage);
                // Apply blind effect
                target.applyEffect(new StatusEffect("blind", 15, 1));
                System.out.println("  → " + target.name + " takes " + damage + " damage and is blinded!");
            }
        }
        resetCooldown();
    }
}
