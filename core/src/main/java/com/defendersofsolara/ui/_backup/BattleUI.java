package com.defendersofsolara.ui._backup;

import com.defendersofsolara.characters.enemies.*;
import com.defendersofsolara.characters.heroes.*;
import com.defendersofsolara.core.Character;
import com.defendersofsolara.core.Skill;
import com.defendersofsolara.core.TargetType;
import com.defendersofsolara.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BattleUI extends JFrame {

    private final int worldId;
    private Character[] playerTeam;
    private Character[] enemyTeam;

    private int currentPlayerIndex = 0;
    private boolean playerTurn = true;
    private Skill selectedSkill = null;
    private boolean waitingForTarget = false;

    private JPanel playerPanel;
    private JPanel enemyPanel;
    private JTextArea combatLog;
    private JPanel skillPanel;
    private JLabel turnLabel;
    private JLabel instructionLabel;

    public BattleUI(int worldId) {
        this.worldId = worldId;

        setTitle("Battle - World " + worldId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UITheme.BATTLE_WIDTH, UITheme.BATTLE_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeTeams();
        initUI();

        setVisible(true);
        startBattle();
    }

    private void initializeTeams() {
        playerTeam = new Character[]{
            new KaelDraven(),
            new VioraNyla(),
            new YlonneKryx()
        };

        enemyTeam = spawnEnemiesForWorld(worldId);
    }

    private Character[] spawnEnemiesForWorld(int worldId) {
        switch (worldId) {
            case 1:
                return new Character[]{new BiomechanicalAlien(), new GravityBeast()};
            case 2:
                return new Character[]{new GravityBeast(), new BiomechanicalAlien(), new GravityBeast()};
            case 3:
                return new Character[]{new XyrrakTheDevourer()};
            case 4:
                return new Character[]{new GravityBeast(), new XyrrakTheDevourer()};
            case 5:
                return new Character[]{new XyrrakTheDevourer(), new XyrrakTheDevourer()};
            default:
                return new Character[]{new BiomechanicalAlien()};
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_DARK);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        buildCharacterPanels();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("WORLD " + worldId + " - BATTLE", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.getFontSubtitle());
        titleLabel.setForeground(UITheme.PRIMARY_CYAN);

        turnLabel = new JLabel("Preparing...", SwingConstants.CENTER);
        turnLabel.setFont(UITheme.getFontHeader());
        turnLabel.setForeground(UITheme.PRIMARY_YELLOW);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(turnLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setBackground(UITheme.BG_PLAYER);
        playerPanel.setBorder(UITheme.createTitledBorder("YOUR TEAM", UITheme.PRIMARY_CYAN, UITheme.PRIMARY_CYAN));

        combatLog = new JTextArea();
        combatLog.setFont(UITheme.getFontLog());
        combatLog.setEditable(false);
        combatLog.setLineWrap(true);
        combatLog.setWrapStyleWord(true);
        combatLog.setBackground(new Color(10, 10, 20));
        combatLog.setForeground(UITheme.LOG_TEXT);
        combatLog.setFocusable(false);
        JScrollPane logScroll = new JScrollPane(combatLog);
        logScroll.setPreferredSize(new Dimension(350, 350));

        enemyPanel = new JPanel();
        enemyPanel.setLayout(new BoxLayout(enemyPanel, BoxLayout.Y_AXIS));
        enemyPanel.setBackground(UITheme.BG_ENEMY);
        enemyPanel.setBorder(UITheme.createTitledBorder("ENEMIES", UITheme.PRIMARY_RED, UITheme.PRIMARY_RED));

        panel.add(playerPanel, BorderLayout.WEST);
        panel.add(logScroll, BorderLayout.CENTER);
        panel.add(enemyPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        instructionLabel = new JLabel("Select a skill", SwingConstants.CENTER);
        instructionLabel.setFont(UITheme.getFontButtonSmall());
        instructionLabel.setForeground(UITheme.PRIMARY_CYAN);

        skillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        skillPanel.setBackground(new Color(30, 30, 50));
        skillPanel.setBorder(UITheme.createCyanBorder(2));

        panel.add(instructionLabel, BorderLayout.NORTH);
        panel.add(skillPanel, BorderLayout.CENTER);

        return panel;
    }

    private void buildCharacterPanels() {
        playerPanel.removeAll();
        enemyPanel.removeAll();

        for (int i = 0; i < playerTeam.length; i++) {
            Character c = playerTeam[i];
            JPanel card = createCharacterCard(c, true, i);
            playerPanel.add(card);
            playerPanel.add(Box.createVerticalStrut(10));
        }

        for (int i = 0; i < enemyTeam.length; i++) {
            Character e = enemyTeam[i];
            JPanel card = createCharacterCard(e, false, i);
            enemyPanel.add(card);
            enemyPanel.add(Box.createVerticalStrut(10));
        }

        revalidate();
        repaint();
    }

    private JPanel createCharacterCard(Character c, boolean isPlayer, int index) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(UITheme.getCharacterCardDim());
        card.setMaximumSize(UITheme.getCharacterCardDim());
        card.setBackground(isPlayer ? new Color(40, 60, 90) : new Color(90, 40, 40));
        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));

        JLabel nameLabel = new JLabel(c.name);
        nameLabel.setFont(UITheme.getFontCardName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar hpBar = new JProgressBar(0, c.maxHP);
        hpBar.setValue(c.currentHP);
        hpBar.setStringPainted(true);
        hpBar.setString(c.currentHP + " / " + c.maxHP);
        hpBar.setForeground(UITheme.HP_GREEN);

        JProgressBar manaBar = new JProgressBar(0, c.maxMana);
        manaBar.setValue(c.currentMana);
        manaBar.setStringPainted(true);
        manaBar.setString(c.currentMana + " / " + c.maxMana);
        manaBar.setForeground(UITheme.MANA_BLUE);

        JPanel barsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        barsPanel.setOpaque(false);
        barsPanel.add(hpBar);
        barsPanel.add(manaBar);

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(barsPanel, BorderLayout.CENTER);

        card.putClientProperty("character", c);
        card.putClientProperty("hpBar", hpBar);
        card.putClientProperty("manaBar", manaBar);
        card.putClientProperty("index", index);
        card.putClientProperty("isPlayer", isPlayer);

        addTargetingHandler(card, c, isPlayer);

        return card;
    }

    private void addTargetingHandler(JPanel card, Character c, boolean isPlayer) {
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (waitingForTarget && !isPlayer && c.isAlive()) {
                    onTargetSelected(c);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (waitingForTarget && !isPlayer && c.isAlive()) {
                    card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HOVER, 3));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (waitingForTarget && !isPlayer) {
                    card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HIGHLIGHT, 2));
                } else {
                    card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
                }
            }
        });
    }

    public void startBattle() {
        appendLog("⚔ Battle Started!");
        appendLog(">>> " + playerTeam[0].name + "'s turn\n");
        preparePlayerTurn();
    }

    private void preparePlayerTurn() {
        while (currentPlayerIndex < playerTeam.length &&
            !playerTeam[currentPlayerIndex].isAlive()) {
            currentPlayerIndex++;
        }
        if (currentPlayerIndex >= playerTeam.length) {
            currentPlayerIndex = 0;
            while (currentPlayerIndex < playerTeam.length &&
                !playerTeam[currentPlayerIndex].isAlive()) {
                currentPlayerIndex++;
            }
        }
        if (currentPlayerIndex >= playerTeam.length ||
            !playerTeam[currentPlayerIndex].isAlive()) {
            checkBattleEnd();
            return;
        }

        Character current = playerTeam[currentPlayerIndex];
        turnLabel.setText("PLAYER TURN: " + current.name);
        instructionLabel.setText("Select a skill for " + current.name);
        instructionLabel.setForeground(UITheme.PRIMARY_CYAN);

        loadSkillButtons(current);
        updateAllBars();

        selectedSkill = null;
        waitingForTarget = false;
        clearHighlights();
    }

    private void loadSkillButtons(Character character) {
        skillPanel.removeAll();

        for (Skill skill : character.skills) {
            JButton skillBtn = createSkillButton(skill, character);
            skillPanel.add(skillBtn);
        }

        skillPanel.revalidate();
        skillPanel.repaint();
    }

    private JButton createSkillButton(Skill skill, Character user) {
        JButton skillBtn = new JButton(skill.getInfo());
        skillBtn.setFont(UITheme.getFontSkill());
        skillBtn.setForeground(UITheme.PRIMARY_CYAN);
        skillBtn.setBackground(UITheme.BG_BUTTON);
        skillBtn.setFocusPainted(false);
        skillBtn.setBorder(UITheme.createCyanBorder(2));
        skillBtn.setPreferredSize(UITheme.getSkillButtonDim());

        boolean canUse = skill.canUse(user);
        skillBtn.setEnabled(canUse);

        if (!canUse) {
            skillBtn.setForeground(UITheme.TEXT_GRAY);
            skillBtn.setBorder(BorderFactory.createLineBorder(UITheme.TEXT_GRAY, 2));
        }

        skillBtn.addActionListener(e -> onSkillSelected(skill, user));

        return skillBtn;
    }

    private void onSkillSelected(Skill skill, Character user) {
        if (!skill.canUse(user)) {
            appendLog("⚠ Cannot use " + skill.name + "!");
            return;
        }

        selectedSkill = skill;
        TargetType targetType = skill.getTargetType();

        switch (targetType) {
            case SELF:
                appendLog(user.name + " uses " + skill.name + " on self!");
                skill.execute(user, new Character[]{user});
                endPlayerTurn();
                break;
            case ALL_ALLIES:
                appendLog(user.name + " uses " + skill.name + " on all allies!");
                skill.execute(user, getAllAlive(playerTeam));
                endPlayerTurn();
                break;
            case ALL_ENEMIES:
                appendLog(user.name + " uses " + skill.name + " on all enemies!");
                skill.execute(user, getAllAlive(enemyTeam));
                endPlayerTurn();
                break;
            case SINGLE_ENEMY:
                instructionLabel.setText("Click on an ENEMY to target!");
                instructionLabel.setForeground(UITheme.PRIMARY_RED);
                waitingForTarget = true;
                setSkillButtonsEnabled(false);
                highlightEnemies();
                break;
            default:
                endPlayerTurn();
                break;
        }
    }

    private void onTargetSelected(Character target) {
        if (!waitingForTarget || selectedSkill == null) return;
        Character user = playerTeam[currentPlayerIndex];
        appendLog(user.name + " uses " + selectedSkill.name + " on " + target.name + "!");
        selectedSkill.execute(user, new Character[]{target});
        clearHighlights();
        endPlayerTurn();
    }

    private void endPlayerTurn() {
        if (selectedSkill != null) {
            selectedSkill.resetCooldown();
        }
        selectedSkill = null;
        waitingForTarget = false;
        instructionLabel.setText("Processing...");
        instructionLabel.setForeground(UITheme.TEXT_GRAY);
        setSkillButtonsEnabled(false);
        updateAllBars();

        Timer delay = new Timer(800, e -> {
            if (checkBattleEnd()) return;

            currentPlayerIndex++;
            if (currentPlayerIndex >= playerTeam.length) {
                currentPlayerIndex = 0;
                enemyTurn();
            } else {
                preparePlayerTurn();
            }
            ((Timer) e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void enemyTurn() {
        turnLabel.setText("ENEMY TURN");
        appendLog("\n=== ENEMY TURN ===");

        Timer enemyDelay = new Timer(500, null);
        final int[] enemyIndex = {0};
        enemyDelay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enemyIndex[0] < enemyTeam.length) {
                    Character enemy = enemyTeam[enemyIndex[0]];
                    if (enemy.isAlive()) {
                        executeEnemyAction(enemy);
                    }
                    enemyIndex[0]++;
                } else {
                    enemyDelay.stop();
                    reduceAllCooldowns();
                    Timer endTurn = new Timer(500, evt -> {
                        if (!checkBattleEnd()) {
                            preparePlayerTurn();
                        }
                        ((Timer) evt.getSource()).stop();
                    });
                    endTurn.setRepeats(false);
                    endTurn.start();
                }
            }
        });

        enemyDelay.start();
    }

    private void executeEnemyAction(Character enemy) {
        Skill skill = null;
        for (Skill s : enemy.skills) {
            if (s.canUse(enemy)) {
                skill = s;
                break;
            }
        }
        if (skill == null && !enemy.skills.isEmpty()) {
            skill = enemy.skills.get(0);
        }
        if (skill != null) {
            Character target = getRandomAlive(playerTeam);
            if (target != null) {
                appendLog(enemy.name + " uses " + skill.name + " on " + target.name + "!");
                skill.execute(enemy, new Character[]{target});
                updateAllBars();
            }
        }
    }

    private boolean checkBattleEnd() {
        boolean playersAlive = anyAlive(playerTeam);
        boolean enemiesAlive = anyAlive(enemyTeam);
        if (!enemiesAlive) {
            endBattle(true);
            return true;
        } else if (!playersAlive) {
            endBattle(false);
            return true;
        }
        return false;
    }

    private void endBattle(boolean victory) {
        setSkillButtonsEnabled(false);

        if (victory) {
            appendLog("\n" + "=".repeat(50));
            appendLog("⭐ VICTORY! ⭐");
            appendLog("=".repeat(50));
            JOptionPane.showMessageDialog(this,
                "Victory! You have cleared World " + worldId + "!",
                "Victory",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            appendLog("\n" + "=".repeat(50));
            appendLog("💀 DEFEAT... 💀");
            appendLog("=".repeat(50));
            JOptionPane.showMessageDialog(this,
                "Defeat... You have been returned to world selection.",
                "Defeat",
                JOptionPane.WARNING_MESSAGE);
        }

        dispose();
        SwingUtilities.invokeLater(WorldSelection::new);
    }

    private void highlightEnemies() {
        for (Component comp : enemyPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                Character c = (Character) card.getClientProperty("character");
                if (c != null && c.isAlive()) {
                    card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_HIGHLIGHT, 2));
                }
            }
        }
    }

    private void clearHighlights() {
        clearHighlightsForPanel(playerPanel);
        clearHighlightsForPanel(enemyPanel);
    }

    private void clearHighlightsForPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBorder(BorderFactory.createLineBorder(UITheme.BORDER_NORMAL, 2));
            }
        }
    }

    private void updateAllBars() {
        updateBarsForPanel(playerPanel);
        updateBarsForPanel(enemyPanel);
    }

    private void updateBarsForPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                Character c = (Character) card.getClientProperty("character");
                JProgressBar hpBar = (JProgressBar) card.getClientProperty("hpBar");
                JProgressBar manaBar = (JProgressBar) card.getClientProperty("manaBar");

                if (c != null && hpBar != null && manaBar != null) {
                    hpBar.setMaximum(c.maxHP);
                    hpBar.setValue(Math.max(0, c.currentHP));
                    hpBar.setString(c.currentHP + " / " + c.maxHP);

                    manaBar.setMaximum(c.maxMana);
                    manaBar.setValue(Math.max(0, c.currentMana));
                    manaBar.setString(c.currentMana + " / " + c.maxMana);

                    if (!c.isAlive()) {
                        card.setBackground(UITheme.DEAD_GRAY);
                    }
                }
            }
        }
    }

    private void setSkillButtonsEnabled(boolean enabled) {
        for (Component comp : skillPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            }
        }
    }

    private void reduceAllCooldowns() {
        for (Character c : playerTeam) {
            if (c != null) {
                for (Skill s : c.skills) {
                    s.reduceCooldown();
                }
            }
        }
        for (Character e : enemyTeam) {
            if (e != null) {
                for (Skill s : e.skills) {
                    s.reduceCooldown();
                }
            }
        }
    }

    private Character[] getAllAlive(Character[] team) {
        List<Character> alive = new ArrayList<>();
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                alive.add(c);
            }
        }
        return alive.toArray(new Character[0]);
    }

    private Character getRandomAlive(Character[] team) {
        List<Character> alive = new ArrayList<>();
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                alive.add(c);
            }
        }
        if (alive.isEmpty()) return null;
        return alive.get((int) (Math.random() * alive.size()));
    }

    private boolean anyAlive(Character[] team) {
        for (Character c : team) {
            if (c != null && c.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private void appendLog(String text) {
        combatLog.append(text + "\n");
        combatLog.setCaretPosition(combatLog.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BattleUI(1));
    }
}
