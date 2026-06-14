package com.tankbattle.model.level;

public class EnemyTypeRatio {
    private int normal;
    private int fast;
    private int heavy;

    public EnemyTypeRatio() {
        this.normal = 70;
        this.fast = 20;
        this.heavy = 10;
    }

    public EnemyTypeRatio(int normal, int fast, int heavy) {
        this.normal = normal;
        this.fast = fast;
        this.heavy = heavy;
    }

    public int getNormal() {
        return normal;
    }

    public void setNormal(int normal) {
        this.normal = normal;
    }

    public int getFast() {
        return fast;
    }

    public void setFast(int fast) {
        this.fast = fast;
    }

    public int getHeavy() {
        return heavy;
    }

    public void setHeavy(int heavy) {
        this.heavy = heavy;
    }

    public int getTotal() {
        return normal + fast + heavy;
    }

    public void normalize() {
        int total = getTotal();
        if (total <= 0) {
            normal = 70;
            fast = 20;
            heavy = 10;
            return;
        }
        if (total == 100) return;
        normal = (int) Math.round(normal * 100.0 / total);
        fast = (int) Math.round(fast * 100.0 / total);
        heavy = 100 - normal - fast;
        if (heavy < 0) heavy = 0;
    }
}
