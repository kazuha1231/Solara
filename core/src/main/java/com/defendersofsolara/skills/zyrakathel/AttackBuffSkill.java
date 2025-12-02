package com.defendersofsolara.skills.zyrakathel;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class AttackBuffSkill extends Skill {
    public AttackBuffSkill() {
        name = "Battle Rage";
        manaCost = 70;
        cooldown = 3;
        description = "Boost attack power";
        targetType = TargetType.SELF;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int attackBoost = scaleAmount(user, 80, 4);
        user.applyEffect(new StatusEffect("buff", attackBoost, 3));
        resetCooldown();
    }
}

