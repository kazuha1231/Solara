package com.defendersofsolara.characters.enemies;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

public class Abyssal extends Character {
    public Abyssal() {
        super("Abyssal Husk", 500, 200, 50, 20, 40);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new ToxicWaveSkill());
    }

    // Add 'static' keyword here
    static class AttackSkill extends Skill {
        AttackSkill() {
            name = "Claw Strike";
            manaCost = 0;
            cooldown = 0;
            description = "Basic attack";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            if (targets.length > 0 && targets[0] != null) {
                int damage = user.currentAttack;
                targets[0].takeDamage(damage);
                System.out.println(user.name + " claws " + targets[0].name + " for " + damage + " damage!");
            }
        }
    }

    // Add 'static' keyword here
    static class ToxicWaveSkill extends Skill {
        ToxicWaveSkill() {
            name = "Corrupting Miasma";
            manaCost = 50;
            cooldown = 3;
            description = "AoE corrupting miasma attack";
            targetType = TargetType.ALL_ENEMIES;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            for (Character target : targets) {
                if (target != null && target.isAlive()) {
                    int damage = (int) (user.currentAttack * 0.6);
                    target.takeDamage(damage);
                    System.out.println("  → " + target.name + " takes " + damage + " poison damage!");
                }
            }
            resetCooldown();
        }
    }
}

