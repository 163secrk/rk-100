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
        this.x = x;
        this.y = y;
        this.type = type;
        this.active = true;
        this.spawnTime = System.currentTimeMillis();
        this.lifetime = 10000;
    }

    public void update() {
        if (!active) return;
        long now = System.currentTimeMillis();
        if (now - spawnTime > lifetime) {
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
