package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.dravik.*;

public class DravikThorn extends Character {
    public DravikThorn() {
        // Bruiser: attack buff & AoE, Ultimate: Wrath Unleashed
        super("Dravik Thorn", 950, 580, 140, 85, 68);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new BerserkerRageSkill());
        skills.add(new CleaveSkill());
        skills.add(new WrathUnleashedUltimate());
    }
}

