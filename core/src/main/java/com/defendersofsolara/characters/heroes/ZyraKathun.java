package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.zyra.*;

public class ZyraKathun extends Character {
    public ZyraKathun() {
        // Powerful caster unlocked after world 3 - has significantly higher stats
        super("Zyra Kathun", 1200, 900, 160, 90, 75);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new FlashSkill());
        skills.add(new ShockwaveSkill());
        skills.add(new BlackHoleUltimate());
    }
}
