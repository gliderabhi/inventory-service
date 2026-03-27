package com.sevis.inventoryservice.repository;

import com.sevis.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
}
