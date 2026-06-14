package com.tankbattle.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MenuPanel extends JPanel {
    private JButton singlePlayerBtn;
    private JButton twoPlayerBtn;
    private JButton mapEditorBtn;
    private JButton levelDesignerBtn;
    private JButton campaignBtn;
    private JButton selectMapBtn;
    private JButton selectLevelBtn;
    private JButton leaderboardBtn;
    private JButton exitBtn;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel selectedMapLabel;
    private JLabel selectedLevelLabel;
    private MenuListener listener;
    private String selectedMap;
    private String selectedLevelConfig;

    public interface MenuListener {
        void onSinglePlayer(String mapFile);
        void onTwoPlayer(String mapFile);
        void onMapEditor();
        void onLevelDesigner();
        void onCampaign(String levelConfigFile);
        void onLeaderboard();
        void onExit();
    }

    public MenuPanel(MenuListener listener) {
        this.listener = listener;
        this.selectedMap = "maps/level1.txt";
        this.selectedLevelConfig = null;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(20, 30, 50));
        setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        titleLabel = new JLabel("坦克大战");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        titleLabel.setForeground(Color.YELLOW);
        gbc.gridy = 0;
        add(titleLabel, gbc);

        subtitleLabel = new JLabel("TANK BATTLE");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 24));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        add(subtitleLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 15;
        gbc.ipadx = 100;

        campaignBtn = createMenuButton("闯关模式", "按关卡配置逐关挑战");
        campaignBtn.addActionListener(e -> {
            if (listener != null) listener.onCampaign(selectedLevelConfig);
        });
        gbc.gridy = 3;
        add(campaignBtn, gbc);

        singlePlayerBtn = createMenuButton("单人闯关(快速)", "WASD移动, 空格射击");
        singlePlayerBtn.addActionListener(e -> {
            if (listener != null) listener.onSinglePlayer(selectedMap);
        });
        gbc.gridy = 4;
        add(singlePlayerBtn, gbc);

        twoPlayerBtn = createMenuButton("双人对抗", "P1:WASD+空格 | P2:方向键+回车");
        twoPlayerBtn.addActionListener(e -> {
            if (listener != null) listener.onTwoPlayer(selectedMap);
        });
        gbc.gridy = 5;
        add(twoPlayerBtn, gbc);

        mapEditorBtn = createMenuButton("地图编辑器", "创建和编辑自定义地图");
        mapEditorBtn.addActionListener(e -> {
            if (listener != null) listener.onMapEditor();
        });
        gbc.gridy = 6;
        add(mapEditorBtn, gbc);

        levelDesignerBtn = createMenuButton("关卡设计器", "配置敌人数、波次、出生点");
        levelDesignerBtn.addActionListener(e -> {
            if (listener != null) listener.onLevelDesigner();
        });
        gbc.gridy = 7;
        add(levelDesignerBtn, gbc);

        leaderboardBtn = createMenuButton("计分排行榜", "查看历史成绩记录");
        leaderboardBtn.addActionListener(e -> {
            if (listener != null) listener.onLeaderboard();
        });
        gbc.gridy = 8;
        add(leaderboardBtn, gbc);

        selectMapBtn = createMenuButton("选择地图", "选择游戏地图");
        selectMapBtn.addActionListener(e -> selectMap());
        gbc.gridy = 9;
        add(selectMapBtn, gbc);

        selectLevelBtn = createMenuButton("选择关卡配置", "选择闯关模式关卡配置文件");
        selectLevelBtn.addActionListener(e -> selectLevelConfig());
        gbc.gridy = 10;
        add(selectLevelBtn, gbc);

        exitBtn = createMenuButton("退出游戏", "退出程序");
        exitBtn.addActionListener(e -> {
            if (listener != null) listener.onExit();
        });
        gbc.gridy = 11;
        add(exitBtn, gbc);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);

        selectedMapLabel = new JLabel("当前地图: " + getMapDisplayName(selectedMap));
        selectedMapLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        selectedMapLabel.setForeground(Color.LIGHT_GRAY);
        infoPanel.add(selectedMapLabel);

        selectedLevelLabel = new JLabel("闯关配置: " +
                (selectedLevelConfig != null ? getMapDisplayName(selectedLevelConfig) : "(默认闯关关卡)"));
        selectedLevelLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        selectedLevelLabel.setForeground(new Color(150, 200, 255));
        infoPanel.add(selectedLevelLabel);

        gbc.gridy = 12;
        gbc.insets = new Insets(30, 10, 10, 10);
        add(infoPanel, gbc);

        JLabel controlsLabel = new JLabel("游戏中按 P 暂停 | R 重新开始 | ESC 返回菜单");
        controlsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        controlsLabel.setForeground(Color.GRAY);
        gbc.gridy = 13;
        gbc.insets = new Insets(5, 10, 10, 10);
        add(controlsLabel, gbc);
    }

    private JButton createMenuButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 80, 120));
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));
        btn.setToolTipText(tooltip);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(80, 120, 180));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 80, 120));
            }
        });
        return btn;
    }

    private void selectMap() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("选择地图文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("地图文件 (*.txt)", "txt"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedMap = selectedFile.getAbsolutePath();
            selectedMapLabel.setText("当前地图: " + getMapDisplayName(selectedMap));
        }
    }

    private void selectLevelConfig() {
        JFileChooser fileChooser = new JFileChooser();
        File levelsDir = new File("levels");
        if (levelsDir.exists()) {
            fileChooser.setCurrentDirectory(levelsDir);
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }
        fileChooser.setDialogTitle("选择关卡配置文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("关卡配置 (*.json)", "json"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedLevelConfig = selectedFile.getAbsolutePath();
            selectedLevelLabel.setText("闯关配置: " + getMapDisplayName(selectedLevelConfig));
        }
    }

    private String getMapDisplayName(String path) {
        if (path == null) return "(未选择)";
        File file = new File(path);
        return file.getName();
    }
}
