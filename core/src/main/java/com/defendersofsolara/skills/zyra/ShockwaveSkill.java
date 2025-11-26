package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class ShockwaveSkill extends Skill {
    public ShockwaveSkill() {
        this.name = "Shockwave";
        this.manaCost = 95;
        this.cooldown = 3;
        this.description = "Huge AoE damage to all enemies (1.8x attack)";
        this.targetType = TargetType.ALL_ENEMIES; // AOE - hits all enemies
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        System.out.println(user.name + " unleashes a devastating shockwave!");
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                // Huge AOE damage - 1.8x attack (much higher than before)
                int damage = (int) (user.currentAttack * 1.8);
                target.takeDamage(damage);
                System.out.println("  → " + target.name + " takes " + damage + " massive damage!");
            }
        }
        resetCooldown();
    }
}
