package com.tankbattle.model.level;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LevelConfig {
    private int levelNumber;
    private String levelName;
    private String mapFile;
    private List<SpawnPoint> spawnPoints;
    private List<WaveConfig> waves;

    public LevelConfig() {
        this.levelNumber = 1;
        this.levelName = "新关卡";
        this.mapFile = "maps/level1.txt";
        this.spawnPoints = new ArrayList<>();
        this.waves = new ArrayList<>();
    }

    public LevelConfig(int levelNumber) {
        this();
        this.levelNumber = levelNumber;
        this.levelName = "第 " + levelNumber + " 关";
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getMapFile() {
        return mapFile;
    }

    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }

    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    public void setSpawnPoints(List<SpawnPoint> spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public void addSpawnPoint(int x, int y) {
        this.spawnPoints.add(new SpawnPoint(x, y));
    }

    public void removeSpawnPoint(int index) {
        if (index >= 0 && index < spawnPoints.size()) {
            spawnPoints.remove(index);
        }
    }

    public List<WaveConfig> getWaves() {
        return waves;
    }

    public void setWaves(List<WaveConfig> waves) {
        this.waves = waves;
    }

    public void addWave(WaveConfig wave) {
        this.waves.add(wave);
    }

    public void removeWave(int index) {
        if (index >= 0 && index < waves.size()) {
            waves.remove(index);
        }
    }

    public int getTotalEnemies() {
        int total = 0;
        for (WaveConfig wave : waves) {
            total += wave.getTotalEnemies();
        }
        return total;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("levelNumber", levelNumber);
        json.put("levelName", levelName);
        json.put("mapFile", mapFile);

        JSONArray spawnArray = new JSONArray();
        for (SpawnPoint sp : spawnPoints) {
            JSONObject spJson = new JSONObject();
            spJson.put("x", sp.getX());
            spJson.put("y", sp.getY());
            spawnArray.put(spJson);
        }
        json.put("spawnPoints", spawnArray);

        JSONArray wavesArray = new JSONArray();
        for (WaveConfig wave : waves) {
            JSONObject waveJson = new JSONObject();
            waveJson.put("waveNumber", wave.getWaveNumber());
            waveJson.put("totalEnemies", wave.getTotalEnemies());
            waveJson.put("spawnInterval", wave.getSpawnInterval());
            waveJson.put("maxEnemiesOnScreen", wave.getMaxEnemiesOnScreen());
            waveJson.put("triggerRemainingEnemies", wave.getTriggerRemainingEnemies());

            JSONObject ratioJson = new JSONObject();
            ratioJson.put("normal", wave.getEnemyTypeRatio().getNormal());
            ratioJson.put("fast", wave.getEnemyTypeRatio().getFast());
            ratioJson.put("heavy", wave.getEnemyTypeRatio().getHeavy());
            waveJson.put("enemyTypeRatio", ratioJson);

            wavesArray.put(waveJson);
        }
        json.put("waves", wavesArray);

        return json;
    }

    public static LevelConfig fromJSON(JSONObject json) {
        LevelConfig config = new LevelConfig();
        config.setLevelNumber(json.optInt("levelNumber", 1));
        config.setLevelName(json.optString("levelName", "关卡"));
        config.setMapFile(json.optString("mapFile", "maps/level1.txt"));

        List<SpawnPoint> spawnPoints = new ArrayList<>();
        JSONArray spawnArray = json.optJSONArray("spawnPoints");
        if (spawnArray != null) {
            for (int i = 0; i < spawnArray.length(); i++) {
                JSONObject spJson = spawnArray.getJSONObject(i);
                spawnPoints.add(new SpawnPoint(
                        spJson.optInt("x", 0),
                        spJson.optInt("y", 0)
                ));
            }
        }
        config.setSpawnPoints(spawnPoints);

        List<WaveConfig> waves = new ArrayList<>();
        JSONArray wavesArray = json.optJSONArray("waves");
        if (wavesArray != null) {
            for (int i = 0; i < wavesArray.length(); i++) {
                JSONObject waveJson = wavesArray.getJSONObject(i);
                WaveConfig wave = new WaveConfig();
                wave.setWaveNumber(waveJson.optInt("waveNumber", i + 1));
                wave.setTotalEnemies(waveJson.optInt("totalEnemies", 10));
                wave.setSpawnInterval(waveJson.optInt("spawnInterval", 3000));
                wave.setMaxEnemiesOnScreen(waveJson.optInt("maxEnemiesOnScreen", 4));
                wave.setTriggerRemainingEnemies(waveJson.optInt("triggerRemainingEnemies", 0));

                JSONObject ratioJson = waveJson.optJSONObject("enemyTypeRatio");
                if (ratioJson != null) {
                    EnemyTypeRatio ratio = new EnemyTypeRatio(
                            ratioJson.optInt("normal", 70),
                            ratioJson.optInt("fast", 20),
                            ratioJson.optInt("heavy", 10)
                    );
                    wave.setEnemyTypeRatio(ratio);
                }

                waves.add(wave);
            }
        }
        config.setWaves(waves);

        return config;
    }

    public void saveToFile(String filename) throws IOException {
        File file = new File(filename);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write(toJSON().toString(4));
        }
    }

    public static LevelConfig loadFromFile(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            return fromJSON(json);
        }
    }
}
