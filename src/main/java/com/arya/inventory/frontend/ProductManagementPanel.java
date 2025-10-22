package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.arya.inventory.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductManagementPanel extends JPanel {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    
    public ProductManagementPanel(HttpClient httpClient, ObjectMapper objectMapper, String authToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        
        initializeUI();
        loadProducts();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddProductDialog());
        editButton.addActionListener(e -> showEditProductDialog());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> loadProducts());
        
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
        searchButton.addActionListener(e -> searchProducts());
        searchPanel.add(searchButton);
        
        searchPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>(new String[]{"All", "Electronics", "Clothing", "Books", "Food", "Other"});
        categoryComboBox.addActionListener(e -> filterByCategory());
        searchPanel.add(categoryComboBox);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(searchPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Name", "SKU", "Category", "Price", "Quantity", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with stock management
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton stockInButton = new JButton("Stock In");
        JButton stockOutButton = new JButton("Stock Out");
        
        stockInButton.addActionListener(e -> showStockDialog(true));
        stockOutButton.addActionListener(e -> showStockDialog(false));
        
        bottomPanel.add(stockInButton);
        bottomPanel.add(stockOutButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadProducts() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Product[] products = objectMapper.readValue(response.body(), Product[].class);
                updateTable(products);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load products", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(Product[] products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getCategory(),
                product.getPrice(),
                product.getQty(),
                product.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchProducts() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products/search?name=" + searchTerm))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Product[] products = objectMapper.readValue(response.body(), Product[].class);
                updateTable(products);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterByCategory() {
        String category = (String) categoryComboBox.getSelectedItem();
        if ("All".equals(category)) {
            loadProducts();
            return;
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products/category/" + category))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Product[] products = objectMapper.readValue(response.body(), Product[].class);
                updateTable(products);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddProductDialog() {
        ProductDialog dialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, null);
        dialog.setVisible(true);
        if (dialog.isProductSaved()) {
            loadProducts();
        }
    }
    
    private void showEditProductDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
        Product product = getProductById(productId);
        if (product != null) {
            ProductDialog dialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, product);
            dialog.setVisible(true);
            if (dialog.isProductSaved()) {
                loadProducts();
            }
        }
    }
    
    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
            deleteProduct(productId);
        }
    }
    
    private void deleteProduct(Long productId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products/" + productId))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Product getProductById(Long productId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products/" + productId))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Product.class);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Error loading product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    private void showStockDialog(boolean isStockIn) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productName = (String) tableModel.getValueAt(selectedRow, 1);
        String quantityStr = JOptionPane.showInputDialog(this, 
                "Enter quantity for " + (isStockIn ? "stock in" : "stock out") + " for " + productName + ":",
                isStockIn ? "Stock In" : "Stock Out",
                JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
                updateStock(productId, isStockIn ? quantity : -quantity);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStock(Long productId, int quantityChange) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/products/" + productId + "/stock?quantityChange=" + quantityChange))
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update stock: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Stock update error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
