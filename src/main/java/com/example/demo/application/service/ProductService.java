package com.example.demo.application.service;

import com.example.demo.domain.model.Product;
import com.example.demo.domain.model.User;
import com.example.demo.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for product-related operations.
 * This service uses the domain service to perform operations and adds application-specific logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductDomainService productDomainService;
    private final UserService userService;

    /**
     * Create a new product
     * @param name The product name
     * @param description The product description
     * @param price The product price
     * @param category The product category
     * @return The created product
     */
    public Product createProduct(String name, String description, BigDecimal price, String category) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
        
        return productDomainService.createProduct(name, description, price, category, currentUser.getId());
    }

    /**
     * Update an existing product
     * @param id The product ID
     * @param name The updated product name
     * @param description The updated product description
     * @param price The updated product price
     * @param category The updated product category
     * @return The updated product
     */
    public Optional<Product> updateProduct(UUID id, String name, String description, BigDecimal price, String category) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
        
        // Check if user has permission to edit this product
        boolean isAdmin = hasRole("ROLE_admin");
        
        if (!isAdmin && !productDomainService.canUserEditProduct(id, currentUser.getId(), false)) {
            throw new AccessDeniedException("You don't have permission to edit this product");
        }
        
        return productDomainService.updateProduct(id, name, description, price, category);
    }

    /**
     * Get a product by ID
     * @param id The product ID
     * @return Optional containing the product if found
     */
    public Optional<Product> getProductById(UUID id) {
        return productDomainService.findById(id);
    }

    /**
     * Get all products
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productDomainService.findAll();
    }

    /**
     * Get products by category
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    public List<Product> getProductsByCategory(String category) {
        return productDomainService.findByCategory(category);
    }

    /**
     * Get products created by the current user
     * @return List of products created by the current user
     */
    public List<Product> getMyProducts() {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
        
        return productDomainService.findByCreatedBy(currentUser.getId());
    }

    /**
     * Delete a product
     * @param id The ID of the product to delete
     * @return true if the product was deleted, false otherwise
     */
    public boolean deleteProduct(UUID id) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
        
        // Check if user has permission to delete this product
        boolean isAdmin = hasRole("ROLE_admin");
        
        if (!isAdmin && !productDomainService.canUserEditProduct(id, currentUser.getId(), false)) {
            throw new AccessDeniedException("You don't have permission to delete this product");
        }
        
        return productDomainService.deleteProduct(id);
    }
    
    /**
     * Check if the current user has a specific role
     * @param roleName The role name to check
     * @return true if the user has the role, false otherwise
     */
    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> authority.equals(roleName));
        }
        
        return false;
    }
}
