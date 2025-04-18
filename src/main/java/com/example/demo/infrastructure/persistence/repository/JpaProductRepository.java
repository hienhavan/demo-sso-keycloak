package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductEntity.
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {
    
    /**
     * Find products by category
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    List<ProductEntity> findByCategory(String category);
    
    /**
     * Find products created by a specific user
     * @param createdBy The ID of the user who created the products
     * @return List of products created by the specified user
     */
    List<ProductEntity> findByCreatedBy(UUID createdBy);
}
