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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.arya.inventory.dto.LoginRequest;
import com.arya.inventory.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginDialog extends JDialog {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private boolean loginSuccessful = false;
    private String authToken;
    private String userRole;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginDialog(JFrame parent, HttpClient httpClient, ObjectMapper objectMapper) {
        super(parent, "Login", true);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Arya Inventory Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Login form
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
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        
        loginButton.addActionListener(new LoginActionListener());
        registerButton.addActionListener(new RegisterActionListener());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
        
        // Focus on username field
        usernameField.requestFocus();
    }
    
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Please enter both username and password",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                LoginRequest loginRequest = new LoginRequest(username, password);
                String requestBody = objectMapper.writeValueAsString(loginRequest);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);
                    authToken = loginResponse.getToken();
                    userRole = loginResponse.getRole();
                    loginSuccessful = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Invalid username or password",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | InterruptedException ex) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            RegisterDialog registerDialog = new RegisterDialog(LoginDialog.this, httpClient, objectMapper);
            registerDialog.setVisible(true);
        }
    }
    
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getUserRole() {
        return userRole;
    }
}
