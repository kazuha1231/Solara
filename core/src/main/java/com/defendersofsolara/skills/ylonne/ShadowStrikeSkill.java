package com.defendersofsolara.skills.ylonne;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class ShadowStrikeSkill extends Skill {
    public ShadowStrikeSkill() {
        name = "Shadow Strike";
        manaCost = 70;
        cooldown = 2;
        description = "High single-target damage";
        targetType = TargetType.SINGLE_ENEMY;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        if (targets.length > 0 && targets[0] != null) {
            double percent = scalePercent(user, 1.3, 0.018);
            int damage = (int) (user.currentAttack * percent);
            targets[0].takeDamage(damage);
        }
        resetCooldown();
    }
}

