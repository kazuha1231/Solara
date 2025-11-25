package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.zyra.*;

public class ZyraKathun extends Character {
    public ZyraKathun() {
        // Glass-cannon caster unlocked later in the game
        super("Zyra Kathun", 980, 720, 135, 70, 64);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new FlashSkill());
        skills.add(new ShockwaveSkill());
        skills.add(new BlackHoleUltimate());
    }
}
