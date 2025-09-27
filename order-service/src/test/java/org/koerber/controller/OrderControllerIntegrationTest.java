package org.koerber.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.order.OrderServiceApplication;
import org.koerber.order.client.InventoryClient;
import org.koerber.order.dto.OrderRequestDto;
import org.koerber.order.dto.OrderResponseDto;
import org.koerber.order.model.Order;
import org.koerber.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = OrderServiceApplication.class)
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private InventoryClient inventoryClient;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/order";
        orderRepository.deleteAll();
    }

    @Test
    void placeOrder_shouldReturnPlaced_whenInventorySucceeds() {

        when(inventoryClient.updateInventory(any())).thenReturn(new ResponseEntity<>("SUCCESS",HttpStatus.OK));

        OrderRequestDto dto = new OrderRequestDto(1L, 5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequestDto> request = new HttpEntity<>(dto, headers);


        ResponseEntity<OrderResponseDto> response =
                restTemplate.postForEntity(baseUrl, request, OrderResponseDto.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("ORDER_PLACED");

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo("ORDER_PLACED");
    }

    @Test
    void placeOrder_shouldReturnFailed_whenInventoryFails() {

        when(inventoryClient.updateInventory(any())).thenReturn(new ResponseEntity<>("404 NOT_FOUND",HttpStatus.NOT_FOUND));

        OrderRequestDto dto = new OrderRequestDto(2L, 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequestDto> request = new HttpEntity<>(dto, headers);


        ResponseEntity<OrderResponseDto> response =
                restTemplate.postForEntity(baseUrl, request, OrderResponseDto.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("FAILED: 404 NOT_FOUND");

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo("FAILED: 404 NOT_FOUND");
    }

    @Test
    void placeOrder_shouldReturnBadRequest_whenInvalidPayload() {
        String body = "{}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
