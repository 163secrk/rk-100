package com.tankbattle.model;

import java.awt.*;

public class Bullet {
    private int x, y;
    private Direction direction;
    private int speed;
    private boolean active;
    private boolean fromPlayer;
    private int playerId;
    public static final int SIZE = 8;

    public Bullet(int x, int y, Direction direction, int speed, boolean fromPlayer, int playerId) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.active = true;
        this.fromPlayer = fromPlayer;
        this.playerId = playerId;
    }

    public void update() {
        if (!active) return;
        switch (direction) {
            case UP: y -= speed; break;
            case DOWN: y += speed; break;
            case LEFT: x -= speed; break;
            case RIGHT: x += speed; break;
        }
    }

    public boolean isOutOfBounds(int mapWidth, int mapHeight) {
        return x < 0 || x > mapWidth || y < 0 || y > mapHeight;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isFromPlayer() { return fromPlayer; }
    public int getPlayerId() { return playerId; }
}
