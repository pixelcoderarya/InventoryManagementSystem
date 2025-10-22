package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

public class RegisterDialog extends JDialog {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    
    public RegisterDialog(JDialog parent, HttpClient httpClient, ObjectMapper objectMapper) {
        super(parent, "Register New User", true);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Register New User", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Registration form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
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
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        
        registerButton.addActionListener(new RegisterActionListener());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(registerButton);
        
        // Focus on username field
        usernameField.requestFocus();
    }
    
    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterDialog.this,
                        "Please fill in all fields",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(RegisterDialog.this,
                        "Passwords do not match",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(User.Role.valueOf(role));
                
                String requestBody = objectMapper.writeValueAsString(user);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                            "User registered successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(RegisterDialog.this,
                            "Registration failed: " + response.body(),
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(RegisterDialog.this,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
