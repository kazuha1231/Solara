package com.defendersofsolara.skills.orin;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class JudgmentOfSolaraUltimate extends Skill {
    public JudgmentOfSolaraUltimate() {
        name = "Abyssal Verdict";
        manaCost = 210;
        cooldown = 8;
        description = "Massive AoE damage + team buffs";
        targetType = TargetType.ALL_ENEMIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double damagePercent = scalePercent(user, 1.9, 0.028);
        // Damage all enemies
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                int damage = (int) (user.currentAttack * damagePercent);
                target.takeDamage(damage);
            }
        }
        // Also buff all allies (would need to get allies from battle system)
        resetCooldown();
    }
}

