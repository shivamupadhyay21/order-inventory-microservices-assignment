package org.koerber.order.client;

import org.koerber.order.dto.InventoryUpdateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String inventoryBaseUrl;

    public InventoryClient(@Value("${inventory.service.url:http://localhost:8081}") String inventoryBaseUrl) {
        this.inventoryBaseUrl = inventoryBaseUrl;
    }

    public ResponseEntity<String> updateInventory(InventoryUpdateDto req) {
        String url = inventoryBaseUrl + "/inventory/update";
        try {
            return restTemplate.postForEntity(url, req, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error calling inventory service: " + e.getMessage());
        }
    }
}

