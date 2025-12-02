package com.defendersofsolara.skills.ka;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class AoeBlastSkill extends Skill {
    public AoeBlastSkill() {
        name = "AoE Blast";
        manaCost = 85;
        cooldown = 3;
        description = "High damage to all enemies";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double percent = scalePercent(user, 1.1, 0.015);
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

