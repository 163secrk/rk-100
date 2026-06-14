package com.tankbattle.ui;

import com.tankbattle.engine.GameEngine;
import com.tankbattle.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private GameEngine engine;
    private Timer timer;
    private boolean[] keys1;
    private boolean[] keys2;
    private boolean paused;
    private GameUIListener listener;
    private int cellSize;
    private int sidePanelWidth;
    private int highScore;
    private int highLevel;
    private boolean levelTransition;
    private long transitionStartTime;
    private static final int TRANSITION_DELAY = 3000;

    private boolean recording;
    private boolean replaying;
    private GameReplay currentReplay;
    private int replayFrameIndex;
    private boolean nameInputShown;
    private String mapFile;

    public interface GameUIListener {
        void onGameOver(boolean victory, int score, int level);
        void onScoreUpdate(int score);
        void onReturnToMenu();
        void onSaveScore(String playerName, int score, int level, GameReplay replay);
        void onReplayEnd();
    }

    public GamePanel(GameEngine engine, GameUIListener listener) {
        this.engine = engine;
        this.listener = listener;
        this.keys1 = new boolean[256];
        this.keys2 = new boolean[256];
        this.paused = false;
        this.cellSize = Obstacle.SIZE;
        this.sidePanelWidth = 180;
        this.highScore = 0;
        this.highLevel = 1;
        this.levelTransition = false;
        this.transitionStartTime = 0;
        this.recording = false;
        this.replaying = false;
        this.currentReplay = null;
        this.replayFrameIndex = 0;
        this.nameInputShown = false;
        this.mapFile = "";
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this);
        timer.start();
    }

    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }

    public String getMapFile() {
        return mapFile;
    }

    public void startRecording() {
        if (engine != null) {
            currentReplay = new GameReplay(
                    engine.getRandomSeed(),
                    mapFile,
                    engine.getMode() == GameEngine.GameMode.SINGLE_PLAYER ? 0 : 1
            );
            recording = true;
        }
    }

    public void stopRecording() {
        recording = false;
    }

    public GameReplay getCurrentReplay() {
        return currentReplay;
    }

    public void startReplay(GameReplay replay) {
        if (replay == null) return;
        this.currentReplay = replay;
        this.replaying = true;
        this.recording = false;
        this.replayFrameIndex = 0;
        this.nameInputShown = false;
        this.paused = false;
        this.levelTransition = false;
    }

    public void stopReplay() {
        replaying = false;
        replayFrameIndex = 0;
    }

    public boolean isReplaying() {
        return replaying;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void setHighScore(int score, int level) {
        this.highScore = score;
        this.highLevel = level;
    }

    public int getSidePanelWidth() {
        return sidePanelWidth;
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void start() {
        if (timer != null) {
            timer.start();
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int gameWidth = getWidth() - sidePanelWidth;
        int gameHeight = getHeight();

        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, 0, gameWidth, gameHeight);

        g2d.setColor(new Color(20, 20, 30));
        g2d.fillRect(gameWidth, 0, sidePanelWidth, gameHeight);
        g2d.setColor(new Color(60, 60, 80));
        g2d.drawLine(gameWidth, 0, gameWidth, gameHeight);

        if (engine == null) return;

        GameMap map = engine.getMap();

        for (Obstacle obs : map.getObstacles()) {
            if (obs.isDestroyed()) continue;
            drawObstacle(g2d, obs);
        }

        for (Tank tank : engine.getTanks()) {
            if (tank.isAlive()) {
                drawTank(g2d, tank);
            }
        }

        for (Bullet bullet : engine.getBullets()) {
            if (bullet.isActive()) {
                drawBullet(g2d, bullet);
            }
        }

        for (PowerUp powerUp : engine.getPowerUps()) {
            if (powerUp.isActive()) {
                drawPowerUp(g2d, powerUp);
            }
        }

        for (Tank tank : engine.getTanks()) {
            if (tank.isAlive()) {
                drawHealthBar(g2d, tank);
                if (tank.isPlayer() && tank.isShieldActive()) {
                    drawShieldEffect(g2d, tank);
                }
            }
        }

        drawHUD(g2d);
        drawSidePanel(g2d);

        if (paused) {
            drawPauseScreen(g2d);
        }

        if (levelTransition) {
            drawLevelTransition(g2d);
        } else if (engine.isGameOver()) {
            drawGameOver(g2d);
        }
    }

    private void drawObstacle(Graphics2D g2d, Obstacle obs) {
        int x = obs.getX();
        int y = obs.getY();
        int w = obs.getWidth();
        int h = obs.getHeight();

        switch (obs.getType()) {
            case BRICK:
                g2d.setColor(new Color(180, 80, 20));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(120, 50, 10));
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        if ((i + j) % 2 == 0) {
                            g2d.fillRect(x + i * 8, y + j * 8, 8, 8);
                        }
                    }
                }
                g2d.setColor(new Color(90, 40, 10));
                g2d.drawRect(x, y, w, h);
                break;
            case STEEL:
                g2d.setColor(new Color(150, 150, 150));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(100, 100, 100));
                g2d.fillRect(x + 2, y + 2, w - 4, h - 4);
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(x + 4, y + 4, x + w - 4, y + h - 4);
                g2d.drawLine(x + w - 4, y + 4, x + 4, y + h - 4);
                break;
            case WATER:
                g2d.setColor(new Color(30, 100, 200));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(50, 130, 230));
                for (int i = 0; i < 3; i++) {
                    g2d.fillOval(x + 4 + i * 10, y + 10, 8, 4);
                    g2d.fillOval(x + 8 + i * 10, y + 20, 8, 4);
                }
                break;
            case GRASS:
                g2d.setColor(new Color(20, 150, 30));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(40, 180, 50));
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        if (randomBool(i + j)) {
                            g2d.fillRect(x + i * 5, y + j * 5, 5, 5);
                        }
                    }
                }
                break;
            case BASE:
                g2d.setColor(new Color(200, 200, 0));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(Color.RED);
                int[] px = {x + w/2, x + w, x + w/2, x};
                int[] py = {y, y + h/2, y + h, y + h/2};
                g2d.fillPolygon(px, py, 4);
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(x + w/2 - 4, y + h/2 - 4, 8, 8);
                break;
        }
    }

    private boolean randomBool(int seed) {
        return (seed * 31 + 17) % 3 == 0;
    }

    private void drawTank(Graphics2D g2d, Tank tank) {
        int x = tank.getX();
        int y = tank.getY();
        int size = Tank.SIZE;
        Direction dir = tank.getDirection();
        Color color = tank.getColor();

        g2d.setColor(color.darker());
        g2d.fillRect(x, y, size, size);

        g2d.setColor(color);
        g2d.fillRect(x + 4, y + 4, size - 8, size - 8);

        g2d.setColor(color.darker().darker());
        switch (dir) {
            case UP:
                g2d.fillRect(x + size/2 - 4, y - 8, 8, size/2 + 4);
                break;
            case DOWN:
                g2d.fillRect(x + size/2 - 4, y + size/2 - 4, 8, size/2 + 8);
                break;
            case LEFT:
                g2d.fillRect(x - 8, y + size/2 - 4, size/2 + 4, 8);
                break;
            case RIGHT:
                g2d.fillRect(x + size/2 - 4, y + size/2 - 4, size/2 + 8, 8);
                break;
        }

        g2d.setColor(color.brighter());
        g2d.fillOval(x + size/2 - 8, y + size/2 - 8, 16, 16);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, size, size);
    }

    private void drawBullet(Graphics2D g2d, Bullet bullet) {
        int x = bullet.getX();
        int y = bullet.getY();
        int size = Bullet.SIZE;

        g2d.setColor(bullet.isFromPlayer() ? Color.YELLOW : Color.RED);
        g2d.fillOval(x, y, size, size);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 2, y + 2, size - 4, size - 4);
    }

    private void drawHealthBar(Graphics2D g2d, Tank tank) {
        int x = tank.getX();
        int y = tank.getY() - 12;
        int width = Tank.SIZE;
        int height = 6;
        int health = tank.getHealth();
        int maxHealth = tank.getMaxHealth();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, y, width, height);

        float ratio = (float) health / maxHealth;
        Color healthColor = ratio > 0.5f ? Color.GREEN : ratio > 0.25f ? Color.YELLOW : Color.RED;
        g2d.setColor(healthColor);
        g2d.fillRect(x + 1, y + 1, (int)((width - 2) * ratio), height - 2);
    }

    private void drawPowerUp(Graphics2D g2d, PowerUp powerUp) {
        int x = powerUp.getX();
        int y = powerUp.getY();
        int size = PowerUp.SIZE;
        long elapsed = System.currentTimeMillis() % 1000;
        float pulse = 0.8f + 0.2f * (float) Math.sin(elapsed * Math.PI * 2 / 1000);

        switch (powerUp.getType()) {
            case STAR:
                g2d.setColor(new Color(255, 215, 0));
                drawStar(g2d, x + size / 2, y + size / 2, (int)(size / 2 * pulse), 5);
                break;
            case SHIELD:
                g2d.setColor(new Color(0, 191, 255));
                g2d.fillOval(x + 2, y + 2, size - 4, size - 4);
                g2d.setColor(new Color(135, 206, 250));
                g2d.fillOval(x + 6, y + 6, size - 12, size - 12);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                String s = "S";
                g2d.drawString(s, x + size / 2 - fm.stringWidth(s) / 2, y + size / 2 + fm.getAscent() / 2 - 2);
                break;
            case BOMB:
                g2d.setColor(new Color(255, 69, 0));
                g2d.fillOval(x + 2, y + 4, size - 4, size - 6);
                g2d.setColor(new Color(255, 140, 0));
                g2d.fillRect(x + size / 2 - 2, y, 4, 8);
                g2d.setColor(new Color(255, 215, 0));
                g2d.fillOval(x + size / 2 - 3, y - 4, 6, 6);
                break;
        }
    }

    private void drawStar(Graphics2D g2d, int cx, int cy, int r, int points) {
        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = i * Math.PI / points - Math.PI / 2;
            int radius = i % 2 == 0 ? r : r / 2;
            xPoints[i] = cx + (int) (Math.cos(angle) * radius);
            yPoints[i] = cy + (int) (Math.sin(angle) * radius);
        }
        g2d.fillPolygon(xPoints, yPoints, points * 2);
    }

    private void drawShieldEffect(Graphics2D g2d, Tank tank) {
        int x = tank.getX();
        int y = tank.getY();
        int size = Tank.SIZE;
        long remaining = tank.getShieldEndTime() - System.currentTimeMillis();
        float alpha = remaining > 2000 ? 0.6f : 0.3f + 0.3f * (float) Math.sin(System.currentTimeMillis() * 0.02);

        g2d.setColor(new Color(0, 191, 255, (int)(alpha * 255)));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(x - 4, y - 4, size + 8, size + 8);
        g2d.setColor(new Color(135, 206, 250, (int)(alpha * 150)));
        g2d.fillOval(x - 2, y - 2, size + 4, size + 4);
    }

    private void drawHUD(Graphics2D g2d) {
        int gameWidth = getWidth() - sidePanelWidth;

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2d.drawString("得分: " + engine.getScore(), 10, 25);

        if (replaying) {
            g2d.setColor(new Color(100, 200, 255));
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String replayText = "▶ 回放中 (" + replayFrameIndex + "/" + currentReplay.getTotalFrames() + "帧)";
            g2d.drawString(replayText, 10, 50);
        } else if (recording) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g2d.drawString("● 录制中", 10, 50);
        }

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2d.drawString("P:暂停  R:重新开始  ESC:返回菜单", gameWidth - 230, getHeight() - 10);
    }

    private void drawSidePanel(Graphics2D g2d) {
        int panelX = getWidth() - sidePanelWidth;
        int padding = 15;
        int currentY = padding;

        g2d.setColor(new Color(40, 40, 60));
        g2d.fillRect(panelX, 0, sidePanelWidth, getHeight());

        g2d.setColor(new Color(100, 100, 140));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(panelX, 0, panelX, getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        String title = "战斗信息";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, panelX + (sidePanelWidth - fm.stringWidth(title)) / 2, currentY + 20);
        currentY += 35;

        g2d.setColor(new Color(80, 80, 100));
        g2d.drawLine(panelX + padding, currentY, panelX + sidePanelWidth - padding, currentY);
        currentY += 15;

        g2d.setColor(new Color(255, 200, 50));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2d.drawString("第 " + engine.getLevel() + " 关", panelX + padding, currentY + 20);
        currentY += 30;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g2d.drawString("剩余敌人:", panelX + padding, currentY + 18);
        g2d.setColor(new Color(255, 80, 80));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g2d.drawString(String.valueOf(engine.getEnemiesRemaining()), panelX + sidePanelWidth - padding - 40, currentY + 20);
        currentY += 30;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g2d.drawString("当前得分:", panelX + padding, currentY + 18);
        g2d.setColor(new Color(255, 220, 100));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2d.drawString(String.valueOf(engine.getScore()), panelX + sidePanelWidth - padding - 60, currentY + 20);
        currentY += 25;

        g2d.setColor(new Color(80, 80, 100));
        g2d.drawLine(panelX + padding, currentY, panelX + sidePanelWidth - padding, currentY);
        currentY += 15;

        g2d.setColor(new Color(150, 200, 255));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.drawString("★ 历史最高", panelX + padding, currentY + 18);
        currentY += 25;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g2d.drawString("最高得分:", panelX + padding, currentY + 16);
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2d.drawString(String.valueOf(highScore), panelX + sidePanelWidth - padding - 60, currentY + 18);
        currentY += 25;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g2d.drawString("最高关卡:", panelX + padding, currentY + 16);
        g2d.setColor(new Color(100, 220, 100));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2d.drawString("第 " + highLevel + " 关", panelX + sidePanelWidth - padding - 60, currentY + 18);
        currentY += 25;

        g2d.setColor(new Color(80, 80, 100));
        g2d.drawLine(panelX + padding, currentY, panelX + sidePanelWidth - padding, currentY);
        currentY += 15;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.drawString("玩家状态", panelX + padding, currentY + 18);
        currentY += 25;

        for (Tank player : engine.getPlayers()) {
            if (!player.isAlive()) continue;
            String label = engine.getMode() == GameEngine.GameMode.TWO_PLAYER ?
                    "P" + player.getPlayerId() : "玩家";
            g2d.setColor(player.getColor());
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g2d.drawString(label, panelX + padding, currentY + 16);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g2d.drawString("生命: " + player.getHealth() + "/" + player.getMaxHealth(),
                    panelX + padding + 5, currentY + 36);

            g2d.drawString("火力: " + player.getFirepower(),
                    panelX + padding + 5, currentY + 54);

            if (player.isShieldActive()) {
                long remaining = (player.getShieldEndTime() - System.currentTimeMillis()) / 1000;
                g2d.setColor(new Color(0, 191, 255));
                g2d.drawString("护盾: " + remaining + "s",
                        panelX + padding + 5, currentY + 72);
                currentY += 78;
            } else {
                currentY += 60;
            }
        }
    }

    private void drawPauseScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "游戏暂停";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        text = "按 P 继续游戏 | 按 ESC 返回菜单";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 50);
    }

    private void drawLevelTransition(Graphics2D g2d) {
        g2d.setColor(new Color(0, 20, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        long elapsed = System.currentTimeMillis() - transitionStartTime;
        int remaining = (int) ((TRANSITION_DELAY - elapsed) / 1000) + 1;

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "关卡完成！";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 60);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 26));
        text = "第 " + engine.getLevel() + " 关 已通关";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);

        g2d.setColor(new Color(100, 255, 100));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        text = "即将进入第 " + (engine.getLevel() + 1) + " 关...";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 50);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        text = "本关得分: " + engine.getScore();
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 100);

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        text = "(" + remaining + " 秒后自动开始)";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 140);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 56));
        String text = replaying ? "回放结束" : (engine.isVictory() ? "胜利！" : "游戏结束");
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(replaying ? new Color(100, 200, 255) : (engine.isVictory() ? Color.YELLOW : Color.RED));
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 40);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        text = "最终得分: " + engine.getScore();
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 20);

        text = "关卡: " + engine.getLevel();
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 60);

        if (replaying) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            text = "按 R 重新回放 | 按 ESC 返回排行榜";
            fm = g2d.getFontMetrics();
            g2d.setColor(Color.GRAY);
            g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 110);
        } else if (!nameInputShown) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            text = "按 R 重新开始 | 按 ESC 返回菜单";
            fm = g2d.getFontMetrics();
            g2d.setColor(Color.GRAY);
            g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 110);
        } else {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            text = "成绩已保存 | 按 R 重新开始 | 按 ESC 返回菜单";
            fm = g2d.getFontMetrics();
            g2d.setColor(new Color(100, 200, 100));
            g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 110);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (engine == null) {
            repaint();
            return;
        }

        if (levelTransition) {
            long elapsed = System.currentTimeMillis() - transitionStartTime;
            if (elapsed >= TRANSITION_DELAY) {
                levelTransition = false;
                engine.nextLevel();
                if (listener != null) {
                    listener.onScoreUpdate(engine.getScore());
                }
            }
            repaint();
            return;
        }

        if (paused) {
            repaint();
            return;
        }

        if (replaying && currentReplay != null && replayFrameIndex >= currentReplay.getTotalFrames()) {
            stopReplay();
            if (listener != null) {
                listener.onReplayEnd();
            }
            repaint();
            return;
        }

        handleInput();
        engine.update();
        repaint();

        if (listener != null) {
            listener.onScoreUpdate(engine.getScore());
        }

        if (engine.isGameOver()) {
            if (engine.isVictory() && !levelTransition && !replaying) {
                levelTransition = true;
                transitionStartTime = System.currentTimeMillis();
            }

            if (!replaying && !nameInputShown && !engine.isVictory()) {
                nameInputShown = true;
                stopRecording();
                showNameInputDialog();
            }

            if (listener != null) {
                listener.onGameOver(engine.isVictory(), engine.getScore(), engine.getLevel());
            }
        }
    }

    private void showNameInputDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String playerName = JOptionPane.showInputDialog(
                        GamePanel.this,
                        "请输入你的名字:",
                        "保存成绩",
                        JOptionPane.PLAIN_MESSAGE
                );

                if (playerName != null && !playerName.trim().isEmpty()) {
                    playerName = playerName.trim();
                    if (playerName.length() > 20) {
                        playerName = playerName.substring(0, 20);
                    }
                    if (listener != null) {
                        listener.onSaveScore(playerName, engine.getScore(), engine.getLevel(), currentReplay);
                    }
                }
            }
        });
    }

    private void handleInput() {
        if (replaying && currentReplay != null) {
            GameReplay.FrameInput frameInput = currentReplay.getFrameInput(replayFrameIndex);
            if (frameInput != null) {
                if (frameInput.p1Up) engine.playerMove(1, Direction.UP);
                if (frameInput.p1Down) engine.playerMove(1, Direction.DOWN);
                if (frameInput.p1Left) engine.playerMove(1, Direction.LEFT);
                if (frameInput.p1Right) engine.playerMove(1, Direction.RIGHT);
                if (frameInput.p1Shoot) engine.playerShoot(1);

                if (engine.getMode() == GameEngine.GameMode.TWO_PLAYER) {
                    if (frameInput.p2Up) engine.playerMove(2, Direction.UP);
                    if (frameInput.p2Down) engine.playerMove(2, Direction.DOWN);
                    if (frameInput.p2Left) engine.playerMove(2, Direction.LEFT);
                    if (frameInput.p2Right) engine.playerMove(2, Direction.RIGHT);
                    if (frameInput.p2Shoot) engine.playerShoot(2);
                }
            }
            replayFrameIndex++;
        } else {
            if (keys1[KeyEvent.VK_W]) engine.playerMove(1, Direction.UP);
            if (keys1[KeyEvent.VK_S]) engine.playerMove(1, Direction.DOWN);
            if (keys1[KeyEvent.VK_A]) engine.playerMove(1, Direction.LEFT);
            if (keys1[KeyEvent.VK_D]) engine.playerMove(1, Direction.RIGHT);
            if (keys1[KeyEvent.VK_SPACE]) engine.playerShoot(1);

            if (engine.getMode() == GameEngine.GameMode.TWO_PLAYER) {
                if (keys2[KeyEvent.VK_UP]) engine.playerMove(2, Direction.UP);
                if (keys2[KeyEvent.VK_DOWN]) engine.playerMove(2, Direction.DOWN);
                if (keys2[KeyEvent.VK_LEFT]) engine.playerMove(2, Direction.LEFT);
                if (keys2[KeyEvent.VK_RIGHT]) engine.playerMove(2, Direction.RIGHT);
                if (keys2[KeyEvent.VK_ENTER]) engine.playerShoot(2);
            }

            if (recording && currentReplay != null) {
                GameReplay.FrameInput frameInput = new GameReplay.FrameInput(
                        keys1[KeyEvent.VK_W],
                        keys1[KeyEvent.VK_S],
                        keys1[KeyEvent.VK_A],
                        keys1[KeyEvent.VK_D],
                        keys1[KeyEvent.VK_SPACE],
                        keys2[KeyEvent.VK_UP],
                        keys2[KeyEvent.VK_DOWN],
                        keys2[KeyEvent.VK_LEFT],
                        keys2[KeyEvent.VK_RIGHT],
                        keys2[KeyEvent.VK_ENTER]
                );
                currentReplay.addFrameInput(frameInput);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE) {
            if (replaying) {
                stopReplay();
                if (listener != null) {
                    listener.onReplayEnd();
                }
            }
            if (listener != null) {
                listener.onReturnToMenu();
            }
            return;
        }

        if (replaying) {
            if (key == KeyEvent.VK_P) {
                paused = !paused;
            }
            if (key == KeyEvent.VK_R) {
                if (engine != null && currentReplay != null) {
                    engine.restart();
                    replayFrameIndex = 0;
                    paused = false;
                    levelTransition = false;
                }
            }
            return;
        }

        if (key >= 0 && key < 256) {
            keys1[key] = true;
            keys2[key] = true;
        }

        if (key == KeyEvent.VK_P) {
            paused = !paused;
        }

        if (key == KeyEvent.VK_R) {
            if (engine != null) {
                engine.restart();
                paused = false;
                levelTransition = false;
                nameInputShown = false;
                if (recording) {
                    startRecording();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (replaying) {
            return;
        }
        int key = e.getKeyCode();
        if (key >= 0 && key < 256) {
            keys1[key] = false;
            keys2[key] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
