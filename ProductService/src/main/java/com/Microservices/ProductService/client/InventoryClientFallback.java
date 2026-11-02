package com.Microservices.ProductService.client;


import com.Microservices.ProductService.dto.InventoryResponse;
import org.springframework.stereotype.Component;

    @Component
    public class InventoryClientFallback implements InventoryClient {

        @Override
        public InventoryResponse getStockBySku(String sku) {
            // Fallback response when Inventory Service is down or circuit is open
            return new InventoryResponse(sku, 0, false);
        }
    }
