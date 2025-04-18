package com.example.demo.domain.repository;

import com.example.demo.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Product domain entity.
 * Following DDD principles, this interface is defined in the domain layer
 * but implemented in the infrastructure layer.
 */
public interface ProductRepository {
    
    /**
     * Find a product by its ID
     * @param id The product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(UUID id);
    
    /**
     * Save a product
     * @param product The product to save
     * @return The saved product
     */
    Product save(Product product);
    
    /**
     * Find all products
     * @return List of all products
     */
    List<Product> findAll();
    
    /**
     * Find products by category
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products created by a specific user
     * @param userId The ID of the user who created the products
     * @return List of products created by the specified user
     */
    List<Product> findByCreatedBy(UUID userId);
    
    /**
     * Delete a product
     * @param id The ID of the product to delete
     */
    void deleteById(UUID id);
}
