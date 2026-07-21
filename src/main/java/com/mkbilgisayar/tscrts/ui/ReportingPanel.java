package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.TicketDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import com.mkbilgisayar.tscrts.util.UIUtils;

public class ReportingPanel extends JPanel {

    private TicketDAO ticketDAO = new TicketDAO();

    public ReportingPanel() {
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loadReport();
    }

    private void loadReport() {
        removeAll();

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

        // === Header ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Monthly Report — " + monthName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton refreshBtn = new JButton("Refresh");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadReport());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // === Content: Main container ===
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // --- 1. Top Section: Monthly Ticket Volume (stat cards) ---
        Map<String, Integer> statusCounts = ticketDAO.countByStatusThisMonth(year, month);
        int totalMonth = statusCounts.values().stream().mapToInt(Integer::intValue).sum();

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.add(createSectionTitle("Ticket Volume This Month (Total: " + totalMonth + ")"), BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsPanel.add(createStatCard("Pending", String.valueOf(statusCounts.getOrDefault("pending", 0)), new Color(230, 126, 34)));
        cardsPanel.add(createStatCard("In Progress", String.valueOf(statusCounts.getOrDefault("in_progress", 0)), new Color(41, 128, 185)));
        cardsPanel.add(createStatCard("Completed", String.valueOf(statusCounts.getOrDefault("completed", 0)), new Color(39, 174, 96)));
        cardsPanel.add(createStatCard("Archived", String.valueOf(statusCounts.getOrDefault("archived", 0)), new Color(127, 140, 141)));
        topSection.add(cardsPanel, BorderLayout.CENTER);

        content.add(topSection, BorderLayout.NORTH);

        // --- Bottom Section: Split (Table left, other stats right) ---
        JPanel bottomSection = new JPanel(new BorderLayout(20, 0));

        // --- 2. Left: Category Breakdown (table) ---
        Map<String, Integer> categoryCounts = ticketDAO.countByCategory();

        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.add(createSectionTitle("Problem Category Breakdown"), BorderLayout.NORTH);

        String[] catCols = {"Category", "Ticket Count"};
        DefaultTableModel catModel = new DefaultTableModel(catCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            catModel.addRow(new Object[]{formatCategory(entry.getKey()), entry.getValue()});
        }
        JTable catTable = createStyledTable(catModel);
        JScrollPane catScroll = new JScrollPane(catTable);
        tableSection.add(catScroll, BorderLayout.CENTER);

        bottomSection.add(tableSection, BorderLayout.CENTER);

        JPanel rightStatsPanel = new JPanel(new GridLayout(2, 1, 0, 20)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 280;
                return d;
            }
        };

        double avgHours = ticketDAO.averageResolutionHours();
        String avgStr;
        if (avgHours == 0) {
            avgStr = "No data";
        } else if (avgHours >= 24) {
            double days = avgHours / 24.0;
            avgStr = String.format("%.1f days (%.0f hours)", days, avgHours);
        } else {
            avgStr = String.format("%.1f hours", avgHours);
        }

        JPanel avgPanel = new JPanel(new BorderLayout(0, 10));
        avgPanel.add(createSectionTitle("Average Resolution Time"), BorderLayout.NORTH);
        avgPanel.add(createStatCard("Avg. Resolution", avgStr, new Color(142, 68, 173)), BorderLayout.CENTER);
        rightStatsPanel.add(avgPanel);

        BigDecimal totalInvoiced = ticketDAO.totalInvoicedThisMonth(year, month);
        JPanel invoicePanel = new JPanel(new BorderLayout(0, 10));
        invoicePanel.add(createSectionTitle("Total Invoiced This Month"), BorderLayout.NORTH);
        invoicePanel.add(createStatCard("Invoiced", totalInvoiced.setScale(2) + " TL", new Color(39, 174, 96)), BorderLayout.CENTER);
        rightStatsPanel.add(invoicePanel);

        // Wrapper to prevent stretching cards vertically to match table height
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.add(rightStatsPanel, BorderLayout.NORTH);

        bottomSection.add(rightWrapper, BorderLayout.EAST);

        content.add(bottomSection, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createSectionTitle(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(Color.DARK_GRAY);
        panel.add(label);
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel valLabel = new JLabel(value, SwingConstants.CENTER);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(valLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFillsViewportHeight(true);

        // Right-align the count column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        return table;
    }

    private String formatCategory(String cat) {
        if (cat == null) return "Unknown";
        switch (cat) {
            case "sql_database": return "SQL Database";
            case "erp_installation": return "ERP Installation";
            case "e_transformation": return "E-Transformation";
            case "network": return "Network";
            case "hardware": return "Hardware";
            default: return cat;
        }
    }
}
