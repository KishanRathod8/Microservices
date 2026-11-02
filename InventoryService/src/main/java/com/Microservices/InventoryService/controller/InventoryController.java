package com.Microservices.InventoryService.controller;

import com.Microservices.InventoryService.model.Inventory;
import com.Microservices.InventoryService.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    // Check stock by skuCode (e.g., GET /api/inventory?skuCode=PROD-LAPTOP-PRO)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam String skuCode) {
        log.info("Checking stock availability for SKU: {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.getQuantity() > 0)
                .orElse(false);
    }

    // Optional: Add new inventory or stock item manually
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> addInventory(@RequestBody Inventory inventory) {
        log.info("Adding inventory for SKU: {}", inventory.getSkuCode());
        inventoryRepository.save(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inventory added successfully");
    }
}