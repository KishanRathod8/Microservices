package com.Microservices.order_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderResilienceService {

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventoryCheck")
    @Retry(name = "inventoryService")
    public String checkInventoryWithResilience(String skuCode) {
        log.info("Calling downstream inventory service for SKU: {}", skuCode);
        // Simulate remote call or operation that might fail
        return "Stock Available";
    }

    // Fallback method must match the exact parameter signature plus the Exception
    public String fallbackInventoryCheck(String skuCode, Throwable throwable) {
        log.error("Circuit breaker active or retries exhausted for SKU: {}. Reason: {}", skuCode, throwable.getMessage());
        return "Inventory Service is currently unavailable. Please try again later. [Circuit Breaker Active]";
    }
}