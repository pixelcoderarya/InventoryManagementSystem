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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.arya.inventory.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserDialog extends JDialog {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    private User user;
    private boolean userSaved = false;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    
    public UserDialog(Window parent, HttpClient httpClient, ObjectMapper objectMapper, String authToken, User user) {
        super(parent, user == null ? "Add User" : "Edit User", ModalityType.APPLICATION_MODAL);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        this.user = user;
        
        initializeUI();
        if (user != null) {
            populateFields();
        }
    }
    
    private void initializeUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        roleComboBox = new JComboBox<>(new String[]{"STAFF", "MANAGER", "ADMIN"});
        formPanel.add(roleComboBox, gbc);
        
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
        
        // Focus on username field
        usernameField.requestFocus();
    }
    
    private void populateFields() {
        usernameField.setText(user.getUsername());
        roleComboBox.setSelectedItem(user.getRole().name());
        // Don't populate password fields for security
    }
    
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (validateFields()) {
                saveUser();
            }
        }
    }
    
    private boolean validateFields() {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveUser() {
        try {
            User userToSave = user != null ? user : new User();
            userToSave.setUsername(usernameField.getText().trim());
            userToSave.setPassword(new String(passwordField.getPassword()));
            userToSave.setRole(User.Role.valueOf((String) roleComboBox.getSelectedItem()));
            
            String requestBody = objectMapper.writeValueAsString(userToSave);
            String url = user != null ? 
                    "http://localhost:8080/api/users/" + user.getId() : 
                    "http://localhost:8080/api/users";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .method(user != null ? "PUT" : "POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                userSaved = true;
                JOptionPane.showMessageDialog(this, "User saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save user: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isUserSaved() {
        return userSaved;
    }
}
