package com.mkbilgisayar.tscrts.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.mkbilgisayar.tscrts.model.User;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private User currentUser;

    public MainApp() {
        setTitle("TS-CRTS - Technical Service & Customer Support Tracking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null); // Center on screen

        // Initialize CardLayout for screen switching
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add views
        cardPanel.add(new LoginPanel(this), "LOGIN");
        cardPanel.add(new DashboardPanel(this), "DASHBOARD");

        add(cardPanel);
        
        // Show login by default
        showView("LOGIN");
    }

    public void showView(String viewName) {
        cardLayout.show(cardPanel, viewName);
        
        // Let the DashboardPanel know it's being shown if we switch to it
        if ("DASHBOARD".equals(viewName)) {
            // Find the dashboard panel and update its UI based on the user
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof DashboardPanel) {
                    ((DashboardPanel) comp).refreshDashboard();
                }
            }
        }
    }

    public void openTicketDetail(int ticketId) {
        showView("DASHBOARD");
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof DashboardPanel) {
                ((DashboardPanel) comp).openTicketDetail(ticketId);
                break;
            }
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void logout() {
        this.currentUser = null;
        showView("LOGIN");
    }

    public static void main(String[] args) {
        // Apply FlatLaf for modern UI Look and Feel before any Swing components are instantiated
        FlatDarkLaf.setup();
        // Customize some FlatLaf properties for a cleaner look
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Component.focusColor", new Color(255, 102, 0));
        UIManager.put("TextComponent.focusedBorderColor", new Color(255, 102, 0));
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
