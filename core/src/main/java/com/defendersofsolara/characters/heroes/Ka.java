package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.ka.*;

public class Ka extends Character {
    public Ka() {
        // High damage AoE specialist with self-heal (offensive caster / DPS)
        // Normal-difficulty balance: solid HP, strong attack, average defense, good speed
        super("Ka", 880, 660, 150, 65, 78);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new AoeBlastSkill());
        skills.add(new SelfHealSkill());
        skills.add(new HuntersInstinctUltimate());
    }
}

