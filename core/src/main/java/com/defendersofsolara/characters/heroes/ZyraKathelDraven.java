package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.zyrakathel.*;

public class ZyraKathelDraven extends Character {
    public ZyraKathelDraven() {
        // Tank/Damage hybrid: shield, attack buff, Ultimate: Last Stand
        super("Zyra Kathel Draven", 1050, 600, 125, 90, 60);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new DefensiveShieldSkill());
        skills.add(new AttackBuffSkill());
        skills.add(new LastStandUltimate());
    }
}

