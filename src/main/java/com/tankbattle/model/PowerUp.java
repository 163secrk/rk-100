package com.tankbattle.model;

import java.awt.*;

public class PowerUp {
    private int x, y;
    private PowerUpType type;
    private boolean active;
    private long spawnTime;
    private long lifetime;
    public static final int SIZE = 28;

    public PowerUp(int x, int y, PowerUpType type) {
        this(x, y, type, System.currentTimeMillis());
    }

    public PowerUp(int x, int y, PowerUpType type, long spawnTime) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.active = true;
        this.spawnTime = spawnTime;
        this.lifetime = 10000;
    }

    public void update() {
        update(System.currentTimeMillis());
    }

    public void update(long currentTime) {
        if (!active) return;
        if (currentTime - spawnTime > lifetime) {
            active = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public PowerUpType getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
