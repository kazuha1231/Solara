package com.defendersofsolara.skills.lyra;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class TeamHealSkill extends Skill {
    public TeamHealSkill() {
        name = "Inner Harmony";
        manaCost = 100;
        cooldown = 4;
        description = "Channel inner harmony to heal all allies for 20-50% of your max HP";
        targetType = TargetType.ALL_ALLIES;  // Auto-heal entire team
    }

    @Override
    public void execute(Character user, Character[] targets) {
        user.currentMana -= manaCost;
        
        // Calculate heal percentage: 20% base, scales up to 50% based on level
        // At level 1: 20%, at level 30: 50%
        double basePercent = 0.20; // 20%
        double maxPercent = 0.50; // 50%
        double percentPerLevel = (maxPercent - basePercent) / 29.0; // Scale from level 1 to 30
        double healPercent = basePercent + (percentPerLevel * Math.max(0, user.level - 1));
        healPercent = Math.min(healPercent, maxPercent); // Cap at 50%
        
        // Heal amount based on Viora's max HP
        int healAmount = (int) Math.round(user.maxHP * healPercent);
        
        for (Character ally : targets) {
            if (ally != null && ally.isAlive()) {
                int hpBefore = ally.currentHP;
                ally.restoreHealth(healAmount);
                int actualHeal = ally.currentHP - hpBefore; // Actual amount healed (capped by maxHP)
                System.out.println("  → " + ally.name + " heals " + actualHeal + " HP (" + 
                    String.format("%.1f", healPercent * 100) + "% of " + user.name + "'s max HP)!");
            }
        }
        resetCooldown();
    }
}

