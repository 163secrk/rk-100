package com.tankbattle.engine;

import com.tankbattle.model.Tank;
import com.tankbattle.model.level.EnemyTypeRatio;
import com.tankbattle.model.level.LevelConfig;
import com.tankbattle.model.level.SpawnPoint;
import com.tankbattle.model.level.WaveConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelRuntimeManager {
    private LevelConfig levelConfig;
    private List<WaveConfig> waves;
    private int currentWaveIndex;
    private WaveConfig currentWave;
    private int enemiesRemainingInWave;
    private int enemiesSpawnedInWave;
    private long lastSpawnTime;
    private boolean waveTriggered;
    private boolean allWavesComplete;
    private Random random;
    private long gameTime;

    public LevelRuntimeManager(LevelConfig config, long randomSeed) {
        this.levelConfig = config;
        this.waves = new ArrayList<>(config.getWaves());
        this.currentWaveIndex = 0;
        this.currentWave = waves.isEmpty() ? null : waves.get(0);
        this.enemiesRemainingInWave = currentWave != null ? currentWave.getTotalEnemies() : 0;
        this.enemiesSpawnedInWave = 0;
        this.lastSpawnTime = 0;
        this.waveTriggered = !waves.isEmpty();
        this.allWavesComplete = waves.isEmpty();
        this.random = new Random(randomSeed);
        this.gameTime = 0;
    }

    public void update(long currentGameTime, int aliveEnemiesCount) {
        this.gameTime = currentGameTime;

        if (allWavesComplete) return;

        checkWaveTransition(aliveEnemiesCount);
    }

    private void checkWaveTransition(int aliveEnemiesCount) {
        if (currentWave == null) return;

        boolean waveFinished = enemiesRemainingInWave <= 0 && aliveEnemiesCount == 0;
        boolean triggerConditionMet = aliveEnemiesCount <= currentWave.getTriggerRemainingEnemies()
                && enemiesSpawnedInWave >= currentWave.getTotalEnemies();

        if (waveFinished || triggerConditionMet) {
            advanceToNextWave();
        }
    }

    private void advanceToNextWave() {
        currentWaveIndex++;
        if (currentWaveIndex >= waves.size()) {
            allWavesComplete = true;
            currentWave = null;
        } else {
            currentWave = waves.get(currentWaveIndex);
            enemiesRemainingInWave = currentWave.getTotalEnemies();
            enemiesSpawnedInWave = 0;
            lastSpawnTime = 0;
            waveTriggered = true;
        }
    }

    public boolean canSpawnEnemy(long currentGameTime, int aliveEnemiesCount) {
        if (allWavesComplete || currentWave == null) return false;
        if (enemiesRemainingInWave <= 0) return false;
        if (aliveEnemiesCount >= currentWave.getMaxEnemiesOnScreen()) return false;

        int interval = currentWave.getSpawnInterval();
        if (lastSpawnTime == 0) {
            return true;
        }
        return (currentGameTime - lastSpawnTime) >= interval;
    }

    public Tank.EnemyType rollEnemyType() {
        if (currentWave == null) return Tank.EnemyType.NORMAL;

        EnemyTypeRatio ratio = currentWave.getEnemyTypeRatio();
        ratio.normalize();
        int roll = random.nextInt(100);

        if (roll < ratio.getNormal()) {
            return Tank.EnemyType.NORMAL;
        } else if (roll < ratio.getNormal() + ratio.getFast()) {
            return Tank.EnemyType.SPECIAL_STAR;
        } else {
            return Tank.EnemyType.SPECIAL_SHIELD;
        }
    }

    public SpawnPoint getRandomSpawnPoint() {
        List<SpawnPoint> points = levelConfig.getSpawnPoints();
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.get(random.nextInt(points.size()));
    }

    public void onEnemySpawned() {
        if (enemiesRemainingInWave > 0) {
            enemiesRemainingInWave--;
            enemiesSpawnedInWave++;
            lastSpawnTime = gameTime;
        }
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public WaveConfig getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public int getEnemiesRemainingInWave() {
        return enemiesRemainingInWave;
    }

    public int getEnemiesRemainingTotal() {
        int total = enemiesRemainingInWave;
        for (int i = currentWaveIndex + 1; i < waves.size(); i++) {
            total += waves.get(i).getTotalEnemies();
        }
        return total;
    }

    public boolean isAllWavesComplete() {
        return allWavesComplete;
    }

    public LevelConfig getLevelConfig() {
        return levelConfig;
    }
}
