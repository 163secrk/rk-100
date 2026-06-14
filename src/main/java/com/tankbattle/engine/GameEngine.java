package com.tankbattle.engine;

import com.tankbattle.model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private GameMap map;
    private List<Tank> tanks;
    private List<Bullet> bullets;
    private List<Tank> players;
    private List<Tank> enemies;
    private boolean gameOver;
    private boolean victory;
    private int score;
    private int level;
    private int enemiesRemaining;
    private int enemiesKilled;
    private int maxEnemiesOnScreen;
    private long lastEnemySpawn;
    private int enemySpawnInterval;
    private Random random;
    private GameMode mode;

    public enum GameMode {
        SINGLE_PLAYER, TWO_PLAYER
    }

    public GameEngine(GameMap map, GameMode mode) {
        this.map = map;
        this.mode = mode;
        this.tanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.players = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.random = new Random();
        this.maxEnemiesOnScreen = 4;
        this.enemySpawnInterval = 3000;
        this.gameOver = false;
        this.victory = false;
        this.score = 0;
        this.level = 1;
        initGame();
    }

    private void initGame() {
        map.resetObstacles();
        tanks.clear();
        bullets.clear();
        players.clear();
        enemies.clear();

        Tank player1 = new Tank(map.getPlayer1Spawn()[0], map.getPlayer1Spawn()[1], true, 1);
        players.add(player1);
        tanks.add(player1);

        if (mode == GameMode.TWO_PLAYER) {
            Tank player2 = new Tank(map.getPlayer2Spawn()[0], map.getPlayer2Spawn()[1], true, 2);
            players.add(player2);
            tanks.add(player2);
        }

        enemiesRemaining = map.getEnemySpawnPoints().size();
        if (enemiesRemaining == 0) {
            enemiesRemaining = 10 + level * 5;
        }
        enemiesKilled = 0;
        lastEnemySpawn = 0;
        gameOver = false;
        victory = false;
    }

    public void update() {
        if (gameOver) return;

        for (Bullet bullet : bullets) {
            bullet.update();
            if (bullet.isOutOfBounds(map.getWidth(), map.getHeight())) {
                bullet.setActive(false);
            }
        }

        updateEnemyAI();
        spawnEnemies();
        checkCollisions();
        cleanup();
        checkGameEnd();
    }

    private void updateEnemyAI() {
        for (Tank enemy : enemies) {
            if (!enemy.isAlive()) continue;

            Tank targetPlayer = findNearestPlayer(enemy);
            Direction separateDir = getSeparationDirection(enemy);

            if (separateDir != null && random.nextInt(100) < 60) {
                enemy.setDirection(separateDir);
            } else if (targetPlayer != null && random.nextInt(100) < 40) {
                Direction chaseDir = getChaseDirection(enemy, targetPlayer);
                if (chaseDir != null) {
                    enemy.setDirection(chaseDir);
                }
            } else {
                if (random.nextInt(100) < 15) {
                    Direction[] dirs = Direction.values();
                    enemy.setDirection(dirs[random.nextInt(dirs.length)]);
                }
            }

            enemy.move(enemy.getDirection(), map, tanks);

            int shootChance = 2;
            if (targetPlayer != null && isAlignedWithPlayer(enemy, targetPlayer)) {
                shootChance = 10;
            }
            if (random.nextInt(100) < shootChance) {
                Bullet bullet = enemy.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
    }

    private Direction getSeparationDirection(Tank enemy) {
        int tooCloseDistance = Tank.SIZE * 2;
        int dx = 0, dy = 0;
        int neighborCount = 0;

        for (Tank other : enemies) {
            if (other == enemy || !other.isAlive()) continue;
            double dist = getDistance(enemy, other);
            if (dist < tooCloseDistance && dist > 0) {
                dx += enemy.getX() - other.getX();
                dy += enemy.getY() - other.getY();
                neighborCount++;
            }
        }

        if (neighborCount == 0) return null;

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    private Tank findNearestPlayer(Tank enemy) {
        Tank nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Tank player : players) {
            if (!player.isAlive()) continue;
            double dist = getDistance(enemy, player);
            if (dist < minDist) {
                minDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    private double getDistance(Tank a, Tank b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private Direction getChaseDirection(Tank enemy, Tank player) {
        int dx = player.getX() - enemy.getX();
        int dy = player.getY() - enemy.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    private boolean isAlignedWithPlayer(Tank enemy, Tank player) {
        Direction dir = enemy.getDirection();
        int ex = enemy.getX() + Tank.SIZE / 2;
        int ey = enemy.getY() + Tank.SIZE / 2;
        int px = player.getX() + Tank.SIZE / 2;
        int py = player.getY() + Tank.SIZE / 2;
        int tolerance = Tank.SIZE / 2;

        switch (dir) {
            case UP:
                return py < ey && Math.abs(ex - px) < tolerance;
            case DOWN:
                return py > ey && Math.abs(ex - px) < tolerance;
            case LEFT:
                return px < ex && Math.abs(ey - py) < tolerance;
            case RIGHT:
                return px > ex && Math.abs(ey - py) < tolerance;
        }
        return false;
    }

    private void spawnEnemies() {
        if (enemiesRemaining <= 0) return;
        long now = System.currentTimeMillis();
        if (now - lastEnemySpawn < enemySpawnInterval) return;
        if (enemies.size() >= maxEnemiesOnScreen) return;

        List<Tank> spawnPoints = map.getEnemySpawnPoints();
        if (spawnPoints.isEmpty()) {
            int[] spawnPositions = {
                    map.getWidth() / 4, 50,
                    map.getWidth() / 2, 50,
                    map.getWidth() * 3 / 4, 50
            };
            int idx = random.nextInt(3) * 2;
            Tank enemy = new Tank(spawnPositions[idx], spawnPositions[idx + 1], false, 0);
            enemies.add(enemy);
            tanks.add(enemy);
        } else {
            Tank spawn = spawnPoints.get(random.nextInt(spawnPoints.size()));
            Tank enemy = new Tank(spawn.getX(), spawn.getY(), false, 0);
            enemies.add(enemy);
            tanks.add(enemy);
        }
        enemiesRemaining--;
        lastEnemySpawn = now;
    }

    private void checkCollisions() {
        List<Bullet> bulletsCopy = new ArrayList<>(bullets);
        for (int i = 0; i < bulletsCopy.size(); i++) {
            Bullet bullet = bulletsCopy.get(i);
            if (!bullet.isActive()) continue;
            Rectangle bulletRect = bullet.getBounds();

            for (int j = i + 1; j < bulletsCopy.size(); j++) {
                Bullet otherBullet = bulletsCopy.get(j);
                if (!otherBullet.isActive()) continue;
                if (bullet.isFromPlayer() == otherBullet.isFromPlayer()) continue;
                Rectangle otherBulletRect = otherBullet.getBounds();
                if (bulletRect.intersects(otherBulletRect)) {
                    bullet.setActive(false);
                    otherBullet.setActive(false);
                    break;
                }
            }

            if (!bullet.isActive()) continue;

            for (Obstacle obs : map.getObstacles()) {
                if (obs.isDestroyed()) continue;
                if (!obs.blocksBullets()) continue;
                Rectangle obsRect = new Rectangle(obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight());
                if (bulletRect.intersects(obsRect)) {
                    bullet.setActive(false);
                    if (obs.isDestructible()) {
                        obs.setDestroyed(true);
                    }
                    if (obs.getType() == ObstacleType.BASE) {
                        gameOver = true;
                        victory = false;
                    }
                    break;
                }
            }

            if (!bullet.isActive()) continue;

            for (Tank tank : tanks) {
                if (!tank.isAlive()) continue;
                if (bullet.isFromPlayer() && tank.isPlayer() && bullet.getPlayerId() == tank.getPlayerId()) continue;
                if (!bullet.isFromPlayer() && !tank.isPlayer()) continue;
                Rectangle tankRect = new Rectangle(tank.getX(), tank.getY(), Tank.SIZE, Tank.SIZE);
                if (bulletRect.intersects(tankRect)) {
                    bullet.setActive(false);
                    tank.takeDamage();
                    if (!tank.isAlive()) {
                        if (tank.isPlayer()) {
                            if (mode == GameMode.SINGLE_PLAYER) {
                                gameOver = true;
                                victory = false;
                            } else {
                                boolean allDead = true;
                                for (Tank p : players) {
                                    if (p.isAlive()) allDead = false;
                                }
                                if (allDead) {
                                    gameOver = true;
                                    victory = false;
                                }
                            }
                        } else {
                            enemiesKilled++;
                            score += 100;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void cleanup() {
        bullets.removeIf(b -> !b.isActive());
        tanks.removeIf(t -> !t.isAlive());
        enemies.removeIf(t -> !t.isAlive());
    }

    private void checkGameEnd() {
        if (gameOver) return;
        if (enemiesRemaining <= 0 && enemies.isEmpty()) {
            gameOver = true;
            victory = true;
        }
    }

    public void playerMove(int playerId, Direction dir) {
        for (Tank player : players) {
            if (player.getPlayerId() == playerId && player.isAlive()) {
                player.move(dir, map, tanks);
            }
        }
    }

    public void playerShoot(int playerId) {
        for (Tank player : players) {
            if (player.getPlayerId() == playerId && player.isAlive()) {
                Bullet bullet = player.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
    }

    public void nextLevel() {
        level++;
        initGame();
    }

    public void restart() {
        level = 1;
        score = 0;
        initGame();
    }

    public GameMap getMap() { return map; }
    public List<Tank> getTanks() { return tanks; }
    public List<Bullet> getBullets() { return bullets; }
    public List<Tank> getPlayers() { return players; }
    public List<Tank> getEnemies() { return enemies; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getEnemiesRemaining() { return enemiesRemaining + enemies.size(); }
    public GameMode getMode() { return mode; }
}
