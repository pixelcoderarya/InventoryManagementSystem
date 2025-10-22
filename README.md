# Inventory Management System

A comprehensive inventory management system built with Java Spring Boot backend, MySQL database, and Java Swing frontend.

## Features

### Core Features
- **User Accounts & Roles**: Admin, Staff, Manager with role-based access control
- **Product Database**: SKU management, categories, descriptions, pricing
- **Supplier Database**: Vendor details, contact information, transaction history
- **Add/Edit/Remove Products**: Complete CRUD operations for product management
- **Stock In/Stock Out**: Purchase, sales, returns tracking
- **Batch/Lot Tracking**: Expiry dates, manufacturing dates, batch numbers
- **Multi-Warehouse Support**: Scalable architecture for multiple locations

### Technical Features
- RESTful API with Spring Boot
- JWT-based authentication
- MySQL database with proper relationships
- Interactive Java Swing GUI
- Real-time stock updates
- Transaction history tracking
- Search and filtering capabilities

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Git

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Arya
```

### 2. Database Setup
1. Install MySQL and start the service
2. Create a database user (optional, can use root):
   ```sql
   CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'inventory_password';
   GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Run the initialization script:
   ```bash
   mysql -u root -p < database/init.sql
   ```

### 3. Configure Application
Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build and Run

#### Backend (Spring Boot)
```bash
mvn clean install
mvn spring-boot:run
```
The backend will start on `http://localhost:8080`

#### Frontend (Java Swing)
```bash
# Run the main application
java -cp target/classes com.arya.inventory.frontend.InventoryManagementApp
```

## Default Login Credentials

- **Username**: admin
- **Password**: admin123
- **Role**: ADMIN

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/category/{category}` - Get products by category
- `GET /api/products/search?name={name}` - Search products
- `GET /api/products/low-stock?threshold={threshold}` - Get low stock products
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `PUT /api/products/{id}/stock?quantityChange={change}` - Update stock
- `DELETE /api/products/{id}` - Delete product

### Suppliers
- `GET /api/suppliers` - Get all suppliers
- `GET /api/suppliers/{id}` - Get supplier by ID
- `GET /api/suppliers/search?name={name}` - Search suppliers
- `POST /api/suppliers` - Create supplier
- `PUT /api/suppliers/{id}` - Update supplier
- `DELETE /api/suppliers/{id}` - Delete supplier

### Transactions
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/product/{productId}` - Get transactions by product
- `GET /api/transactions/type/{type}` - Get transactions by type
- `GET /api/transactions/date-range?startDate={start}&endDate={end}` - Get transactions by date range
- `GET /api/transactions/batch/{batchNumber}` - Get transactions by batch number
- `POST /api/transactions` - Create transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Users (Admin only)
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

## Usage Guide

### 1. Login
- Launch the application
- Use the default admin credentials or register a new user
- The interface will show different tabs based on your role

### 2. Product Management
- **Add Products**: Click "Add Product" to create new inventory items
- **Edit Products**: Select a product and click "Edit Product"
- **Stock Management**: Use "Stock In" and "Stock Out" buttons for inventory adjustments
- **Search & Filter**: Use the search field and category dropdown to find products

### 3. Supplier Management
- **Add Suppliers**: Click "Add Supplier" to add new vendors
- **Edit Suppliers**: Select a supplier and click "Edit Supplier"
- **Search**: Use the search field to find suppliers by name

### 4. Transaction Tracking
- **Add Transactions**: Fill in the transaction form to record stock movements
- **View History**: The transaction table shows all inventory movements
- **Batch Tracking**: Use batch and lot numbers for detailed tracking

### 5. User Management (Admin only)
- **Add Users**: Create new user accounts with appropriate roles
- **Edit Users**: Modify user information and roles
- **Delete Users**: Remove user accounts (use with caution)

## Database Schema

### Users Table
- `id` - Primary key
- `username` - Unique username
- `password` - Encrypted password
- `role` - User role (ADMIN, STAFF, MANAGER)

### Products Table
- `id` - Primary key
- `name` - Product name
- `sku` - Unique SKU code
- `category` - Product category
- `price` - Product price
- `qty` - Current quantity
- `description` - Product description
- `supplier_id` - Foreign key to suppliers

### Suppliers Table
- `id` - Primary key
- `name` - Supplier name
- `contact` - Contact information
- `history` - Transaction history
- `address` - Physical address
- `email` - Email address
- `phone` - Phone number

### Transactions Table
- `id` - Primary key
- `product_id` - Foreign key to products
- `qty` - Transaction quantity
- `type` - Transaction type (STOCK_IN, STOCK_OUT, etc.)
- `date` - Transaction date
- `user_id` - Foreign key to users
- `notes` - Additional notes
- `batch_number` - Batch tracking
- `lot_number` - Lot tracking
- `expiry_date` - Product expiry date
- `manufacturing_date` - Manufacturing date

## Security Features

- JWT token-based authentication
- Password encryption using BCrypt
- Role-based access control
- CORS configuration for web clients
- Input validation and sanitization

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL is running
   - Check database credentials in application.properties
   - Ensure database exists and user has proper permissions

2. **Frontend Connection Error**
   - Ensure backend is running on port 8080
   - Check firewall settings
   - Verify network connectivity

3. **Authentication Issues**
   - Use correct username/password
   - Check if user account is active
   - Verify JWT token expiration

### Logs
- Backend logs: Check console output or log files
- Database logs: Check MySQL error logs
- Frontend logs: Check console output for Java Swing application

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/arya/inventory/
│   │   ├── config/          # Security and configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/           # Data transfer objects
│   │   ├── entity/        # JPA entities
│   │   ├── frontend/      # Java Swing GUI
│   │   ├── repository/    # Data access layer
│   │   ├── service/      # Business logic
│   │   └── util/         # Utility classes
│   └── resources/
│       └── application.properties
├── database/
│   └── init.sql          # Database initialization
└── pom.xml              # Maven configuration
```

### Adding New Features
1. Create entity classes in `entity/` package
2. Add repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Add frontend components in `frontend/` package

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team or create an issue in the project repository.
