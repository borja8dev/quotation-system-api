package com.quotation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single line item within a quotation")
public class QuotationLineRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to include", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity to quote (must be positive)", example = "10.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "Line-level discount percentage (0–100). Defaults to 0 if not provided.", example = "5.00")
    private BigDecimal discountPercent;

    @Schema(description = "Optional description or notes for this line", example = "Cut to 60x60 cm")
    private String description;
}
