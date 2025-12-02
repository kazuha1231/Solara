package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.seraphina.*;

public class Seraphina extends Character {
    public Seraphina() {
        // Seraphina Vale — Arcane Tactician (Support/Debuff)
        // Backline support with shields, healing, and battlefield control
        super("Seraphina Vale", 820, 780, 90, 78, 68);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new MultiTargetHealSkill());
        skills.add(new TeamShieldSkill());
        skills.add(new AscensionUltimate());
    }
}

