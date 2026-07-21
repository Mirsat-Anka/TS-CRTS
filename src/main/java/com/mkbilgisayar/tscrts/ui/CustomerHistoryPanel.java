package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.*;
import com.mkbilgisayar.tscrts.model.*;
import com.mkbilgisayar.tscrts.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerHistoryPanel extends JPanel {

    private MainApp mainApp;

    private CustomerDAO customerDAO = new CustomerDAO();
    private DeviceDAO deviceDAO = new DeviceDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private TicketHistoryDAO historyDAO = new TicketHistoryDAO();
    private TicketPartDAO ticketPartDAO = new TicketPartDAO();
    private PartDAO partDAO = new PartDAO();
    private UserDAO userDAO = new UserDAO();

    // Search components
    private JTextField searchField;
    private JButton searchButton;
    private DefaultListModel<Customer> listModel;
    private JList<Customer> resultList;

    // Detail area
    private JPanel detailPanel;
    private JScrollPane detailScrollPane;

    // Expand/collapse state: ticket ID -> expanded?
    private Map<Integer, Boolean> expandedTickets = new HashMap<>();

    public CustomerHistoryPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        buildUI();
    }

    private void buildUI() {
        // === TOP: Search Section ===
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        searchPanel.putClientProperty("FlatLaf.styleClass", "card");

        JLabel titleLabel = new JLabel("Customer History Lookup");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchInputPanel = new JPanel(new BorderLayout(8, 0));
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search by customer name, phone or company...");
        searchButton = new JButton("Search History");
        UIUtils.stylePrimaryButton(searchButton);

        // Enter key submits search
        searchField.addActionListener(e -> searchButton.doClick());
        
        // Enable Ctrl+Z / Ctrl+Y
        UIUtils.enableUndoRedo(searchField);

        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);

        // === LEFT: Search Results List ===
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer(new CustomerListCellRenderer());
        
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
        listScrollPane.setPreferredSize(new Dimension(280, 0));

        // === CENTER: Detail Panel (scrollable) ===
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailScrollPane = new JScrollPane(detailPanel);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Customer Details & Ticket History"));
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Show a placeholder initially
        showPlaceholder("Search for a customer to view their history.");

        // === Layout Assembly ===
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(searchPanel, BorderLayout.CENTER);
        add(topWrapper, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailScrollPane);
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.0);
        add(splitPane, BorderLayout.CENTER);

        // === Event Listeners ===
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch()); // Enter key

        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Customer selected = resultList.getSelectedValue();
                if (selected != null) {
                    expandedTickets.clear();
                    loadCustomerHistory(selected);
                }
            }
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Customer> results = new ArrayList<>();
        if (query.contains("-")) {
            Ticket ticket = ticketDAO.findByTicketCode(query.toUpperCase());
            if (ticket != null) {
                Device device = deviceDAO.findById(ticket.getDeviceId());
                if (device != null) {
                    Customer c = customerDAO.findById(device.getCustomerId());
                    if (c != null) {
                        results.add(c);
                        expandedTickets.put(ticket.getId(), true); // Auto-expand
                    }
                }
            }
        }
        
        if (results.isEmpty()) {
            results = customerDAO.searchByNameOrPhone(query);
        }
        
        listModel.clear();
        for (Customer c : results) {
            listModel.addElement(c);
        }

        if (results.isEmpty()) {
            showPlaceholder("No customers found for: \"" + query + "\"");
        } else {
            showPlaceholder("Select a customer from the list to view their history.");
        }
    }

    private void showPlaceholder(String message) {
        detailPanel.removeAll();
        JLabel placeholder = new JLabel(message);
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholder.setForeground(UIManager.getColor("Label.disabledForeground"));
        placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeholder.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        detailPanel.add(placeholder);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void loadCustomerHistory(Customer customer) {
        detailPanel.removeAll();

        // --- Customer Info Card ---
        JPanel customerCard = createCard();
        customerCard.setLayout(new GridLayout(1, 3, 20, 0));
        customerCard.add(createInfoLabel("Customer", customer.getName()));
        customerCard.add(createInfoLabel("Phone", customer.getPhone()));
        customerCard.add(createInfoLabel("Company", customer.getCompanyName() != null ? customer.getCompanyName() : "—"));
        customerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        customerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(customerCard);
        detailPanel.add(Box.createVerticalStrut(15));

        // --- Fetch all devices for this customer ---
        List<Device> devices = deviceDAO.findByCustomerId(customer.getId());

        if (devices.isEmpty()) {
            JLabel noDevices = new JLabel("No devices or tickets found for this customer.");
            noDevices.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noDevices.setForeground(UIManager.getColor("Label.disabledForeground"));
            noDevices.setAlignmentX(Component.LEFT_ALIGNMENT);
            noDevices.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            detailPanel.add(noDevices);
        } else {
            // Fetch all tickets for all devices in one query
            List<Integer> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
            List<Ticket> allTickets = ticketDAO.findByDeviceIds(deviceIds);

            // Group tickets by device_id
            Map<Integer, List<Ticket>> ticketsByDevice = new LinkedHashMap<>();
            for (Device d : devices) {
                ticketsByDevice.put(d.getId(), new ArrayList<>());
            }
            for (Ticket t : allTickets) {
                ticketsByDevice.computeIfAbsent(t.getDeviceId(), k -> new ArrayList<>()).add(t);
            }

            // Render each device + its tickets
            for (Device device : devices) {
                JPanel deviceSection = buildDeviceSection(device, ticketsByDevice.getOrDefault(device.getId(), Collections.emptyList()));
                deviceSection.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailPanel.add(deviceSection);
                detailPanel.add(Box.createVerticalStrut(12));
            }
        }

        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate();
        detailPanel.repaint();

        // Scroll to top
        SwingUtilities.invokeLater(() -> detailScrollPane.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildDeviceSection(Device device, List<Ticket> tickets) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Device header
        JPanel deviceHeader = createCard();
        deviceHeader.setLayout(new BorderLayout());
        deviceHeader.setBackground(UIManager.getColor("Panel.background"));

        String deviceStr;
        if ("Service Record".equals(device.getDeviceType())) {
            String details = (device.getNotes() != null && !device.getNotes().isEmpty()) ? device.getNotes() : "No details";
            deviceStr = "\uD83D\uDCCB Service Request: " + (details.length() > 80 ? details.substring(0, 80) + "..." : details);
        } else {
            deviceStr = "\uD83D\uDCE6 " + device.getDeviceType() + "  |  " + device.getRam() + "  |  " + device.getCpu();
        }

        JLabel deviceLabel = new JLabel("<html><b>" + deviceStr + "</b></html>");
        deviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deviceHeader.add(deviceLabel, BorderLayout.WEST);

        JLabel ticketCount = new JLabel(tickets.size() + " ticket(s)");
        ticketCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        ticketCount.setForeground(UIManager.getColor("Label.disabledForeground"));
        deviceHeader.add(ticketCount, BorderLayout.EAST);

        deviceHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        deviceHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(deviceHeader);

        // Tickets under this device
        if (tickets.isEmpty()) {
            JLabel noTickets = new JLabel("    No tickets for this device.");
            noTickets.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noTickets.setForeground(UIManager.getColor("Label.disabledForeground"));
            noTickets.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(noTickets);
        } else {
            for (Ticket ticket : tickets) {
                JPanel ticketRow = buildTicketRow(ticket);
                ticketRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                section.add(ticketRow);
            }
        }

        return section;
    }

    private JPanel buildTicketRow(Ticket ticket) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // Ticket summary row (clickable)
        JPanel summaryRow = new JPanel(new BorderLayout(10, 0));
        Color statusColor = UIUtils.getStatusColor(ticket.getStatus());
        summaryRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, statusColor),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        summaryRow.setBackground(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 25)); // Light tint
        summaryRow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        User assignedUser = (ticket.getAssignedUserId() != null) ? userDAO.findById(ticket.getAssignedUserId()) : null;
        String assignedStr = (assignedUser != null) ? assignedUser.getName() : "Unassigned";
        String dateStr = (ticket.getCreatedAt() != null) ? ticket.getCreatedAt().toString().substring(0, 16) : "—";

        boolean isExpanded = expandedTickets.getOrDefault(ticket.getId(), false);
        String arrow = isExpanded ? "▼" : "▶";

        String displayCode = (ticket.getTicketCode() != null) ? ticket.getTicketCode() : String.valueOf(ticket.getId());
        JLabel leftLabel = new JLabel(arrow + "  " + displayCode + "  |  " + assignedStr);
        leftLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JLabel catBadge = UIUtils.createBadge(UIUtils.formatCategory(ticket.getCategory()), UIUtils.getCategoryColor(ticket.getCategory()), Color.WHITE);
        JLabel statusBadge = UIUtils.createBadge(ticket.getStatus().toUpperCase(java.util.Locale.ENGLISH).replace("_", " "), UIUtils.getStatusColor(ticket.getStatus()), Color.WHITE);
        
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        dateLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        if ("archived".equals(ticket.getStatus()) && ticket.getInvoiceAmount() != null) {
            JLabel invoiceBadge = new JLabel(ticket.getInvoiceAmount().setScale(2) + " TL");
            invoiceBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            invoiceBadge.setForeground(new Color(39, 174, 96));
            rightPanel.add(invoiceBadge);
        }
        rightPanel.add(catBadge);
        rightPanel.add(statusBadge);
        rightPanel.add(dateLabel);

        summaryRow.add(leftLabel, BorderLayout.CENTER);
        summaryRow.add(rightPanel, BorderLayout.EAST);

        // Expanded detail panel (history + parts)
        JPanel expandedPanel = new JPanel();
        expandedPanel.setLayout(new BoxLayout(expandedPanel, BoxLayout.Y_AXIS));
        expandedPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtils.getStatusColor(ticket.getStatus()).darker()),
                BorderFactory.createEmptyBorder(5, 25, 10, 10)
        ));
        expandedPanel.setVisible(isExpanded);

        if (isExpanded) {
            populateExpandedPanel(expandedPanel, ticket);
        }

        // Click to expand/collapse
        summaryRow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean newState = !expandedTickets.getOrDefault(ticket.getId(), false);
                expandedTickets.put(ticket.getId(), newState);
                
                leftLabel.setText((newState ? "▼" : "▶") + "  " + displayCode + "  |  " + assignedStr);

                if (newState) {
                    expandedPanel.removeAll();
                    populateExpandedPanel(expandedPanel, ticket);
                }
                expandedPanel.setVisible(newState);
                
                container.revalidate();
                container.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                summaryRow.setBackground(UIManager.getColor("List.selectionBackground"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                summaryRow.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        container.add(summaryRow);
        container.add(expandedPanel);
        return container;
    }

    private void populateExpandedPanel(JPanel panel, Ticket ticket) {
        // Ticket history entries
        List<TicketHistory> histories = historyDAO.findByTicketId(ticket.getId());
        if (histories.isEmpty()) {
            JLabel noHistory = new JLabel("No history entries.");
            noHistory.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noHistory.setForeground(UIManager.getColor("Label.disabledForeground"));
            noHistory.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noHistory);
        } else {
            for (TicketHistory th : histories) {
                JPanel entry = new JPanel(new BorderLayout(10, 0));
                entry.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                entry.setAlignmentX(Component.LEFT_ALIGNMENT);
                entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                String tsStr = (th.getTimestamp() != null) ? th.getTimestamp().toString().substring(0, 16) : "";
                JLabel tsLabel = new JLabel(tsStr);
                tsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                tsLabel.setForeground(Color.GRAY);
                tsLabel.setPreferredSize(new Dimension(130, 20));

                JLabel noteLabel = new JLabel(th.getNote());
                noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                
                // Highlight customer notes
                if (th.getNote().startsWith("Customer Note:")) {
                    noteLabel.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
                    noteLabel.setForeground(new Color(0, 120, 180));
                }

                entry.add(tsLabel, BorderLayout.WEST);
                entry.add(noteLabel, BorderLayout.CENTER);
                panel.add(entry);
            }
        }

        // Parts used
        List<TicketPart> parts = ticketPartDAO.findByTicketId(ticket.getId());
        if (!parts.isEmpty()) {
            panel.add(Box.createVerticalStrut(5));
            JLabel partsHeader = new JLabel("\uD83D\uDD27 Parts Used:");
            partsHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            partsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(partsHeader);

            for (TicketPart tp : parts) {
                Part part = partDAO.findById(tp.getPartId());
                String partName = (part != null) ? part.getName() : "Unknown Part #" + tp.getPartId();
                JLabel partLabel = new JLabel("    • " + partName + " × " + tp.getQuantity());
                partLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                partLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(partLabel);
            }
        }
    }

    // ---- Helper methods ----

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.putClientProperty("FlatLaf.styleClass", "card");
        return card;
    }

    private JLabel createInfoLabel(String title, String value) {
        JLabel label = new JLabel("<html><span style='color:gray;font-size:10px'>" + title + "</span><br><b>" + value + "</b></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    // Methods getStatusColor and formatCategory removed, using UIUtils

    // Custom cell renderer for the customer list
    private static class CustomerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Customer) {
                Customer c = (Customer) value;
                String company = (c.getCompanyName() != null && !c.getCompanyName().isEmpty()) ? " — " + c.getCompanyName() : "";
                setText("<html><b>" + c.getName() + "</b><br><span style='color:gray'>" + c.getPhone() + company + "</span></html>");
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            }
            return this;
        }
    }
}
