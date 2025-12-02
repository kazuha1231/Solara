package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.seraphine.*;

public class SeraphineDrael extends Character {
    public SeraphineDrael() {
        // Healer/Support: multi-target heal, shields, Ultimate: Ascension
        super("Seraphine Drael", 800, 750, 90, 75, 68);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new MultiTargetHealSkill());
        skills.add(new TeamShieldSkill());
        skills.add(new AscensionUltimate());
    }
}

