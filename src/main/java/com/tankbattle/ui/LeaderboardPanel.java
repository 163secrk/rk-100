package com.tankbattle.ui;

import com.tankbattle.engine.HighScoreManager;
import com.tankbattle.model.ScoreRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private HighScoreManager highScoreManager;
    private LeaderboardListener listener;
    private JTable recordTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> levelFilterCombo;
    private JComboBox<String> timeFilterCombo;
    private JLabel titleLabel;
    private JButton backBtn;
    private JButton replayBtn;
    private JLabel statusLabel;
    private List<ScoreRecord> currentRecords;
    private int selectedRow = -1;

    private static final Color GOLD_COLOR = new Color(255, 215, 0);
    private static final Color SILVER_COLOR = new Color(192, 192, 192);
    private static final Color BRONZE_COLOR = new Color(205, 127, 50);

    public interface LeaderboardListener {
        void onBack();
        void onReplay(ScoreRecord record);
    }

    public LeaderboardPanel(HighScoreManager highScoreManager, LeaderboardListener listener) {
        this.highScoreManager = highScoreManager;
        this.listener = listener;
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 30, 50));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel("🏆 计分排行榜", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterPanel.setOpaque(false);

        JLabel levelLabel = new JLabel("关卡筛选:");
        levelLabel.setForeground(Color.LIGHT_GRAY);
        levelLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        filterPanel.add(levelLabel);

        levelFilterCombo = new JComboBox<>();
        levelFilterCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        levelFilterCombo.setPreferredSize(new Dimension(120, 28));
        levelFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        filterPanel.add(levelFilterCombo);

        JLabel timeLabel = new JLabel("时间筛选:");
        timeLabel.setForeground(Color.LIGHT_GRAY);
        timeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        filterPanel.add(timeLabel);

        timeFilterCombo = new JComboBox<>(new String[]{"全部", "今天", "本周", "本月"});
        timeFilterCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        timeFilterCombo.setPreferredSize(new Dimension(120, 28));
        timeFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        filterPanel.add(timeFilterCombo);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"排名", "玩家名", "得分", "到达关卡", "日期", "回放"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordTable = new JTable(tableModel);
        recordTable.setRowHeight(36);
        recordTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        recordTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        recordTable.getTableHeader().setBackground(new Color(60, 80, 120));
        recordTable.getTableHeader().setForeground(Color.WHITE);
        recordTable.setSelectionBackground(new Color(80, 120, 180));
        recordTable.setSelectionForeground(Color.WHITE);
        recordTable.setShowGrid(true);
        recordTable.setGridColor(new Color(80, 80, 100));
        recordTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        recordTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        recordTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        recordTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        recordTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        recordTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        recordTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row == 0) {
                        c.setBackground(GOLD_COLOR);
                        c.setForeground(Color.BLACK);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (row == 1) {
                        c.setBackground(SILVER_COLOR);
                        c.setForeground(Color.BLACK);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (row == 2) {
                        c.setBackground(BRONZE_COLOR);
                        c.setForeground(Color.WHITE);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setBackground(new Color(30, 40, 60));
                        c.setForeground(Color.LIGHT_GRAY);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }

                if (column == 0 && row < 3) {
                    String medal = row == 0 ? "🥇" : row == 1 ? "🥈" : "🥉";
                    setText(medal + " 第" + (row + 1) + "名");
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 0) {
                    setText("第" + (row + 1) + "名");
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (column == 3) {
                    setText("第 " + value + " 关");
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 5) {
                    ScoreRecord record = currentRecords.get(row);
                    if (record.getReplayFileName() != null && !record.getReplayFileName().isEmpty()) {
                        setText("▶ 回放");
                        setForeground(new Color(100, 200, 255));
                        setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        setText("-");
                        setHorizontalAlignment(SwingConstants.CENTER);
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        recordTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = recordTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < currentRecords.size()) {
                    ScoreRecord record = currentRecords.get(selectedRow);
                    if (record.getReplayFileName() != null && !record.getReplayFileName().isEmpty()) {
                        replayBtn.setEnabled(true);
                        if (e.getClickCount() == 2) {
                            if (listener != null) {
                                listener.onReplay(record);
                            }
                        }
                    } else {
                        replayBtn.setEnabled(false);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recordTable);
        scrollPane.getViewport().setBackground(new Color(20, 30, 50));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 100, 140), 2));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        statusLabel = new JLabel("共 0 条记录", SwingConstants.LEFT);
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        replayBtn = new JButton("▶ 播放回放");
        replayBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        replayBtn.setForeground(Color.WHITE);
        replayBtn.setBackground(new Color(60, 140, 80));
        replayBtn.setOpaque(true);
        replayBtn.setFocusPainted(false);
        replayBtn.setBorderPainted(true);
        replayBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 120), 2));
        replayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        replayBtn.setEnabled(false);
        replayBtn.setPreferredSize(new Dimension(130, 35));
        replayBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow >= 0 && selectedRow < currentRecords.size()) {
                    ScoreRecord record = currentRecords.get(selectedRow);
                    if (listener != null && record.getReplayFileName() != null) {
                        listener.onReplay(record);
                    }
                }
            }
        });
        replayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (replayBtn.isEnabled()) {
                    replayBtn.setBackground(new Color(80, 180, 100));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (replayBtn.isEnabled()) {
                    replayBtn.setBackground(new Color(60, 140, 80));
                }
            }
        });
        btnPanel.add(replayBtn);

        backBtn = new JButton("返回菜单");
        backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(60, 80, 120));
        backBtn.setOpaque(true);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(true);
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(130, 35));
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onBack();
                }
            }
        });
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(new Color(80, 120, 180));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(new Color(60, 80, 120));
            }
        });
        btnPanel.add(backBtn);

        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        updateLevelFilter();
        applyFilters();
    }

    private void updateLevelFilter() {
        String selected = (String) levelFilterCombo.getSelectedItem();
        levelFilterCombo.removeAllItems();
        levelFilterCombo.addItem("全部关卡");
        List<Integer> levels = highScoreManager.getAvailableLevels();
        for (int level : levels) {
            levelFilterCombo.addItem("第 " + level + " 关");
        }
        if (selected != null) {
            levelFilterCombo.setSelectedItem(selected);
        }
    }

    private void applyFilters() {
        String levelFilter = (String) levelFilterCombo.getSelectedItem();
        String timeFilter = (String) timeFilterCombo.getSelectedItem();

        long startTime = 0;
        long endTime = Long.MAX_VALUE;

        if (timeFilter != null && !timeFilter.equals("全部")) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            endTime = cal.getTimeInMillis();

            if (timeFilter.equals("今天")) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
            } else if (timeFilter.equals("本周")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
            } else if (timeFilter.equals("本月")) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
            }
        }

        if (levelFilter != null && levelFilter.startsWith("第 ")) {
            int level = Integer.parseInt(levelFilter.replaceAll("[^0-9]", ""));
            currentRecords = highScoreManager.getRecordsByLevelAndTimeRange(level, startTime, endTime);
        } else {
            currentRecords = highScoreManager.getRecordsByTimeRange(startTime, endTime);
        }

        updateTable();
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (ScoreRecord record : currentRecords) {
            Object[] row = {
                    "",
                    record.getPlayerName(),
                    record.getScore(),
                    record.getLevel(),
                    record.getFormattedDate(),
                    ""
            };
            tableModel.addRow(row);
        }
        statusLabel.setText("共 " + currentRecords.size() + " 条记录 | 双击记录可播放回放");
        selectedRow = -1;
        replayBtn.setEnabled(false);
    }
}
