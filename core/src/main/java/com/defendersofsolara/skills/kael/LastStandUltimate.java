package com.defendersofsolara.skills.kael;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class LastStandUltimate extends Skill {
    public LastStandUltimate() {
        name = "Last Stand";
        manaCost = 200;
        cooldown = 8;
        description = "Cannot die for 1 turn";
        targetType = TargetType.SELF;  // Auto-cast on self
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int defenseBoost = scaleAmount(user, 140, 8);
        int heal = scaleAmount(user, 400, 18);
        user.restoreHealth(heal);
        user.applyEffect(new StatusEffect("buff", defenseBoost, 3));
        System.out.println(user.name + " enters Last Stand (+" + heal + " HP, +" + defenseBoost + " DEF)!");
        resetCooldown();
    }
}
