package com.defendersofsolara.skills.zyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;

public class FlashSkill extends Skill {
    public FlashSkill() {
        this.name = "Flash";
        this.manaCost = 65;
        this.cooldown = 2;
        this.description = "Blind enemies (AoE)";
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        for (Character target : targets) {
            target.applyEffect(new StatusEffect("blind", 15, 1));
        }
        System.out.println(user.name + " blinds all enemies!");
        resetCooldown();
    }
}
