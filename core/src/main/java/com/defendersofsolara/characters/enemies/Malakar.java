package com.defendersofsolara.characters.enemies;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class Malakar extends Character {
    public Malakar() {
        // Final boss: Malakar the Abyssbound King (reuse existing enemy wiring)
        super("Malakar the Abyssbound King", 5000, 2000, 200, 100, 80);
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
            name = "Black Star Collapse";
            manaCost = 220;
            cooldown = 5;
            description = "Summon a collapsing star that deals massive AoE damage";
            targetType = TargetType.ALL_ENEMIES;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            for (Character target : targets) {
                if (target != null && target.isAlive()) {
                    int damage = (int) (user.currentAttack * 0.9);
                    target.takeDamage(damage);
                    System.out.println("  → " + target.name + " takes " + damage + " voidflame damage!");
                }
            }
            resetCooldown();
        }
    }

    static class DevourSkill extends Skill {
        DevourSkill() {
            name = "Soul Rend";
            manaCost = 150;
            cooldown = 4;
            description = "Rend a foe's soul and steal life";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            if (targets.length > 0 && targets[0] != null) {
                int damage = (int) (user.currentAttack * 1.2);
                targets[0].takeDamage(damage);
                int heal = damage / 2;
                user.restoreHealth(heal);
                System.out.println(user.name + " rends " + targets[0].name + " for " + damage + " damage and heals " + heal + " HP!");
            }
            resetCooldown();
        }
    }

    static class CosmicRageSkill extends Skill {
        CosmicRageSkill() {
            name = "Obsidian Crown Ascendance";
            manaCost = 100;
            cooldown = 3;
            description = "The Obsidian Crown surges, greatly boosting attack power";
            targetType = TargetType.SELF;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            user.applyEffect(new StatusEffect("buff", 80, 3));
            System.out.println(user.name + " channels the Obsidian Crown's power!");
            resetCooldown();
        }
    }
}

