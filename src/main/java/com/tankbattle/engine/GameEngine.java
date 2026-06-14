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
    private List<PowerUp> powerUps;
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
    private long randomSeed;
    private GameMode mode;
    private long gameTime;
    private static final int FRAME_DURATION = 16;
    private boolean replayMode;

    public enum GameMode {
        SINGLE_PLAYER, TWO_PLAYER
    }

    public GameEngine(GameMap map, GameMode mode) {
        this(map, mode, System.currentTimeMillis());
    }

    public GameEngine(GameMap map, GameMode mode, long randomSeed) {
        this.map = map;
        this.mode = mode;
        this.randomSeed = randomSeed;
        this.tanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.players = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.random = new Random(randomSeed);
        this.maxEnemiesOnScreen = 4;
        this.enemySpawnInterval = 3000;
        this.gameOver = false;
        this.victory = false;
        this.score = 0;
        this.level = 1;
        this.gameTime = 0;
        this.replayMode = false;
        initGame();
    }

    private void initGame() {
        map.resetObstacles();
        tanks.clear();
        bullets.clear();
        players.clear();
        enemies.clear();
        powerUps.clear();

        Tank player1 = new Tank(map.getPlayer1Spawn()[0], map.getPlayer1Spawn()[1], true, 1);
        players.add(player1);
        tanks.add(player1);

        if (mode == GameMode.TWO_PLAYER) {
            Tank player2 = new Tank(map.getPlayer2Spawn()[0], map.getPlayer2Spawn()[1], true, 2);
            players.add(player2);
            tanks.add(player2);
        }

        int baseEnemies = map.getEnemySpawnPoints().size();
        enemiesRemaining = baseEnemies > 0 ? baseEnemies + (level - 1) * 3 : 10 + level * 5;
        maxEnemiesOnScreen = 4 + Math.min(level - 1, 4);
        enemySpawnInterval = Math.max(1500, 3000 - (level - 1) * 300);
        enemiesKilled = 0;
        lastEnemySpawn = 0;
        gameOver = false;
        victory = false;
    }

    public void update() {
        if (gameOver) return;

        gameTime += FRAME_DURATION;

        for (Bullet bullet : bullets) {
            bullet.update();
            if (bullet.isOutOfBounds(map.getWidth(), map.getHeight())) {
                bullet.setActive(false);
            }
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.update(gameTime);
        }

        updateEnemyAI();
        spawnEnemies();
        checkCollisions();
        checkPowerUpPickup();
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
                List<Bullet> newBullets = enemy.shoot(gameTime);
                if (newBullets != null) {
                    bullets.addAll(newBullets);
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
        if (gameTime - lastEnemySpawn < enemySpawnInterval) return;
        if (enemies.size() >= maxEnemiesOnScreen) return;

        List<Tank> spawnPoints = map.getEnemySpawnPoints();
        Tank enemy;
        if (spawnPoints.isEmpty()) {
            int[] spawnPositions = {
                    map.getWidth() / 4, 50,
                    map.getWidth() / 2, 50,
                    map.getWidth() * 3 / 4, 50
            };
            int idx = random.nextInt(3) * 2;
            enemy = new Tank(spawnPositions[idx], spawnPositions[idx + 1], false, 0);
            enemies.add(enemy);
            tanks.add(enemy);
        } else {
            Tank spawn = spawnPoints.get(random.nextInt(spawnPoints.size()));
            enemy = new Tank(spawn.getX(), spawn.getY(), false, 0);
            enemies.add(enemy);
            tanks.add(enemy);
        }

        int typeRoll = random.nextInt(100);
        if (typeRoll < 10) {
            enemy.setEnemyType(Tank.EnemyType.SPECIAL_STAR);
        } else if (typeRoll < 20) {
            enemy.setEnemyType(Tank.EnemyType.SPECIAL_SHIELD);
        } else if (typeRoll < 30) {
            enemy.setEnemyType(Tank.EnemyType.SPECIAL_BOMB);
        } else {
            enemy.setEnemyType(Tank.EnemyType.NORMAL);
        }

        int enemySpeed = 2 + Math.min(level - 1, 3);
        int enemyShotCooldown = Math.max(400, 800 - (level - 1) * 100);
        enemy.setSpeed(enemySpeed);
        enemy.setShotCooldown(enemyShotCooldown);

        enemiesRemaining--;
        lastEnemySpawn = gameTime;
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
                    tank.takeDamage(gameTime);
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
                            dropPowerUp(tank);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void dropPowerUp(Tank enemy) {
        Tank.EnemyType type = enemy.getEnemyType();
        PowerUpType powerUpType = null;
        switch (type) {
            case SPECIAL_STAR:
                powerUpType = PowerUpType.STAR;
                break;
            case SPECIAL_SHIELD:
                powerUpType = PowerUpType.SHIELD;
                break;
            case SPECIAL_BOMB:
                powerUpType = PowerUpType.BOMB;
                break;
            default:
                return;
        }
        int px = enemy.getX() + Tank.SIZE / 2 - PowerUp.SIZE / 2;
        int py = enemy.getY() + Tank.SIZE / 2 - PowerUp.SIZE / 2;
        powerUps.add(new PowerUp(px, py, powerUpType, gameTime));
    }

    private void checkPowerUpPickup() {
        for (Tank player : players) {
            if (!player.isAlive()) continue;
            Rectangle playerRect = new Rectangle(player.getX(), player.getY(), Tank.SIZE, Tank.SIZE);
            for (PowerUp powerUp : powerUps) {
                if (!powerUp.isActive()) continue;
                Rectangle powerUpRect = powerUp.getBounds();
                if (playerRect.intersects(powerUpRect)) {
                    applyPowerUp(player, powerUp);
                    powerUp.setActive(false);
                }
            }
        }
    }

    private void applyPowerUp(Tank player, PowerUp powerUp) {
        switch (powerUp.getType()) {
            case STAR:
                player.increaseFirepower();
                score += 50;
                break;
            case SHIELD:
                player.activateShield(8000, gameTime);
                score += 50;
                break;
            case BOMB:
                for (Tank enemy : enemies) {
                    if (enemy.isAlive()) {
                        enemy.setAlive(false);
                        enemiesKilled++;
                        score += 100;
                    }
                }
                bullets.clear();
                break;
        }
    }

    private void cleanup() {
        bullets.removeIf(b -> !b.isActive());
        tanks.removeIf(t -> !t.isAlive());
        enemies.removeIf(t -> !t.isAlive());
        powerUps.removeIf(p -> !p.isActive());
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
                List<Bullet> newBullets = player.shoot(gameTime);
                if (newBullets != null) {
                    bullets.addAll(newBullets);
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
        gameTime = 0;
        random = new Random(randomSeed);
        initGame();
    }

    public GameMap getMap() { return map; }
    public List<Tank> getTanks() { return tanks; }
    public List<Bullet> getBullets() { return bullets; }
    public List<Tank> getPlayers() { return players; }
    public List<Tank> getEnemies() { return enemies; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getEnemiesRemaining() { return enemiesRemaining + enemies.size(); }
    public GameMode getMode() { return mode; }
    public long getRandomSeed() { return randomSeed; }
    public long getGameTime() { return gameTime; }
    public boolean isReplayMode() { return replayMode; }
    public void setReplayMode(boolean replayMode) { this.replayMode = replayMode; }
    public static int getFrameDuration() { return FRAME_DURATION; }
}
