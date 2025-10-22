package com.arya.inventory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arya.inventory.entity.Product;
import com.arya.inventory.entity.Transaction;
import com.arya.inventory.entity.User;
import com.arya.inventory.repository.ProductRepository;
import com.arya.inventory.repository.TransactionRepository;
import com.arya.inventory.repository.UserRepository;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Transaction createTransaction(Transaction transaction) {
        // Validate product exists
        Product product = productRepository.findById(transaction.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Validate user exists
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        transaction.setProduct(product);
        transaction.setUser(user);
        transaction.setDate(LocalDateTime.now());
        
        // Update product stock based on transaction type
        updateProductStock(product, transaction.getQty(), transaction.getType());
        
        return transactionRepository.save(transaction);
    }
    
    private void updateProductStock(Product product, Integer quantity, Transaction.TransactionType type) {
        int stockChange = 0;
        
        switch (type) {
            case STOCK_IN:
            case PURCHASE:
                stockChange = quantity;
                break;
            case STOCK_OUT:
            case SALE:
                stockChange = -quantity;
                break;
            case RETURN:
                stockChange = quantity; // Return adds stock back
                break;
            case ADJUSTMENT:
                // For adjustment, we might want to set absolute quantity
                // For now, treating as stock in
                stockChange = quantity;
                break;
        }
        
        int newQuantity = product.getQty() + stockChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Current stock: " + product.getQty() + 
                                     ", Required: " + Math.abs(stockChange));
        }
        
        product.setQty(newQuantity);
        productRepository.save(product);
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> getTransactionsByProduct(Long productId) {
        return transactionRepository.findByProductId(productId);
    }
    
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        return transactionRepository.findByType(type);
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }
    
    public List<Transaction> getTransactionsByProductAndDateRange(Long productId, 
                                                                LocalDateTime startDate, 
                                                                LocalDateTime endDate) {
        return transactionRepository.findByProductAndDateRange(productId, startDate, endDate);
    }
    
    public List<Transaction> getTransactionsByBatchNumber(String batchNumber) {
        return transactionRepository.findByBatchNumber(batchNumber);
    }
    
    public List<Transaction> getTransactionsByLotNumber(String lotNumber) {
        return transactionRepository.findByLotNumber(lotNumber);
    }
    
    public Integer getTotalQuantityByProductAndType(Long productId, Transaction.TransactionType type) {
        Integer total = transactionRepository.getTotalQuantityByProductAndType(productId, type);
        return total != null ? total : 0;
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
