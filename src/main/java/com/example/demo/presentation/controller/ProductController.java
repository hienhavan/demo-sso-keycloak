package com.example.demo.presentation.controller;

import com.example.demo.application.service.ProductService;
import com.example.demo.domain.model.Product;
import com.example.demo.presentation.dto.ProductDTO;
import com.example.demo.presentation.dto.ProductRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST cho các hoạt động liên quan đến sản phẩm.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Lấy tất cả sản phẩm
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Lấy sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        return productService.getProductById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Lấy sản phẩm được tạo bởi người dùng hiện tại
     */
    @GetMapping("/my-products")
    public ResponseEntity<List<ProductDTO>> getMyProducts() {
        List<Product> products = productService.getMyProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductRequestDTO requestDTO) {
        Product product = productService.createProduct(
                requestDTO.getName(),
                requestDTO.getDescription(),
                requestDTO.getPrice(),
                requestDTO.getCategory()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(product));
    }

    /**
     * Cập nhật sản phẩm hiện có
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductRequestDTO requestDTO
    ) {
        return productService.updateProduct(
                id,
                requestDTO.getName(),
                requestDTO.getDescription(),
                requestDTO.getPrice(),
                requestDTO.getCategory()
        )
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        boolean deleted = productService.deleteProduct(id);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint dành cho admin để quản lý tất cả sản phẩm
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<ProductDTO>> adminGetAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Chuyển đổi từ model Product sang ProductDTO
     */
    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
