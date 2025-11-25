package com.defendersofsolara.skills.ylonne;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class RicochetShotSkill extends Skill {
    public RicochetShotSkill() {
        name = "Ricochet Shot";
        manaCost = 75;
        cooldown = 2;
        description = "Hit all enemies";
        targetType = TargetType.ALL_ENEMIES;  // Auto-hit all enemies
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        System.out.println(user.name + " fires a ricochet shot!");
        for (Character target : targets) {
            if (target != null && target.isAlive()) {
                double basePercent = 0.75;
                double percent = scalePercent(user, basePercent, 0.01);
                int damage = (int) (user.currentAttack * percent);
                target.takeDamage(damage);
                System.out.println("  → " + target.name + " takes " + damage + " damage!");
            }
        }
        resetCooldown();
    }
}
