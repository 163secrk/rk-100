package com.tankbattle.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreRecord implements Serializable, Comparable<ScoreRecord> {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private int level;
    private long dateTime;
    private String replayFileName;

    public ScoreRecord(String playerName, int score, int level, long dateTime, String replayFileName) {
        this.playerName = playerName;
        this.score = score;
        this.level = level;
        this.dateTime = dateTime;
        this.replayFileName = replayFileName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getReplayFileName() {
        return replayFileName;
    }

    public void setReplayFileName(String replayFileName) {
        this.replayFileName = replayFileName;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(dateTime));
    }

    @Override
    public int compareTo(ScoreRecord other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return playerName + "\t" + score + "\t" + level + "\t" + dateTime + "\t" + replayFileName;
    }

    public static ScoreRecord fromString(String line) {
        String[] parts = line.split("\t", 5);
        if (parts.length != 5) return null;
        try {
            String playerName = parts[0];
            int score = Integer.parseInt(parts[1]);
            int level = Integer.parseInt(parts[2]);
            long dateTime = Long.parseLong(parts[3]);
            String replayFileName = parts[4];
            return new ScoreRecord(playerName, score, level, dateTime, replayFileName);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
