package com.arya.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arya.inventory.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByName(String name);
    boolean existsByName(String name);
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM Supplier s WHERE s.email = :email")
    Optional<Supplier> findByEmail(@Param("email") String email);
    
    @Query("SELECT s FROM Supplier s WHERE s.phone = :phone")
    Optional<Supplier> findByPhone(@Param("phone") String phone);
}
