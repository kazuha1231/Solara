package com.defendersofsolara.skills.orin;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class DivineProtectionSkill extends Skill {
    public DivineProtectionSkill() {
        name = "Divine Protection";
        manaCost = 90;
        cooldown = 4;
        description = "Shield + heal all allies";
        targetType = TargetType.ALL_ALLIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int healAmount = scaleAmount(user, 180, 10);
        int barrier = scaleAmount(user, 150, 8);
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                ally.restoreHealth(healAmount + barrier);
                ally.applyEffect(new StatusEffect("buff", 60, 2));
            }
        }
        resetCooldown();
    }
}

