package com.defendersofsolara.skills.zyrakathel;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class LastStandUltimate extends Skill {
    public LastStandUltimate() {
        name = "Last Stand";
        manaCost = 180;
        cooldown = 7;
        description = "Massive damage + defense boost";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double damagePercent = scalePercent(user, 1.5, 0.02);
        int defenseBoost = scaleAmount(user, 120, 6);
        user.applyEffect(new StatusEffect("buff", defenseBoost, 4));
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * damagePercent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

