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

import com.arya.inventory.entity.Supplier;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SupplierManagementPanel extends JPanel {
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public SupplierManagementPanel(HttpClient httpClient, ObjectMapper objectMapper, String authToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
        
        initializeUI();
        loadSuppliers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Supplier");
        JButton editButton = new JButton("Edit Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddSupplierDialog());
        editButton.addActionListener(e -> showEditSupplierDialog());
        deleteButton.addActionListener(e -> deleteSelectedSupplier());
        refreshButton.addActionListener(e -> loadSuppliers());
        
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
        searchButton.addActionListener(e -> searchSuppliers());
        searchPanel.add(searchButton);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(searchPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Name", "Contact", "Email", "Phone", "Address", "History"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadSuppliers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/suppliers"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Supplier[] suppliers = objectMapper.readValue(response.body(), Supplier[].class);
                updateTable(suppliers);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load suppliers", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(Supplier[] suppliers) {
        tableModel.setRowCount(0);
        for (Supplier supplier : suppliers) {
            Object[] row = {
                supplier.getId(),
                supplier.getName(),
                supplier.getContact(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getHistory()
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchSuppliers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadSuppliers();
            return;
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/suppliers/search?name=" + searchTerm))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Supplier[] suppliers = objectMapper.readValue(response.body(), Supplier[].class);
                updateTable(suppliers);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddSupplierDialog() {
        SupplierDialog dialog = new SupplierDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, null);
        dialog.setVisible(true);
        if (dialog.isSupplierSaved()) {
            loadSuppliers();
        }
    }
    
    private void showEditSupplierDialog() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long supplierId = (Long) tableModel.getValueAt(selectedRow, 0);
        Supplier supplier = getSupplierById(supplierId);
        if (supplier != null) {
            SupplierDialog dialog = new SupplierDialog(SwingUtilities.getWindowAncestor(this), httpClient, objectMapper, authToken, supplier);
            dialog.setVisible(true);
            if (dialog.isSupplierSaved()) {
                loadSuppliers();
            }
        }
    }
    
    private void deleteSelectedSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Long supplierId = (Long) tableModel.getValueAt(selectedRow, 0);
            deleteSupplier(supplierId);
        }
    }
    
    private void deleteSupplier(Long supplierId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/suppliers/" + supplierId))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSuppliers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete supplier", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Supplier getSupplierById(Long supplierId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/suppliers/" + supplierId))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Supplier.class);
            }
        } catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Error loading supplier: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
