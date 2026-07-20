package com.quotation.application.dto;

import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
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
@Schema(description = "Product data returned by the API")
public class ProductResponse {

    @Schema(description = "Unique product identifier", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Oak Tablero 18mm")
    private String name;

    @Schema(description = "Product category", example = "TABLERO")
    private ProductCategory category;

    @Schema(description = "Unit in which the product is sold", example = "TABLERO")
    private SaleUnit saleUnit;

    @Schema(description = "Price per square metre (m²)", example = "25.50")
    private BigDecimal pricePerM2;

    @Schema(description = "Price per unit", example = "45.00")
    private BigDecimal pricePerUnit;

    @Schema(description = "Price per rate unit", example = "30.00")
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
