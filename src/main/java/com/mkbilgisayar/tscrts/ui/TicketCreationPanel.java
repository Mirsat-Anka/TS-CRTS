package com.mkbilgisayar.tscrts.ui;

import com.mkbilgisayar.tscrts.dao.CustomerDAO;
import com.mkbilgisayar.tscrts.dao.DeviceDAO;
import com.mkbilgisayar.tscrts.dao.TicketDAO;
import com.mkbilgisayar.tscrts.model.Customer;
import com.mkbilgisayar.tscrts.model.Device;
import com.mkbilgisayar.tscrts.model.Ticket;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import com.mkbilgisayar.tscrts.util.UIUtils;

public class TicketCreationPanel extends JPanel {

    private MainApp mainApp;
    
    // Customer Fields
    private JTextField customerNameField;
    private JLabel customerNameError;
    private JTextField customerPhoneField;
    private JLabel customerPhoneError;
    private JTextField companyNameField;
    private JLabel companyNameError;
    
    // Hardware Fields
    private JPanel hardwarePanel;
    private JTextField deviceTypeField;
    private JLabel deviceTypeError;
    private JTextField ramField;
    private JComboBox<String> ramUnitBox;
    private JLabel ramError;
    private JTextField cpuField;
    private JLabel cpuError;

    // Service Fields
    private JPanel servicePanel;
    private JTextArea issueDetailsArea;
    private JLabel issueDetailsError;
    
    // Ticket Fields
    private JComboBox<String> categoryComboBox;
    private JButton submitButton;
    
    private CustomerDAO customerDAO;
    private DeviceDAO deviceDAO;
    private TicketDAO ticketDAO;
    
    private Border defaultBorder;
    private Border errorBorder = BorderFactory.createLineBorder(Color.RED, 1);
    
    private static final String[] CATEGORY_KEYS = {"sql_database", "erp_installation", "e_transformation", "network", "hardware"};

    public TicketCreationPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        this.customerDAO = new CustomerDAO();
        this.deviceDAO = new DeviceDAO();
        this.ticketDAO = new TicketDAO();
        
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        formPanel.putClientProperty("FlatLaf.styleClass", "card");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Create New Ticket");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        int row = 1;

        // --- Customer Section ---
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel custHeader = new JLabel("Customer Details");
        custHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        custHeader.setForeground(UIManager.getColor("Label.disabledForeground"));
        formPanel.add(custHeader, gbc);
        
        customerNameField = new JTextField(20);
        customerNameError = addFormField(formPanel, "Full Name:", customerNameField, gbc, row); row += 2;

        customerPhoneField = new JTextField(20);
        customerPhoneError = addFormField(formPanel, "Phone:", customerPhoneField, gbc, row); row += 2;

        companyNameField = new JTextField(20);
        companyNameError = addFormField(formPanel, "Company:", companyNameField, gbc, row); row += 2;

        // --- Ticket Category Section ---
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        JLabel ticketHeader = new JLabel("Problem Category");
        ticketHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ticketHeader.setForeground(UIManager.getColor("Label.disabledForeground"));
        formPanel.add(ticketHeader, gbc);

        gbc.gridy = row++; gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        formPanel.add(new JLabel("Category:"), gbc);
        
        String[] formattedCategories = new String[CATEGORY_KEYS.length];
        for (int i = 0; i < CATEGORY_KEYS.length; i++) {
            formattedCategories[i] = UIUtils.formatCategory(CATEGORY_KEYS[i]);
        }
        categoryComboBox = new JComboBox<>(formattedCategories);
        UIUtils.styleComboBoxForEmojis(categoryComboBox);
        gbc.gridx = 1; formPanel.add(categoryComboBox, gbc);

        // --- Dynamic Details Section ---
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        JLabel devHeader = new JLabel("Device / Issue Details");
        devHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        devHeader.setForeground(UIManager.getColor("Label.disabledForeground"));
        formPanel.add(devHeader, gbc);

        // Hardware Panel - uses a 3-column grid: [label | field | (optional unit selector)]
        hardwarePanel = new JPanel(new GridBagLayout());
        GridBagConstraints hGbc = new GridBagConstraints();
        hGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Device Type row (spans columns 1-2)
        deviceTypeField = new JTextField(20);
        hGbc.gridy = 0; hGbc.gridx = 0; hGbc.gridwidth = 1;
        hGbc.insets = new Insets(5, 10, 0, 10);
        hardwarePanel.add(new JLabel("Device Type:"), hGbc);
        hGbc.gridx = 1; hGbc.gridwidth = 2; hGbc.weightx = 1.0;
        hardwarePanel.add(deviceTypeField, hGbc);
        hGbc.gridy = 1; hGbc.gridx = 1;
        hGbc.insets = new Insets(2, 10, 5, 10);
        deviceTypeError = new JLabel(" ");
        deviceTypeError.setForeground(Color.RED);
        deviceTypeError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hardwarePanel.add(deviceTypeError, hGbc);

        // RAM row (field in column 1, unit selector in column 2)
        ramField = new JTextField(10);
        ramUnitBox = new JComboBox<>(new String[]{"GB", "MB"});
        hGbc.gridy = 2; hGbc.gridx = 0; hGbc.gridwidth = 1; hGbc.weightx = 0.0;
        hGbc.insets = new Insets(5, 10, 0, 10);
        hardwarePanel.add(new JLabel("RAM:"), hGbc);
        hGbc.gridx = 1; hGbc.gridwidth = 1; hGbc.weightx = 1.0;
        hardwarePanel.add(ramField, hGbc);
        hGbc.gridx = 2; hGbc.gridwidth = 1; hGbc.weightx = 0.0;
        hGbc.insets = new Insets(5, 5, 0, 10);
        hardwarePanel.add(ramUnitBox, hGbc);
        hGbc.gridy = 3; hGbc.gridx = 1; hGbc.gridwidth = 2;
        hGbc.insets = new Insets(2, 10, 5, 10);
        ramError = new JLabel(" ");
        ramError.setForeground(Color.RED);
        ramError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hardwarePanel.add(ramError, hGbc);

        // CPU row (spans columns 1-2)
        cpuField = new JTextField(20);
        hGbc.gridy = 4; hGbc.gridx = 0; hGbc.gridwidth = 1; hGbc.weightx = 0.0;
        hGbc.insets = new Insets(5, 10, 0, 10);
        hardwarePanel.add(new JLabel("CPU:"), hGbc);
        hGbc.gridx = 1; hGbc.gridwidth = 2; hGbc.weightx = 1.0;
        hardwarePanel.add(cpuField, hGbc);
        hGbc.gridy = 5; hGbc.gridx = 1;
        hGbc.insets = new Insets(2, 10, 5, 10);
        cpuError = new JLabel(" ");
        cpuError.setForeground(Color.RED);
        cpuError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hardwarePanel.add(cpuError, hGbc);

        // Service Panel
        servicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.fill = GridBagConstraints.HORIZONTAL;
        
        issueDetailsArea = new JTextArea(4, 20);
        issueDetailsArea.setLineWrap(true);
        issueDetailsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(issueDetailsArea);
        
        sGbc.gridy = 0; sGbc.gridx = 0; sGbc.gridwidth = 1; sGbc.weightx = 0.0;
        sGbc.insets = new Insets(5, 10, 0, 10);
        sGbc.anchor = GridBagConstraints.NORTHWEST;
        servicePanel.add(new JLabel("Issue Details:"), sGbc);
        
        sGbc.gridx = 1; sGbc.gridwidth = 2; sGbc.weightx = 1.0;
        sGbc.anchor = GridBagConstraints.CENTER;
        servicePanel.add(scrollPane, sGbc);
        
        sGbc.gridy = 1; sGbc.gridx = 1;
        sGbc.insets = new Insets(2, 10, 5, 10);
        issueDetailsError = new JLabel(" ");
        issueDetailsError.setForeground(Color.RED);
        issueDetailsError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        servicePanel.add(issueDetailsError, sGbc);

        // Container to hold dynamic panels
        JPanel dynamicContainer = new JPanel(new BorderLayout());
        
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(dynamicContainer, gbc);

        // Submit Button
        row++;
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        submitButton = new JButton("Create Ticket");
        UIUtils.stylePrimaryButton(submitButton);
        submitButton.addActionListener(e -> createTicket());
        formPanel.add(submitButton, gbc);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        wrapperPanel.add(formPanel, BorderLayout.NORTH);

        JScrollPane mainScroll = new JScrollPane(wrapperPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
        
        defaultBorder = customerNameField.getBorder();
        addValidationListeners();

        // Listen for category changes to swap UI
        categoryComboBox.addActionListener(e -> {
            dynamicContainer.removeAll();
            if (isHardwareCategory()) {
                dynamicContainer.add(hardwarePanel, BorderLayout.CENTER);
            } else {
                dynamicContainer.add(servicePanel, BorderLayout.CENTER);
            }
            dynamicContainer.revalidate();
            dynamicContainer.repaint();
            if (dynamicContainer.getParent() != null) {
                dynamicContainer.getParent().revalidate();
            }
        });
        
        // Trigger initial state
        categoryComboBox.setSelectedIndex(-1);
        categoryComboBox.setSelectedIndex(0);
        
        UIUtils.enableUndoRedo(customerNameField, customerPhoneField, companyNameField, deviceTypeField, ramField, cpuField, issueDetailsArea);
    }

    private boolean isHardwareCategory() {
        int idx = categoryComboBox.getSelectedIndex();
        if (idx < 0) return false;
        String cat = CATEGORY_KEYS[idx];
        return "hardware".equals(cat) || "network".equals(cat);
    }

    private JLabel addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int yPos) {
        gbc.gridy = yPos;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 0, 10);
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);

        gbc.gridy = yPos + 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(2, 10, 5, 10);
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(errorLabel, gbc);
        return errorLabel;
    }

    private void addValidationListeners() {
        attachValidation(customerNameField, this::validateCustomerName);
        attachValidation(customerPhoneField, this::validateCustomerPhone);
        attachValidation(companyNameField, this::validateCompany);
        attachValidation(deviceTypeField, this::validateDeviceType);
        attachValidation(ramField, this::validateRam);
        attachValidation(cpuField, this::validateCpu);
        
        issueDetailsArea.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validateIssueDetails(); } });
        issueDetailsArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateIssueDetails(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateIssueDetails(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateIssueDetails(); }
        });
    }

    private void attachValidation(JTextField field, Runnable validator) {
        field.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validator.run(); } });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validator.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validator.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validator.run(); }
        });
    }

    private boolean validateCustomerName() {
        String name = customerNameField.getText().trim();
        if (name.isEmpty()) return setError(customerNameField, customerNameError, "Required");
        if (name.length() < 2) return setError(customerNameField, customerNameError, "Must be at least 2 characters");
        if (name.matches("\\d+")) return setError(customerNameField, customerNameError, "Cannot be pure numbers");
        return clearError(customerNameField, customerNameError);
    }

    private boolean validateCustomerPhone() {
        String phone = customerPhoneField.getText().trim();
        if (phone.isEmpty()) return setError(customerPhoneField, customerPhoneError, "Required");
        if (!phone.matches("^[\\d\\s\\+\\-\\(\\)]+$")) return setError(customerPhoneField, customerPhoneError, "Invalid phone format");
        return clearError(customerPhoneField, customerPhoneError);
    }

    private boolean validateCompany() {
        String comp = companyNameField.getText().trim();
        if (comp.isEmpty()) return setError(companyNameField, companyNameError, "Required");
        return clearError(companyNameField, companyNameError);
    }

    private boolean validateDeviceType() {
        if (!isHardwareCategory()) return true;
        String dev = deviceTypeField.getText().trim();
        if (dev.isEmpty()) return setError(deviceTypeField, deviceTypeError, "Required");
        if (dev.length() < 2) return setError(deviceTypeField, deviceTypeError, "Must be at least 2 characters");
        return clearError(deviceTypeField, deviceTypeError);
    }

    private boolean validateRam() {
        if (!isHardwareCategory()) return true;
        String ram = ramField.getText().trim();
        if (ram.isEmpty()) return setError(ramField, ramError, "Required");
        if (!ram.matches("\\d+")) return setError(ramField, ramError, "Numeric only");
        return clearError(ramField, ramError);
    }

    private boolean validateCpu() {
        if (!isHardwareCategory()) return true;
        String cpu = cpuField.getText().trim();
        if (cpu.isEmpty()) return setError(cpuField, cpuError, "Required");
        if (cpu.length() < 2) return setError(cpuField, cpuError, "Must be at least 2 characters");
        return clearError(cpuField, cpuError);
    }

    private boolean validateIssueDetails() {
        if (isHardwareCategory()) return true;
        String details = issueDetailsArea.getText().trim();
        if (details.isEmpty()) {
            issueDetailsArea.setBorder(errorBorder);
            issueDetailsError.setText("Required");
            return false;
        }
        issueDetailsArea.setBorder(UIManager.getBorder("ScrollPane.border"));
        issueDetailsError.setText(" ");
        return true;
    }

    private boolean validateAllFields() {
        boolean valid = true;
        if (!validateCustomerName()) valid = false;
        if (!validateCustomerPhone()) valid = false;
        if (!validateCompany()) valid = false;
        if (!validateDeviceType()) valid = false;
        if (!validateRam()) valid = false;
        if (!validateCpu()) valid = false;
        if (!validateIssueDetails()) valid = false;
        return valid;
    }

    private boolean setError(JComponent field, JLabel label, String msg) {
        field.setBorder(errorBorder);
        label.setText(msg);
        return false;
    }

    private boolean clearError(JComponent field, JLabel label) {
        if (field instanceof JScrollPane || field instanceof JTextArea) {
             field.setBorder(UIManager.getBorder("ScrollPane.border"));
        } else {
             field.setBorder(defaultBorder);
        }
        label.setText(" ");
        return true;
    }

    private void createTicket() {
        if (!validateAllFields()) {
            JOptionPane.showMessageDialog(this, "Please correct the highlighted errors before submitting.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String custName = customerNameField.getText().trim();
        String custPhone = customerPhoneField.getText().trim();
        String company = companyNameField.getText().trim();

        int catIdx = categoryComboBox.getSelectedIndex();
        String category = CATEGORY_KEYS[catIdx];

        String devType, ramVal, cpu, notes;
        if (isHardwareCategory()) {
            devType = deviceTypeField.getText().trim();
            ramVal = ramField.getText().trim() + " " + ramUnitBox.getSelectedItem();
            cpu = cpuField.getText().trim();
            notes = "";
        } else {
            devType = "Service Record"; // Generic device type for non-hardware
            ramVal = "";
            cpu = "";
            notes = issueDetailsArea.getText().trim();
        }

        // 1. Reuse or Create Customer
        Customer customer = null;
        List<Customer> existingCustomers = customerDAO.searchByName(custName);
        for (Customer c : existingCustomers) {
            if (c.getPhone().equals(custPhone)) {
                customer = c;
                break;
            }
        }
        
        if (customer == null) {
            customer = new Customer(0, custName, custPhone, company);
            boolean created = customerDAO.create(customer);
            if (!created) {
                JOptionPane.showMessageDialog(this, "Failed to create customer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 2. Create Device
        Device device = new Device(0, customer.getId(), devType, ramVal, cpu, notes);
        boolean devCreated = deviceDAO.create(device);
        if (!devCreated) {
            JOptionPane.showMessageDialog(this, "Failed to create device.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Create Ticket (Status: pending)
        Ticket ticket = new Ticket(0, null, device.getId(), null, category, "pending", null, null);
        boolean ticketCreated = ticketDAO.create(ticket);
        
        if (ticketCreated) {
            JOptionPane.showMessageDialog(this, "Ticket successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create ticket.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        customerNameField.setText(""); clearError(customerNameField, customerNameError);
        customerPhoneField.setText(""); clearError(customerPhoneField, customerPhoneError);
        companyNameField.setText(""); clearError(companyNameField, companyNameError);
        deviceTypeField.setText(""); clearError(deviceTypeField, deviceTypeError);
        ramField.setText(""); clearError(ramField, ramError);
        cpuField.setText(""); clearError(cpuField, cpuError);
        issueDetailsArea.setText(""); clearError(issueDetailsArea, issueDetailsError);
        categoryComboBox.setSelectedIndex(0);
    }
}
