package org.koerber.inventory.repository;


import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProductOrderByExpiryDateAsc(Product product);
}
