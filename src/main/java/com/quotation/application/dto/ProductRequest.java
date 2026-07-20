package com.quotation.application.dto;

import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a product")
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Product name", example = "Oak Tablero 18mm", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Category is required")
    @Schema(description = "Product category", example = "TABLERO", requiredMode = Schema.RequiredMode.REQUIRED)
    private ProductCategory category;

    @NotNull(message = "Sale unit is required")
    @Schema(description = "Unit in which the product is sold", example = "TABLERO", requiredMode = Schema.RequiredMode.REQUIRED)
    private SaleUnit saleUnit;

    @Schema(description = "Price per square metre (m²) — used for area-based products", example = "25.50")
    private BigDecimal pricePerM2;

    @Schema(description = "Price per unit — used for unit-based products", example = "45.00")
    private BigDecimal pricePerUnit;

    @Schema(description = "Price per rate unit — used for service/time-based products", example = "30.00")
    private BigDecimal pricePerRateUnit;

    @Schema(description = "Rate type for service products", example = "HOUR")
    private RateType rateType;

    @Schema(description = "Width in centimetres", example = "244")
    private Integer widthCm;

    @Schema(description = "Length in centimetres", example = "122")
    private Integer lengthCm;

    @Schema(description = "Thickness in millimetres", example = "18")
    private Integer thicknessMm;

    @Schema(description = "Colour or finish", example = "Natural Oak")
    private String color;
}
