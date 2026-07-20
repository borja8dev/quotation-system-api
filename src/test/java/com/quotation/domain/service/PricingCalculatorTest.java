package com.quotation.domain.service;

import com.quotation.domain.entity.Product;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.SaleUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void resolveUnitPrice_WhenPricePerUnitSet_ThenReturnsPricePerUnit() {
        Product product = baseProduct()
                .pricePerUnit(new BigDecimal("45.00"))
                .build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("45.00"));
    }

    @Test
    void resolveUnitPrice_WhenOnlyPricePerM2Set_ThenReturnsPricePerM2() {
        Product product = baseProduct()
                .pricePerM2(new BigDecimal("18.50"))
                .build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("18.50"));
    }

    @Test
    void resolveUnitPrice_WhenOnlyPricePerRateUnitSet_ThenReturnsPricePerRateUnit() {
        Product product = baseProduct()
                .pricePerRateUnit(new BigDecimal("12.00"))
                .build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    void resolveUnitPrice_WhenAllPricesSet_ThenPricePerUnitTakesPriority() {
        Product product = baseProduct()
                .pricePerUnit(new BigDecimal("45.00"))
                .pricePerM2(new BigDecimal("18.50"))
                .pricePerRateUnit(new BigDecimal("12.00"))
                .build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("45.00"));
    }

    @Test
    void resolveUnitPrice_WhenNoPricesSet_ThenReturnsZero() {
        Product product = baseProduct().build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void resolveUnitPrice_WhenProductHasNullFields_ThenReturnsZero() {
        Product product = Product.builder()
                .id(1L)
                .name("No Price Product")
                .category(ProductCategory.OTROS)
                .saleUnit(SaleUnit.UNIDAD)
                .build();

        BigDecimal result = calculator.resolveUnitPrice(product);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Product.ProductBuilder baseProduct() {
        return Product.builder()
                .id(1L)
                .name("Test Product")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO);
    }
}
