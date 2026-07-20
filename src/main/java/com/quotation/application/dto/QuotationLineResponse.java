package com.quotation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single calculated line item within a quotation")
public class QuotationLineResponse {

    @Schema(description = "Unique line identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the referenced product", example = "3")
    private Long productId;

    @Schema(description = "Name of the referenced product", example = "Oak Tablero 18mm")
    private String productName;

    @Schema(description = "Quantity", example = "10.00")
    private BigDecimal quantity;

    @Schema(description = "Unit price applied at the time of quotation", example = "45.00")
    private BigDecimal unitPrice;

    @Schema(description = "Discount percentage applied to this line", example = "5.00")
    private BigDecimal discountPercent;

    @Schema(description = "Calculated line total: quantity × unitPrice × (1 - discount/100)", example = "427.50")
    private BigDecimal lineTotal;

    @Schema(description = "Optional notes for this line", example = "Cut to 60x60 cm")
    private String description;

    @Schema(description = "Breakdown of applied discounts", example = "Volume 6% (48+ boards) + 16mm bonus 3% = 9%")
    private String discountBreakdown;
}
