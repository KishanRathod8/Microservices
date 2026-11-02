package com.Microservices.ProductService.client;

import com.Microservices.ProductService.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/{sku}")
    InventoryResponse getStockBySku(@PathVariable("sku") String sku);
}