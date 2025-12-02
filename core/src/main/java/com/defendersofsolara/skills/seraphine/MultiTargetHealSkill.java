package com.defendersofsolara.skills.seraphine;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class MultiTargetHealSkill extends Skill {
    public MultiTargetHealSkill() {
        name = "Arcane Wind";
        manaCost = 110;
        cooldown = 4;
        description = "Call forth an arcane wind that restores health to all allies.";
        targetType = TargetType.ALL_ALLIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double healPercent = scalePercent(user, 0.25, 0.005);
        int healAmount = (int) (user.maxHP * healPercent);
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                ally.restoreHealth(healAmount);
            }
        }
        resetCooldown();
    }
}

