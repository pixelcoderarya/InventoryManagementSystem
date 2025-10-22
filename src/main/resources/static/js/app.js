// Global variables
let authToken = null;
let currentUser = null;
let currentTab = 'products';

// API Configuration
const API_BASE_URL = 'http://localhost:3000/api';

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Check if user is already logged in
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('currentUser');
    
    if (savedToken && savedUser) {
        authToken = savedToken;
        currentUser = JSON.parse(savedUser);
        showMainApp();
    } else {
        showLoginModal();
    }
    
    // Setup event listeners
    setupEventListeners();
}

function setupEventListeners() {
    // Login form
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerBtn').addEventListener('click', showRegisterModal);
    document.getElementById('backToLoginBtn').addEventListener('click', showLoginModal);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Navigation tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            switchTab(this.dataset.tab);
        });
    });
    
    // Logout
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
}

// Authentication Functions
async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        showLoading(true);
        
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            authToken = data.token;
            currentUser = {
                id: data.userId,
                username: data.username,
                role: data.role
            };
            
            // Save to localStorage
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMainApp();
            showToast('Login successful!', 'success');
        } else {
            const error = await response.text();
            showToast(error || 'Login failed', 'error');
        }
    } catch (error) {
        showToast('Connection error. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;
    const role = document.getElementById('regRole').value;
    
    if (password !== confirmPassword) {
        showToast('Passwords do not match', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password, role })
        });
        
        if (response.ok) {
            showToast('Registration successful! Please login.', 'success');
            showLoginModal();
            document.getElementById('registerForm').reset();
        } else {
            const error = await response.text();
            showToast(error || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Connection error. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

function handleLogout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showLoginModal();
    showToast('Logged out successfully', 'success');
}

// UI Functions
function showLoginModal() {
    document.getElementById('loginModal').style.display = 'flex';
    document.getElementById('registerModal').style.display = 'none';
    document.getElementById('mainApp').style.display = 'none';
}

function showRegisterModal() {
    document.getElementById('loginModal').style.display = 'none';
    document.getElementById('registerModal').style.display = 'flex';
}

function showMainApp() {
    document.getElementById('loginModal').style.display = 'none';
    document.getElementById('registerModal').style.display = 'none';
    document.getElementById('mainApp').style.display = 'block';
    
    // Update user display
    document.getElementById('userDisplayName').textContent = `Welcome, ${currentUser.username}`;
    
    // Show/hide users tab based on role
    const usersTab = document.getElementById('usersTab');
    if (currentUser.role === 'ADMIN') {
        usersTab.style.display = 'flex';
    } else {
        usersTab.style.display = 'none';
    }
    
    // Load initial data
    loadCurrentTabData();
}

function switchTab(tabName) {
    // Update active tab
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    
    // Update active content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabName}Tab`).classList.add('active');
    
    currentTab = tabName;
    loadCurrentTabData();
}

function loadCurrentTabData() {
    switch (currentTab) {
        case 'products':
            loadProducts();
            break;
        case 'suppliers':
            loadSuppliers();
            break;
        case 'transactions':
            loadTransactions();
            break;
        case 'users':
            if (currentUser.role === 'ADMIN') {
                loadUsers();
            }
            break;
    }
}

// API Helper Functions
async function apiRequest(endpoint, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, mergedOptions);
    
    if (!response.ok) {
        if (response.status === 401) {
            handleLogout();
            throw new Error('Unauthorized');
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    return response;
}

// Product Management Functions
async function loadProducts() {
    try {
        showLoading(true);
        const response = await apiRequest('/products');
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showToast('Failed to load products', 'error');
    } finally {
        showLoading(false);
    }
}

function displayProducts(products) {
    const tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = '';
    
    products.forEach(product => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${product.id}</td>
            <td>${product.name}</td>
            <td>${product.sku}</td>
            <td>${product.category}</td>
            <td>$${product.price}</td>
            <td>${product.qty}</td>
            <td>${product.description || '-'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-primary" onclick="editProduct(${product.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger" onclick="deleteProduct(${product.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function searchProducts() {
    const searchTerm = document.getElementById('productSearch').value;
    if (!searchTerm.trim()) {
        loadProducts();
        return;
    }
    
    try {
        showLoading(true);
        const response = await apiRequest(`/products/search?name=${encodeURIComponent(searchTerm)}`);
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showToast('Search failed', 'error');
    } finally {
        showLoading(false);
    }
}

async function filterProductsByCategory() {
    const category = document.getElementById('categoryFilter').value;
    if (!category) {
        loadProducts();
        return;
    }
    
    try {
        showLoading(true);
        const response = await apiRequest(`/products/category/${encodeURIComponent(category)}`);
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showToast('Filter failed', 'error');
    } finally {
        showLoading(false);
    }
}

function refreshProducts() {
    loadProducts();
}

// Supplier Management Functions
async function loadSuppliers() {
    try {
        showLoading(true);
        const response = await apiRequest('/suppliers');
        const suppliers = await response.json();
        displaySuppliers(suppliers);
    } catch (error) {
        showToast('Failed to load suppliers', 'error');
    } finally {
        showLoading(false);
    }
}

function displaySuppliers(suppliers) {
    const tbody = document.getElementById('suppliersTableBody');
    tbody.innerHTML = '';
    
    suppliers.forEach(supplier => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${supplier.id}</td>
            <td>${supplier.name}</td>
            <td>${supplier.contact}</td>
            <td>${supplier.email || '-'}</td>
            <td>${supplier.phone || '-'}</td>
            <td>${supplier.address || '-'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-primary" onclick="editSupplier(${supplier.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger" onclick="deleteSupplier(${supplier.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function searchSuppliers() {
    const searchTerm = document.getElementById('supplierSearch').value;
    if (!searchTerm.trim()) {
        loadSuppliers();
        return;
    }
    
    try {
        showLoading(true);
        const response = await apiRequest(`/suppliers/search?name=${encodeURIComponent(searchTerm)}`);
        const suppliers = await response.json();
        displaySuppliers(suppliers);
    } catch (error) {
        showToast('Search failed', 'error');
    } finally {
        showLoading(false);
    }
}

function refreshSuppliers() {
    loadSuppliers();
}

// Transaction Management Functions
async function loadTransactions() {
    try {
        showLoading(true);
        const response = await apiRequest('/transactions');
        const transactions = await response.json();
        displayTransactions(transactions);
    } catch (error) {
        showToast('Failed to load transactions', 'error');
    } finally {
        showLoading(false);
    }
}

function displayTransactions(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    tbody.innerHTML = '';
    
    transactions.forEach(transaction => {
        const row = document.createElement('tr');
        const date = new Date(transaction.date).toLocaleString();
        row.innerHTML = `
            <td>${transaction.id}</td>
            <td>${transaction.product.id}</td>
            <td>${transaction.qty}</td>
            <td><span class="transaction-type ${transaction.type.toLowerCase().replace('_', '-')}">${transaction.type}</span></td>
            <td>${date}</td>
            <td>${transaction.user.id}</td>
            <td>${transaction.batchNumber || '-'}</td>
            <td>${transaction.lotNumber || '-'}</td>
            <td>${transaction.notes || '-'}</td>
        `;
        tbody.appendChild(row);
    });
}

function refreshTransactions() {
    loadTransactions();
}

// User Management Functions
async function loadUsers() {
    if (currentUser.role !== 'ADMIN') return;
    
    try {
        showLoading(true);
        const response = await apiRequest('/users');
        const users = await response.json();
        displayUsers(users);
    } catch (error) {
        showToast('Failed to load users', 'error');
    } finally {
        showLoading(false);
    }
}

function displayUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';
    
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td><span class="status-badge active">${user.role}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-primary" onclick="editUser(${user.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger" onclick="deleteUser(${user.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function searchUsers() {
    const searchTerm = document.getElementById('userSearch').value;
    if (!searchTerm.trim()) {
        loadUsers();
        return;
    }
    
    // Simple client-side search for now
    const response = await apiRequest('/users');
    const users = await response.json();
    const filteredUsers = users.filter(user => 
        user.username.toLowerCase().includes(searchTerm.toLowerCase())
    );
    displayUsers(filteredUsers);
}

function refreshUsers() {
    loadUsers();
}

// Modal Functions (Placeholder implementations)
function showAddProductModal() {
    showToast('Add Product modal - To be implemented', 'info');
}

function showAddSupplierModal() {
    showToast('Add Supplier modal - To be implemented', 'info');
}

function showAddTransactionModal() {
    showToast('Add Transaction modal - To be implemented', 'info');
}

function showAddUserModal() {
    showToast('Add User modal - To be implemented', 'info');
}

function showStockModal(type) {
    showToast(`Stock ${type} modal - To be implemented`, 'info');
}

function editProduct(id) {
    showToast(`Edit Product ${id} - To be implemented`, 'info');
}

function editSupplier(id) {
    showToast(`Edit Supplier ${id} - To be implemented`, 'info');
}

function editUser(id) {
    showToast(`Edit User ${id} - To be implemented`, 'info');
}

function deleteProduct(id) {
    if (confirm('Are you sure you want to delete this product?')) {
        showToast(`Delete Product ${id} - To be implemented`, 'info');
    }
}

function deleteSupplier(id) {
    if (confirm('Are you sure you want to delete this supplier?')) {
        showToast(`Delete Supplier ${id} - To be implemented`, 'info');
    }
}

function deleteUser(id) {
    if (confirm('Are you sure you want to delete this user?')) {
        showToast(`Delete User ${id} - To be implemented`, 'info');
    }
}

// Utility Functions
function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'block' : 'none';
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
}
