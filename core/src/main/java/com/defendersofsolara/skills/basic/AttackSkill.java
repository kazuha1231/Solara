package com.defendersofsolara.skills.basic;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class AttackSkill extends Skill {
    public AttackSkill() {
        name = "Attack";
        manaCost = 0;
        cooldown = 0;
        description = "Basic attack";
        targetType = TargetType.SINGLE_ENEMY;  // Need to click enemy
    }

    @Override
    public void execute(Character user, Character[] targets) {
        if (targets.length > 0 && targets[0] != null) {
            int damage = user.currentAttack;
            targets[0].takeDamage(damage);
            System.out.println(user.name + " attacks " + targets[0].name + " for " + damage + " damage!");
        }
    }
}
