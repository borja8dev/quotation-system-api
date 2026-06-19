package com.agloval.domain.service;

import com.agloval.domain.entity.Product;
import com.agloval.domain.entity.QuotationLine;
import com.agloval.domain.enums.ProductCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DiscountCalculator {

    private static final int VOLUME_HIGH_THRESHOLD = 48;
    private static final int VOLUME_LOW_THRESHOLD = 24;
    private static final int PREFERRED_THICKNESS_MM = 16;
    private static final BigDecimal VOLUME_HIGH_DISCOUNT_PERCENT = new BigDecimal("6");
    private static final BigDecimal VOLUME_LOW_DISCOUNT_PERCENT = new BigDecimal("3");
    private static final BigDecimal THICKNESS_DISCOUNT_PERCENT = new BigDecimal("3");
    private static final BigDecimal REGULAR_CUSTOMER_DISCOUNT_PERCENT = new BigDecimal("2");

    public BigDecimal calculateLineDiscount(Product product, int totalBoardCount, boolean isRegularCustomer) {
        BigDecimal discount = BigDecimal.ZERO;

        if (product.getCategory() == ProductCategory.TABLERO) {
            if (totalBoardCount >= VOLUME_HIGH_THRESHOLD) {
                discount = discount.add(VOLUME_HIGH_DISCOUNT_PERCENT);
            } else if (totalBoardCount >= VOLUME_LOW_THRESHOLD) {
                discount = discount.add(VOLUME_LOW_DISCOUNT_PERCENT);
            }

            if (product.getThicknessMm() != null && product.getThicknessMm() == PREFERRED_THICKNESS_MM) {
                discount = discount.add(THICKNESS_DISCOUNT_PERCENT);
            }
        }

        if (isRegularCustomer) {
            discount = discount.add(REGULAR_CUSTOMER_DISCOUNT_PERCENT);
        }

        return discount;
    }

    public String buildDiscountBreakdown(Product product, int totalBoardCount, boolean isRegularCustomer) {
        List<String> parts = new ArrayList<>();

        if (product.getCategory() == ProductCategory.TABLERO) {
            if (totalBoardCount >= VOLUME_HIGH_THRESHOLD) {
                parts.add("Volume 6% (48+ boards)");
            } else if (totalBoardCount >= VOLUME_LOW_THRESHOLD) {
                parts.add("Volume 3% (24+ boards)");
            }

            if (product.getThicknessMm() != null && product.getThicknessMm() == PREFERRED_THICKNESS_MM) {
                parts.add("16mm bonus 3%");
            }
        }

        if (isRegularCustomer) {
            parts.add("Regular customer 2%");
        }

        if (parts.isEmpty()) {
            return null;
        }

        BigDecimal total = calculateLineDiscount(product, totalBoardCount, isRegularCustomer);
        return String.join(" + ", parts) + " = " + total + "%";
    }

    public int countTotalBoards(List<QuotationLine> lines) {
        BigDecimal total = lines.stream()
                .filter(line -> line.getProduct().getCategory() == ProductCategory.TABLERO)
                .map(QuotationLine::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.intValue();
    }
}
