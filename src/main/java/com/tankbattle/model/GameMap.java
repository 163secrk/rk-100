package com.tankbattle.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private int width;
    private int height;
    private List<Obstacle> obstacles;
    private List<Tank> enemySpawnPoints;
    private int[] player1Spawn;
    private int[] player2Spawn;
    private int[] basePosition;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.obstacles = new ArrayList<>();
        this.enemySpawnPoints = new ArrayList<>();
        this.player1Spawn = new int[]{100, height - 80};
        this.player2Spawn = new int[]{width - 140, height - 80};
        this.basePosition = new int[]{width / 2 - 16, height - 48};
    }

    public void addObstacle(Obstacle obs) {
        obstacles.add(obs);
    }

    public void addEnemySpawn(int x, int y) {
        enemySpawnPoints.add(new Tank(x, y, false, 0));
    }

    public void loadFromFile(String filename) throws IOException {
        obstacles.clear();
        enemySpawnPoints.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row * Obstacle.SIZE < height) {
                for (int col = 0; col < line.length() && col * Obstacle.SIZE < width; col++) {
                    char c = line.charAt(col);
                    int x = col * Obstacle.SIZE;
                    int y = row * Obstacle.SIZE;
                    switch (c) {
                        case 'B': addObstacle(new Obstacle(x, y, ObstacleType.BRICK)); break;
                        case 'S': addObstacle(new Obstacle(x, y, ObstacleType.STEEL)); break;
                        case 'W': addObstacle(new Obstacle(x, y, ObstacleType.WATER)); break;
                        case 'G': addObstacle(new Obstacle(x, y, ObstacleType.GRASS)); break;
                        case 'X': addObstacle(new Obstacle(x, y, ObstacleType.BASE)); break;
                        case 'E': addEnemySpawn(x, y); break;
                        case '1': player1Spawn = new int[]{x, y}; break;
                        case '2': player2Spawn = new int[]{x, y}; break;
                    }
                }
                row++;
            }
        }
    }

    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            int rows = height / Obstacle.SIZE;
            int cols = width / Obstacle.SIZE;
            char[][] grid = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = '.';
                }
            }
            for (Obstacle obs : obstacles) {
                int col = obs.getX() / Obstacle.SIZE;
                int row = obs.getY() / Obstacle.SIZE;
                if (row < rows && col < cols) {
                    switch (obs.getType()) {
                        case BRICK: grid[row][col] = 'B'; break;
                        case STEEL: grid[row][col] = 'S'; break;
                        case WATER: grid[row][col] = 'W'; break;
                        case GRASS: grid[row][col] = 'G'; break;
                        case BASE: grid[row][col] = 'X'; break;
                    }
                }
            }
            for (Tank spawn : enemySpawnPoints) {
                int col = spawn.getX() / Obstacle.SIZE;
                int row = spawn.getY() / Obstacle.SIZE;
                if (row < rows && col < cols) {
                    grid[row][col] = 'E';
                }
            }
            int p1Col = player1Spawn[0] / Obstacle.SIZE;
            int p1Row = player1Spawn[1] / Obstacle.SIZE;
            if (p1Row < rows && p1Col < cols) grid[p1Row][p1Col] = '1';
            int p2Col = player2Spawn[0] / Obstacle.SIZE;
            int p2Row = player2Spawn[1] / Obstacle.SIZE;
            if (p2Row < rows && p2Col < cols) grid[p2Row][p2Col] = '2';

            for (int i = 0; i < rows; i++) {
                bw.write(new String(grid[i]));
                bw.newLine();
            }
        }
    }

    public void resetObstacles() {
        for (Obstacle obs : obstacles) {
            obs.setDestroyed(false);
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Tank> getEnemySpawnPoints() { return enemySpawnPoints; }
    public int[] getPlayer1Spawn() { return player1Spawn; }
    public int[] getPlayer2Spawn() { return player2Spawn; }
    public int[] getBasePosition() { return basePosition; }

    public void setPlayer1Spawn(int x, int y) { player1Spawn = new int[]{x, y}; }
    public void setPlayer2Spawn(int x, int y) { player2Spawn = new int[]{x, y}; }
}
