package com.defendersofsolara.characters.heroes;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.skills.basic.AttackSkill;
import com.defendersofsolara.skills.nyx.*;

public class NyxValora extends Character {
    public NyxValora() {
        // Stealth/Assassin: damage & untargetable, Ultimate: Nightfall Execution
        super("Nyx Valora", 750, 600, 155, 55, 92);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new ShadowStrikeSkill());
        skills.add(new StealthSkill());
        skills.add(new NightfallExecutionUltimate());
    }
}

