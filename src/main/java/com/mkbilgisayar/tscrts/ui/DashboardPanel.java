package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.model.User;
import com.mkbilgisayar.tscrts.model.Ticket;
import com.mkbilgisayar.tscrts.dao.TicketDAO;
import com.mkbilgisayar.tscrts.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {

    private MainApp mainApp;
    private JLabel welcomeLabel;
    private JLabel roleLabel;

    public DashboardPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel (Card)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Use a FlatLaf client property to give the header a slightly different background
        headerPanel.putClientProperty("FlatLaf.styleClass", "card");

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        roleLabel = new JLabel("Role: Unknown");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(welcomeLabel);
        infoPanel.add(roleLabel);
        
        JButton logoutButton = new JButton("Logout");
        UIUtils.styleDangerButton(logoutButton);
        logoutButton.addActionListener(e -> mainApp.logout());

        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content Area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 0, 0, 0),
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true)
        ));
        
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        
        summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        summaryPanel.setOpaque(false);
        northContainer.add(summaryPanel);
        
        teamStatusPanel = new JPanel();
        teamStatusPanel.setLayout(new BoxLayout(teamStatusPanel, BoxLayout.Y_AXIS));
        teamStatusPanel.setOpaque(false);
        teamStatusPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        northContainer.add(teamStatusPanel);
        
        contentPanel.add(northContainer, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private JTabbedPane tabbedPane;
    private JPanel summaryPanel;
    private JPanel teamStatusPanel;
    private TicketListPanel ticketListPanel;
    private TicketCreationPanel ticketCreationPanel;
    private CustomerHistoryPanel customerHistoryPanel;
    private TicketDAO ticketDAO = new TicketDAO();
    private com.mkbilgisayar.tscrts.dao.UserDAO userDAO = new com.mkbilgisayar.tscrts.dao.UserDAO();

    public void refreshDashboard() {
        User user = mainApp.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
            
            String displayRole = user.getRole().replace("_", " ");
            String[] words = displayRole.split(" ");
            StringBuilder formattedRole = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    formattedRole.append(Character.toUpperCase(word.charAt(0)))
                                 .append(word.substring(1)).append(" ");
                }
            }
            roleLabel.setText("Role: " + formattedRole.toString().trim());
            
            // Rebuild summary panel
            summaryPanel.removeAll();
            if (user.getRole().equals("engineer") || user.getRole().equals("technician") || user.getRole().equals("it_staff")) {
                int inProgressCount = ticketDAO.countAssignedInProgress(user.getId());
                int completedCount = ticketDAO.countCompletedByUser(user.getId());
                
                List<String> userCats = new ArrayList<>();
                if (user.getRole().equals("engineer")) {
                    userCats.add("sql_database");
                    userCats.add("erp_installation");
                    userCats.add("e_transformation");
                } else if (user.getRole().equals("technician")) {
                    userCats.add("hardware");
                } else if (user.getRole().equals("it_staff")) {
                    userCats.add("network");
                }
                
                int pendingCount = ticketDAO.countAvailableInCategories(userCats);
                
                summaryPanel.add(createStatCard("My Assigned Tickets", String.valueOf(inProgressCount), new Color(41, 128, 185)));
                summaryPanel.add(createStatCard("Available in My Category", String.valueOf(pendingCount), new Color(230, 126, 34)));
                summaryPanel.add(createStatCard("Completed by Me", String.valueOf(completedCount), new Color(39, 174, 96)));
                summaryPanel.setVisible(true);
            } else {
                summaryPanel.setVisible(false);
            }
            summaryPanel.revalidate();
            summaryPanel.repaint();

            // Rebuild team status panel
            teamStatusPanel.removeAll();
            if (user.getRole().equals("it_staff")) {
                JLabel title = new JLabel("Team Status");
                title.setFont(new Font("Segoe UI", Font.BOLD, 14));
                title.setForeground(Color.DARK_GRAY);
                title.setAlignmentX(Component.LEFT_ALIGNMENT);
                teamStatusPanel.add(title);
                teamStatusPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                
                List<String[]> itStaffStatus = userDAO.getITStaffStatus();
                for (String[] status : itStaffStatus) {
                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
                    row.setOpaque(false);
                    
                    JLabel nameLbl = new JLabel("• " + status[0] + " ");
                    nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    
                    JLabel statusLbl;
                    if ("available".equals(status[2])) {
                        statusLbl = UIUtils.createBadge("Available", new Color(39, 174, 96), Color.WHITE);
                    } else {
                        statusLbl = UIUtils.createBadge(status[1], UIUtils.STATUS_IN_PROGRESS, Color.WHITE);
                    }
                    
                    row.add(nameLbl);
                    row.add(statusLbl);
                    teamStatusPanel.add(row);
                }
                teamStatusPanel.setVisible(true);
            } else {
                teamStatusPanel.setVisible(false);
            }
            teamStatusPanel.revalidate();
            teamStatusPanel.repaint();

            // Rebuild tabs based on role — always create fresh instances to avoid state leaks between sessions
            tabbedPane.removeAll();
            
            if (user.getRole().equals("customer_support")) {
                ticketCreationPanel = new TicketCreationPanel(mainApp);
                tabbedPane.addTab("Create Ticket", ticketCreationPanel);
            }
            
            ticketListPanel = new TicketListPanel(mainApp);
            tabbedPane.addTab("Ticket List", ticketListPanel);
            
            // Customer History tab - available to all roles
            customerHistoryPanel = new CustomerHistoryPanel(mainApp);
            tabbedPane.addTab("Customer History", customerHistoryPanel);
            
            // Reports tab - manager only
            if (user.getRole().equals("manager")) {
                tabbedPane.addTab("Reports", new ReportingPanel());
            }
            
            // Trigger data load
            ticketListPanel.loadTickets();
        }
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel valLabel = new JLabel(value, SwingConstants.CENTER);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(color);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        card.add(valLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    public void openTicketDetail(int ticketId) {
        // Check if a detail tab for this ticket already exists
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof TicketDetailPanel) {
                if (((TicketDetailPanel) comp).getTicketId() == ticketId) {
                    // Refresh data in case status changed
                    ((TicketDetailPanel) comp).buildUI();
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }
        
        // Create new tab
        TicketDetailPanel detailPanel = new TicketDetailPanel(mainApp, ticketId, this);
        Ticket t = ticketDAO.findById(ticketId);
        String tabTitle = (t != null && t.getTicketCode() != null) ? "Ticket: " + t.getTicketCode() : "Ticket #" + ticketId;
        tabbedPane.addTab(tabTitle, detailPanel);
        
        // Switch to the newly opened tab
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public void closeTab(Component comp) {
        tabbedPane.remove(comp);
    }
}
