package com.defendersofsolara.skills.ylonne;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class SilentStepSkill extends Skill {
    public SilentStepSkill() {
        name = "Silent Step";
        manaCost = 70;
        cooldown = 3;
        description = "Self heal + evasion";
        targetType = TargetType.SELF;  // Auto-cast on self
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        int heal = scaleAmount(user, 180, 12);
        int buff = scaleAmount(user, 30, 3);
        user.restoreHealth(heal);
        user.applyEffect(new StatusEffect("buff", buff, 2));
        System.out.println(user.name + " heals " + heal + " HP and sharpens instincts (+" + buff + ")!");
        resetCooldown();
    }
}


