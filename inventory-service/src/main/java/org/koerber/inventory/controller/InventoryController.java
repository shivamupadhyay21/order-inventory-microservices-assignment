package org.koerber.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koerber.inventory.dto.InventoryUpdateDto;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.service.InventoryService;
import org.koerber.inventory.service.impl.InventoryServiceFactory;
import org.koerber.inventory.status.UpdateStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryServiceFactory factory;

    @Operation(
            summary = "Place a new order",
            description = "Creates an order for a product with the given productId and quantity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order placed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided"),
                    @ApiResponse(responseCode = "404", description = "Product not available"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryBatch>> getInventory(@PathVariable Long productId) {
        InventoryService service = factory.getService("default");
        List<InventoryBatch> inventoryBatches=service.getBatchesByProduct(productId);
        if(inventoryBatches.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(inventoryBatches);
    }

    @Operation(
            summary = "Update Inventory",
            description = "Update batch for inventory with the given productId and quantity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated batch successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided /Insufficient Stock"),
                    @ApiResponse(responseCode = "404", description = "Product not available"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/update")
    public ResponseEntity<String> updateInventory(@Valid @RequestBody InventoryUpdateDto inventoryUpdateDto ) {

        InventoryService service = factory.getService("default");

        UpdateStatus updated=service.updateInventory(inventoryUpdateDto.getProductId(), inventoryUpdateDto.getQuantity());

        return switch (updated) {
            case PRODUCT_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with id=" + inventoryUpdateDto.getProductId());
            case INSUFFICIENT_STOCK -> ResponseEntity.badRequest()
                    .body("Failed: Not enough stock for productId=" + inventoryUpdateDto.getProductId());
            case SUCCESS -> ResponseEntity.ok("Inventory updated successfully for productId=" + inventoryUpdateDto.getProductId());
        };
    }
}
