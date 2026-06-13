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

    public interface GameUIListener {
        void onGameOver(boolean victory, int score, int level);
        void onScoreUpdate(int score);
    }

    public GamePanel(GameEngine engine, GameUIListener listener) {
        this.engine = engine;
        this.listener = listener;
        this.keys1 = new boolean[256];
        this.keys2 = new boolean[256];
        this.paused = false;
        this.cellSize = Obstacle.SIZE;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this);
        timer.start();
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
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

        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());

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

        for (Tank tank : engine.getTanks()) {
            if (tank.isAlive()) {
                drawHealthBar(g2d, tank);
            }
        }

        drawHUD(g2d);

        if (paused) {
            drawPauseScreen(g2d);
        }

        if (engine.isGameOver()) {
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

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("得分: " + engine.getScore(), 10, 25);
        g2d.drawString("关卡: " + engine.getLevel(), 120, 25);
        g2d.drawString("敌人: " + engine.getEnemiesRemaining(), 220, 25);

        int xOffset = getWidth() - 200;
        for (Tank player : engine.getPlayers()) {
            String label = engine.getMode() == GameEngine.GameMode.TWO_PLAYER ?
                    "P" + player.getPlayerId() : "生命";
            g2d.setColor(player.getColor());
            g2d.drawString(label + ": " + player.getHealth() + "/" + player.getMaxHealth(),
                    xOffset, 25);
            xOffset += 100;
        }

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("P:暂停  R:重新开始", getWidth() - 150, getHeight() - 10);
    }

    private void drawPauseScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "游戏暂停";
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "按 P 继续游戏";
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 50);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.BOLD, 56));
        String text = engine.isVictory() ? "胜利！" : "游戏结束";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(engine.isVictory() ? Color.YELLOW : Color.RED);
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 40);

        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        text = "最终得分: " + engine.getScore();
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 20);

        text = "关卡: " + engine.getLevel();
        fm = g2d.getFontMetrics();
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 60);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        text = "按 R 重新开始 | 按 ESC 返回菜单";
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.GRAY);
        g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 110);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || engine == null) {
            repaint();
            return;
        }

        handleInput();
        engine.update();
        repaint();

        if (listener != null) {
            listener.onScoreUpdate(engine.getScore());
        }

        if (engine.isGameOver() && listener != null) {
            listener.onGameOver(engine.isVictory(), engine.getScore(), engine.getLevel());
        }
    }

    private void handleInput() {
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
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
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
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
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
