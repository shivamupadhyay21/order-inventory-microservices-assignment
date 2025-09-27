package org.koerber.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.koerber.inventory.repository.InventoryRepository;
import org.koerber.inventory.repository.ProductRepository;
import org.koerber.inventory.service.InventoryService;
import org.koerber.inventory.status.UpdateStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.koerber.inventory.status.UpdateStatus.INSUFFICIENT_STOCK;
import static org.koerber.inventory.status.UpdateStatus.PRODUCT_NOT_FOUND;

@Service("defaultInventoryService")
@RequiredArgsConstructor
public class DefaultInventoryService implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<InventoryBatch> getBatchesByProduct(Long productId) {
        Product product = Product.builder().id(productId).build();
        List<InventoryBatch> inventoryBatches= inventoryRepository.findByProductOrderByExpiryDateAsc(product);
        if(inventoryBatches.isEmpty()){
            return Collections.emptyList();
        }
        return inventoryBatches;
    }

    @Override
    @Transactional
    public UpdateStatus updateInventory(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) {
            return PRODUCT_NOT_FOUND;
        }
        List<InventoryBatch> batches = inventoryRepository.findByProductOrderByExpiryDateAsc(product);
        //Check if there is no batch for given product
        if (batches.isEmpty()) {
            return INSUFFICIENT_STOCK;
        }

        int totalAvailable = batches.stream().mapToInt(InventoryBatch::getQuantity).sum();
        //Checking if available quantity is less than requested quantity
        if (totalAvailable < quantity) {
            return INSUFFICIENT_STOCK;
        }

        //updating inventory
        int remaining = quantity;
        for (InventoryBatch batch : batches) {
            if (remaining <= 0) break;
            int used = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - used);
            remaining -= used;
        }

        inventoryRepository.saveAll(batches);
        return UpdateStatus.SUCCESS;
    }

}
