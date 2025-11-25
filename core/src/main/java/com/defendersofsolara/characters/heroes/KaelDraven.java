package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.kael.*;

public class KaelDraven extends Character {
    public KaelDraven() {
        // Front-line tank: lower base HP, modest mana pool
        super("Kael Draven", 900, 500, 120, 85, 52);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new ShieldWallSkill());
        skills.add(new OverdriveBurstSkill());
        skills.add(new LastStandUltimate());
    }
}
