package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.ylonne.*;

public class YlonneKryx extends Character {
    public YlonneKryx() {
        // Agile DPS: slightly lower HP, moderate mana for burst abilities
        super("Ylonne Kryx", 800, 620, 145, 60, 90);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new SilentStepSkill());
        skills.add(new ShadowStrikeSkill()); // High single-target damage
        skills.add(new RicochetUltimate()); // Ultimate: Ricochet
    }
}
