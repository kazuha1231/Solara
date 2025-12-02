package com.defendersofsolara.skills.seraphina;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class AscensionUltimate extends Skill {
    public AscensionUltimate() {
        name = "Mindbind Sigil";
        manaCost = 220;
        cooldown = 8;
        description = "Unleash a mindbinding sigil that massively heals and empowers all allies.";
        targetType = TargetType.ALL_ALLIES;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int healAmount = (int) (user.maxHP * 0.6);
        int buffAmount = scaleAmount(user, 100, 5);
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                ally.restoreHealth(healAmount);
                ally.applyEffect(new StatusEffect("buff", buffAmount, 3));
            }
        }
        resetCooldown();
    }
}

