package com.defendersofsolara.skills.nyx;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class ShadowStrikeSkill extends Skill {
    public ShadowStrikeSkill() {
        name = "Rebound Arrow";
        manaCost = 65;
        cooldown = 2;
        description = "Loose a deadly arrow that strikes a single target with precision.";
        targetType = TargetType.SINGLE_ENEMY;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        if (targets.length > 0 && targets[0] != null) {
            double percent = scalePercent(user, 1.4, 0.02);
            int damage = (int) (user.currentAttack * percent);
            targets[0].takeDamage(damage);
        }
        resetCooldown();
    }
}

