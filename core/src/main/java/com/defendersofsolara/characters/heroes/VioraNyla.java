package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.viora.*;

public class VioraNyla extends Character {

    public VioraNyla() {
        // Lyra Stormgale — Runeblade Monk (Crowd Control)
        // Balanced HP/mana leaning toward casting and battlefield control
        super("Lyra Stormgale", 860, 760, 100, 75, 70);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new HoloFieldSkill());
        skills.add(new TeamHealSkill());
        skills.add(new CodeBreakerUltimate());
    }
}
