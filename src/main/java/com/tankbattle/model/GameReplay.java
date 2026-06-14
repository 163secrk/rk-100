package com.tankbattle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameReplay implements Serializable {
    private static final long serialVersionUID = 1L;

    private long randomSeed;
    private String mapFile;
    private int gameMode;
    private List<FrameInput> frameInputs;
    private int totalFrames;

    public static class FrameInput implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean p1Up;
        public boolean p1Down;
        public boolean p1Left;
        public boolean p1Right;
        public boolean p1Shoot;
        public boolean p2Up;
        public boolean p2Down;
        public boolean p2Left;
        public boolean p2Right;
        public boolean p2Shoot;

        public FrameInput() {
        }

        public FrameInput(boolean p1Up, boolean p1Down, boolean p1Left, boolean p1Right, boolean p1Shoot,
                          boolean p2Up, boolean p2Down, boolean p2Left, boolean p2Right, boolean p2Shoot) {
            this.p1Up = p1Up;
            this.p1Down = p1Down;
            this.p1Left = p1Left;
            this.p1Right = p1Right;
            this.p1Shoot = p1Shoot;
            this.p2Up = p2Up;
            this.p2Down = p2Down;
            this.p2Left = p2Left;
            this.p2Right = p2Right;
            this.p2Shoot = p2Shoot;
        }
    }

    public GameReplay(long randomSeed, String mapFile, int gameMode) {
        this.randomSeed = randomSeed;
        this.mapFile = mapFile;
        this.gameMode = gameMode;
        this.frameInputs = new ArrayList<>();
        this.totalFrames = 0;
    }

    public void addFrameInput(FrameInput input) {
        frameInputs.add(input);
        totalFrames++;
    }

    public FrameInput getFrameInput(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= frameInputs.size()) {
            return new FrameInput();
        }
        return frameInputs.get(frameIndex);
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public String getMapFile() {
        return mapFile;
    }

    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public List<FrameInput> getFrameInputs() {
        return frameInputs;
    }

    public void setFrameInputs(List<FrameInput> frameInputs) {
        this.frameInputs = frameInputs;
        this.totalFrames = frameInputs.size();
    }

    public int getTotalFrames() {
        return totalFrames;
    }
}
