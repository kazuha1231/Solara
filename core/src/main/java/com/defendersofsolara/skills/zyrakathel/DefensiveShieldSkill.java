package com.defendersofsolara.skills.zyrakathel;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class DefensiveShieldSkill extends Skill {
    public DefensiveShieldSkill() {
        name = "Defensive Shield";
        manaCost = 80;
        cooldown = 4;
        description = "Self shield + defense boost";
        targetType = TargetType.SELF;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int defenseBoost = scaleAmount(user, 100, 5);
        int barrier = scaleAmount(user, 250, 15);
        user.restoreHealth(barrier / 2);
        user.applyEffect(new StatusEffect("buff", defenseBoost, 3));
        resetCooldown();
    }
}

