package com.tankbattle.ui;

import com.tankbattle.engine.GameEngine;
import com.tankbattle.model.GameMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame implements MenuPanel.MenuListener, GamePanel.GameUIListener, KeyListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private MapEditorPanel mapEditorPanel;
    private GameEngine engine;
    private GameMap currentMap;
    private int mapWidth = 800;
    private int mapHeight = 608;

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

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(20, 30, 50));
        mainPanel.setOpaque(true);

        menuPanel = new MenuPanel(this);
        mapEditorPanel = new MapEditorPanel(this);

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(mapEditorPanel, "EDITOR");

        setContentPane(mainPanel);
        pack();
        setSize(mapWidth + 20, mapHeight + 60);
        setLocationRelativeTo(null);

        createDefaultMaps();
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
        startGame(mapFile, GameEngine.GameMode.SINGLE_PLAYER);
    }

    @Override
    public void onTwoPlayer(String mapFile) {
        startGame(mapFile, GameEngine.GameMode.TWO_PLAYER);
    }

    @Override
    public void onMapEditor() {
        cardLayout.show(mainPanel, "EDITOR");
        mapEditorPanel.requestFocus();
    }

    @Override
    public void onExit() {
        System.exit(0);
    }

    private void startGame(String mapFile, GameEngine.GameMode mode) {
        try {
            currentMap = new GameMap(mapWidth, mapHeight);
            File file = new File(mapFile);
            if (file.exists()) {
                currentMap.loadFromFile(mapFile);
            } else {
                JOptionPane.showMessageDialog(this, "地图文件不存在: " + mapFile, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            engine = new GameEngine(currentMap, mode);
            if (gamePanel != null) {
                gamePanel.stop();
                mainPanel.remove(gamePanel);
            }
            gamePanel = new GamePanel(engine, this);
            gamePanel.setPreferredSize(new Dimension(mapWidth, mapHeight));
            mainPanel.add(gamePanel, "GAME");
            cardLayout.show(mainPanel, "GAME");
            gamePanel.requestFocus();
            pack();
            setSize(mapWidth + 20, mapHeight + 60);
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
    }

    @Override
    public void onScoreUpdate(int score) {
    }

    @Override
    public void onReturnToMenu() {
        returnToMenu();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (mapEditorPanel != null && mapEditorPanel.isShowing()) {
                cardLayout.show(mainPanel, "MENU");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
