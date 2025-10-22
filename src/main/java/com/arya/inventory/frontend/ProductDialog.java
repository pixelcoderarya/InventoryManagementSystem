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
import java.math.BigDecimal;
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

import com.arya.inventory.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductDialog extends JDialog {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    private Product product;
    private boolean productSaved = false;
    
    private JTextField nameField;
    private JTextField skuField;
    private JTextField categoryField;
    private JTextField priceField;
    private JTextField quantityField;
    private JTextArea descriptionArea;
    
    public ProductDialog(Window parent, HttpClient httpClient, ObjectMapper objectMapper, String authToken, Product product) {
        super(parent, product == null ? "Add Product" : "Edit Product", ModalityType.APPLICATION_MODAL);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        this.product = product;
        
        initializeUI();
        if (product != null) {
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
        
        // SKU
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("SKU:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        skuField = new JTextField(20);
        formPanel.add(skuField, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        categoryField = new JTextField(20);
        formPanel.add(categoryField, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        priceField = new JTextField(20);
        formPanel.add(priceField, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        quantityField = new JTextField(20);
        formPanel.add(quantityField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
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
        nameField.setText(product.getName());
        skuField.setText(product.getSku());
        categoryField.setText(product.getCategory());
        priceField.setText(product.getPrice().toString());
        quantityField.setText(product.getQty().toString());
        descriptionArea.setText(product.getDescription());
    }
    
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (validateFields()) {
                saveProduct();
            }
        }
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (skuField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "SKU is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Price must be positive", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity < 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be non-negative", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveProduct() {
        try {
            Product productToSave = product != null ? product : new Product();
            productToSave.setName(nameField.getText().trim());
            productToSave.setSku(skuField.getText().trim());
            productToSave.setCategory(categoryField.getText().trim());
            productToSave.setPrice(new BigDecimal(priceField.getText().trim()));
            productToSave.setQty(Integer.parseInt(quantityField.getText().trim()));
            productToSave.setDescription(descriptionArea.getText().trim());
            
            String requestBody = objectMapper.writeValueAsString(productToSave);
            String url = product != null ? 
                    "http://localhost:8080/api/products/" + product.getId() : 
                    "http://localhost:8080/api/products";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .method(product != null ? "PUT" : "POST", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                productSaved = true;
                JOptionPane.showMessageDialog(this, "Product saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save product: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isProductSaved() {
        return productSaved;
    }
}
