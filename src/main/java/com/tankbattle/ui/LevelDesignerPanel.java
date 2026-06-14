package com.tankbattle.ui;

import com.tankbattle.model.*;
import com.tankbattle.model.level.EnemyTypeRatio;
import com.tankbattle.model.level.LevelConfig;
import com.tankbattle.model.level.SpawnPoint;
import com.tankbattle.model.level.WaveConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LevelDesignerPanel extends JPanel implements ActionListener {
    private static final int CELL_SIZE = Obstacle.SIZE;
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 608;

    private MainFrame mainFrame;
    private LevelConfig currentConfig;
    private GameMap currentMap;
    private int selectedWaveIndex;
    private int draggingSpawnIndex;
    private int spawnCounter;

    private JTextField levelNumberField;
    private JTextField levelNameField;
    private JComboBox<String> mapFileCombo;
    private JLabel totalEnemiesLabel;

    private JPanel mapCanvas;
    private JSpinner normalRatioSpinner;
    private JSpinner fastRatioSpinner;
    private JSpinner heavyRatioSpinner;
    private JSpinner waveEnemyCountSpinner;
    private JSpinner spawnIntervalSpinner;
    private JSpinner maxOnScreenSpinner;
    private JSpinner triggerRemainingSpinner;
    private JTable wavesTable;
    private DefaultTableModel wavesTableModel;
    private TimelinePanel timelinePanel;

    private JButton saveBtn;
    private JButton loadBtn;
    private JButton addWaveBtn;
    private JButton removeWaveBtn;
    private JButton upWaveBtn;
    private JButton downWaveBtn;
    private JButton addSpawnBtn;
    private JButton clearSpawnBtn;
    private JButton backBtn;
    private JButton selectMapBtn;

    public LevelDesignerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.currentConfig = new LevelConfig(1);
        this.selectedWaveIndex = -1;
        this.draggingSpawnIndex = -1;
        this.spawnCounter = 0;
        initUI();
        loadMapFile("maps/level1.txt");
        initDefaultWaves();
        refreshAll();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 40));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(350);
        mainSplit.setResizeWeight(0.0);

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("关卡设计器");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        infoPanel.add(titleLabel);

        infoPanel.add(Box.createHorizontalStrut(20));

        infoPanel.add(new JLabel("关卡号:"));
        levelNumberField = new JTextField("1", 5);
        levelNumberField.addActionListener(e -> {
            try {
                currentConfig.setLevelNumber(Integer.parseInt(levelNumberField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        });
        infoPanel.add(levelNumberField);

        infoPanel.add(new JLabel("名称:"));
        levelNameField = new JTextField("第 1 关", 15);
        levelNameField.addActionListener(e -> currentConfig.setLevelName(levelNameField.getText().trim()));
        infoPanel.add(levelNameField);

        infoPanel.add(new JLabel("地图:"));
        mapFileCombo = new JComboBox<>();
        refreshMapFileList();
        mapFileCombo.setPreferredSize(new Dimension(180, 25));
        mapFileCombo.addActionListener(e -> {
            String selected = (String) mapFileCombo.getSelectedItem();
            if (selected != null) {
                loadMapFile(selected);
            }
        });
        infoPanel.add(mapFileCombo);

        selectMapBtn = new JButton("选择...");
        selectMapBtn.addActionListener(this);
        infoPanel.add(selectMapBtn);

        totalEnemiesLabel = new JLabel("总敌人: 0");
        totalEnemiesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        totalEnemiesLabel.setForeground(new Color(255, 200, 100));
        infoPanel.add(Box.createHorizontalStrut(30));
        infoPanel.add(totalEnemiesLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnPanel.setOpaque(false);

        saveBtn = new JButton("保存关卡");
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        saveBtn.addActionListener(this);
        btnPanel.add(saveBtn);

        loadBtn = new JButton("加载关卡");
        loadBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        loadBtn.addActionListener(this);
        btnPanel.add(loadBtn);

        backBtn = new JButton("返回菜单");
        backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        backBtn.addActionListener(e -> mainFrame.returnToMenu());
        btnPanel.add(backBtn);

        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(350);
        split.setResizeWeight(0.5);

        JPanel wavesPanel = createWavesPanel();
        JPanel waveConfigPanel = createWaveConfigPanel();

        split.setTopComponent(wavesPanel);
        split.setBottomComponent(waveConfigPanel);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createWavesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(40, 40, 55));
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 140)),
                "波次列表",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 14),
                Color.WHITE
        );
        panel.setBorder(border);

        String[] columns = {"#", "敌人", "间隔", "最大同屏", "触发剩余"};
        wavesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        wavesTable = new JTable(wavesTableModel);
        wavesTable.setBackground(new Color(30, 30, 45));
        wavesTable.setForeground(Color.WHITE);
        wavesTable.setGridColor(new Color(70, 70, 90));
        wavesTable.setSelectionBackground(new Color(80, 100, 160));
        wavesTable.setSelectionForeground(Color.WHITE);
        wavesTable.getTableHeader().setBackground(new Color(60, 60, 80));
        wavesTable.getTableHeader().setForeground(Color.WHITE);
        wavesTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        wavesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = wavesTable.getSelectedRow();
                if (row >= 0 && row < currentConfig.getWaves().size()) {
                    selectedWaveIndex = row;
                    loadWaveConfigToForm(currentConfig.getWaves().get(row));
                }
            }
        });
        JScrollPane scroll = new JScrollPane(wavesTable);
        scroll.getViewport().setBackground(new Color(30, 30, 45));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        btnPanel.setOpaque(false);

        addWaveBtn = new JButton("+ 新增");
        addWaveBtn.addActionListener(this);
        removeWaveBtn = new JButton("- 删除");
        removeWaveBtn.addActionListener(this);
        upWaveBtn = new JButton("↑ 上移");
        upWaveBtn.addActionListener(this);
        downWaveBtn = new JButton("↓ 下移");
        downWaveBtn.addActionListener(this);

        btnPanel.add(addWaveBtn);
        btnPanel.add(removeWaveBtn);
        btnPanel.add(upWaveBtn);
        btnPanel.add(downWaveBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWaveConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 40, 55));
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 140)),
                "当前波次配置",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 14),
                Color.WHITE
        );
        panel.setBorder(border);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addConfigRow(panel, gbc, row++, "本波敌人数量:",
                waveEnemyCountSpinner = createSpinner(10, 1, 200, 1));
        addConfigRow(panel, gbc, row++, "出怪间隔(毫秒):",
                spawnIntervalSpinner = createSpinner(3000, 200, 10000, 100));
        addConfigRow(panel, gbc, row++, "最大同屏数量:",
                maxOnScreenSpinner = createSpinner(4, 1, 20, 1));
        addConfigRow(panel, gbc, row++, "触发下一波(剩余N个):",
                triggerRemainingSpinner = createSpinner(0, 0, 50, 1));

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel ratioLabel = new JLabel("敌人类型比例 (%):");
        ratioLabel.setForeground(Color.LIGHT_GRAY);
        ratioLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        panel.add(ratioLabel, gbc);

        addConfigRow(panel, gbc, row++, "  普通 (NORMAL):",
                normalRatioSpinner = createSpinner(70, 0, 100, 5));
        addConfigRow(panel, gbc, row++, "  快速 (FAST):",
                fastRatioSpinner = createSpinner(20, 0, 100, 5));
        addConfigRow(panel, gbc, row++, "  重装 (HEAVY):",
                heavyRatioSpinner = createSpinner(10, 0, 100, 5));

        ChangeListener ratioListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int n = (int) normalRatioSpinner.getValue();
                int f = (int) fastRatioSpinner.getValue();
                int h = (int) heavyRatioSpinner.getValue();
                int total = n + f + h;
                if (total > 100 || total < 100) {
                }
            }
        };
        normalRatioSpinner.addChangeListener(ratioListener);
        fastRatioSpinner.addChangeListener(ratioListener);
        heavyRatioSpinner.addChangeListener(ratioListener);

        JButton applyBtn = new JButton("应用到当前波次");
        applyBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        applyBtn.setBackground(new Color(60, 120, 80));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.addActionListener(e -> applyWaveConfigFromForm());
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 5, 8);
        panel.add(applyBtn, gbc);

        JPanel tipPanel = new JPanel();
        tipPanel.setOpaque(false);
        JLabel tipLabel = new JLabel("<html><div style='color:#999;font-size:11px;'>" +
                "• 普通: 标准敌人<br>" +
                "• 快速: 速度更快 (黄色)<br>" +
                "• 重装: 防御更高 (蓝色)</div></html>");
        tipPanel.add(tipLabel);
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 8, 5, 8);
        panel.add(tipPanel, gbc);

        return panel;
    }

    private void addConfigRow(JPanel panel, GridBagConstraints gbc, int row,
                               String labelText, JSpinner spinner) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.LIGHT_GRAY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(spinner, gbc);
    }

    private JSpinner createSpinner(int value, int min, int max, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setBackground(new Color(30, 30, 45));
        spinner.setForeground(Color.WHITE);
        return spinner;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(40, 40, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(MAP_HEIGHT + 80);
        split.setResizeWeight(0.7);

        JPanel mapAreaPanel = new JPanel(new BorderLayout(5, 5));
        mapAreaPanel.setOpaque(false);

        JPanel mapToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        mapToolbar.setOpaque(false);
        TitledBorder mapBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 140)),
                "地图视图 (拖拽红色方块调整出生点, 点击空白添加)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 14),
                Color.WHITE
        );
        mapToolbar.setBorder(mapBorder);

        addSpawnBtn = new JButton("+ 添加出生点");
        addSpawnBtn.addActionListener(e -> addSpawnPointAtCenter());
        clearSpawnBtn = new JButton("清空出生点");
        clearSpawnBtn.addActionListener(e -> {
            currentConfig.getSpawnPoints().clear();
            spawnCounter = 0;
            refreshMapCanvas();
            refreshTimeline();
        });
        JLabel info = new JLabel("| 左键拖拽移动 | 右键删除 |");
        info.setForeground(Color.LIGHT_GRAY);

        mapToolbar.add(addSpawnBtn);
        mapToolbar.add(clearSpawnBtn);
        mapToolbar.add(info);

        mapAreaPanel.add(mapToolbar, BorderLayout.NORTH);

        mapCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMap(g);
            }
        };
        mapCanvas.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        mapCanvas.setBackground(Color.BLACK);
        mapCanvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mapCanvas.addMouseListener(new MapMouseHandler());
        mapCanvas.addMouseMotionListener(new MapMouseMotionHandler());

        JScrollPane mapScroll = new JScrollPane(mapCanvas);
        mapScroll.getViewport().setBackground(Color.BLACK);
        mapAreaPanel.add(mapScroll, BorderLayout.CENTER);

        timelinePanel = new TimelinePanel(this);
        JScrollPane timelineScroll = new JScrollPane(timelinePanel);
        timelineScroll.getViewport().setBackground(new Color(30, 30, 45));

        split.setTopComponent(mapAreaPanel);
        split.setBottomComponent(timelineScroll);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void refreshMapFileList() {
        mapFileCombo.removeAllItems();
        File mapsDir = new File("maps");
        if (mapsDir.exists() && mapsDir.isDirectory()) {
            File[] files = mapsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File f : files) {
                    mapFileCombo.addItem("maps/" + f.getName());
                }
            }
        }
    }

    private void loadMapFile(String path) {
        try {
            currentMap = new GameMap(MAP_WIDTH, MAP_HEIGHT);
            File file = new File(path);
            if (file.exists()) {
                currentMap.loadFromFile(path);
            }
            currentConfig.setMapFile(path);
            if (currentConfig.getSpawnPoints().isEmpty()) {
                for (Tank sp : currentMap.getEnemySpawnPoints()) {
                    currentConfig.addSpawnPoint(sp.getX(), sp.getY());
                }
            }
            if (mapFileCombo != null) {
                mapFileCombo.setSelectedItem(path);
            }
            refreshMapCanvas();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "加载地图失败: " + ex.getMessage());
        }
    }

    private void initDefaultWaves() {
        if (currentConfig.getWaves().isEmpty()) {
            WaveConfig w1 = new WaveConfig(1);
            w1.setTotalEnemies(8);
            w1.setEnemyTypeRatio(new EnemyTypeRatio(80, 15, 5));
            w1.setSpawnInterval(3000);
            w1.setMaxEnemiesOnScreen(4);
            w1.setTriggerRemainingEnemies(0);
            currentConfig.addWave(w1);

            WaveConfig w2 = new WaveConfig(2);
            w2.setTotalEnemies(12);
            w2.setEnemyTypeRatio(new EnemyTypeRatio(60, 25, 15));
            w2.setSpawnInterval(2500);
            w2.setMaxEnemiesOnScreen(5);
            w2.setTriggerRemainingEnemies(2);
            currentConfig.addWave(w2);

            WaveConfig w3 = new WaveConfig(3);
            w3.setTotalEnemies(15);
            w3.setEnemyTypeRatio(new EnemyTypeRatio(50, 30, 20));
            w3.setSpawnInterval(2000);
            w3.setMaxEnemiesOnScreen(6);
            w3.setTriggerRemainingEnemies(3);
            currentConfig.addWave(w3);
        }
    }

    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(20, 20, 25));
        g2d.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        g2d.setColor(new Color(50, 50, 65));
        int cols = MAP_WIDTH / CELL_SIZE;
        int rows = MAP_HEIGHT / CELL_SIZE;
        for (int i = 0; i <= cols; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, MAP_HEIGHT);
        }
        for (int i = 0; i <= rows; i++) {
            g2d.drawLine(0, i * CELL_SIZE, MAP_WIDTH, i * CELL_SIZE);
        }

        if (currentMap != null) {
            for (Obstacle obs : currentMap.getObstacles()) {
                if (obs.isDestroyed()) continue;
                drawObstacle(g2d, obs);
            }
        }

        List<SpawnPoint> spawnPoints = currentConfig.getSpawnPoints();
        for (int i = 0; i < spawnPoints.size(); i++) {
            SpawnPoint sp = spawnPoints.get(i);
            boolean isDragging = (i == draggingSpawnIndex);
            drawSpawnPoint(g2d, sp.getX(), sp.getY(), i + 1, isDragging);
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
                        if ((i + j) % 2 == 0) g2d.fillRect(x + i * 8, y + j * 8, 8, 8);
                    }
                }
                break;
            case STEEL:
                g2d.setColor(new Color(150, 150, 150));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(new Color(100, 100, 100));
                g2d.fillRect(x + 2, y + 2, w - 4, h - 4);
                break;
            case WATER:
                g2d.setColor(new Color(30, 100, 200));
                g2d.fillRect(x, y, w, h);
                break;
            case GRASS:
                g2d.setColor(new Color(20, 150, 30));
                g2d.fillRect(x, y, w, h);
                break;
            case BASE:
                g2d.setColor(new Color(200, 200, 0));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(Color.RED);
                int[] px = {x + w/2, x + w, x + w/2, x};
                int[] py = {y, y + h/2, y + h, y + h/2};
                g2d.fillPolygon(px, py, 4);
                break;
        }
    }

    private void drawSpawnPoint(Graphics2D g2d, int x, int y, int num, boolean dragging) {
        int size = Tank.SIZE;
        g2d.setColor(dragging ? new Color(255, 100, 100, 200) : new Color(180, 30, 30, 180));
        g2d.fillRoundRect(x, y, size, size, 6, 6);
        g2d.setColor(dragging ? Color.YELLOW : new Color(255, 80, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, size, size, 6, 6);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String s = "S" + num;
        g2d.drawString(s, x + size/2 - fm.stringWidth(s)/2, y + size/2 + 6);

        if (dragging) {
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{4}, 0));
            g2d.drawRect(x - 4, y - 4, size + 8, size + 8);
        }
    }

    private class MapMouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int mx = e.getX();
            int my = e.getY();

            if (SwingUtilities.isRightMouseButton(e)) {
                int idx = findSpawnPointAt(mx, my);
                if (idx >= 0) {
                    currentConfig.removeSpawnPoint(idx);
                    refreshMapCanvas();
                    refreshTimeline();
                }
                return;
            }

            int idx = findSpawnPointAt(mx, my);
            if (idx >= 0) {
                draggingSpawnIndex = idx;
                refreshMapCanvas();
            } else {
                int gridX = (mx / CELL_SIZE) * CELL_SIZE;
                int gridY = (my / CELL_SIZE) * CELL_SIZE;
                if (gridX >= 0 && gridX < MAP_WIDTH && gridY >= 0 && gridY < MAP_HEIGHT) {
                    currentConfig.addSpawnPoint(gridX, gridY);
                    spawnCounter++;
                    draggingSpawnIndex = currentConfig.getSpawnPoints().size() - 1;
                    refreshMapCanvas();
                    refreshTimeline();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (draggingSpawnIndex >= 0) {
                SpawnPoint sp = currentConfig.getSpawnPoints().get(draggingSpawnIndex);
                int gridX = (sp.getX() / CELL_SIZE) * CELL_SIZE;
                int gridY = (sp.getY() / CELL_SIZE) * CELL_SIZE;
                sp.setX(Math.max(0, Math.min(MAP_WIDTH - Tank.SIZE, gridX)));
                sp.setY(Math.max(0, Math.min(MAP_HEIGHT - Tank.SIZE, gridY)));
                draggingSpawnIndex = -1;
                refreshMapCanvas();
                refreshTimeline();
            }
        }
    }

    private class MapMouseMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggingSpawnIndex >= 0 && draggingSpawnIndex < currentConfig.getSpawnPoints().size()) {
                SpawnPoint sp = currentConfig.getSpawnPoints().get(draggingSpawnIndex);
                int newX = e.getX() - Tank.SIZE / 2;
                int newY = e.getY() - Tank.SIZE / 2;
                sp.setX(Math.max(0, Math.min(MAP_WIDTH - Tank.SIZE, newX)));
                sp.setY(Math.max(0, Math.min(MAP_HEIGHT - Tank.SIZE, newY)));
                refreshMapCanvas();
            }
        }
    }

    private int findSpawnPointAt(int mx, int my) {
        List<SpawnPoint> points = currentConfig.getSpawnPoints();
        for (int i = points.size() - 1; i >= 0; i--) {
            SpawnPoint sp = points.get(i);
            if (mx >= sp.getX() && mx <= sp.getX() + Tank.SIZE &&
                    my >= sp.getY() && my <= sp.getY() + Tank.SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void addSpawnPointAtCenter() {
        int y = 50;
        int x = 100 + (currentConfig.getSpawnPoints().size() % 5) * 120;
        currentConfig.addSpawnPoint(x, y);
        spawnCounter++;
        refreshMapCanvas();
        refreshTimeline();
    }

    private void loadWaveConfigToForm(WaveConfig wave) {
        if (wave == null) return;
        waveEnemyCountSpinner.setValue(wave.getTotalEnemies());
        spawnIntervalSpinner.setValue(wave.getSpawnInterval());
        maxOnScreenSpinner.setValue(wave.getMaxEnemiesOnScreen());
        triggerRemainingSpinner.setValue(wave.getTriggerRemainingEnemies());
        EnemyTypeRatio r = wave.getEnemyTypeRatio();
        normalRatioSpinner.setValue(r.getNormal());
        fastRatioSpinner.setValue(r.getFast());
        heavyRatioSpinner.setValue(r.getHeavy());
    }

    private void applyWaveConfigFromForm() {
        if (selectedWaveIndex < 0 || selectedWaveIndex >= currentConfig.getWaves().size()) {
            JOptionPane.showMessageDialog(this, "请先在上方波次列表中选择一个波次");
            return;
        }
        WaveConfig wave = currentConfig.getWaves().get(selectedWaveIndex);
        wave.setTotalEnemies((int) waveEnemyCountSpinner.getValue());
        wave.setSpawnInterval((int) spawnIntervalSpinner.getValue());
        wave.setMaxEnemiesOnScreen((int) maxOnScreenSpinner.getValue());
        wave.setTriggerRemainingEnemies((int) triggerRemainingSpinner.getValue());
        EnemyTypeRatio ratio = new EnemyTypeRatio(
                (int) normalRatioSpinner.getValue(),
                (int) fastRatioSpinner.getValue(),
                (int) heavyRatioSpinner.getValue()
        );
        ratio.normalize();
        wave.setEnemyTypeRatio(ratio);
        refreshWavesTable();
        refreshTotalEnemies();
        refreshTimeline();
    }

    private void refreshAll() {
        refreshWavesTable();
        refreshTotalEnemies();
        refreshMapCanvas();
        refreshTimeline();
    }

    private void refreshWavesTable() {
        wavesTableModel.setRowCount(0);
        List<WaveConfig> waves = currentConfig.getWaves();
        for (int i = 0; i < waves.size(); i++) {
            WaveConfig w = waves.get(i);
            wavesTableModel.addRow(new Object[]{
                    i + 1,
                    w.getTotalEnemies(),
                    w.getSpawnInterval() + "ms",
                    w.getMaxEnemiesOnScreen(),
                    w.getTriggerRemainingEnemies()
            });
        }
        if (selectedWaveIndex >= 0 && selectedWaveIndex < waves.size()) {
            wavesTable.setRowSelectionInterval(selectedWaveIndex, selectedWaveIndex);
        }
    }

    private void refreshTotalEnemies() {
        totalEnemiesLabel.setText("总敌人: " + currentConfig.getTotalEnemies()
                + " | 波次: " + currentConfig.getWaves().size());
    }

    private void refreshMapCanvas() {
        if (mapCanvas != null) {
            mapCanvas.repaint();
        }
    }

    private void refreshTimeline() {
        if (timelinePanel != null) {
            timelinePanel.refresh();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == addWaveBtn) {
            int num = currentConfig.getWaves().size() + 1;
            WaveConfig newWave = new WaveConfig(num);
            if (!currentConfig.getWaves().isEmpty()) {
                WaveConfig last = currentConfig.getWaves().get(currentConfig.getWaves().size() - 1);
                newWave.setSpawnInterval(Math.max(500, last.getSpawnInterval() - 200));
                newWave.setMaxEnemiesOnScreen(Math.min(20, last.getMaxEnemiesOnScreen() + 1));
            }
            currentConfig.addWave(newWave);
            selectedWaveIndex = currentConfig.getWaves().size() - 1;
            refreshWavesTable();
            refreshTotalEnemies();
            refreshTimeline();
            loadWaveConfigToForm(newWave);
        } else if (src == removeWaveBtn) {
            if (selectedWaveIndex >= 0 && currentConfig.getWaves().size() > 1) {
                currentConfig.removeWave(selectedWaveIndex);
                for (int i = 0; i < currentConfig.getWaves().size(); i++) {
                    currentConfig.getWaves().get(i).setWaveNumber(i + 1);
                }
                selectedWaveIndex = Math.min(selectedWaveIndex, currentConfig.getWaves().size() - 1);
                refreshWavesTable();
                refreshTotalEnemies();
                refreshTimeline();
                if (selectedWaveIndex >= 0) {
                    loadWaveConfigToForm(currentConfig.getWaves().get(selectedWaveIndex));
                }
            }
        } else if (src == upWaveBtn) {
            if (selectedWaveIndex > 0) {
                List<WaveConfig> waves = currentConfig.getWaves();
                WaveConfig temp = waves.get(selectedWaveIndex);
                waves.set(selectedWaveIndex, waves.get(selectedWaveIndex - 1));
                waves.set(selectedWaveIndex - 1, temp);
                for (int i = 0; i < waves.size(); i++) {
                    waves.get(i).setWaveNumber(i + 1);
                }
                selectedWaveIndex--;
                refreshWavesTable();
                refreshTimeline();
            }
        } else if (src == downWaveBtn) {
            if (selectedWaveIndex >= 0 && selectedWaveIndex < currentConfig.getWaves().size() - 1) {
                List<WaveConfig> waves = currentConfig.getWaves();
                WaveConfig temp = waves.get(selectedWaveIndex);
                waves.set(selectedWaveIndex, waves.get(selectedWaveIndex + 1));
                waves.set(selectedWaveIndex + 1, temp);
                for (int i = 0; i < waves.size(); i++) {
                    waves.get(i).setWaveNumber(i + 1);
                }
                selectedWaveIndex++;
                refreshWavesTable();
                refreshTimeline();
            }
        } else if (src == saveBtn) {
            saveLevelConfig();
        } else if (src == loadBtn) {
            loadLevelConfig();
        } else if (src == selectMapBtn) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("maps"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("地图文件 (*.txt)", "txt"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadMapFile(chooser.getSelectedFile().getPath());
            }
        }
    }

    private void saveLevelConfig() {
        String defaultName = "level_" + currentConfig.getLevelNumber() + ".json";
        String filename = JOptionPane.showInputDialog(this, "请输入关卡文件名:", defaultName);
        if (filename == null || filename.trim().isEmpty()) return;
        if (!filename.endsWith(".json")) filename += ".json";

        try {
            File levelsDir = new File("levels");
            if (!levelsDir.exists()) levelsDir.mkdir();
            String filepath = "levels/" + filename;

            currentConfig.setLevelName(levelNameField.getText().trim());
            try {
                currentConfig.setLevelNumber(Integer.parseInt(levelNumberField.getText().trim()));
            } catch (NumberFormatException ignored) {}

            currentConfig.saveToFile(filepath);
            mainFrame.showMessage("保存成功", "关卡配置已保存到: " + filepath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLevelConfig() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("levels"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("关卡配置 (*.json)", "json"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentConfig = LevelConfig.loadFromFile(chooser.getSelectedFile().getPath());
                levelNumberField.setText(String.valueOf(currentConfig.getLevelNumber()));
                levelNameField.setText(currentConfig.getLevelName());
                loadMapFile(currentConfig.getMapFile());
                selectedWaveIndex = currentConfig.getWaves().isEmpty() ? -1 : 0;
                refreshAll();
                if (selectedWaveIndex >= 0) {
                    loadWaveConfigToForm(currentConfig.getWaves().get(0));
                }
                mainFrame.showMessage("加载成功", "已加载关卡: " + currentConfig.getLevelName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public LevelConfig getCurrentConfig() {
        return currentConfig;
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    private static class TimelinePanel extends JPanel {
        private static final int ROW_HEIGHT = 40;
        private static final int HEADER_HEIGHT = 40;
        private static final int LABEL_WIDTH = 100;
        private static final int PIXELS_PER_SECOND = 60;
        private LevelDesignerPanel designer;

        public TimelinePanel(LevelDesignerPanel designer) {
            this.designer = designer;
            setBackground(new Color(25, 25, 40));
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 140)),
                    "出怪时间轴预览 (基于预估: 每个敌人约存活8-15秒)",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font(Font.SANS_SERIF, Font.BOLD, 14),
                    Color.WHITE
            ));
        }

        public void refresh() {
            revalidate();
            repaint();
        }

        private List<WaveTimeline> calculateTimeline() {
            List<WaveTimeline> result = new ArrayList<>();
            LevelConfig cfg = designer.getCurrentConfig();
            if (cfg == null) return result;

            long currentTime = 0;
            int numSpawns = Math.max(1, cfg.getSpawnPoints().size());

            for (int wi = 0; wi < cfg.getWaves().size(); wi++) {
                WaveConfig w = cfg.getWaves().get(wi);
                WaveTimeline wt = new WaveTimeline();
                wt.waveIndex = wi;
                wt.waveNumber = wi + 1;
                wt.startTime = currentTime;

                int remaining = w.getTotalEnemies();
                int spawned = 0;
                int maxOnScreen = w.getMaxEnemiesOnScreen();
                int interval = w.getSpawnInterval();
                long t = currentTime;

                int simulatedAlive = 0;
                int aliveCountIdx = 0;
                List<Long> deathTimes = new ArrayList<>();
                long avgLifeMs = 10000;

                while (remaining > 0) {
                    while (!deathTimes.isEmpty() && deathTimes.get(0) <= t) {
                        deathTimes.remove(0);
                        simulatedAlive--;
                    }

                    int canSpawnThisTick = Math.min(
                            maxOnScreen - simulatedAlive,
                            (int) Math.max(1, numSpawns * 0.5)
                    );

                    for (int s = 0; s < canSpawnThisTick && remaining > 0; s++) {
                        wt.spawnTimes.add(t);
                        deathTimes.add(t + avgLifeMs + (long)(Math.random() * 5000 - 2500));
                        simulatedAlive++;
                        remaining--;
                        spawned++;
                    }

                    if (remaining > 0) {
                        t += interval / Math.max(1, numSpawns / 2);
                    }

                    boolean triggerCond = (spawned >= w.getTotalEnemies() &&
                            simulatedAlive <= w.getTriggerRemainingEnemies());
                    if (triggerCond && remaining == 0) {
                        break;
                    }

                    if (t - currentTime > 600000) break;
                }

                wt.endTime = t;
                if (!deathTimes.isEmpty()) {
                    wt.endTime = Math.max(wt.endTime, deathTimes.get(deathTimes.size() - 1));
                }

                currentTime = wt.endTime;
                result.add(wt);
            }

            return result;
        }

        @Override
        public Dimension getPreferredSize() {
            List<WaveTimeline> timelines = calculateTimeline();
            long maxTime = 10000;
            for (WaveTimeline wt : timelines) {
                maxTime = Math.max(maxTime, wt.endTime);
            }
            int width = LABEL_WIDTH + (int)(maxTime / 1000.0 * PIXELS_PER_SECOND) + 100;
            int height = HEADER_HEIGHT + timelines.size() * ROW_HEIGHT + 80;
            return new Dimension(Math.max(900, width), Math.max(250, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            List<WaveTimeline> timelines = calculateTimeline();

            int w = getWidth();
            long maxTime = 10000;
            for (WaveTimeline wt : timelines) {
                maxTime = Math.max(maxTime, wt.endTime);
            }

            g2d.setColor(new Color(25, 25, 40));
            g2d.fillRect(0, 0, w, getHeight());

            g2d.setColor(new Color(60, 60, 80));
            g2d.fillRect(0, 0, LABEL_WIDTH, getHeight());
            g2d.setColor(new Color(100, 100, 140));
            g2d.drawLine(LABEL_WIDTH, 0, LABEL_WIDTH, getHeight());
            g2d.drawLine(0, HEADER_HEIGHT, w, HEADER_HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            for (int sec = 0; sec * 1000 <= maxTime; sec += 5) {
                int x = LABEL_WIDTH + (int)(sec * 1000 / 1000.0 * PIXELS_PER_SECOND);
                if (x >= w) break;
                g2d.setColor(new Color(60, 60, 80));
                g2d.drawLine(x, HEADER_HEIGHT, x, getHeight());
                g2d.setColor(new Color(180, 180, 200));
                g2d.drawString(sec + "s", x + 2, HEADER_HEIGHT - 8);
            }

            LevelConfig cfg = designer.getCurrentConfig();
            Color[] waveColors = {
                    new Color(100, 180, 100),
                    new Color(180, 160, 100),
                    new Color(180, 100, 100),
                    new Color(100, 150, 200),
                    new Color(180, 120, 200),
                    new Color(200, 150, 100)
            };

            for (int i = 0; i < timelines.size(); i++) {
                WaveTimeline wt = timelines.get(i);
                int rowY = HEADER_HEIGHT + i * ROW_HEIGHT;
                Color c = waveColors[i % waveColors.length];

                g2d.setColor(new Color(40, 40, 60));
                g2d.fillRect(LABEL_WIDTH, rowY, w - LABEL_WIDTH, ROW_HEIGHT - 2);

                g2d.setColor(c);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                g2d.drawString("第" + wt.waveNumber + "波", 10, rowY + ROW_HEIGHT/2 + 4);

                WaveConfig waveCfg = cfg.getWaves().get(i);
                g2d.setColor(c.darker());
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g2d.drawString(waveCfg.getTotalEnemies() + "敌 " + waveCfg.getSpawnInterval()/1000.0 + "s",
                        10, rowY + ROW_HEIGHT/2 + 18);

                int barX = LABEL_WIDTH + (int)(wt.startTime / 1000.0 * PIXELS_PER_SECOND);
                int barW = Math.max(5, (int)((wt.endTime - wt.startTime) / 1000.0 * PIXELS_PER_SECOND));
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
                g2d.fillRoundRect(barX, rowY + 8, barW, ROW_HEIGHT - 18, 8, 8);
                g2d.setColor(c);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(barX, rowY + 8, barW, ROW_HEIGHT - 18, 8, 8);

                for (Long st : wt.spawnTimes) {
                    int sx = LABEL_WIDTH + (int)(st / 1000.0 * PIXELS_PER_SECOND);
                    g2d.setColor(c.brighter().brighter());
                    g2d.fillRect(sx - 1, rowY + 4, 3, ROW_HEIGHT - 10);
                }
            }

            int legendY = HEADER_HEIGHT + timelines.size() * ROW_HEIGHT + 20;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g2d.drawString("图例: 彩色条=波次活动区间  |  竖线=敌人出生时刻  |  基于模拟估算, 实际节奏受玩家击杀速度影响",
                    LABEL_WIDTH + 10, legendY);
        }

        private static class WaveTimeline {
            int waveIndex;
            int waveNumber;
            long startTime;
            long endTime;
            List<Long> spawnTimes = new ArrayList<>();
        }
    }
}
