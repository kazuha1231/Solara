package com.defendersofsolara.skills.ylonne;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class HuntersInstinctUltimate extends Skill {
    public HuntersInstinctUltimate() {
        name = "Hunter's Instinct";
        manaCost = 190;
        cooldown = 8;
        description = "Massive damage to one target";
        targetType = TargetType.SINGLE_ENEMY;  // Click enemy
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        if (targets.length > 0 && targets[0] != null) {
            double percent = scalePercent(user, 1.25, 0.02);
            int damage = (int) (user.currentAttack * percent);
            targets[0].takeDamage(damage);
            System.out.println("  → " + targets[0].name + " takes " + damage + " CRITICAL damage!");
        }
        resetCooldown();
    }
}
