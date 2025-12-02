package com.defendersofsolara.characters.enemies;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.StatusEffect;
import com.defendersofsolara.core.TargetType;

public class Stonebound extends Character {
    public Stonebound() {
        super("Stonebound Horror", 800, 300, 70, 50, 35);
    }

    @Override
    public void initializeSkills() {
        skills.add(new AttackSkill());
        skills.add(new GravityShieldSkill());
        skills.add(new GravityCrushSkill());
    }

    static class AttackSkill extends Skill {
        AttackSkill() {
            name = "Earthshatter Slam";
            manaCost = 0;
            cooldown = 0;
            description = "Basic attack";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            if (targets.length > 0 && targets[0] != null) {
                int damage = user.currentAttack;
                targets[0].takeDamage(damage);
                System.out.println(user.name + " slams " + targets[0].name + " for " + damage + " damage!");
            }
        }
    }

   static class GravityShieldSkill extends Skill {
        GravityShieldSkill() {
            name = "Stoneward Shield";
            manaCost = 60;
            cooldown = 4;
            description = "Increase defense";
            targetType = TargetType.SELF;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            user.currentDefense += 50;
            user.applyEffect(new StatusEffect("shield", 100, 2));
            System.out.println(user.name + " creates a gravity shield!");
            resetCooldown();
        }
    }

    static class GravityCrushSkill extends Skill {
        GravityCrushSkill() {
            name = "Crushing Descent";
            manaCost = 80;
            cooldown = 3;
            description = "Heavy damage to one target";
            targetType = TargetType.SINGLE_ENEMY;
        }
        public void execute(Character user, Character[] targets) {
            user.currentMana -= manaCost;
            if (targets.length > 0 && targets[0] != null) {
                int damage = (int) (user.currentAttack * 1.5);
                targets[0].takeDamage(damage);
                System.out.println(user.name + " crushes " + targets[0].name + " for " + damage + " damage!");
            }
            resetCooldown();
        }
    }
}

