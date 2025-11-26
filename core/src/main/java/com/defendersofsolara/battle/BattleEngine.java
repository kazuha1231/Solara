package com.defendersofsolara.battle;

import com.defendersofsolara.characters.heroes.Hero;
import com.defendersofsolara.characters.enemies.Enemy;
import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;

import java.util.*;
import java.util.stream.Collectors;

public class BattleEngine {

    private List<Hero> heroes;
    private List<Enemy> enemies;

    public BattleEngine(List<Hero> heroes, List<Enemy> enemies) {
        this.heroes = heroes;
        this.enemies = enemies;
    }

    public BattleEngine(int worldId, List<Character> characters) {
        // Separate characters into heroes and enemies
        this.heroes = characters.stream()
            .filter(c -> c instanceof Hero)
            .map(c -> (Hero) c)
            .collect(Collectors.toList());

        this.enemies = characters.stream()
            .filter(c -> c instanceof Enemy)
            .map(c -> (Enemy) c)
            .collect(Collectors.toList());
    }

    // Start the battle loop (for console-based battles only)
    public void startBattle() {
        System.out.println("⚔ Battle Started!");

        int round = 1;
        while (heroesAlive() && enemiesAlive()) {
            System.out.println("\n=== Round " + round + " ===");
            heroTurn();
            if (!enemiesAlive()) break;
            enemyTurn();
            round++;
        }

        if (heroesAlive()) {
            System.out.println("\n🏆 Heroes Win!");
        } else {
            System.out.println("\n☠ Enemies Win!");
        }
    }

    // Heroes take their turn
    private void heroTurn() {
        for (Hero hero : heroes) {
            if (!hero.isAlive()) continue;

            // Choose first available skill
            Skill skill = selectSkill(hero);
            if (skill == null || !skill.canUse(hero)) continue;

            // Select target
            Character target = selectTarget(hero, enemies);
            if (target != null) {
                skill.execute(hero, new Character[]{target});
                skill.resetCooldown();
                hero.currentMana -= skill.getManaCost();
            }
        }
    }

    // Enemies take their turn
    private void enemyTurn() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            Skill skill = selectSkill(enemy);
            if (skill == null || !skill.canUse(enemy)) continue;

            Character target = selectTarget(enemy, heroes);
            if (target != null) {
                skill.execute(enemy, new Character[]{target});
                skill.resetCooldown();
                enemy.currentMana -= skill.getManaCost();
            }
        }
    }

    private Skill selectSkill(Character character) {
        if (character.skills == null || character.skills.isEmpty()) {
            return null;
        }
        return character.skills.get(0);
    }

    private Character selectTarget(Character attacker, List<? extends Character> targets) {
        return targets.stream()
            .filter(Character::isAlive)
            .findFirst()
            .orElse(null);
    }

    private boolean heroesAlive() {
        return heroes.stream().anyMatch(Character::isAlive);
    }

    private boolean enemiesAlive() {
        return enemies.stream().anyMatch(Character::isAlive);
    }
}
