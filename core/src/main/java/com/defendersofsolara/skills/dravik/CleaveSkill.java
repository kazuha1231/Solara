package com.defendersofsolara.skills.dravik;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class CleaveSkill extends Skill {
    public CleaveSkill() {
        name = "Cleave";
        manaCost = 90;
        cooldown = 3;
        description = "AoE damage to all enemies";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double percent = scalePercent(user, 1.2, 0.018);
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
            }
        }
        resetCooldown();
    }
}

