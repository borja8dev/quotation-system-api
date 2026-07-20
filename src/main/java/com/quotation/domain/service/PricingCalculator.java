package com.quotation.domain.service;

import com.quotation.domain.entity.Product;

import java.math.BigDecimal;

public class PricingCalculator {

    public BigDecimal resolveUnitPrice(Product product) {
        if (product.getPricePerUnit() != null) {
            return product.getPricePerUnit();
        }
        if (product.getPricePerM2() != null) {
            return product.getPricePerM2();
        }
        if (product.getPricePerRateUnit() != null) {
            return product.getPricePerRateUnit();
        }
        return BigDecimal.ZERO;
    }
}
