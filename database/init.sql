-- Create database
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF', 'MANAGER') NOT NULL
);

-- Create suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(255) NOT NULL,
    history TEXT,
    address VARCHAR(500),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sku VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    qty INT NOT NULL DEFAULT 0,
    description TEXT,
    supplier_id BIGINT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    qty INT NOT NULL,
    type ENUM('STOCK_IN', 'STOCK_OUT', 'PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT') NOT NULL,
    date DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    notes TEXT,
    batch_number VARCHAR(100),
    lot_number VARCHAR(100),
    expiry_date DATETIME,
    manufacturing_date DATETIME,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhHxONpO/7cGjO8Q5Q5Q5Q5Q5', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- Insert sample suppliers
INSERT INTO suppliers (name, contact, email, phone, address) VALUES 
('TechCorp Electronics', 'John Smith - Sales Manager', 'john@techcorp.com', '+1-555-0123', '123 Tech Street, Silicon Valley, CA'),
('Global Supplies Ltd', 'Sarah Johnson - Procurement', 'sarah@globalsupplies.com', '+1-555-0456', '456 Supply Ave, New York, NY'),
('Office Depot', 'Mike Wilson - Account Manager', 'mike@officedepot.com', '+1-555-0789', '789 Office Blvd, Chicago, IL')
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample products
INSERT INTO products (name, sku, category, price, qty, description, supplier_id) VALUES 
('Laptop Computer', 'LAPTOP-001', 'Electronics', 999.99, 25, 'High-performance laptop with 16GB RAM', 1),
('Office Chair', 'CHAIR-001', 'Furniture', 199.99, 50, 'Ergonomic office chair with lumbar support', 3),
('Wireless Mouse', 'MOUSE-001', 'Electronics', 29.99, 100, 'Optical wireless mouse with USB receiver', 1),
('Notebook Set', 'NOTEBOOK-001', 'Office Supplies', 15.99, 200, 'Set of 5 spiral-bound notebooks', 3),
('Coffee Maker', 'COFFEE-001', 'Appliances', 89.99, 15, '12-cup programmable coffee maker', 2)
ON DUPLICATE KEY UPDATE name=name;
