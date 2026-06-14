package com.tankbattle.ui;

import com.tankbattle.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class MapEditorPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private int mapWidth = 800;
    private int mapHeight = 608;
    private int cellSize = Obstacle.SIZE;
    private int cols = mapWidth / cellSize;
    private int rows = mapHeight / cellSize;
    private char[][] grid;
    private ObstacleType selectedTool;
    private char selectedSpawn;
    private boolean mouseDown;
    private boolean eraseMode;
    private MainFrame mainFrame;

    private JButton saveBtn;
    private JButton loadBtn;
    private JButton clearBtn;
    private JButton backBtn;
    private JToolBar toolBar;
    private JPanel editorCanvas;
    private JLabel statusLabel;

    public MapEditorPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.grid = new char[rows][cols];
        this.selectedTool = ObstacleType.BRICK;
        this.selectedSpawn = '.';
        this.mouseDown = false;
        this.eraseMode = false;
        initUI();
        clearGrid();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 40, 40));

        toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(50, 50, 50));

        addToolButton("砖墙", ObstacleType.BRICK, new Color(180, 80, 20), 'B');
        addToolButton("钢墙", ObstacleType.STEEL, new Color(150, 150, 150), 'S');
        addToolButton("水域", ObstacleType.WATER, new Color(30, 100, 200), 'W');
        addToolButton("草地", ObstacleType.GRASS, new Color(20, 150, 30), 'G');
        addToolButton("基地", ObstacleType.BASE, new Color(200, 200, 0), 'X');

        toolBar.addSeparator();

        JButton eraseBtn = new JButton("橡皮擦");
        eraseBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        eraseBtn.setFocusPainted(false);
        eraseBtn.addActionListener(e -> {
            eraseMode = true;
            selectedSpawn = '.';
            statusLabel.setText("当前工具: 橡皮擦");
        });
        toolBar.add(eraseBtn);

        toolBar.addSeparator();

        addSpawnButton("敌人出生点", 'E', Color.RED);
        addSpawnButton("玩家1出生点", '1', Color.YELLOW);
        addSpawnButton("玩家2出生点", '2', Color.CYAN);

        toolBar.addSeparator();

        saveBtn = new JButton("保存地图");
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        saveBtn.addActionListener(this);
        toolBar.add(saveBtn);

        loadBtn = new JButton("加载地图");
        loadBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        loadBtn.addActionListener(this);
        toolBar.add(loadBtn);

        clearBtn = new JButton("清空地图");
        clearBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        clearBtn.addActionListener(this);
        toolBar.add(clearBtn);

        toolBar.addSeparator();

        backBtn = new JButton("返回菜单");
        backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        backBtn.addActionListener(e -> mainFrame.returnToMenu());
        toolBar.add(backBtn);

        add(toolBar, BorderLayout.WEST);

        editorCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEditorGrid(g);
            }
        };
        editorCanvas.setPreferredSize(new Dimension(mapWidth, mapHeight));
        editorCanvas.setBackground(Color.BLACK);
        editorCanvas.addMouseListener(this);
        editorCanvas.addMouseMotionListener(this);
        editorCanvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        add(editorCanvas, BorderLayout.CENTER);

        statusLabel = new JLabel("当前工具: 砖墙 (B) | 左键绘制, 右键擦除");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(30, 30, 30));
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void addToolButton(String name, ObstacleType type, Color color, char symbol) {
        JButton btn = new JButton(name);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addActionListener(e -> {
            selectedTool = type;
            selectedSpawn = '.';
            eraseMode = false;
            statusLabel.setText("当前工具: " + name + " (" + symbol + ")");
        });
        toolBar.add(btn);
    }

    private void addSpawnButton(String name, char symbol, Color color) {
        JButton btn = new JButton(name);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addActionListener(e -> {
            selectedSpawn = symbol;
            eraseMode = false;
            statusLabel.setText("当前工具: " + name + " (" + symbol + ")");
        });
        toolBar.add(btn);
    }

    private void clearGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = '.';
            }
        }
        editorCanvas.repaint();
    }

    private void drawEditorGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(20, 20, 20));
        g2d.fillRect(0, 0, mapWidth, mapHeight);

        g2d.setColor(new Color(60, 60, 60));
        for (int i = 0; i <= cols; i++) {
            g2d.drawLine(i * cellSize, 0, i * cellSize, mapHeight);
        }
        for (int i = 0; i <= rows; i++) {
            g2d.drawLine(0, i * cellSize, mapWidth, i * cellSize);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                int x = j * cellSize;
                int y = i * cellSize;
                drawCell(g2d, x, y, c);
            }
        }
    }

    private void drawCell(Graphics2D g2d, int x, int y, char c) {
        switch (c) {
            case 'B':
                g2d.setColor(new Color(180, 80, 20));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(new Color(120, 50, 10));
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        if ((i + j) % 2 == 0) {
                            g2d.fillRect(x + i * 8, y + j * 8, 8, 8);
                        }
                    }
                }
                break;
            case 'S':
                g2d.setColor(new Color(150, 150, 150));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(new Color(100, 100, 100));
                g2d.fillRect(x + 2, y + 2, cellSize - 4, cellSize - 4);
                break;
            case 'W':
                g2d.setColor(new Color(30, 100, 200));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(new Color(50, 130, 230));
                for (int i = 0; i < 3; i++) {
                    g2d.fillOval(x + 4 + i * 10, y + 10, 8, 4);
                }
                break;
            case 'G':
                g2d.setColor(new Color(20, 150, 30));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(new Color(40, 180, 50));
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        if ((i + j) % 3 == 0) {
                            g2d.fillRect(x + i * 5, y + j * 5, 5, 5);
                        }
                    }
                }
                break;
            case 'X':
                g2d.setColor(new Color(200, 200, 0));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.RED);
                int[] px = {x + cellSize/2, x + cellSize, x + cellSize/2, x};
                int[] py = {y, y + cellSize/2, y + cellSize, y + cellSize/2};
                g2d.fillPolygon(px, py, 4);
                break;
            case 'E':
                g2d.setColor(new Color(100, 0, 0));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("E", x + cellSize/2 - 6, y + cellSize/2 + 7);
                break;
            case '1':
                g2d.setColor(new Color(150, 150, 0));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("P1", x + cellSize/2 - 10, y + cellSize/2 + 7);
                break;
            case '2':
                g2d.setColor(new Color(0, 100, 150));
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("P2", x + cellSize/2 - 10, y + cellSize/2 + 7);
                break;
        }
    }

    private void paintCell(MouseEvent e) {
        int col = e.getX() / cellSize;
        int row = e.getY() / cellSize;
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            if (eraseMode || SwingUtilities.isRightMouseButton(e)) {
                grid[row][col] = '.';
            } else if (selectedSpawn != '.') {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        if (grid[i][j] == selectedSpawn) {
                            grid[i][j] = '.';
                        }
                    }
                }
                grid[row][col] = selectedSpawn;
            } else {
                char symbol = '.';
                switch (selectedTool) {
                    case BRICK: symbol = 'B'; break;
                    case STEEL: symbol = 'S'; break;
                    case WATER: symbol = 'W'; break;
                    case GRASS: symbol = 'G'; break;
                    case BASE: symbol = 'X'; break;
                }
                grid[row][col] = symbol;
            }
            editorCanvas.repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            saveMap();
        } else if (e.getSource() == loadBtn) {
            loadMap();
        } else if (e.getSource() == clearBtn) {
            int result = JOptionPane.showConfirmDialog(this, "确定要清空地图吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                clearGrid();
            }
        }
    }

    private void saveMap() {
        String filename = JOptionPane.showInputDialog(this, "请输入地图文件名:", "custom_map.txt");
        if (filename == null || filename.trim().isEmpty()) return;
        if (!filename.endsWith(".txt")) filename += ".txt";

        try {
            File mapsDir = new File("maps");
            if (!mapsDir.exists()) mapsDir.mkdir();
            String filepath = "maps/" + filename;

            GameMap map = new GameMap(mapWidth, mapHeight);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    char c = grid[i][j];
                    int x = j * cellSize;
                    int y = i * cellSize;
                    switch (c) {
                        case 'B': map.addObstacle(new Obstacle(x, y, ObstacleType.BRICK)); break;
                        case 'S': map.addObstacle(new Obstacle(x, y, ObstacleType.STEEL)); break;
                        case 'W': map.addObstacle(new Obstacle(x, y, ObstacleType.WATER)); break;
                        case 'G': map.addObstacle(new Obstacle(x, y, ObstacleType.GRASS)); break;
                        case 'X': map.addObstacle(new Obstacle(x, y, ObstacleType.BASE)); break;
                        case 'E': map.addEnemySpawn(x, y); break;
                        case '1': map.setPlayer1Spawn(x, y); break;
                        case '2': map.setPlayer2Spawn(x, y); break;
                    }
                }
            }
            map.saveToFile(filepath);
            mainFrame.showMessage("保存成功", "地图已保存到: " + filepath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMap() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("maps"));
        fileChooser.setDialogTitle("选择地图文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("地图文件 (*.txt)", "txt"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                GameMap map = new GameMap(mapWidth, mapHeight);
                map.loadFromFile(fileChooser.getSelectedFile().getAbsolutePath());

                clearGrid();
                for (Obstacle obs : map.getObstacles()) {
                    int col = obs.getX() / cellSize;
                    int row = obs.getY() / cellSize;
                    if (row < rows && col < cols) {
                        switch (obs.getType()) {
                            case BRICK: grid[row][col] = 'B'; break;
                            case STEEL: grid[row][col] = 'S'; break;
                            case WATER: grid[row][col] = 'W'; break;
                            case GRASS: grid[row][col] = 'G'; break;
                            case BASE: grid[row][col] = 'X'; break;
                        }
                    }
                }
                for (Tank spawn : map.getEnemySpawnPoints()) {
                    int col = spawn.getX() / cellSize;
                    int row = spawn.getY() / cellSize;
                    if (row < rows && col < cols) grid[row][col] = 'E';
                }
                int p1Col = map.getPlayer1Spawn()[0] / cellSize;
                int p1Row = map.getPlayer1Spawn()[1] / cellSize;
                if (p1Row < rows && p1Col < cols) grid[p1Row][p1Col] = '1';
                int p2Col = map.getPlayer2Spawn()[0] / cellSize;
                int p2Row = map.getPlayer2Spawn()[1] / cellSize;
                if (p2Row < rows && p2Col < cols) grid[p2Row][p2Col] = '2';

                editorCanvas.repaint();
                mainFrame.showMessage("加载成功", "地图已加载: " + fileChooser.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        paintCell(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseDown) {
            paintCell(e);
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
