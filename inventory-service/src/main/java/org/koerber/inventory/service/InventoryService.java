package org.koerber.inventory.service;

import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.status.UpdateStatus;

import java.util.List;

public interface InventoryService {
    List<InventoryBatch> getBatchesByProduct(Long productId);
    UpdateStatus updateInventory(Long productId, int quantity);
}

