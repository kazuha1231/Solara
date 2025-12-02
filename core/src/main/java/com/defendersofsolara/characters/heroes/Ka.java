package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.ka.*;

public class Ka extends Character {
    public Ka() {
        // High damage AoE specialist with self-heal
        super("Ka", 850, 650, 145, 65, 75);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new AoeBlastSkill());
        skills.add(new SelfHealSkill());
        skills.add(new HuntersInstinctUltimate());
    }
}

