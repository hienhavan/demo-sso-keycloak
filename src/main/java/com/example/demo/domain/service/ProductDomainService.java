package com.example.demo.domain.service;

import com.example.demo.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for product-related business logic.
 * In DDD, domain services contain business logic that doesn't naturally fit within an entity.
 */
public interface ProductDomainService {
    
    /**
     * Create a new product
     * @param name The product name
     * @param description The product description
     * @param price The product price
     * @param category The product category
     * @param createdBy The ID of the user creating the product
     * @return The created product
     */
    Product createProduct(String name, String description, BigDecimal price, String category, UUID createdBy);
    
    /**
     * Update an existing product
     * @param id The product ID
     * @param name The updated product name
     * @param description The updated product description
     * @param price The updated product price
     * @param category The updated product category
     * @return The updated product
     */
    Optional<Product> updateProduct(UUID id, String name, String description, BigDecimal price, String category);
    
    /**
     * Find a product by ID
     * @param id The product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(UUID id);
    
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
     * @return true if the product was deleted, false otherwise
     */
    boolean deleteProduct(UUID id);
    
    /**
     * Check if a user can edit a product
     * @param productId The product ID
     * @param userId The user ID
     * @param isAdmin Whether the user is an admin
     * @return true if the user can edit the product, false otherwise
     */
    boolean canUserEditProduct(UUID productId, UUID userId, boolean isAdmin);
}
