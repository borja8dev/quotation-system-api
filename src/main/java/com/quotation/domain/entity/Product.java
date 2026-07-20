package com.quotation.domain.entity;

import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(name = "price_per_m2", precision = 10, scale = 2)
    private BigDecimal pricePerM2;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "price_per_rate_unit", precision = 10, scale = 2)
    private BigDecimal pricePerRateUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type")
    private RateType rateType;

    @Column(name = "width_cm")
    private Integer widthCm;

    @Column(name = "length_cm")
    private Integer lengthCm;

    @Column(name = "thickness_mm")
    private Integer thicknessMm;

    private String color;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_unit", nullable = false)
    private SaleUnit saleUnit;

    public BigDecimal calculateAreaM2() {
        if (widthCm == null || lengthCm == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf((double) widthCm * lengthCm / 10000);
    }
}