package com.defendersofsolara.skills.viora;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class CodeBreakerUltimate extends Skill {
    public CodeBreakerUltimate() {
        name = "Code Breaker";
        manaCost = 180;
        cooldown = 8;
        description = "Confuse one enemy";
        targetType = TargetType.SINGLE_ENEMY;  // Click enemy to confuse
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        if (targets.length > 0 && targets[0] != null) {
            Character enemy = targets[0];
            int debuff = scaleAmount(user, 45, 4);
            enemy.applyEffect(new StatusEffect("debuff", debuff, 3));
            System.out.println(user.name + " disrupts " + enemy.name + " (-" + debuff + " ATK)!");
        }
        resetCooldown();
    }
}
