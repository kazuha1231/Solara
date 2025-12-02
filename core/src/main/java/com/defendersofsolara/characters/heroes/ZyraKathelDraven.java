package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.zyrakathel.*;

public class ZyraKathelDraven extends Character {
    public ZyraKathelDraven() {
        // Tank/Damage hybrid: shield, attack buff, Ultimate: Last Stand
        // Normal-difficulty balance: very sturdy front-liner with modest damage
        super("Aric Stoneward", 1080, 620, 125, 98, 60);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new DefensiveShieldSkill());
        skills.add(new AttackBuffSkill());
        skills.add(new LastStandUltimate());
    }
}

