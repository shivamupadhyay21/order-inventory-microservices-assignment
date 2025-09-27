package org.koerber.inventory.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.koerber.inventory.repository.InventoryRepository;
import org.koerber.inventory.repository.ProductRepository;
import org.koerber.inventory.status.UpdateStatus;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultInventoryServiceTest {

    private InventoryRepository inventoryRepository;
    private ProductRepository productRepository;
    private DefaultInventoryService service;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        productRepository = mock(ProductRepository.class);
        service = new DefaultInventoryService(inventoryRepository, productRepository);
    }

    @Test
    void updateInventory_shouldReturnProductNotFound_whenProductDoesNotExist() {

        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        UpdateStatus status = service.updateInventory(productId, 5);

        assertThat(status).isEqualTo(UpdateStatus.PRODUCT_NOT_FOUND);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void updateInventory_shouldReturnInsufficientStock_whenNoBatchesFound() {

        Long productId = 1L;
        Product product = Product.builder().id(productId).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductOrderByExpiryDateAsc(product))
                .thenReturn(Collections.emptyList());

        UpdateStatus status = service.updateInventory(productId, 5);

        assertThat(status).isEqualTo(UpdateStatus.INSUFFICIENT_STOCK);
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void updateInventory_shouldReturnInsufficientStock_whenAvailableLessThanRequested() {

        Long productId = 1L;
        Product product = Product.builder().id(productId).build();

        InventoryBatch batch1 = InventoryBatch.builder()
                .id(101L).product(product).quantity(2).expiryDate(LocalDate.now().plusDays(10)).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductOrderByExpiryDateAsc(product))
                .thenReturn(List.of(batch1));


        UpdateStatus status = service.updateInventory(productId, 5);

        assertThat(status).isEqualTo(UpdateStatus.INSUFFICIENT_STOCK);
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void updateInventory_shouldUpdateBatches_whenSufficientStockExists() {

        Long productId = 1L;
        Product product = Product.builder().id(productId).build();

        InventoryBatch batch1 = InventoryBatch.builder()
                .id(101L).product(product).quantity(5).expiryDate(LocalDate.now().plusDays(10)).build();
        InventoryBatch batch2 = InventoryBatch.builder()
                .id(102L).product(product).quantity(10).expiryDate(LocalDate.now().plusDays(20)).build();

        List<InventoryBatch> batches = Arrays.asList(batch1, batch2);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductOrderByExpiryDateAsc(product)).thenReturn(batches);

        UpdateStatus status = service.updateInventory(productId, 8);

        assertThat(status).isEqualTo(UpdateStatus.SUCCESS);


        assertThat(batch1.getQuantity()).isEqualTo(0);
        assertThat(batch2.getQuantity()).isEqualTo(7);

        ArgumentCaptor<List<InventoryBatch>> captor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(captor.capture());
        List<InventoryBatch> savedBatches = captor.getValue();
        assertThat(savedBatches).hasSize(2);
        assertThat(savedBatches.get(0).getQuantity()).isEqualTo(0);
        assertThat(savedBatches.get(1).getQuantity()).isEqualTo(7);
    }
}
