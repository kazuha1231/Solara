package com.defendersofsolara;

import com.defendersofsolara.battle.BattleManager;
import com.defendersofsolara.characters.enemies.BiomechanicalAlien;
import com.defendersofsolara.characters.enemies.GravityBeast;
import com.defendersofsolara.characters.enemies.XyrrakTheDevourer;
import com.defendersofsolara.characters.heroes.KaelDraven;
import com.defendersofsolara.characters.heroes.VioraNyla;
import com.defendersofsolara.characters.heroes.YlonneKryx;
import com.defendersofsolara.characters.heroes.ZyraKathun;
import com.defendersofsolara.core.Character;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=".repeat(60));
        System.out.println("DEFENDERS OF SOLARA");
        System.out.println("=".repeat(60));
        System.out.println("\nA galactic warlord named Xyrrak the Devourer has unleashed");
        System.out.println("an army of bio-mechanical aliens to conquer the Veil System.");
        System.out.println("Four heroes must rise to stop him!\n");

        // Create player team
        Character[] playerTeam = {
                new KaelDraven(),
                new VioraNyla(),
                new YlonneKryx(),
                new ZyraKathun()
        };

        System.out.println("Your Team:");
        for (Character c : playerTeam) {
            System.out.println("  - " + c.name + " (HP: " + c.maxHP + ", Mana: " + c.maxMana + ")");
        }

        boolean gameRunning = true;
        while (gameRunning) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Fight Bio-Alien");
            System.out.println("2. Fight Gravity Beast");
            System.out.println("3. Fight Xyrrak (Final Boss)");
            System.out.println("4. View Stats");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    BattleManager battle1 = new BattleManager(playerTeam, new Character[]{new BiomechanicalAlien()});
                    battle1.startBattle();
                    break;
                case 2:
                    BattleManager battle2 = new BattleManager(playerTeam, new Character[]{new GravityBeast()});
                    battle2.startBattle();
                    break;
                case 3:
                    BattleManager battle3 = new BattleManager(playerTeam, new Character[]{new XyrrakTheDevourer()});
                    battle3.startBattle();
                    break;
                case 4:
                    System.out.println("\n=== TEAM STATS ===");
                    for (Character c : playerTeam) {
                        System.out.println(c.getStatus());
                    }
                    break;
                case 5:
                    gameRunning = false;
                    System.out.println("Thanks for playing!");
                    break;
            }
        }

        scanner.close();
    }
}
