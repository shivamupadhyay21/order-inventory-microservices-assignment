package org.koerber.inventory.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdateDto {

    @NotNull(message = "productId is mandatory")
    @Schema(description = "ID of the product to order", example = "101")
    Long productId;

    @NotNull(message = "quantity is mandatory")
    @Min(value = 1, message = "quantity must be greater than 0")
    @Schema(description = "Quantity of the product", example = "2")
    int quantity;
}
