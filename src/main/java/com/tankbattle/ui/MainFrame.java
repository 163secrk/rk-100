package com.tankbattle.ui;

import com.tankbattle.engine.GameEngine;
import com.tankbattle.engine.HighScoreManager;
import com.tankbattle.model.GameMap;
import com.tankbattle.model.GameReplay;
import com.tankbattle.model.ScoreRecord;
import com.tankbattle.model.level.EnemyTypeRatio;
import com.tankbattle.model.level.LevelConfig;
import com.tankbattle.model.level.WaveConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements MenuPanel.MenuListener, GamePanel.GameUIListener,
        LeaderboardPanel.LeaderboardListener, KeyListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private MapEditorPanel mapEditorPanel;
    private LevelDesignerPanel levelDesignerPanel;
    private LeaderboardPanel leaderboardPanel;
    private GameEngine engine;
    private GameMap currentMap;
    private HighScoreManager highScoreManager;
    private int mapWidth = 800;
    private int mapHeight = 608;
    private int sidePanelWidth = 180;

    public MainFrame() {
        setTitle("坦克大战 - Tank Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        addKeyListener(this);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        highScoreManager = new HighScoreManager();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(20, 30, 50));
        mainPanel.setOpaque(true);

        menuPanel = new MenuPanel(this);
        mapEditorPanel = new MapEditorPanel(this);
        levelDesignerPanel = new LevelDesignerPanel(this);
        leaderboardPanel = new LeaderboardPanel(highScoreManager, this);

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(mapEditorPanel, "EDITOR");
        mainPanel.add(levelDesignerPanel, "LEVEL_DESIGNER");
        mainPanel.add(leaderboardPanel, "LEADERBOARD");

        setContentPane(mainPanel);
        pack();
        setSize(mapWidth + sidePanelWidth + 20, mapHeight + 60);
        setLocationRelativeTo(null);

        createDefaultMaps();
        createDefaultLevels();
    }

    private void createDefaultMaps() {
        File mapsDir = new File("maps");
        if (!mapsDir.exists()) {
            mapsDir.mkdir();
        }

        createLevel1();
        createLevel2();
        createLevel3();
        createDualMap();
    }

    private void createDefaultLevels() {
        File levelsDir = new File("levels");
        if (!levelsDir.exists()) {
            levelsDir.mkdir();
        }

        try {
            File l1 = new File("levels/level_1.json");
            if (!l1.exists()) {
                createDefaultLevelConfig(1, "maps/level1.txt", "第 1 关 - 新兵训练营").saveToFile("levels/level_1.json");
            }
            File l2 = new File("levels/level_2.json");
            if (!l2.exists()) {
                createDefaultLevelConfig(2, "maps/level2.txt", "第 2 关 - 城市巷战").saveToFile("levels/level_2.json");
            }
            File l3 = new File("levels/level_3.json");
            if (!l3.exists()) {
                createDefaultLevelConfig(3, "maps/level3.txt", "第 3 关 - 最终决战").saveToFile("levels/level_3.json");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LevelConfig createDefaultLevelConfig(int levelNum, String mapFile, String name) {
        LevelConfig cfg = new LevelConfig(levelNum);
        cfg.setLevelName(name);
        cfg.setMapFile(mapFile);

        for (int i = 0; i < 5; i++) {
            cfg.addSpawnPoint(100 + i * 130, 50);
        }

        List<WaveConfig> waves = new ArrayList<>();
        switch (levelNum) {
            case 1:
                waves.add(createWave(1, 8, 80, 15, 5, 3000, 4, 0));
                waves.add(createWave(2, 10, 70, 20, 10, 2500, 4, 2));
                waves.add(createWave(3, 12, 60, 25, 15, 2000, 5, 3));
                break;
            case 2:
                waves.add(createWave(1, 10, 70, 20, 10, 2800, 4, 0));
                waves.add(createWave(2, 12, 60, 25, 15, 2300, 5, 2));
                waves.add(createWave(3, 15, 50, 30, 20, 2000, 6, 3));
                waves.add(createWave(4, 10, 40, 30, 30, 1800, 6, 2));
                break;
            case 3:
                waves.add(createWave(1, 12, 60, 25, 15, 2500, 5, 0));
                waves.add(createWave(2, 15, 50, 30, 20, 2200, 5, 3));
                waves.add(createWave(3, 18, 40, 35, 25, 1800, 6, 3));
                waves.add(createWave(4, 15, 30, 35, 35, 1500, 7, 2));
                waves.add(createWave(5, 20, 25, 35, 40, 1200, 8, 5));
                break;
            default:
                waves.add(createWave(1, 10, 70, 20, 10, 3000, 4, 0));
        }
        cfg.setWaves(waves);
        return cfg;
    }

    private WaveConfig createWave(int num, int total, int normal, int fast, int heavy,
                                   int interval, int maxOnScreen, int trigger) {
        WaveConfig w = new WaveConfig(num);
        w.setTotalEnemies(total);
        w.setEnemyTypeRatio(new EnemyTypeRatio(normal, fast, heavy));
        w.setSpawnInterval(interval);
        w.setMaxEnemiesOnScreen(maxOnScreen);
        w.setTriggerRemainingEnemies(trigger);
        return w;
    }

    private void createLevel1() {
        try {
            GameMap map = new GameMap(mapWidth, mapHeight);
            for (int i = 0; i < 5; i++) {
                map.addEnemySpawn(100 + i * 150, 50);
            }
            for (int row = 3; row < 15; row += 2) {
                for (int col = 2; col < 23; col += 3) {
                    if (Math.random() > 0.3) {
                        map.addObstacle(new com.tankbattle.model.Obstacle(
                                col * 32, row * 32,
                                Math.random() > 0.7 ? com.tankbattle.model.ObstacleType.STEEL :
                                        com.tankbattle.model.ObstacleType.BRICK));
                    }
                }
            }
            int baseX = mapWidth / 2 - 16;
            int baseY = mapHeight - 48;
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX, baseY, com.tankbattle.model.ObstacleType.BASE));
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX - 32, baseY, com.tankbattle.model.ObstacleType.BRICK));
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX + 32, baseY, com.tankbattle.model.ObstacleType.BRICK));
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX - 32, baseY - 32, com.tankbattle.model.ObstacleType.BRICK));
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX, baseY - 32, com.tankbattle.model.ObstacleType.BRICK));
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX + 32, baseY - 32, com.tankbattle.model.ObstacleType.BRICK));

            map.saveToFile("maps/level1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLevel2() {
        try {
            GameMap map = new GameMap(mapWidth, mapHeight);
            for (int i = 0; i < 6; i++) {
                map.addEnemySpawn(80 + i * 130, 50);
            }
            for (int row = 2; row < 16; row++) {
                for (int col = 1; col < 24; col++) {
                    if ((row + col) % 4 == 0) {
                        com.tankbattle.model.ObstacleType type = com.tankbattle.model.ObstacleType.BRICK;
                        if (row % 5 == 0) type = com.tankbattle.model.ObstacleType.STEEL;
                        if (row % 7 == 0) type = com.tankbattle.model.ObstacleType.WATER;
                        map.addObstacle(new com.tankbattle.model.Obstacle(col * 32, row * 32, type));
                    }
                }
            }
            int baseX = mapWidth / 2 - 16;
            int baseY = mapHeight - 48;
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX, baseY, com.tankbattle.model.ObstacleType.BASE));
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 0; j++) {
                    if (i == 0 && j == 0) continue;
                    map.addObstacle(new com.tankbattle.model.Obstacle(
                            baseX + i * 32, baseY + j * 32, com.tankbattle.model.ObstacleType.STEEL));
                }
            }
            map.saveToFile("maps/level2.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLevel3() {
        try {
            GameMap map = new GameMap(mapWidth, mapHeight);
            for (int i = 0; i < 8; i++) {
                map.addEnemySpawn(60 + i * 100, 50);
            }
            for (int col = 0; col < 25; col++) {
                map.addObstacle(new com.tankbattle.model.Obstacle(col * 32, 6 * 32, com.tankbattle.model.ObstacleType.BRICK));
                map.addObstacle(new com.tankbattle.model.Obstacle(col * 32, 12 * 32, com.tankbattle.model.ObstacleType.BRICK));
            }
            for (int i = 0; i < 5; i++) {
                map.addObstacle(new com.tankbattle.model.Obstacle(100 + i * 32, 6 * 32, com.tankbattle.model.ObstacleType.GRASS));
                map.addObstacle(new com.tankbattle.model.Obstacle(600 + i * 32, 12 * 32, com.tankbattle.model.ObstacleType.GRASS));
            }
            for (int row = 6; row < 12; row++) {
                map.addObstacle(new com.tankbattle.model.Obstacle(12 * 32, row * 32, com.tankbattle.model.ObstacleType.STEEL));
            }
            int baseX = mapWidth / 2 - 16;
            int baseY = mapHeight - 48;
            map.addObstacle(new com.tankbattle.model.Obstacle(baseX, baseY, com.tankbattle.model.ObstacleType.BASE));
            map.saveToFile("maps/level3.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDualMap() {
        try {
            GameMap map = new GameMap(mapWidth, mapHeight);
            for (int i = 0; i < 6; i++) {
                map.addEnemySpawn(100 + i * 120, 50);
            }
            for (int row = 3; row < 15; row++) {
                for (int col = 0; col < 25; col++) {
                    if (col == 12 && row > 4 && row < 14) continue;
                    if ((row * col) % 7 == 0) {
                        map.addObstacle(new com.tankbattle.model.Obstacle(col * 32, row * 32,
                                Math.random() > 0.5 ? com.tankbattle.model.ObstacleType.BRICK :
                                        com.tankbattle.model.ObstacleType.STEEL));
                    }
                }
            }
            map.saveToFile("maps/dual.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSinglePlayer(String mapFile) {
        startGame(mapFile, GameEngine.GameMode.SINGLE_PLAYER, null);
    }

    @Override
    public void onTwoPlayer(String mapFile) {
        startGame(mapFile, GameEngine.GameMode.TWO_PLAYER, null);
    }

    @Override
    public void onMapEditor() {
        cardLayout.show(mainPanel, "EDITOR");
        mapEditorPanel.requestFocus();
    }

    @Override
    public void onLevelDesigner() {
        cardLayout.show(mainPanel, "LEVEL_DESIGNER");
        levelDesignerPanel.requestFocus();
    }

    @Override
    public void onCampaign(String levelConfigFile) {
        List<LevelConfig> levels = loadCampaignLevels(levelConfigFile);
        startCampaignGame(levels, GameEngine.GameMode.SINGLE_PLAYER);
    }

    private List<LevelConfig> loadCampaignLevels(String specificFile) {
        List<LevelConfig> result = new ArrayList<>();

        if (specificFile != null && !specificFile.isEmpty()) {
            try {
                LevelConfig cfg = LevelConfig.loadFromFile(specificFile);
                result.add(cfg);
                return result;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "加载关卡配置失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        for (int i = 1; i <= 10; i++) {
            File f = new File("levels/level_" + i + ".json");
            if (f.exists()) {
                try {
                    result.add(LevelConfig.loadFromFile(f.getPath()));
                } catch (IOException ignored) {}
            }
        }

        if (result.isEmpty()) {
            result.add(createDefaultLevelConfig(1, "maps/level1.txt", "第 1 关"));
            result.add(createDefaultLevelConfig(2, "maps/level2.txt", "第 2 关"));
            result.add(createDefaultLevelConfig(3, "maps/level3.txt", "第 3 关"));
        }
        return result;
    }

    @Override
    public void onLeaderboard() {
        leaderboardPanel.refreshData();
        cardLayout.show(mainPanel, "LEADERBOARD");
        leaderboardPanel.requestFocus();
    }

    @Override
    public void onExit() {
        System.exit(0);
    }

    private void startGame(String mapFile, GameEngine.GameMode mode, LevelConfig singleLevel) {
        try {
            currentMap = new GameMap(mapWidth, mapHeight);
            File file = new File(mapFile);
            if (file.exists()) {
                currentMap.loadFromFile(mapFile);
            } else {
                JOptionPane.showMessageDialog(this, "地图文件不存在: " + mapFile, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (singleLevel != null) {
                engine = new GameEngine(currentMap, mode, singleLevel, System.currentTimeMillis());
            } else {
                engine = new GameEngine(currentMap, mode);
            }

            if (gamePanel != null) {
                gamePanel.stop();
                mainPanel.remove(gamePanel);
            }
            gamePanel = new GamePanel(engine, this);
            gamePanel.setMapFile(mapFile);
            gamePanel.setPreferredSize(new Dimension(mapWidth + sidePanelWidth, mapHeight));
            gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
            gamePanel.startRecording();
            mainPanel.add(gamePanel, "GAME");
            cardLayout.show(mainPanel, "GAME");
            gamePanel.requestFocus();
            pack();
            setSize(mapWidth + sidePanelWidth + 20, mapHeight + 60);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载地图失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void startCampaignGame(List<LevelConfig> levels, GameEngine.GameMode mode) {
        if (levels == null || levels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可用的关卡配置", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String firstMap = levels.get(0).getMapFile();
            if (firstMap == null || firstMap.isEmpty()) firstMap = "maps/level1.txt";

            currentMap = new GameMap(mapWidth, mapHeight);
            File file = new File(firstMap);
            if (file.exists()) {
                currentMap.loadFromFile(firstMap);
            } else {
                currentMap.loadFromFile("maps/level1.txt");
            }

            engine = new GameEngine(currentMap, mode, levels, System.currentTimeMillis());

            if (gamePanel != null) {
                gamePanel.stop();
                mainPanel.remove(gamePanel);
            }
            gamePanel = new GamePanel(engine, this);
            gamePanel.setMapFile(firstMap);
            gamePanel.setPreferredSize(new Dimension(mapWidth + sidePanelWidth, mapHeight));
            gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
            gamePanel.startRecording();
            mainPanel.add(gamePanel, "GAME");
            cardLayout.show(mainPanel, "GAME");
            gamePanel.requestFocus();
            pack();
            setSize(mapWidth + sidePanelWidth + 20, mapHeight + 60);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "启动失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void startReplay(ScoreRecord record) {
        GameReplay replay = highScoreManager.loadReplay(record.getReplayFileName());
        if (replay == null) {
            JOptionPane.showMessageDialog(this, "回放文件不存在或已损坏", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String mapFile = replay.getMapFile();
            if (mapFile == null || mapFile.isEmpty()) {
                mapFile = "maps/level1.txt";
            }
            File file = new File(mapFile);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "地图文件不存在: " + mapFile, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentMap = new GameMap(mapWidth, mapHeight);
            currentMap.loadFromFile(mapFile);

            GameEngine.GameMode mode = replay.getGameMode() == 0 ?
                    GameEngine.GameMode.SINGLE_PLAYER : GameEngine.GameMode.TWO_PLAYER;

            engine = new GameEngine(currentMap, mode, replay.getRandomSeed());
            engine.setReplayMode(true);

            if (gamePanel != null) {
                gamePanel.stop();
                mainPanel.remove(gamePanel);
            }

            gamePanel = new GamePanel(engine, this);
            gamePanel.setMapFile(mapFile);
            gamePanel.setPreferredSize(new Dimension(mapWidth + sidePanelWidth, mapHeight));
            gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
            gamePanel.startReplay(replay);

            mainPanel.add(gamePanel, "GAME");
            cardLayout.show(mainPanel, "GAME");
            gamePanel.requestFocus();
            pack();
            setSize(mapWidth + sidePanelWidth + 20, mapHeight + 60);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载地图失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void returnToMenu() {
        if (gamePanel != null) {
            gamePanel.stop();
        }
        cardLayout.show(mainPanel, "MENU");
    }

    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onGameOver(boolean victory, int score, int level) {
        if (victory) {
            highScoreManager.checkAndUpdateHighScore(score, level);
            if (gamePanel != null) {
                gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
            }
        } else {
            highScoreManager.checkAndUpdateHighScore(score, level);
            if (gamePanel != null) {
                gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
            }
        }
    }

    @Override
    public void onScoreUpdate(int score) {
        highScoreManager.checkAndUpdateHighScore(score, engine.getLevel());
        if (gamePanel != null) {
            gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
        }
    }

    @Override
    public void onReturnToMenu() {
        if (gamePanel != null && gamePanel.isReplaying()) {
            gamePanel.stopReplay();
        }
        returnToMenu();
    }

    @Override
    public void onSaveScore(String playerName, int score, int level, GameReplay replay) {
        highScoreManager.addScore(playerName, score, level, replay);
        if (gamePanel != null) {
            gamePanel.setHighScore(highScoreManager.getHighScore(), highScoreManager.getHighLevel());
        }
    }

    @Override
    public void onReplayEnd() {
    }

    @Override
    public void onBack() {
        cardLayout.show(mainPanel, "MENU");
    }

    @Override
    public void onReplay(ScoreRecord record) {
        if (record.getReplayFileName() == null || record.getReplayFileName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "该记录没有回放数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        startReplay(record);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (mapEditorPanel != null && mapEditorPanel.isShowing()) {
                cardLayout.show(mainPanel, "MENU");
            }
            if (levelDesignerPanel != null && levelDesignerPanel.isShowing()) {
                cardLayout.show(mainPanel, "MENU");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
