package com.defendersofsolara.skills.ka;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class SelfHealSkill extends Skill {
    public SelfHealSkill() {
        name = "Self Heal";
        manaCost = 60;
        cooldown = 3;
        description = "Heal self for 30-40% max HP";
        targetType = TargetType.SELF;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        double healPercent = scalePercent(user, 0.30, 0.0033); // 30% to 40% over 30 levels
        int healAmount = (int) (user.maxHP * healPercent);
        user.restoreHealth(healAmount);
        resetCooldown();
    }
}

