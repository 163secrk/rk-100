package com.tankbattle.engine;

import com.tankbattle.model.GameReplay;
import com.tankbattle.model.ScoreRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final String SCORES_FILE = "scores.dat";
    private static final String REPLAYS_DIR = "replays";
    private static final int MAX_RECORDS = 100;

    private List<ScoreRecord> records;

    public HighScoreManager() {
        records = new ArrayList<>();
        loadScores();
        ensureReplaysDir();
    }

    private void ensureReplaysDir() {
        File dir = new File(REPLAYS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void loadScores() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                ScoreRecord record = ScoreRecord.fromString(line);
                if (record != null) {
                    records.add(record);
                }
            }
            Collections.sort(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORES_FILE))) {
            for (ScoreRecord record : records) {
                writer.write(record.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addScore(String playerName, int score, int level, GameReplay replay) {
        String replayFileName = null;
        if (replay != null) {
            replayFileName = saveReplay(replay);
        }
        long now = System.currentTimeMillis();
        ScoreRecord record = new ScoreRecord(playerName, score, level, now, replayFileName);
        records.add(record);
        Collections.sort(records);
        if (records.size() > MAX_RECORDS) {
            List<ScoreRecord> removed = records.subList(MAX_RECORDS, records.size());
            for (ScoreRecord r : removed) {
                deleteReplay(r.getReplayFileName());
            }
            records = new ArrayList<>(records.subList(0, MAX_RECORDS));
        }
        saveScores();
    }

    private String saveReplay(GameReplay replay) {
        ensureReplaysDir();
        String fileName = "replay_" + System.currentTimeMillis() + ".dat";
        File file = new File(REPLAYS_DIR, fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(replay);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public GameReplay loadReplay(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        File file = new File(REPLAYS_DIR, fileName);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameReplay) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteReplay(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        File file = new File(REPLAYS_DIR, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public List<ScoreRecord> getAllRecords() {
        return new ArrayList<>(records);
    }

    public List<ScoreRecord> getRecordsByLevel(int level) {
        List<ScoreRecord> result = new ArrayList<>();
        for (ScoreRecord record : records) {
            if (record.getLevel() == level) {
                result.add(record);
            }
        }
        Collections.sort(result);
        return result;
    }

    public List<ScoreRecord> getRecordsByTimeRange(long startTime, long endTime) {
        List<ScoreRecord> result = new ArrayList<>();
        for (ScoreRecord record : records) {
            if (record.getDateTime() >= startTime && record.getDateTime() <= endTime) {
                result.add(record);
            }
        }
        Collections.sort(result);
        return result;
    }

    public List<ScoreRecord> getRecordsByLevelAndTimeRange(int level, long startTime, long endTime) {
        List<ScoreRecord> result = new ArrayList<>();
        for (ScoreRecord record : records) {
            if (record.getLevel() == level &&
                record.getDateTime() >= startTime &&
                record.getDateTime() <= endTime) {
                result.add(record);
            }
        }
        Collections.sort(result);
        return result;
    }

    public int getHighScore() {
        if (records.isEmpty()) return 0;
        return records.get(0).getScore();
    }

    public int getHighLevel() {
        if (records.isEmpty()) return 1;
        int maxLevel = 1;
        for (ScoreRecord record : records) {
            if (record.getLevel() > maxLevel) {
                maxLevel = record.getLevel();
            }
        }
        return maxLevel;
    }

    public boolean checkAndUpdateHighScore(int score, int level) {
        int currentHighScore = getHighScore();
        return score > currentHighScore;
    }

    public void resetHighScore() {
        for (ScoreRecord record : records) {
            deleteReplay(record.getReplayFileName());
        }
        records.clear();
        saveScores();
    }

    public int getRecordCount() {
        return records.size();
    }

    public List<Integer> getAvailableLevels() {
        List<Integer> levels = new ArrayList<>();
        for (ScoreRecord record : records) {
            if (!levels.contains(record.getLevel())) {
                levels.add(record.getLevel());
            }
        }
        Collections.sort(levels);
        return levels;
    }
}
