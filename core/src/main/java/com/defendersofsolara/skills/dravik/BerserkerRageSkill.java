package com.defendersofsolara.skills.dravik;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class BerserkerRageSkill extends Skill {
    public BerserkerRageSkill() {
        name = "Berserker Rage";
        manaCost = 75;
        cooldown = 3;
        description = "Boost attack power significantly";
        targetType = TargetType.SELF;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int attackBoost = scaleAmount(user, 100, 5);
        user.applyEffect(new StatusEffect("buff", attackBoost, 4));
        resetCooldown();
    }
}

