package com.defendersofsolara.skills.viora;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class HoloFieldSkill extends Skill {
    public HoloFieldSkill() {
        name = "Holo-Field";
        manaCost = 80;
        cooldown = 3;
        description = "Team evasion buff";
        targetType = TargetType.ALL_ALLIES;  // Auto-buff entire team
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                int defenseBoost = scaleAmount(user, 35, 3);
                ally.applyEffect(new StatusEffect("buff", defenseBoost, 2));
                System.out.println("  → " + ally.name + " gains Holo-Field (+" + defenseBoost + " DEF)!");
            }
        }
        resetCooldown();
    }
}
