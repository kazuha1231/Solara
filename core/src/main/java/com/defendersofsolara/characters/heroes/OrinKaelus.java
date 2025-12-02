package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.orin.*;

public class OrinKaelus extends Character {
    public OrinKaelus() {
        // Tank/Support: AoE holy damage, shield & heal, Ultimate: Judgment of Solara
        super("Orin Kaelus", 1000, 680, 115, 95, 62);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new HolySmiteSkill());
        skills.add(new DivineProtectionSkill());
        skills.add(new JudgmentOfSolaraUltimate());
    }
}

