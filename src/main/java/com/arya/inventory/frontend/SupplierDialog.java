package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.arya.inventory.entity.Supplier;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SupplierDialog extends JDialog {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    private Supplier supplier;
    private boolean supplierSaved = false;
    
    private JTextField nameField;
    private JTextField contactField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextArea historyArea;
    
    public SupplierDialog(Window parent, HttpClient httpClient, ObjectMapper objectMapper, String authToken, Supplier supplier) {
        super(parent, supplier == null ? "Add Supplier" : "Edit Supplier", ModalityType.APPLICATION_MODAL);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        this.supplier = supplier;
        
        initializeUI();
        if (supplier != null) {
            populateFields();
        }
    }
    
    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Contact
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        contactField = new JTextField(20);
        formPanel.add(contactField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        addressField = new JTextField(20);
        formPanel.add(addressField, gbc);
        
        // History
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("History:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        historyArea = new JTextArea(4, 20);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        formPanel.add(scrollPane, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(new SaveActionListener());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
        
        // Focus on name field
        nameField.requestFocus();
    }
    
    private void populateFields() {
        nameField.setText(supplier.getName());
        contactField.setText(supplier.getContact());
        emailField.setText(supplier.getEmail());
        phoneField.setText(supplier.getPhone());
        addressField.setText(supplier.getAddress());
        historyArea.setText(supplier.getHistory());
    }
    
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (validateFields()) {
                saveSupplier();
            }
        }
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (contactField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void saveSupplier() {
        try {
            Supplier supplierToSave = supplier != null ? supplier : new Supplier();
            supplierToSave.setName(nameField.getText().trim());
            supplierToSave.setContact(contactField.getText().trim());
            supplierToSave.setEmail(emailField.getText().trim());
            supplierToSave.setPhone(phoneField.getText().trim());
            supplierToSave.setAddress(addressField.getText().trim());
            supplierToSave.setHistory(historyArea.getText().trim());
            
            String requestBody = objectMapper.writeValueAsString(supplierToSave);
            String url = supplier != null ? 
                    "http://localhost:8080/api/suppliers/" + supplier.getId() : 
                    "http://localhost:8080/api/suppliers";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .method(supplier != null ? "PUT" : "POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                supplierSaved = true;
                JOptionPane.showMessageDialog(this, "Supplier saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save supplier: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSupplierSaved() {
        return supplierSaved;
    }
}
