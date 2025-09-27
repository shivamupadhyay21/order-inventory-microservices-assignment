package org.koerber.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    @Schema(description = "Unique ID of the created order", example = "5001")
    private Long orderId;

    @Schema(description = "Status of the order", example = "CONFIRMED")
    private String status;
}
