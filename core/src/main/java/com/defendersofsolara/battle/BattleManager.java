package com.defendersofsolara.battle;

import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BattleManager {
    private Character[] playerTeam;
    private Character[] enemyTeam;
    private int turn = 0;

    public BattleManager(Character[] players, Character[] enemies) {
        this.playerTeam = players;
        this.enemyTeam = enemies;
    }

    public void startBattle() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("BATTLE START!");
        System.out.println("=".repeat(60) + "\n");

        while (isBattleActive()) {
            turn++;
            System.out.println("\n--- TURN " + turn + " ---");

            playerTurn();
            if (!isBattleActive()) break;

            enemyTurn();

            // Update effects and reset stats after each round
            for (Character c : playerTeam) {
                c.updateEffects();
                c.resetStats();
            }
            for (Character c : enemyTeam) {
                c.updateEffects();
                c.resetStats();
            }
        }

        displayBattleResult();
    }

    // --- Player Turn with Smart Targeting ---
    private void playerTurn() {
        System.out.println("\n[PLAYER TURN]");
        for (Character player : playerTeam) {
            if (!player.isAlive()) continue;

            // Random skill
            Skill skill = player.skills.get((int)(Math.random() * player.skills.size()));

            if (!skill.canUse(player)) {
                System.out.println(player.name + " cannot use " + skill.name + "!");
                continue;
            }

            // Smart targeting based on skill type
            Character[] targets = getTargetsForSkill(skill, player, true);

            if (targets != null && targets.length > 0) {
                System.out.println(player.name + " uses " + skill.name + "!");
                skill.execute(player, targets);
            }
        }
    }

    // --- Enemy Turn with Smart Targeting ---
    private void enemyTurn() {
        System.out.println("\n[ENEMY TURN]");
        for (Character enemy : enemyTeam) {
            if (!enemy.isAlive()) continue;

            // Pick usable skill
            Skill skill = null;
            for (Skill s : enemy.skills) {
                if (s.canUse(enemy)) {
                    skill = s;
                    break;
                }
            }
            if (skill == null) skill = enemy.skills.get(0);

            // Smart targeting based on skill type
            Character[] targets = getTargetsForSkill(skill, enemy, false);

            if (targets != null && targets.length > 0) {
                System.out.println(enemy.name + " uses " + skill.name + "!");
                skill.execute(enemy, targets);
            }
        }

        // Reduce skill cooldowns
        for (Character enemy : enemyTeam) {
            for (Skill skill : enemy.skills) {
                skill.reduceCooldown();
            }
        }
    }

    // --- Smart Target Selection Based on TargetType ---
    private Character[] getTargetsForSkill(Skill skill, Character user, boolean isPlayer) {
        TargetType targetType = skill.getTargetType();

        switch (targetType) {
            case SELF:
                // Target self
                return new Character[]{user};

            case ALL_ALLIES:
                // Target all allies (player team or enemy team)
                return isPlayer ? getAllAlive(playerTeam) : getAllAlive(enemyTeam);

            case ALL_ENEMIES:
                // Target all enemies
                return isPlayer ? getAllAlive(enemyTeam) : getAllAlive(playerTeam);

            case SINGLE_ENEMY:
                // Target random enemy
                Character enemy = getRandomAliveCharacter(isPlayer ? enemyTeam : playerTeam);
                return enemy != null ? new Character[]{enemy} : new Character[0];

            case SINGLE_ALLY:
                // Target random ally
                Character ally = getRandomAliveCharacter(isPlayer ? playerTeam : enemyTeam);
                return ally != null ? new Character[]{ally} : new Character[0];

            case RANDOM_ENEMY:
                // Random enemy (for AI)
                Character randomEnemy = getRandomAliveCharacter(isPlayer ? enemyTeam : playerTeam);
                return randomEnemy != null ? new Character[]{randomEnemy} : new Character[0];

            case RANDOM_ALLY:
                // Random ally (for AI)
                Character randomAlly = getRandomAliveCharacter(isPlayer ? playerTeam : enemyTeam);
                return randomAlly != null ? new Character[]{randomAlly} : new Character[0];

            default:
                // Fallback to random enemy
                Character fallback = getRandomAliveCharacter(isPlayer ? enemyTeam : playerTeam);
                return fallback != null ? new Character[]{fallback} : new Character[0];
        }
    }

    // --- Helper to get all alive characters from a team ---
    private Character[] getAllAlive(Character[] team) {
        List<Character> alive = Arrays.stream(team)
                .filter(Character::isAlive)
                .collect(Collectors.toList());
        return alive.toArray(new Character[0]);
    }

    // --- Helper to pick random alive character ---
    private Character getRandomAliveCharacter(Character[] team) {
        Character[] alive = Arrays.stream(team)
                .filter(Character::isAlive)
                .toArray(Character[]::new);
        if (alive.length == 0) return null;
        return alive[(int)(Math.random() * alive.length)];
    }

    // --- Check if battle still ongoing ---
    private boolean isBattleActive() {
        boolean playersActive = Arrays.stream(playerTeam).anyMatch(Character::isAlive);
        boolean enemiesActive = Arrays.stream(enemyTeam).anyMatch(Character::isAlive);
        return playersActive && enemiesActive;
    }

    // --- Display results ---
    private void displayBattleResult() {
        boolean playerWon = Arrays.stream(playerTeam).anyMatch(Character::isAlive);

        System.out.println("\n" + "=".repeat(60));
        if (playerWon) {
            System.out.println("VICTORY!");
            for (Character player : playerTeam) {
                if (player.isAlive()) {
                    player.gainExp(50);
                }
            }
        } else {
            System.out.println("DEFEAT!");
        }
        System.out.println("=".repeat(60) + "\n");
    }
}
