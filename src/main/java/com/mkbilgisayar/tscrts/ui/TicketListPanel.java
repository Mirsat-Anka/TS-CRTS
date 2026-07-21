package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.CustomerDAO;
import com.mkbilgisayar.tscrts.dao.DeviceDAO;
import com.mkbilgisayar.tscrts.dao.TicketDAO;
import com.mkbilgisayar.tscrts.model.Customer;
import com.mkbilgisayar.tscrts.model.Device;
import com.mkbilgisayar.tscrts.model.Ticket;
import com.mkbilgisayar.tscrts.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.mkbilgisayar.tscrts.util.UIUtils;

public class TicketListPanel extends JPanel {

    private MainApp mainApp;
    private JTable ticketTable;
    private DefaultTableModel tableModel;
    private TicketDAO ticketDAO;
    private DeviceDAO deviceDAO;
    private CustomerDAO customerDAO;
    private List<Ticket> currentTickets = new ArrayList<>();  // Currently displayed (after all filters)
    private List<Ticket> roleFilteredTickets = new ArrayList<>(); // After role filter, before dropdown filters

    private JComboBox<String> statusFilter;
    private JComboBox<String> categoryFilter;

    // Internal category keys matching DB values
    private static final String[] STATUS_KEYS = {"all", "pending", "in_progress", "completed", "archived"};
    private static final String[] STATUS_LABELS = {"All", "Pending", "In Progress", "Completed", "Archived"};
    private static final String[] CATEGORY_KEYS = {"all", "sql_database", "erp_installation", "e_transformation", "network", "hardware"};
    private static final String[] CATEGORY_LABELS = {"All", "SQL Database", "ERP Installation", "E-Transformation", "Network", "Hardware"};

    public TicketListPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        this.ticketDAO = new TicketDAO();
        this.deviceDAO = new DeviceDAO();
        this.customerDAO = new CustomerDAO();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === Top section: title + refresh ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Ticket List");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("Refresh");
        UIUtils.styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadTickets());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // === Filter bar ===
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        filterPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(STATUS_LABELS);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.addActionListener(e -> applyFilters());
        filterPanel.add(statusFilter);

        filterPanel.add(Box.createHorizontalStrut(10));

        filterPanel.add(new JLabel("Category:"));
        
        String[] formattedCategories = new String[CATEGORY_KEYS.length];
        for (int i = 0; i < CATEGORY_KEYS.length; i++) {
            if ("all".equals(CATEGORY_KEYS[i])) formattedCategories[i] = "All";
            else formattedCategories[i] = UIUtils.formatCategory(CATEGORY_KEYS[i]);
        }
        
        categoryFilter = new JComboBox<>(formattedCategories);
        UIUtils.styleComboBoxForEmojis(categoryFilter);
        categoryFilter.addActionListener(e -> applyFilters());
        filterPanel.add(categoryFilter);

        // Combine header + filter into NORTH
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        northPanel.add(headerPanel);
        northPanel.add(filterPanel);
        add(northPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Ticket Code", "Customer", "Device", "Category", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing directly in the list
            }
        };
        
        ticketTable = new JTable(tableModel);
        ticketTable.setRowHeight(36);
        ticketTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ticketTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ticketTable.setFillsViewportHeight(true);
        
        // Custom renderers for badges
        BadgeCellRenderer categoryRenderer = new BadgeCellRenderer(true);
        BadgeCellRenderer statusRenderer = new BadgeCellRenderer(false);
        ticketTable.getColumnModel().getColumn(3).setCellRenderer(categoryRenderer);
        ticketTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer);
        
        // Handle double click on row
        ticketTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && ticketTable.getSelectedRow() != -1) {
                    int ticketId = currentTickets.get(ticketTable.getSelectedRow()).getId();
                    mainApp.openTicketDetail(ticketId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true));
        
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadTickets() {
        User user = mainApp.getCurrentUser();
        if (user == null) return;

        List<Ticket> allTickets = ticketDAO.findAll();
        roleFilteredTickets.clear();

        // Role-based filtering (unchanged logic)
        for (Ticket t : allTickets) {
            String role = user.getRole();
            String cat = t.getCategory();
            
            if (role.equals("manager") || role.equals("customer_support")) {
                roleFilteredTickets.add(t); // See all
            } else if (role.equals("engineer")) {
                if (cat.equals("sql_database") || cat.equals("erp_installation") || cat.equals("e_transformation")) {
                    roleFilteredTickets.add(t);
                }
            } else if (role.equals("technician")) {
                if (cat.equals("hardware")) {
                    roleFilteredTickets.add(t);
                }
            } else if (role.equals("it_staff")) {
                if (cat.equals("network")) {
                    roleFilteredTickets.add(t);
                }
            }
        }

        applyFilters();
    }

    private void applyFilters() {
        int statusIdx = statusFilter.getSelectedIndex();
        int categoryIdx = categoryFilter.getSelectedIndex();

        String selectedStatus = STATUS_KEYS[statusIdx];
        String selectedCategory = CATEGORY_KEYS[categoryIdx];

        currentTickets.clear();
        tableModel.setRowCount(0);

        for (Ticket t : roleFilteredTickets) {
            // Status filter
            if (!"all".equals(selectedStatus) && !t.getStatus().equals(selectedStatus)) {
                continue;
            }
            // Category filter
            if (!"all".equals(selectedCategory) && !t.getCategory().equals(selectedCategory)) {
                continue;
            }
            currentTickets.add(t);
        }

        // Populate table
        for (Ticket t : currentTickets) {
            Device device = deviceDAO.findById(t.getDeviceId());
            String customerName = "Unknown";
            String deviceName = "Unknown";
            
            if (device != null) {
                deviceName = device.getDeviceType();
                Customer customer = customerDAO.findById(device.getCustomerId());
                if (customer != null) {
                    customerName = customer.getName();
                }
            }

            tableModel.addRow(new Object[]{
                t.getTicketCode() != null ? t.getTicketCode() : String.valueOf(t.getId()),
                customerName,
                deviceName,
                t.getCategory(),
                t.getStatus(),
                t.getCreatedAt()
            });
        }
    }

    // Custom cell renderer for badges
    private class BadgeCellRenderer extends DefaultTableCellRenderer {
        private boolean isCategory;

        public BadgeCellRenderer(boolean isCategory) {
            this.isCategory = isCategory;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) value = "";
            String text = value.toString();
            
            Color bg = isCategory ? UIUtils.getCategoryColor(text) : UIUtils.getStatusColor(text);
            Color fg = Color.WHITE;
            String display = isCategory ? UIUtils.formatCategory(text) : text.toUpperCase(java.util.Locale.ENGLISH).replace("_", " ");
            
            JLabel badge = UIUtils.createBadge(display, bg, fg);
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            panel.add(badge);
            
            return panel;
        }
    }
}
