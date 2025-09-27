package org.koerber.order.service;


import org.koerber.order.dto.OrderResponseDto;

public interface OrderService {
    OrderResponseDto placeOrder(Long productId, int quantity);
}

