package org.koerber.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.order.client.InventoryClient;
import org.koerber.order.dto.InventoryUpdateDto;
import org.koerber.order.dto.OrderResponseDto;
import org.koerber.order.model.Order;
import org.koerber.order.repository.OrderRepository;
import org.koerber.order.service.impl.DefaultOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultOrderServiceTest {

    private OrderRepository orderRepository;
    private InventoryClient inventoryClient;
    private DefaultOrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        inventoryClient = mock(InventoryClient.class);
        orderService = new DefaultOrderService(orderRepository, inventoryClient);

        // Save returns the same entity with ID populated
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(1L); // simulate generated ID
            }
            return o;
        });
    }

    @Test
    void placeOrder_shouldSetStatusPlaced_whenInventoryUpdateSucceeds() {

        Long productId = 100L;
        int quantity = 5;
        when(inventoryClient.updateInventory(any(InventoryUpdateDto.class))).thenReturn(new ResponseEntity<>("SUCCESS", HttpStatus.OK));


        OrderResponseDto response = orderService.placeOrder(productId, quantity);


        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("ORDER_PLACED");

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(inventoryClient).updateInventory(new InventoryUpdateDto(productId, quantity));
    }

    @Test
    void placeOrder_shouldSetStatusFailed_whenInventoryUpdateFails() {

        Long productId = 200L;
        int quantity = 3;
        when(inventoryClient.updateInventory(any(InventoryUpdateDto.class))).thenReturn(new ResponseEntity<>("404 NOT_FOUND", HttpStatus.NOT_FOUND));


        OrderResponseDto response = orderService.placeOrder(productId, quantity);


        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("FAILED: 404 NOT_FOUND");

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(inventoryClient).updateInventory(new InventoryUpdateDto(productId, quantity));
    }
}


