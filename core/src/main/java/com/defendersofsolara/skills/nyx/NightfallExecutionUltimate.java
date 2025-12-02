package com.defendersofsolara.skills.nyx;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class NightfallExecutionUltimate extends Skill {
    public NightfallExecutionUltimate() {
        name = "Nightfall Execution";
        manaCost = 190;
        cooldown = 6;
        description = "Massive single-target damage";
        targetType = TargetType.SINGLE_ENEMY;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        if (targets.length > 0 && targets[0] != null) {
            double percent = scalePercent(user, 2.5, 0.035);
            int damage = (int) (user.currentAttack * percent);
            targets[0].takeDamage(damage);
        }
        resetCooldown();
    }
}

