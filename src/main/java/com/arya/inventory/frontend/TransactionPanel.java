package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.arya.inventory.entity.Product;
import com.arya.inventory.entity.Transaction;
import com.arya.inventory.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransactionPanel extends JPanel {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> typeComboBox;
    private JTextField productIdField;
    private JTextField quantityField;
    private JTextField batchNumberField;
    private JTextField lotNumberField;
    private JTextArea notesArea;
    
    public TransactionPanel(HttpClient httpClient, ObjectMapper objectMapper, String authToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        
        initializeUI();
        loadTransactions();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with transaction form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Transaction Type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        typeComboBox = new JComboBox<>(new String[]{"STOCK_IN", "STOCK_OUT", "PURCHASE", "SALE", "RETURN", "ADJUSTMENT"});
        formPanel.add(typeComboBox, gbc);
        
        // Product ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        productIdField = new JTextField(10);
        formPanel.add(productIdField, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        quantityField = new JTextField(10);
        formPanel.add(quantityField, gbc);
        
        // Batch Number
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Batch Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        batchNumberField = new JTextField(15);
        formPanel.add(batchNumberField, gbc);
        
        // Lot Number
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Lot Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        lotNumberField = new JTextField(15);
        formPanel.add(lotNumberField, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        formPanel.add(scrollPane, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addTransactionButton = new JButton("Add Transaction");
        JButton refreshButton = new JButton("Refresh");
        
        addTransactionButton.addActionListener(e -> addTransaction());
        refreshButton.addActionListener(e -> loadTransactions());
        
        buttonPanel.add(addTransactionButton);
        buttonPanel.add(refreshButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Product ID", "Quantity", "Type", "Date", "User ID", "Batch", "Lot", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane2 = new JScrollPane(transactionTable);
        add(scrollPane2, BorderLayout.CENTER);
    }
    
    private void loadTransactions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/transactions"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Transaction[] transactions = objectMapper.readValue(response.body(), Transaction[].class);
                updateTable(transactions);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load transactions", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(Transaction[] transactions) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getId(),
                transaction.getProduct().getId(),
                transaction.getQty(),
                transaction.getType(),
                transaction.getDate().format(formatter),
                transaction.getUser().getId(),
                transaction.getBatchNumber(),
                transaction.getLotNumber(),
                transaction.getNotes()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addTransaction() {
        if (!validateFields()) {
            return;
        }
        
        try {
            Transaction transaction = new Transaction();
            
            // Create product object with ID
            Product product = new Product();
            product.setId(Long.parseLong(productIdField.getText().trim()));
            transaction.setProduct(product);
            
            // Create user object with ID (assuming current user)
            User user = new User();
            user.setId(1L); // This should be the current user's ID
            transaction.setUser(user);
            
            transaction.setQty(Integer.parseInt(quantityField.getText().trim()));
            transaction.setType(Transaction.TransactionType.valueOf((String) typeComboBox.getSelectedItem()));
            transaction.setBatchNumber(batchNumberField.getText().trim());
            transaction.setLotNumber(lotNumberField.getText().trim());
            transaction.setNotes(notesArea.getText().trim());
            
            String requestBody = objectMapper.writeValueAsString(transaction);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/transactions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadTransactions();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Add transaction error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateFields() {
        if (productIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product ID is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            Long.parseLong(productIdField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Product ID format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void clearFields() {
        productIdField.setText("");
        quantityField.setText("");
        batchNumberField.setText("");
        lotNumberField.setText("");
        notesArea.setText("");
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
