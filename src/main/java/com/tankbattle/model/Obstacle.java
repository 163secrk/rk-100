package com.tankbattle.model;

public class Obstacle {
    private int x, y;
    private ObstacleType type;
    private boolean destroyed;
    public static final int SIZE = 32;

    public Obstacle(int x, int y, ObstacleType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.destroyed = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public ObstacleType getType() { return type; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }

    public boolean isDestructible() {
        return type == ObstacleType.BRICK;
    }

    public boolean isPassable() {
        return type == ObstacleType.GRASS;
    }

    public boolean blocksBullets() {
        return type != ObstacleType.GRASS && type != ObstacleType.WATER;
    }

    public int getWidth() { return SIZE; }
    public int getHeight() { return SIZE; }
}
