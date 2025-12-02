package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.dravik.*;

public class DravikThorn extends Character {
    public DravikThorn() {
        // Bruiser: attack buff & AoE, Ultimate: Wrath Unleashed
        // Normal-difficulty balance: durable frontline damage dealer
        super("Dravik Thorn", 980, 600, 140, 88, 70);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new BerserkerRageSkill());
        skills.add(new CleaveSkill());
        skills.add(new WrathUnleashedUltimate());
    }
}

