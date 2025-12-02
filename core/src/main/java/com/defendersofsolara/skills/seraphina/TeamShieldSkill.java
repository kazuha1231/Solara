package com.defendersofsolara.skills.seraphina;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class TeamShieldSkill extends Skill {
    public TeamShieldSkill() {
        name = "Illusory Veil";
        manaCost = 95;
        cooldown = 5;
        description = "Wrap all allies in an illusory veil, shielding them from harm.";
        targetType = TargetType.ALL_ALLIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int barrier = scaleAmount(user, 200, 10);
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                ally.restoreHealth(barrier);
                ally.applyEffect(new StatusEffect("buff", 50, 2));
            }
        }
        resetCooldown();
    }
}

