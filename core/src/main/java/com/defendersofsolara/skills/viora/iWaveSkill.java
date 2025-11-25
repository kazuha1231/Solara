package com.defendersofsolara.skills.viora;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class iWaveSkill extends Skill {
    public iWaveSkill() {
        name = "iWave";
        manaCost = 90;
        cooldown = 3;
        description = "Team heal + speed boost";
        targetType = TargetType.ALL_ALLIES;  // Auto-heal entire team
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                int heal = scaleAmount(user, 210, 15);
                int buff = scaleAmount(user, 28, 2);
                ally.restoreHealth(heal);
                ally.applyEffect(new StatusEffect("buff", buff, 2));
                System.out.println("  → " + ally.name + " heals " + heal + " HP and gains +" + buff + " stats!");
            }
        }
        resetCooldown();
    }
}

