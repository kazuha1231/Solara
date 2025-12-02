package com.defendersofsolara.skills.nyx;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class StealthSkill extends Skill {
    public StealthSkill() {
        name = "Stealth";
        manaCost = 80;
        cooldown = 4;
        description = "Become untargetable + speed boost";
        targetType = TargetType.SELF;
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        // Stealth effect - high evasion/untargetable
        int speedBoost = scaleAmount(user, 50, 3);
        user.applyEffect(new StatusEffect("buff", speedBoost, 2));
        // Note: Untargetable would need special handling in battle system
        resetCooldown();
    }
}

