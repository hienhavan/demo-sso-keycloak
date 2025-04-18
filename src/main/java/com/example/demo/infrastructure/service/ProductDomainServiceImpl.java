package com.example.demo.infrastructure.service;

import com.example.demo.domain.model.Product;
import com.example.demo.domain.repository.ProductRepository;
import com.example.demo.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDomainServiceImpl implements ProductDomainService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product createProduct(String name, String description, BigDecimal price, String category, UUID createdBy) {
        log.info("Creating new product: {} in category: {}", name, category);
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> updateProduct(UUID id, String name, String description, BigDecimal price, String category) {
        log.info("Updating product with ID: {}", id);
        
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (name != null) {
                        existingProduct.setName(name);
                    }
                    
                    if (description != null) {
                        existingProduct.setDescription(description);
                    }
                    
                    if (price != null) {
                        existingProduct.setPrice(price);
                    }
                    
                    if (category != null) {
                        existingProduct.setCategory(category);
                    }
                    
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    return productRepository.save(existingProduct);
                });
    }

    @Override
    public Optional<Product> findById(UUID id) {
        log.info("Finding product by ID: {}", id);
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        log.info("Finding all products");
        return productRepository.findAll();
    }

    @Override
    public List<Product> findByCategory(String category) {
        log.info("Finding products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> findByCreatedBy(UUID userId) {
        log.info("Finding products created by user with ID: {}", userId);
        return productRepository.findByCreatedBy(userId);
    }

    @Override
    @Transactional
    public boolean deleteProduct(UUID id) {
        log.info("Deleting product with ID: {}", id);
        
        if (productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean canUserEditProduct(UUID productId, UUID userId, boolean isAdmin) {
        log.info("Checking if user {} can edit product {}", userId, productId);
        
        // Admin can edit any product
        if (isAdmin) {
            return true;
        }
        
        // Regular users can only edit their own products
        return productRepository.findById(productId)
                .map(product -> product.isCreatedByUser(userId))
                .orElse(false);
    }
}
