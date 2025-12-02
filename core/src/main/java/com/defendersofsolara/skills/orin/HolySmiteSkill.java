package com.defendersofsolara.skills.orin;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class HolySmiteSkill extends Skill {
    public HolySmiteSkill() {
        name = "Holy Smite";
        manaCost = 85;
        cooldown = 3;
        description = "AoE holy damage to all enemies";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double percent = scalePercent(user, 1.15, 0.016);
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

