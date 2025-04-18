package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product domain entity representing a product in the system.
 * This is an example domain entity to demonstrate authorization based on roles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain methods
    public boolean isPriceValid() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isCreatedByUser(UUID userId) {
        return createdBy != null && createdBy.equals(userId);
    }
    
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
}
