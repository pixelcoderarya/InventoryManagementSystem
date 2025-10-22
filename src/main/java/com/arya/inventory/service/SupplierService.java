package com.arya.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arya.inventory.entity.Supplier;
import com.arya.inventory.repository.SupplierRepository;

@Service
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new RuntimeException("Supplier with name " + supplier.getName() + " already exists");
        }
        return supplierRepository.save(supplier);
    }
    
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Optional<Supplier> getSupplierByName(String name) {
        return supplierRepository.findByName(name);
    }
    
    public List<Supplier> searchSuppliersByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Optional<Supplier> getSupplierByEmail(String email) {
        return supplierRepository.findByEmail(email);
    }
    
    public Optional<Supplier> getSupplierByPhone(String phone) {
        return supplierRepository.findByPhone(phone);
    }
    
    public Supplier updateSupplier(Supplier supplier) {
        if (supplier.getId() == null) {
            throw new RuntimeException("Supplier ID is required for update");
        }
        return supplierRepository.save(supplier);
    }
    
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return supplierRepository.existsByName(name);
    }
}
