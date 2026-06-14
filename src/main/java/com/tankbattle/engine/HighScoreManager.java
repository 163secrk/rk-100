package com.tankbattle.engine;

import java.util.prefs.Preferences;

public class HighScoreManager {
    private static final String HIGH_SCORE_KEY = "high_score";
    private static final String HIGH_LEVEL_KEY = "high_level";
    private Preferences prefs;

    public HighScoreManager() {
        prefs = Preferences.userNodeForPackage(HighScoreManager.class);
    }

    public int getHighScore() {
        return prefs.getInt(HIGH_SCORE_KEY, 0);
    }

    public int getHighLevel() {
        return prefs.getInt(HIGH_LEVEL_KEY, 1);
    }

    public boolean checkAndUpdateHighScore(int score, int level) {
        int currentHighScore = getHighScore();
        if (score > currentHighScore) {
            prefs.putInt(HIGH_SCORE_KEY, score);
            prefs.putInt(HIGH_LEVEL_KEY, level);
            return true;
        }
        return false;
    }

    public void resetHighScore() {
        prefs.putInt(HIGH_SCORE_KEY, 0);
        prefs.putInt(HIGH_LEVEL_KEY, 1);
    }
}
