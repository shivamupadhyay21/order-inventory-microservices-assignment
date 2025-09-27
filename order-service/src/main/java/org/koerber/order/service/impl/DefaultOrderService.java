package org.koerber.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.koerber.order.client.InventoryClient;
import org.koerber.order.dto.InventoryUpdateDto;
import org.koerber.order.dto.OrderResponseDto;
import org.koerber.order.model.Order;
import org.koerber.order.repository.OrderRepository;
import org.koerber.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;


    @Override
    public OrderResponseDto placeOrder(Long productId, int quantity) {
        Order order = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .status("PENDING")
                .build();

        orderRepository.save(order);

        ResponseEntity<String> updateInventoryResponse = inventoryClient.updateInventory(
                new InventoryUpdateDto(productId, quantity)
        );

        if (updateInventoryResponse.getStatusCode().is2xxSuccessful()) {
            order.setStatus("ORDER_PLACED");
        } else if (updateInventoryResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            order.setStatus("FAILED: "+updateInventoryResponse.getBody());
        } else if (updateInventoryResponse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            order.setStatus("FAILED: "+updateInventoryResponse.getBody());
        } else {
            order.setStatus("FAILED: " + updateInventoryResponse.getBody());
        }

        orderRepository.save(order);

        return new OrderResponseDto(order.getId(), order.getStatus());
    }
}

