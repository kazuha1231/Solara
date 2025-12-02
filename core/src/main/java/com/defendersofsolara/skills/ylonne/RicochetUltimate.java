package com.defendersofsolara.skills.ylonne;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class RicochetUltimate extends Skill {
    public RicochetUltimate() {
        name = "Ricochet";
        manaCost = 180;
        cooldown = 6;
        description = "Ultimate: Massive damage to all enemies";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double percent = scalePercent(user, 1.6, 0.025);
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

