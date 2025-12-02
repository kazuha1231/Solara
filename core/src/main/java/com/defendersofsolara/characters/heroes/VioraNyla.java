package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.viora.*;

public class VioraNyla extends Character {

    public VioraNyla() {
        // Support mage: balanced HP/mana leaning toward casting
        super("Viora Nyla", 850, 720, 100, 70, 70);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new HoloFieldSkill());
        skills.add(new TeamHealSkill());
        skills.add(new CodeBreakerUltimate());
    }
}
