package com.defendersofsolara.skills.kael;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class ShieldWallSkill extends Skill {
    public ShieldWallSkill() {
        name = "Shield Wall";
        manaCost = 90;
        cooldown = 4;
        description = "Self shield";
        targetType = TargetType.SELF;  // Auto-cast on self
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int defenseBoost = scaleAmount(user, 90, 4);
        int barrier = scaleAmount(user, 220, 12);
        user.restoreHealth(barrier / 2);
        user.applyEffect(new StatusEffect("buff", defenseBoost, 2));
        System.out.println(user.name + " raises a Shield Wall (+" + defenseBoost + " DEF, +" + barrier / 2 + " HP)!");
        resetCooldown();
    }
}
