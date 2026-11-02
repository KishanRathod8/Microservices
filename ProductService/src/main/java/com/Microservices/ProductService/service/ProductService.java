package com.Microservices.ProductService.service;

import com.Microservices.ProductService.client.InventoryClient;
import com.Microservices.ProductService.dto.CreateProductRequest;
import com.Microservices.ProductService.dto.InventoryResponse;
import com.Microservices.ProductService.dto.ProductResponse;
import com.Microservices.ProductService.entity.Product;
import com.Microservices.ProductService.exception.DuplicateSkuException;
import com.Microservices.ProductService.exception.ProductNotFoundException;
import com.Microservices.ProductService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.sku());

        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException("Product with SKU '" + request.sku() + "' already exists.");
        }

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct, 0);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        log.info("Fetching product details for SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        Integer availableStock = 0;
        try {
            InventoryResponse inventory = inventoryClient.getStockBySku(sku);
            if (inventory != null) {
                availableStock = inventory.quantity();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch stock from Inventory Service for SKU: {}. Defaulting to 0.", sku, ex);
        }

        return ProductResponse.fromEntity(product, availableStock);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .map(product -> ProductResponse.fromEntity(product, 0))
                .toList();
    }
}