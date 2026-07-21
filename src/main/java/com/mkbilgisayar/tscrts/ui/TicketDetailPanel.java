package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.*;
import com.mkbilgisayar.tscrts.model.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import com.mkbilgisayar.tscrts.util.UIUtils;

public class TicketDetailPanel extends JPanel {

    private MainApp mainApp;
    private DashboardPanel dashboardPanel;
    private int ticketId;

    private TicketDAO ticketDAO = new TicketDAO();
    private DeviceDAO deviceDAO = new DeviceDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private UserDAO userDAO = new UserDAO();
    private TicketHistoryDAO historyDAO = new TicketHistoryDAO();
    private PartDAO partDAO = new PartDAO();
    private TicketPartDAO ticketPartDAO = new TicketPartDAO();

    // UI Components to refresh
    private JLabel statusLabel;
    private JPanel historyPanel;
    private JPanel actionPanel;

    private Border defaultBorder = UIManager.getBorder("TextField.border");
    private Border errorBorder = BorderFactory.createLineBorder(Color.RED, 1);

    public TicketDetailPanel(MainApp mainApp, int ticketId, DashboardPanel dashboardPanel) {
        this.mainApp = mainApp;
        this.ticketId = ticketId;
        this.dashboardPanel = dashboardPanel;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        buildUI();
    }

    public int getTicketId() {
        return ticketId;
    }

    public void buildUI() {
        removeAll();
        Ticket ticket = ticketDAO.findById(ticketId);
        if (ticket == null) {
            add(new JLabel("Ticket not found."), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        Device device = deviceDAO.findById(ticket.getDeviceId());
        Customer customer = (device != null) ? customerDAO.findById(device.getCustomerId()) : null;
        User assignedUser = (ticket.getAssignedUserId() != null) ? userDAO.findById(ticket.getAssignedUserId()) : null;

        // --- TOP: Info Header ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        headerPanel.putClientProperty("FlatLaf.styleClass", "card");

        String custStr = (customer != null) ? customer.getName() + " (" + customer.getPhone() + ")" : "Unknown Customer";
        
        // Dynamic Device/Service String
        String devStr;
        if (device != null && "Service Record".equals(device.getDeviceType())) {
            devStr = "Service Request: " + device.getNotes();
        } else {
            devStr = (device != null) ? device.getDeviceType() + " | " + device.getRam() + " | " + device.getCpu() : "Unknown Device";
        }
        
        String assignStr = (assignedUser != null) ? assignedUser.getName() : "Unassigned";

        headerPanel.add(new JLabel("<html><b>Customer:</b> " + custStr + "</html>"));
        headerPanel.add(new JLabel("<html><b>Device/Issue:</b> " + devStr + "</html>"));
        
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.add(new JLabel("<html><b>Status / Cat:</b> </html>"));
        badgesPanel.add(UIUtils.createBadge(ticket.getStatus().toUpperCase(java.util.Locale.ENGLISH).replace("_", " "), UIUtils.getStatusColor(ticket.getStatus()), Color.WHITE));
        badgesPanel.add(UIUtils.createBadge(UIUtils.formatCategory(ticket.getCategory()), UIUtils.getCategoryColor(ticket.getCategory()), Color.WHITE));
        headerPanel.add(badgesPanel);
        headerPanel.add(new JLabel("<html><b>Assigned To:</b> " + assignStr + "</html>"));

        // Show invoice amount for archived tickets
        if ("archived".equals(ticket.getStatus()) && ticket.getInvoiceAmount() != null) {
            JLabel invoiceLabel = new JLabel("<html><b>Invoice:</b> " + ticket.getInvoiceAmount().setScale(2) + " TL</html>");
            invoiceLabel.setForeground(new Color(39, 174, 96));
            invoiceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            headerPanel.add(invoiceLabel);
        }

        // Add a close button
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(headerPanel, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close Tab");
        UIUtils.styleButton(closeBtn);
        closeBtn.addActionListener(e -> dashboardPanel.closeTab(this));
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.add(closeBtn);
        topWrapper.add(closePanel, BorderLayout.NORTH);

        add(topWrapper, BorderLayout.NORTH);

        // --- CENTER: History ---
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(UIManager.getColor("Panel.background"));
        
        List<TicketHistory> histories = historyDAO.findByTicketId(ticketId);
        for (TicketHistory th : histories) {
            JPanel item = new JPanel(new BorderLayout());
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            JLabel tsLabel = new JLabel(th.getTimestamp().toString());
            tsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            tsLabel.setForeground(Color.GRAY);
            
            JTextArea noteArea = new JTextArea(th.getNote());
            noteArea.setWrapStyleWord(true);
            noteArea.setLineWrap(true);
            noteArea.setOpaque(false);
            noteArea.setEditable(false);
            
            // Highlight Customer Notes
            if (th.getNote().startsWith("Customer Note:")) {
                noteArea.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
                noteArea.setForeground(UIManager.getColor("Label.disabledForeground"));
            } else {
                noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            item.add(tsLabel, BorderLayout.NORTH);
            item.add(noteArea, BorderLayout.CENTER);
            historyPanel.add(item);
        }

        JScrollPane scrollPane = new JScrollPane(historyPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Ticket History"));
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: Actions ---
        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        actionPanel.putClientProperty("FlatLaf.styleClass", "card");

        User currentUser = mainApp.getCurrentUser();
        buildActionPanel(ticket, currentUser);

        // Customer Support Note Feature (always appended to bottom if role is customer_support and ticket isn't archived)
        if ("customer_support".equals(currentUser.getRole()) && !"archived".equals(ticket.getStatus())) {
            buildCustomerNotePanel(ticket);
        }

        add(actionPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void buildCustomerNotePanel(Ticket ticket) {
        actionPanel.add(new JLabel("Add Customer Note:"));
        JTextField noteField = new JTextField(25);
        actionPanel.add(noteField);

        JButton addNoteBtn = new JButton("Submit Note");
        UIUtils.stylePrimaryButton(addNoteBtn);
        addNoteBtn.addActionListener(e -> {
            String note = noteField.getText().trim();
            if (note.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Note cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            historyDAO.create(new TicketHistory(0, ticket.getId(), null, "Customer Note: " + note));
            buildUI();
        });
        
        actionPanel.add(addNoteBtn);
    }

    private void buildActionPanel(Ticket ticket, User currentUser) {
        actionPanel.removeAll();
        String status = ticket.getStatus();
        String role = currentUser.getRole();
        String category = ticket.getCategory();

        boolean isAssignedToCurrentUser = (ticket.getAssignedUserId() != null && ticket.getAssignedUserId() == currentUser.getId());

        // 1. Pending -> Pick up
        if ("pending".equals(status)) {
            boolean canPickup = false;
            if ("engineer".equals(role) && (category.equals("sql_database") || category.equals("erp_installation") || category.equals("e_transformation"))) canPickup = true;
            if ("technician".equals(role) && category.equals("hardware")) canPickup = true;
            if ("it_staff".equals(role) && category.equals("network")) canPickup = true;

            if (canPickup) {
                JButton pickupBtn = new JButton("Pick up ticket");
                UIUtils.stylePrimaryButton(pickupBtn);
                pickupBtn.addActionListener(e -> {
                    ticket.setAssignedUserId(currentUser.getId());
                    ticket.setStatus("in_progress");
                    ticketDAO.update(ticket);
                    historyDAO.create(new TicketHistory(0, ticket.getId(), null, "Ticket picked up by " + currentUser.getName()));
                    buildUI();
                });
                actionPanel.add(pickupBtn);
            } else {
                if (!"customer_support".equals(role)) {
                    actionPanel.add(new JLabel("Waiting for " + category + " specialist to pick up."));
                }
            }
        }

        // 2. In Progress -> Add Note / Mark Completed
        else if ("in_progress".equals(status)) {
            if (isAssignedToCurrentUser) {
                
                JPanel notePanel = new JPanel(new BorderLayout(5, 5));
                JTextField noteField = new JTextField(30);
                JLabel errorLabel = new JLabel(" ");
                errorLabel.setForeground(Color.RED);
                errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                noteField.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        if (noteField.getText().trim().isEmpty()) {
                            noteField.setBorder(errorBorder);
                            errorLabel.setText("Note cannot be empty");
                        } else {
                            noteField.setBorder(defaultBorder);
                            errorLabel.setText(" ");
                        }
                    }
                });

                JButton addNoteBtn = new JButton("Add Note");
                UIUtils.styleButton(addNoteBtn);
                addNoteBtn.addActionListener(e -> {
                    String note = noteField.getText().trim();
                    if (note.isEmpty()) {
                        noteField.setBorder(errorBorder);
                        errorLabel.setText("Note cannot be empty");
                        return;
                    }
                    historyDAO.create(new TicketHistory(0, ticket.getId(), null, "Technical Note: " + note));
                    buildUI();
                });

                JPanel inputWrapper = new JPanel(new BorderLayout());
                inputWrapper.add(noteField, BorderLayout.CENTER);
                inputWrapper.add(errorLabel, BorderLayout.SOUTH);

                notePanel.add(inputWrapper, BorderLayout.CENTER);
                notePanel.add(addNoteBtn, BorderLayout.EAST);
                
                actionPanel.add(notePanel);

                JButton completeBtn = new JButton("Mark as Completed");
                UIUtils.stylePrimaryButton(completeBtn);
                completeBtn.addActionListener(e -> {
                    ticket.setStatus("completed");
                    ticket.setClosedAt(new Timestamp(System.currentTimeMillis()));
                    ticketDAO.update(ticket);
                    historyDAO.create(new TicketHistory(0, ticket.getId(), null, "Ticket marked as COMPLETED by " + currentUser.getName()));
                    buildUI();
                });
                actionPanel.add(completeBtn);
            } else {
                if (!"customer_support".equals(role)) {
                    actionPanel.add(new JLabel("Ticket is currently in progress by another user."));
                }
            }
        }

        // 3. Completed -> Log Parts / Archive
        else if ("completed".equals(status)) {
            // Log Parts Form (only for the user assigned to this ticket)
            if (isAssignedToCurrentUser) {
                List<Part> allParts = partDAO.findAll();
                if (!allParts.isEmpty()) {
                    JPanel partsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                    partsPanel.setBorder(BorderFactory.createTitledBorder("Log Part Used"));
                    
                    JComboBox<Part> partBox = new JComboBox<>(allParts.toArray(new Part[0]));
                    JTextField qtyField = new JTextField(3);
                    JLabel qtyError = new JLabel(" ");
                    qtyError.setForeground(Color.RED);
                    qtyError.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                    qtyField.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e) {
                            if (!qtyField.getText().trim().isEmpty()) {
                                validateQty(qtyField, qtyError);
                            } else {
                                qtyField.setBorder(defaultBorder);
                                qtyError.setText(" ");
                            }
                        }
                    });

                    JButton logPartBtn = new JButton("Log Part");
                    UIUtils.styleButton(logPartBtn);
                    logPartBtn.addActionListener(e -> {
                        if (!validateQty(qtyField, qtyError)) return;
                        
                        Part selectedPart = (Part) partBox.getSelectedItem();
                        int qty = Integer.parseInt(qtyField.getText().trim());
                        
                        // Deduct stock
                        boolean deducted = partDAO.decrementStock(selectedPart.getId(), qty);
                        if (!deducted) {
                            JOptionPane.showMessageDialog(this, "Insufficient stock for " + selectedPart.getName(), "Inventory Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Insert to ticket_parts
                        TicketPart tp = new TicketPart(0, ticket.getId(), selectedPart.getId(), qty);
                        ticketPartDAO.create(tp);

                        historyDAO.create(new TicketHistory(0, ticket.getId(), null, "Logged " + qty + "x " + selectedPart.getName()));
                        buildUI();
                    });

                    partsPanel.add(partBox);
                    partsPanel.add(new JLabel("Qty:"));
                    
                    JPanel qtyWrapper = new JPanel(new BorderLayout());
                    qtyWrapper.add(qtyField, BorderLayout.CENTER);
                    qtyWrapper.add(qtyError, BorderLayout.SOUTH);
                    
                    partsPanel.add(qtyWrapper);
                    partsPanel.add(logPartBtn);
                    actionPanel.add(partsPanel);
                } else {
                    actionPanel.add(new JLabel("No parts available in inventory to log."));
                }
            } else {
                if (!"manager".equals(role) && !"customer_support".equals(role)) {
                    actionPanel.add(new JLabel("Ticket is completed."));
                }
            }

            // Manager Archive Button
            if ("manager".equals(role)) {
                JButton archiveBtn = new JButton("Archive Ticket");
                UIUtils.stylePrimaryButton(archiveBtn);
                archiveBtn.addActionListener(e -> {
                    // Show invoice dialog
                    String input = JOptionPane.showInputDialog(
                            this,
                            "Enter invoice amount (TL):",
                            "Invoice & Archive",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (input == null) return; // Cancelled
                    
                    input = input.trim();
                    try {
                        BigDecimal amount = new BigDecimal(input);
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            JOptionPane.showMessageDialog(this, "Invoice amount must be greater than 0.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        ticket.setInvoiceAmount(amount);
                        ticket.setStatus("archived");
                        ticketDAO.update(ticket);
                        historyDAO.create(new TicketHistory(0, ticket.getId(), null,
                                "Invoiced " + amount.setScale(2) + " TL and archived by Manager."));
                        buildUI();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                });
                actionPanel.add(archiveBtn);
            }
        }
        
        else if ("archived".equals(status)) {
            actionPanel.add(new JLabel("Ticket is archived and read-only."));
        }
    }

    private boolean validateQty(JTextField field, JLabel label) {
        String txt = field.getText().trim();
        if (txt.isEmpty()) {
            field.setBorder(errorBorder);
            label.setText("Req");
            return false;
        }
        try {
            int q = Integer.parseInt(txt);
            if (q <= 0) throw new NumberFormatException();
            field.setBorder(defaultBorder);
            label.setText(" ");
            return true;
        } catch (NumberFormatException ex) {
            field.setBorder(errorBorder);
            label.setText("Invalid");
            return false;
        }
    }
}
