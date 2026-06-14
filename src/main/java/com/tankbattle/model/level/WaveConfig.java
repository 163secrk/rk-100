package com.tankbattle.model.level;

public class WaveConfig {
    private int waveNumber;
    private int totalEnemies;
    private EnemyTypeRatio enemyTypeRatio;
    private int spawnInterval;
    private int maxEnemiesOnScreen;
    private int triggerRemainingEnemies;

    public WaveConfig() {
        this.waveNumber = 1;
        this.totalEnemies = 10;
        this.enemyTypeRatio = new EnemyTypeRatio();
        this.spawnInterval = 3000;
        this.maxEnemiesOnScreen = 4;
        this.triggerRemainingEnemies = 0;
    }

    public WaveConfig(int waveNumber) {
        this();
        this.waveNumber = waveNumber;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    public int getTotalEnemies() {
        return totalEnemies;
    }

    public void setTotalEnemies(int totalEnemies) {
        this.totalEnemies = totalEnemies;
    }

    public EnemyTypeRatio getEnemyTypeRatio() {
        return enemyTypeRatio;
    }

    public void setEnemyTypeRatio(EnemyTypeRatio enemyTypeRatio) {
        this.enemyTypeRatio = enemyTypeRatio;
    }

    public int getSpawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public int getMaxEnemiesOnScreen() {
        return maxEnemiesOnScreen;
    }

    public void setMaxEnemiesOnScreen(int maxEnemiesOnScreen) {
        this.maxEnemiesOnScreen = maxEnemiesOnScreen;
    }

    public int getTriggerRemainingEnemies() {
        return triggerRemainingEnemies;
    }

    public void setTriggerRemainingEnemies(int triggerRemainingEnemies) {
        this.triggerRemainingEnemies = triggerRemainingEnemies;
    }
}
