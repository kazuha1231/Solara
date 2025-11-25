package com.defendersofsolara.characters.enemies;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class XyrrakTheDevourer extends Character {
    public XyrrakTheDevourer() {
        super("Xyrrak the Devourer", 5000, 2000, 200, 100, 80);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new BlackHoleSkill());
        skills.add(new DevourSkill());
        skills.add(new CosmicRageSkill());
    }

    static class AttackSkill extends Skill {
        AttackSkill() {
            name = "Dark Strike";
            manaCost = 0;
            cooldown = 0;
            description = "Basic attack";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            if (targets.length > 0 && targets[0] != null) {
                int damage = user.currentAttack;
                targets[0].takeDamage(damage);
                System.out.println(user.name + " strikes " + targets[0].name + " for " + damage + " damage!");
            }
        }
    }

    static class BlackHoleSkill extends Skill {
        BlackHoleSkill() {
            name = "Black Hole";
            manaCost = 220;
            cooldown = 5;
            description = "Massive AoE damage";
            targetType = TargetType.ALL_ENEMIES;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            for (Character target : targets) {
                if (target != null && target.isAlive()) {
                    int damage = (int) (user.currentAttack * 0.9);
                    target.takeDamage(damage);
                    System.out.println("  → " + target.name + " takes " + damage + " cosmic damage!");
                }
            }
            resetCooldown();
        }
    }

    static class DevourSkill extends Skill {
        DevourSkill() {
            name = "Devour";
            manaCost = 150;
            cooldown = 4;
            description = "Life steal attack";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            if (targets.length > 0 && targets[0] != null) {
                int damage = (int) (user.currentAttack * 1.2);
                targets[0].takeDamage(damage);
                int heal = damage / 2;
                user.restoreHealth(heal);
                System.out.println(user.name + " devours " + targets[0].name + " for " + damage + " damage and heals " + heal + " HP!");
            }
            resetCooldown();
        }
    }

    static class CosmicRageSkill extends Skill {
        CosmicRageSkill() {
            name = "Cosmic Rage";
            manaCost = 100;
            cooldown = 3;
            description = "Boost attack power";
            targetType = TargetType.SELF;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            user.applyEffect(new StatusEffect("buff", 80, 3));
            System.out.println(user.name + " enters Cosmic Rage mode!");
            resetCooldown();
        }
    }
}
