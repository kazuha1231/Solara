package com.defendersofsolara.skills.kael;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class OverdriveBurstSkill extends Skill {
    public OverdriveBurstSkill() {
        name = "Overdrive Burst";
        manaCost = 70;
        cooldown = 3;
        description = "Self attack boost";
        targetType = TargetType.SELF;  // Auto-cast on self
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int attackBoost = scaleAmount(user, 45, 5);
        int defenseBoost = scaleAmount(user, 15, 2);
        user.applyEffect(new StatusEffect("buff", attackBoost, 3));
        user.currentDefense += defenseBoost;
        System.out.println(user.name + " enters Overdrive (+"
            + attackBoost + " ATK / +" + defenseBoost + " DEF)!");
        resetCooldown();
    }
}
