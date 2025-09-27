package org.koerber.inventory.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.inventory.dto.InventoryUpdateDto;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.koerber.inventory.repository.InventoryRepository;
import org.koerber.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class InventoryControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/inventory";
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void getInventory_shouldReturnBatches() {
        Product product = productRepository.saveAndFlush(
                Product.builder().name("Product A").build()
        );

        inventoryRepository.saveAndFlush(
                InventoryBatch.builder()
                        .product(product)
                        .quantity(10)
                        .expiryDate(LocalDate.now().plusDays(30))
                        .build()
        );

        ResponseEntity<InventoryBatch[]> response =
                restTemplate.getForEntity(baseUrl + "/" + product.getId(), InventoryBatch[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getQuantity()).isEqualTo(10);
    }

    @Test
    void updateInventory_shouldReturnSuccess_whenSufficientStock() {
        Product product = productRepository.saveAndFlush(
                Product.builder().name("Product B").build()
        );

        inventoryRepository.saveAndFlush(
                InventoryBatch.builder()
                        .product(product)
                        .quantity(10)
                        .expiryDate(LocalDate.now().plusDays(30))
                        .build()
        );

        InventoryUpdateDto dto = new InventoryUpdateDto(product.getId(), 5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/update", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Inventory updated successfully");

        InventoryBatch updated = inventoryRepository.findAll().get(0);
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    void updateInventory_shouldReturnBadRequest_whenInsufficientStock() {
        Product product = productRepository.saveAndFlush(
                Product.builder().name("Product C").build()
        );

        inventoryRepository.saveAndFlush(
                InventoryBatch.builder()
                        .product(product)
                        .quantity(3)
                        .expiryDate(LocalDate.now().plusDays(30))
                        .build()
        );

        InventoryUpdateDto dto = new InventoryUpdateDto(product.getId(), 5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/update", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Not enough stock");
    }

    @Test
    void updateInventory_shouldReturnNotFound_whenProductDoesNotExist() {
        InventoryUpdateDto dto = new InventoryUpdateDto(999L, 5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateDto> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/update", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Product not found");
    }

    @Test
    void updateInventory_shouldReturnBadRequest_whenQuantityOrProductIdMissing() {
        String body = "{}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/update", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
