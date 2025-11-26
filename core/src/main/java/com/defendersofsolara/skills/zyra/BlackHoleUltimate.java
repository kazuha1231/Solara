package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class BlackHoleUltimate extends Skill {
    public BlackHoleUltimate() {
        this.name = "Black Hole";
        this.manaCost = 220;
        this.cooldown = 8;
        this.description = "Ultimate AoE damage to all enemies (2.5x attack)";
        this.targetType = TargetType.ALL_ENEMIES; // AOE - hits all enemies
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        System.out.println(user.name + " creates a devastating black hole that consumes all enemies!");
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                // Ultimate AOE damage - 2.5x attack (massive damage)
                int damage = (int) (user.currentAttack * 2.5);
                target.takeDamage(damage);
                System.out.println("  → " + target.name + " is consumed by the void for " + damage + " catastrophic damage!");
            }
        }
        resetCooldown();
    }
}
