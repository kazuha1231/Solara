package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.orin.*;

public class OrinKaelus extends Character {
    public OrinKaelus() {
        // Tank/Support: AoE holy damage, shield & heal, Ultimate: Abyssal Verdict
        // Very tough protector with good mana pool
        super("Orin Kaelus", 1040, 700, 115, 100, 62);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new HolySmiteSkill());
        skills.add(new DivineProtectionSkill());
        skills.add(new JudgmentOfSolaraUltimate());
    }
}

