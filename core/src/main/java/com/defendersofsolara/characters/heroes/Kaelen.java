package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.kaelen.*;

public class Kaelen extends Character {
    public Kaelen() {
        // Kaelen Mirethorn — Shadow Ranger (Assassin)
        // Extreme burst and speed, but very fragile
        super("Kaelen Mirethorn", 770, 620, 160, 55, 95);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new ShadowStrikeSkill());
        skills.add(new StealthSkill());
        skills.add(new NightfallExecutionUltimate());
    }
}

