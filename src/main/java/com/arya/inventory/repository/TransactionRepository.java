package com.arya.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arya.inventory.entity.Transaction;
import com.arya.inventory.entity.Transaction.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByProductId(Long productId);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByType(TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByProductAndDateRange(@Param("productId") Long productId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.batchNumber = :batchNumber")
    List<Transaction> findByBatchNumber(@Param("batchNumber") String batchNumber);
    
    @Query("SELECT t FROM Transaction t WHERE t.lotNumber = :lotNumber")
    List<Transaction> findByLotNumber(@Param("lotNumber") String lotNumber);
    
    @Query("SELECT SUM(t.qty) FROM Transaction t WHERE t.product.id = :productId AND t.type = :type")
    Integer getTotalQuantityByProductAndType(@Param("productId") Long productId, 
                                           @Param("type") TransactionType type);
}
