package com.tankbattle.model;

import java.awt.*;
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
    public static final int SIZE = 40;

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
        this.color = player ? (playerId == 1 ? Color.YELLOW : Color.CYAN) : Color.RED;
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

    public Bullet shoot() {
        if (!alive) return null;
        long now = System.currentTimeMillis();
        if (now - lastShotTime < shotCooldown) return null;
        lastShotTime = now;
        int bx = x + SIZE / 2 - Bullet.SIZE / 2;
        int by = y + SIZE / 2 - Bullet.SIZE / 2;
        switch (direction) {
            case UP: by = y - Bullet.SIZE; break;
            case DOWN: by = y + SIZE; break;
            case LEFT: bx = x - Bullet.SIZE; break;
            case RIGHT: bx = x + SIZE; break;
        }
        return new Bullet(bx, by, direction, bulletSpeed, player, playerId);
    }

    public void takeDamage() {
        health--;
        if (health <= 0) {
            alive = false;
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
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
