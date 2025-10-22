package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.arya.inventory.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserManagementPanel extends JPanel {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public UserManagementPanel(HttpClient httpClient, ObjectMapper objectMapper, String authToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        
        initializeUI();
        loadUsers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> showEditUserDialog());
        deleteButton.addActionListener(e -> deleteSelectedUser());
        refreshButton.addActionListener(e -> loadUsers());
        
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(refreshButton);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchUsers());
        searchPanel.add(searchButton);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(searchPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Username", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                User[] users = objectMapper.readValue(response.body(), User[].class);
                updateTable(users);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load users", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(User[] users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getRole()
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchUsers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }
        
        // For now, just filter the current table data
        // In a real application, you might want to implement server-side search
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String username = (String) model.getValueAt(i, 1);
            if (username.toLowerCase().contains(searchTerm.toLowerCase())) {
                userTable.setRowSelectionInterval(i, i);
                userTable.scrollRectToVisible(userTable.getCellRect(i, 0, true));
                break;
            }
        }
    }
    
    private void showAddUserDialog() {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, null);
        dialog.setVisible(true);
        if (dialog.isUserSaved()) {
            loadUsers();
        }
    }
    
    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
        User user = getUserById(userId);
        if (user != null) {
            UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, user);
            dialog.setVisible(true);
            if (dialog.isUserSaved()) {
                loadUsers();
            }
        }
    }
    
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
            deleteUser(userId);
        }
    }
    
    private void deleteUser(Long userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/" + userId))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "User deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private User getUserById(Long userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/" + userId))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), User.class);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Error loading user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
