package com.arya.inventory.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.arya.inventory.entity.Product;
import com.arya.inventory.entity.Supplier;
import com.arya.inventory.entity.User;
import com.arya.inventory.repository.ProductRepository;
import com.arya.inventory.repository.SupplierRepository;
import com.arya.inventory.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
        }
        
        // Create sample suppliers
        if (supplierRepository.count() == 0) {
            Supplier supplier1 = new Supplier();
            supplier1.setName("TechCorp Electronics");
            supplier1.setContact("John Smith - Sales Manager");
            supplier1.setEmail("john@techcorp.com");
            supplier1.setPhone("+1-555-0123");
            supplier1.setAddress("123 Tech Street, Silicon Valley, CA");
            supplierRepository.save(supplier1);
            
            Supplier supplier2 = new Supplier();
            supplier2.setName("Global Supplies Ltd");
            supplier2.setContact("Sarah Johnson - Procurement");
            supplier2.setEmail("sarah@globalsupplies.com");
            supplier2.setPhone("+1-555-0456");
            supplier2.setAddress("456 Supply Ave, New York, NY");
            supplierRepository.save(supplier2);
            
            Supplier supplier3 = new Supplier();
            supplier3.setName("Office Depot");
            supplier3.setContact("Mike Wilson - Account Manager");
            supplier3.setEmail("mike@officedepot.com");
            supplier3.setPhone("+1-555-0789");
            supplier3.setAddress("789 Office Blvd, Chicago, IL");
            supplierRepository.save(supplier3);
        }
        
        // Create sample products
        if (productRepository.count() == 0) {
            Supplier supplier1 = supplierRepository.findByName("TechCorp Electronics").orElse(null);
            Supplier supplier2 = supplierRepository.findByName("Global Supplies Ltd").orElse(null);
            Supplier supplier3 = supplierRepository.findByName("Office Depot").orElse(null);
            
            Product product1 = new Product();
            product1.setName("Laptop Computer");
            product1.setSku("LAPTOP-001");
            product1.setCategory("Electronics");
            product1.setPrice(new BigDecimal("999.99"));
            product1.setQty(25);
            product1.setDescription("High-performance laptop with 16GB RAM");
            product1.setSupplier(supplier1);
            productRepository.save(product1);
            
            Product product2 = new Product();
            product2.setName("Office Chair");
            product2.setSku("CHAIR-001");
            product2.setCategory("Furniture");
            product2.setPrice(new BigDecimal("199.99"));
            product2.setQty(50);
            product2.setDescription("Ergonomic office chair with lumbar support");
            product2.setSupplier(supplier3);
            productRepository.save(product2);
            
            Product product3 = new Product();
            product3.setName("Wireless Mouse");
            product3.setSku("MOUSE-001");
            product3.setCategory("Electronics");
            product3.setPrice(new BigDecimal("29.99"));
            product3.setQty(100);
            product3.setDescription("Optical wireless mouse with USB receiver");
            product3.setSupplier(supplier1);
            productRepository.save(product3);
            
            Product product4 = new Product();
            product4.setName("Notebook Set");
            product4.setSku("NOTEBOOK-001");
            product4.setCategory("Office Supplies");
            product4.setPrice(new BigDecimal("15.99"));
            product4.setQty(200);
            product4.setDescription("Set of 5 spiral-bound notebooks");
            product4.setSupplier(supplier3);
            productRepository.save(product4);
            
            Product product5 = new Product();
            product5.setName("Coffee Maker");
            product5.setSku("COFFEE-001");
            product5.setCategory("Appliances");
            product5.setPrice(new BigDecimal("89.99"));
            product5.setQty(15);
            product5.setDescription("12-cup programmable coffee maker");
            product5.setSupplier(supplier2);
            productRepository.save(product5);
        }
    }
}
