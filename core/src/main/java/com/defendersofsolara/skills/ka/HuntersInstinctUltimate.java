package com.defendersofsolara.skills.ka;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class HuntersInstinctUltimate extends Skill {
    public HuntersInstinctUltimate() {
        name = "Hunter's Instinct";
        manaCost = 200;
        cooldown = 6;
        description = "Massive AoE damage to all enemies";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double percent = scalePercent(user, 1.8, 0.025);
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

