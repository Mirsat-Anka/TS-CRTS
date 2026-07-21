package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.UserDAO;
import com.mkbilgisayar.tscrts.model.User;

import javax.swing.*;
import java.awt.*;
import com.mkbilgisayar.tscrts.util.UIUtils;

public class LoginPanel extends JPanel {
    
    private MainApp mainApp;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO;

    public LoginPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        this.userDAO = new UserDAO();
        
        setLayout(new GridBagLayout());
        
        // Use a container panel to act as a 'card' with a slightly lighter background
        JPanel formPanel = new JPanel(new GridBagLayout());
        // Add a nice border/padding to create the card effect
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                        BorderFactory.createMatteBorder(5, 0, 0, 0, UIUtils.LOGO_ORANGE)
                ),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("TS-CRTS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(UIUtils.LOGO_ORANGE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Please sign in to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 20, 10); // extra bottom padding
        formPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(10, 10, 5, 10);
        // Username Label
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(userLabel, gbc);

        // Username Field
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        usernameField = new JTextField(20);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");
        formPanel.add(usernameField, gbc);

        // Password Label
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(passLabel, gbc);

        // Password Field
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");
        formPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridy = 6;
        gbc.insets = new Insets(30, 10, 10, 10); // extra top padding
        JButton loginButton = new JButton("Login to Dashboard");
        UIUtils.styleSuccessButton(loginButton);
        
        // Enter key submits form
        usernameField.addActionListener(e -> loginButton.doClick());
        passwordField.addActionListener(e -> loginButton.doClick());
        
        // Enable Ctrl+Z / Ctrl+Y
        UIUtils.enableUndoRedo(usernameField, passwordField);

        formPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> attemptLogin());
        add(formPanel);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verify user against database
        User user = userDAO.findByName(username);
        
        if (user != null && user.getPasswordHash().equals(password)) {
            // Success
            usernameField.setText("");
            passwordField.setText("");
            mainApp.setCurrentUser(user);
            mainApp.showView("DASHBOARD");
        } else {
            // No bypass allowed. If DB fails or credentials wrong, show error.
            if (user == null) {
                JOptionPane.showMessageDialog(this, "User not found or database connection failed. Check console for details.", "Login Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
