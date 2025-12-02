package com.defendersofsolara.skills.dravik;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class WrathUnleashedUltimate extends Skill {
    public WrathUnleashedUltimate() {
        name = "Wrath Unleashed";
        manaCost = 200;
        cooldown = 7;
        description = "Massive AoE + attack boost";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double damagePercent = scalePercent(user, 2.0, 0.03);
        int attackBoost = scaleAmount(user, 150, 8);
        user.applyEffect(new StatusEffect("buff", attackBoost, 5));
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * damagePercent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

