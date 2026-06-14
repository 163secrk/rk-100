package com.tankbattle.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Tank {
    private int x, y;
    private Direction direction;
    private int speed;
    private boolean alive;
    private boolean player;
    private int playerId;
    private int health;
    private int maxHealth;
    private long lastShotTime;
    private int shotCooldown;
    private int bulletSpeed;
    private Color color;
    private int firepower;
    private boolean shielded;
    private long shieldEndTime;
    private EnemyType enemyType;
    public static final int SIZE = 40;

    public enum EnemyType {
        NORMAL,
        SPECIAL_STAR,
        SPECIAL_SHIELD,
        SPECIAL_BOMB
    }

    public Tank(int x, int y, boolean player, int playerId) {
        this.x = x;
        this.y = y;
        this.direction = player ? Direction.UP : Direction.DOWN;
        this.speed = 3;
        this.alive = true;
        this.player = player;
        this.playerId = playerId;
        this.maxHealth = player ? 3 : 1;
        this.health = maxHealth;
        this.shotCooldown = player ? 300 : 800;
        this.bulletSpeed = 8;
        this.firepower = 1;
        this.shielded = false;
        this.enemyType = EnemyType.NORMAL;
        if (player) {
            this.color = playerId == 1 ? Color.YELLOW : Color.CYAN;
        } else {
            this.color = Color.RED;
        }
    }

    public void move(Direction dir, GameMap map, List<Tank> allTanks) {
        if (!alive) return;
        this.direction = dir;
        int newX = x, newY = y;
        switch (dir) {
            case UP: newY -= speed; break;
            case DOWN: newY += speed; break;
            case LEFT: newX -= speed; break;
            case RIGHT: newX += speed; break;
        }
        if (canMoveTo(newX, newY, map, allTanks)) {
            x = newX;
            y = newY;
        }
    }

    private boolean canMoveTo(int newX, int newY, GameMap map, List<Tank> allTanks) {
        if (newX < 0 || newX + SIZE > map.getWidth() ||
            newY < 0 || newY + SIZE > map.getHeight()) {
            return false;
        }
        Rectangle tankRect = new Rectangle(newX, newY, SIZE, SIZE);
        for (Obstacle obs : map.getObstacles()) {
            if (!obs.isDestroyed() && !obs.isPassable()) {
                Rectangle obsRect = new Rectangle(obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight());
                if (tankRect.intersects(obsRect)) {
                    return false;
                }
            }
        }
        for (Tank other : allTanks) {
            if (other == this || !other.isAlive()) continue;
            Rectangle otherRect = new Rectangle(other.getX(), other.getY(), SIZE, SIZE);
            if (tankRect.intersects(otherRect)) {
                return false;
            }
        }
        return true;
    }

    public List<Bullet> shoot() {
        if (!alive) return null;
        long now = System.currentTimeMillis();
        if (now - lastShotTime < shotCooldown) return null;
        lastShotTime = now;
        List<Bullet> bullets = new ArrayList<>();
        int bx = x + SIZE / 2 - Bullet.SIZE / 2;
        int by = y + SIZE / 2 - Bullet.SIZE / 2;

        if (firepower >= 1) {
            int bx1 = bx, by1 = by;
            switch (direction) {
                case UP: by1 = y - Bullet.SIZE; break;
                case DOWN: by1 = y + SIZE; break;
                case LEFT: bx1 = x - Bullet.SIZE; break;
                case RIGHT: bx1 = x + SIZE; break;
            }
            bullets.add(new Bullet(bx1, by1, direction, bulletSpeed, player, playerId));
        }

        if (firepower >= 2) {
            int offset = 12;
            int bx2 = bx, by2 = by;
            switch (direction) {
                case UP:
                    by2 = y - Bullet.SIZE;
                    bx2 = bx - offset;
                    break;
                case DOWN:
                    by2 = y + SIZE;
                    bx2 = bx - offset;
                    break;
                case LEFT:
                    bx2 = x - Bullet.SIZE;
                    by2 = by - offset;
                    break;
                case RIGHT:
                    bx2 = x + SIZE;
                    by2 = by - offset;
                    break;
            }
            bullets.add(new Bullet(bx2, by2, direction, bulletSpeed, player, playerId));

            int bx3 = bx, by3 = by;
            switch (direction) {
                case UP:
                    by3 = y - Bullet.SIZE;
                    bx3 = bx + offset;
                    break;
                case DOWN:
                    by3 = y + SIZE;
                    bx3 = bx + offset;
                    break;
                case LEFT:
                    bx3 = x - Bullet.SIZE;
                    by3 = by + offset;
                    break;
                case RIGHT:
                    bx3 = x + SIZE;
                    by3 = by + offset;
                    break;
            }
            bullets.add(new Bullet(bx3, by3, direction, bulletSpeed, player, playerId));
        }

        return bullets;
    }

    public void takeDamage() {
        if (shielded && isShieldActive()) return;
        health--;
        if (health <= 0) {
            alive = false;
        }
    }

    public boolean isShieldActive() {
        if (!shielded) return false;
        long now = System.currentTimeMillis();
        if (now > shieldEndTime) {
            shielded = false;
            return false;
        }
        return true;
    }

    public void activateShield(long duration) {
        this.shielded = true;
        this.shieldEndTime = System.currentTimeMillis() + duration;
    }

    public void increaseFirepower() {
        if (firepower < 2) {
            firepower++;
        }
    }

    public void setEnemyType(EnemyType type) {
        this.enemyType = type;
        switch (type) {
            case NORMAL:
                this.color = Color.RED;
                break;
            case SPECIAL_STAR:
                this.color = new Color(255, 215, 0);
                break;
            case SPECIAL_SHIELD:
                this.color = new Color(0, 191, 255);
                break;
            case SPECIAL_BOMB:
                this.color = new Color(255, 69, 0);
                break;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public boolean isAlive() { return alive; }
    public boolean isPlayer() { return player; }
    public int getPlayerId() { return playerId; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public Color getColor() { return color; }
    public int getFirepower() { return firepower; }
    public EnemyType getEnemyType() { return enemyType; }
    public long getShieldEndTime() { return shieldEndTime; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public void setSpeed(int speed) { this.speed = speed; }
    public void setShotCooldown(int cooldown) { this.shotCooldown = cooldown; }
}
