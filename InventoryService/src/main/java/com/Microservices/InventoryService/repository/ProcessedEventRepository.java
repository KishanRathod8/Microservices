package com.Microservices.InventoryService.repository;

import com.Microservices.InventoryService.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByOrderNumber(String orderNumber);
}