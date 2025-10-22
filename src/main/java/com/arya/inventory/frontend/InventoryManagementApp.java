package com.arya.inventory.frontend;

import java.awt.BorderLayout;
import java.net.http.HttpClient;
import java.time.Duration;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class InventoryManagementApp extends JFrame {
    private static final String BASE_URL = "http://localhost:8080/api";
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String authToken;
    private String currentUserRole;
    
    private JTabbedPane tabbedPane;
    private ProductManagementPanel productPanel;
    private SupplierManagementPanel supplierPanel;
    private TransactionPanel transactionPanel;
    private UserManagementPanel userPanel;
    
    public InventoryManagementApp() {
        initializeHttpClient();
        initializeUI();
        showLoginDialog();
    }
    
    private void initializeHttpClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
    }
    
    private void initializeUI() {
        setTitle("Arya Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create menu bar
        createMenuBar();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize panels
        productPanel = new ProductManagementPanel(httpClient, objectMapper, authToken);
        supplierPanel = new SupplierManagementPanel(httpClient, objectMapper, authToken);
        transactionPanel = new TransactionPanel(httpClient, objectMapper, authToken);
        userPanel = new UserManagementPanel(httpClient, objectMapper, authToken);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Products", productPanel);
        tabbedPane.addTab("Suppliers", supplierPanel);
        tabbedPane.addTab("Transactions", transactionPanel);
        
        // Only show user management for admin
        if ("ADMIN".equals(currentUserRole)) {
            tabbedPane.addTab("Users", userPanel);
        }
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this, httpClient, objectMapper);
        loginDialog.setVisible(true);
        
        if (loginDialog.isLoginSuccessful()) {
            authToken = loginDialog.getAuthToken();
            currentUserRole = loginDialog.getUserRole();
            updatePanelsWithAuth();
            setVisible(true);
        } else {
            System.exit(0);
        }
    }
    
    private void updatePanelsWithAuth() {
        productPanel.setAuthToken(authToken);
        supplierPanel.setAuthToken(authToken);
        transactionPanel.setAuthToken(authToken);
        userPanel.setAuthToken(authToken);
    }
    
    private void logout() {
        authToken = null;
        currentUserRole = null;
        dispose();
        new InventoryManagementApp();
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Arya Inventory Management System\n" +
                "Version 1.0\n" +
                "Built with Java Spring Boot and Swing",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new InventoryManagementApp();
        });
    }
}
